plugins {
    id("com.android.library")
}

android {
    namespace = "fr.ubordeaux.math.paridroidgp"
    compileSdk = 35
    ndkVersion = "28.2.13676358"

    defaultConfig {
        minSdk = 21
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    externalNativeBuild {
        ndkBuild {
            path("src/main/jni/Android.mk")
        }
    }
    kotlin {
        explicitApi()
    }
}

tasks.register("prepareNativeHeaders") {
    doLast {
        val abis = listOf("arm64-v8a", "x86_64")
        val rootJni = file("src/main/jni")
        val pariHeaders = file("../srclib/pari/src/headers")
        val miniGmp = file("../srclib/gmp/mini-gmp/mini-gmp.h")

        abis.forEach { abi ->
            val pariInc = file("$rootJni/$abi/pari/include/pari")
            val gmpInc = file("$rootJni/$abi/gmp/include")
            pariInc.mkdirs()
            gmpInc.mkdirs()

            if (pariHeaders.exists()) {
                pariHeaders.listFiles()?.forEach { f ->
                    if (f.isFile) {
                        f.copyTo(File(pariInc, f.name), overwrite = true)
                    }
                }
            }

            if (miniGmp.exists()) {
                miniGmp.copyTo(File(gmpInc, "gmp.h"), overwrite = true)
            }

            // Create paricfg.h
            File(pariInc, "paricfg.h").writeText("""
                #ifndef __PARICFG_H__
                #define __PARICFG_H__
                #define UNIX
                #define GPHELP ""
                #define GPDATADIR ""
                #define SHELL_Q '\''
                #define PARIVERSION "GP/PARI CALCULATOR Version 2.15.0"
                #define PARIINFO ""
                #define PARI_VERSION_CODE 136448
                #define PARI_VERSION(a,b,c) (((a) << 16) + ((b) << 8) + (c))
                #define PARI_VERSION_SHIFT 8
                #define PARI_VCSVERSION ""
                #define PARI_MT_ENGINE "single"
                #define PARI_DOUBLE_FORMAT 1
                #define GCC_VERSION "12.0.0"
                #define __HAS_NO_ASM__
                #endif
            """.trimIndent())

            // Create parimt.h
            File(pariInc, "parimt.h").writeText("""
                #define MT_SIGINT_BLOCK(block)
                #define MT_SIGINT_UNBLOCK(block)
            """.trimIndent())

            // Create mpinl.h
            File(pariInc, "mpinl.h").writeText("/* mpinl.h stub */")
        }
    }
}

tasks.matching { it.name.contains("ExternalNativeBuild") || it.name.contains("NdkBuild") }.configureEach {
    dependsOn("prepareNativeHeaders")
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
