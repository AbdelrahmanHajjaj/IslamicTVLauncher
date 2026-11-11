import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin

plugins {
    kotlin("multiplatform") version "2.2.10"
}

group = "tv.afnon"

kotlin {
    jvmToolchain(17)
    jvm()
    js {
        outputModuleName = "IslamicTVLauncher-core"
        nodejs()
        generateTypeScriptDefinitions()
        binaries.executable()
        compilerOptions {
            target.set("es5")
            moduleKind.set(JsModuleKind.MODULE_UMD)
        }
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
    }
}

plugins.withType<NodeJsPlugin> {
    extensions.configure<NodeJsEnvSpec> {
        download.set(false)
    }
}

rootProject.plugins.withType<NodeJsRootPlugin> {
    rootProject.extensions.configure<NodeJsEnvSpec> {
        download.set(false)
    }
}

tasks.register<Exec>("legacyCheck") {
    dependsOn("compileProductionExecutableKotlinJs")
    workingDir(projectDir)
    commandLine("node", "test-legacy.cjs")
}

tasks.named("check") { dependsOn("legacyCheck") }
