import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlinx.jsPlainObjects)
}

group = "me.user"
version = "1.0-SNAPSHOT"

kotlin {
    js(IR) {
        browser {
            runTask {
                
            }
        }
        useEsModules()
        binaries.executable()

        compilerOptions {
            optIn.add("kotlin.js.ExperimentalJsExport")
        }
        generateTypeScriptDefinitions()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jsMain.dependencies {
            implementation(libs.kotlinx.browser)
            implementation(npm("pretty-print-json", "3.0.5"))

        }
    }
}