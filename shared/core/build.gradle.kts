import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmExtension

plugins {
    kotlin("multiplatform") version "2.2.10"
}

group = "tv.afnon"

val testToolPatches = mapOf(
    "glob" to "14.0.2", "minimatch" to "11.0.7",
    "diff" to "8.0.3", "serialize-javascript" to "7.1.3",
)

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
    // Patch Node test tools without changing the compiler or shipped ES5 runtime.
    rootProject.extensions.configure<NpmExtension> {
        testToolPatches.forEach { (name, version) -> override(name, version) }
    }
    // The pinned plugin does not track override changes as root-package inputs.
    rootProject.tasks.named("rootPackageJson") {
        inputs.property("testToolPatches", testToolPatches)
    }
}

tasks.register<Exec>("legacyCheck") {
    dependsOn("compileProductionExecutableKotlinJs")
    workingDir(projectDir)
    commandLine("node", "test-legacy.cjs")
}

tasks.named("check") { dependsOn("legacyCheck") }
