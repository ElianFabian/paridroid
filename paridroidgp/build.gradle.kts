plugins {
    id("com.android.library")
}

// Switch versions in gradle.properties (pariVersion=2.17.1) or with -PpariVersion=...
// Any tag "pari-<version>" of the PARI git repo works, or "master".
val pariVersion = providers.gradleProperty("pariVersion").getOrElse("2.17.1")
val nativeAbis = listOf("arm64-v8a", "x86_64")
val minApi = 21

val customNativeDir = layout.projectDirectory.dir(".native/pari-$pariVersion").asFile
val prebuiltNativeDir = layout.projectDirectory.dir("src/main/jni").asFile

fun hasValidNativeLibs(dir: File): Boolean {
    return nativeAbis.all { abi ->
        File(dir, "$abi/gmp/lib/libgmp.so").exists() &&
        File(dir, "$abi/pari/lib/libpari.so").exists()
    }
}

// Use .native/pari-$pariVersion if available, otherwise bundled prebuilts in src/main/jni
val nativeDepsDir = when {
    hasValidNativeLibs(customNativeDir) -> customNativeDir
    hasValidNativeLibs(prebuiltNativeDir) -> prebuiltNativeDir
    else -> customNativeDir // Triggers buildPariNative on-demand if prebuilts are missing
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
        val runner = providers.gradleProperty("nativeRunner").getOrElse(if (isWindows) "docker" else "local")

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

// Only trigger source build if valid prebuilt libs are missing or if nativeForce is explicitly requested.
val nativeForce = providers.gradleProperty("nativeForce").isPresent
val needsSourceBuild = !hasValidNativeLibs(nativeDepsDir) || nativeForce

if (needsSourceBuild) {
    tasks.configureEach {
        if (name.startsWith("configureNdkBuild") ||
            name.startsWith("buildNdkBuild") ||
            name.startsWith("generateJsonModel")
        ) dependsOn(buildPariNative)
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
