plugins {
	id("com.android.library")
}

// Switch versions in gradle.properties (pariVersion=2.17.1) or with -PpariVersion=...
// Any tag "pari-<version>" of the PARI git repo works, or "master".
val pariVersion = providers.gradleProperty("pariVersion").getOrElse("2.17.1")

fun parseVersion(v: String): List<Int> {
    if (v == "master") return listOf(999, 999, 999)
    return v.split(".")
        .mapNotNull { it.replace(Regex("[^0-9].*"), "").toIntOrNull() }
}

val minSupportedVersion = listOf(2, 9, 0)
val currentVersionParts = parseVersion(pariVersion)

fun compareVersions(v1: List<Int>, v2: List<Int>): Int {
    for (i in 0 until maxOf(v1.size, v2.size)) {
        val p1 = v1.getOrElse(i) { 0 }
        val p2 = v2.getOrElse(i) { 0 }
        if (p1 != p2) return p1.compareTo(p2)
    }
    return 0
}

if (pariVersion != "master" && compareVersions(currentVersionParts, minSupportedVersion) < 0) {
    throw GradleException("PARI version '$pariVersion' is not supported. Minimum supported PARI version is 2.9.0.")
}

val nativeAbis = providers.gradleProperty("nativeAbis")
	.map { it.split(",").map(String::trim).filter(String::isNotEmpty) }
	.getOrElse(listOf("arm64-v8a", "x86_64"))
val minApi = 21

val customNativeDir = layout.projectDirectory.dir(".native/pari-$pariVersion").asFile
val prebuiltNativeDir = layout.projectDirectory.dir("src/main/jni").asFile

fun hasValidNativeLibs(dir: File): Boolean {
	return nativeAbis.all { abi ->
		File(dir, "$abi/gmp/lib/libgmp.so").exists() &&
			File(dir, "$abi/pari/lib/libpari.so").exists()
	}
}

val DEFAULT_PREBUILT_VERSION = "2.17.1"

// Use .native/pari-$pariVersion if available, otherwise fallback to bundled prebuilts in src/main/jni only if pariVersion matches default
val nativeDepsDir = when {
	hasValidNativeLibs(customNativeDir) -> customNativeDir
	pariVersion == DEFAULT_PREBUILT_VERSION && hasValidNativeLibs(prebuiltNativeDir) -> prebuiltNativeDir
	else -> customNativeDir
}

// Automatically build native dependencies during configuration if they don't exist yet
// Automatically build native dependencies during configuration if they don't exist yet
if (!hasValidNativeLibs(nativeDepsDir)) {
	logger.lifecycle("[Paridroid] Native libraries for PARI $pariVersion not found at ${nativeDepsDir.absolutePath}. Building automatically...")
	nativeDepsDir.mkdirs()
	val script = layout.projectDirectory.file("native/build-native.sh").asFile
	val isWindows = System.getProperty("os.name").lowercase().contains("win")

	val toWslPath: (File) -> String = { file ->
		val absPath = file.absolutePath
		val drive = absPath.substring(0, 1).lowercase()
		"/mnt/$drive" + absPath.substring(2).replace('\\', '/')
	}

    val cmd = buildList {
        if (isWindows) {
            add("wsl")
        }
        add("bash")
        if (isWindows) {
            add(toWslPath(script))
        } else {
            add(script.path)
        }
        add("--pari-version"); add(pariVersion)
        add("--api"); add(minApi.toString())
        add("--out")
        if (isWindows) {
            add(toWslPath(nativeDepsDir))
        } else {
            add(nativeDepsDir.path)
        }
        add("--verbose")

        if (nativeAbis.isNotEmpty()) {
            add("--abis")
            add(nativeAbis.joinToString(" "))
        }
    }

	val process = ProcessBuilder(cmd)
		.redirectErrorStream(true) // Merge stdout and stderr
		.start()

	// Close stdin so WSL doesn't hang waiting for input
	process.outputStream.close()

	// Read and print the output line-by-line in real time
	process.inputStream.bufferedReader().use { reader ->
		var line: String?
		while (reader.readLine().also { line = it } != null) {
			println(line) // Gradle captures standard output and prints it live
		}
	}

	val exitCode = process.waitFor()
	if (exitCode != 0) {
		throw GradleException("Failed to build native dependencies for PARI $pariVersion (exit $exitCode)")
	}
	logger.lifecycle("[Paridroid] Successfully built native dependencies for PARI $pariVersion!")
}

// Always wire buildPariNative when native libraries are missing or version changes
val nativeForce = providers.gradleProperty("nativeForce").isPresent
val needsSourceBuild = !hasValidNativeLibs(nativeDepsDir) || nativeForce

if (needsSourceBuild) {
	tasks.configureEach {
		if (name.startsWith("configureNdkBuild") ||
			name.startsWith("buildNdkBuild") ||
			name.startsWith("generateJsonModel") ||
			name.startsWith("externalNativeBuild")
		) {
			dependsOn(buildPariNative)
		}
	}
}

android {
	namespace = "fr.ubordeaux.math.paridroidgp"
	compileSdk = 35
	ndkVersion = "28.2.13676358"

	defaultConfig {
		minSdk = minApi
		ndk { abiFilters.addAll(nativeAbis) }
		externalNativeBuild {
			ndkBuild {
				arguments("NATIVE_DEPS_DIR=" + nativeDepsDir.absolutePath.replace('\\', '/'))
			}
		}
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	externalNativeBuild {
		ndkBuild { path("src/main/jni/Android.mk") }
	}
	kotlin { explicitApi() }
}

val buildPariNative = tasks.register<Exec>("buildPariNative") {
	group = "native"
	description = "Builds GMP + PARI $pariVersion for Android (skipped when up to date)"

	val script = layout.projectDirectory.file("native/build-native.sh").asFile
	val force = providers.gradleProperty("nativeForce").isPresent

	inputs.file(script)
	inputs.property("pariVersion", pariVersion)
	inputs.property("abis", nativeAbis)
	inputs.property("api", minApi)
	outputs.dir(nativeDepsDir)
	if (force) outputs.upToDateWhen { false }

	doFirst {
		nativeDepsDir.mkdirs()
		val isWindows = System.getProperty("os.name").lowercase().contains("win")
		val runner = providers.gradleProperty("nativeRunner").getOrElse(if (isWindows) "wsl" else "local")

		// Pass each ABI with its own --abis flag to prevent word splitting
		val common = listOf(
			"--pari-version", pariVersion,
			"--api", minApi.toString()
		) + nativeAbis.flatMap { listOf("--abis", it) } + if (force) listOf("--force") else emptyList()

		// Helper to convert Windows paths (C:\...) to WSL paths (/mnt/c/...)
		val toWslPath: (File) -> String = { file ->
			val absPath = file.absolutePath
			val drive = absPath.substring(0, 1).lowercase()
			"/mnt/$drive" + absPath.substring(2).replace('\\', '/')
		}

		commandLine(
			when (runner) {
				// Linux / macOS: use the NDK that Gradle resolved.
				"local" -> listOf(
					"bash", script.path,
					"--ndk", androidComponents.sdkComponents.ndkDirectory.get().asFile.path,
					"--out", nativeDepsDir.path
				) + common

				// Windows: run inside the default WSL distro using converted /mnt/... paths.
				"wsl" -> listOf(
					"wsl", "bash", toWslPath(script),
					"--out", toWslPath(nativeDepsDir)
				) + common

				// Anywhere with Docker (the Linux NDK is x86_64, hence --platform).
				"docker" -> listOf(
					"docker", "run", "--rm", "--platform", "linux/amd64",
					"-v", "${nativeDepsDir.path}:/out",
					"-v", "${script.parentFile.path}:/native:ro",
					"-v", "paridroid-native-cache:/cache",
					"ubuntu:24.04", "bash", "-c",
					"apt-get update -qq && apt-get install -y -qq build-essential m4 git curl xz-utils unzip ca-certificates bison flex patchelf >/dev/null && exec bash /native/build-native.sh \"\$@\"",
					"_", "--out", "/out", "--work", "/cache"
				) + common

				else -> throw GradleException("Unknown nativeRunner '$runner' (use local, wsl or docker)")
			}
		)
	}
}



tasks.register<Delete>("cleanNative") {
	group = "native"
	delete(layout.projectDirectory.dir(".native"))
}

dependencies {
	implementation("androidx.core:core-ktx:1.15.0")
	implementation("androidx.appcompat:appcompat:1.7.0")
}
