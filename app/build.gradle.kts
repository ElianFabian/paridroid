import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File

plugins {
    id("com.android.application")
}

android {
    namespace = "fr.ubordeaux.math.paridroid"

    compileSdk = 36

    defaultConfig {
        applicationId = "fr.ubordeaux.math.paridroid"
        minSdk = 21
        targetSdk = 36
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

//    kotlinOptions {
//        jvmTarget = "17"
//    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.txt")
        }
    }

    externalNativeBuild {
        ndkBuild {
            path("src/main/jni/Android.mk")
        }
    }
}

tasks.register("prepareNativeHeaders") {
    doLast {
        val abis = listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
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

            // Create stub .so files if missing
            file("$rootJni/$abi/gmp/lib").mkdirs()
            file("$rootJni/$abi/pari/lib").mkdirs()
            val gmpSo = file("$rootJni/$abi/gmp/lib/libgmp.so")
            val pariSo = file("$rootJni/$abi/pari/lib/libpari.so")
            if (!gmpSo.exists()) gmpSo.writeText("# stub")
            if (!pariSo.exists()) pariSo.writeText("# stub")
        }
    }
}

tasks.matching { it.name.contains("ExternalNativeBuild") || it.name.contains("NdkBuild") }.configureEach {
    dependsOn("prepareNativeHeaders")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("com.google.android.material:material:1.12.0")
}
