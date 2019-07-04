import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val jvmTargetVersion = JavaVersion.VERSION_1_8.toString()

val assertjVarsion: String by project
val detektToolVersion: String by project
val h2Version: String by project
val jupiterVersion: String by project
val ktlintVersion: String by project

plugins {
    kotlin("jvm") version "1.3.40"
    id("org.jlleitschuh.gradle.ktlint") version "8.1.0"
    id("com.github.nwillc.vplugin") version "2.3.0"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC16"
}

group = "com.github.nwillc"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    runtime("com.h2database:h2:$h2Version")

    testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
    testImplementation("org.assertj:assertj-core:$assertjVarsion")
}

ktlint {
    version.set(ktlintVersion)
}

detekt {
    toolVersion = detektToolVersion
}

tasks {
    named<Jar>("jar") {
        manifest.attributes["Automatic-Module-Name"] = "${project.group}.${project.name}"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = jvmTargetVersion
    }
    withType<Test> {
        useJUnitPlatform {
            includeEngines = setOf("junit-jupiter")
        }
        testLogging.showStandardStreams = true
        beforeTest(KotlinClosure1<TestDescriptor, Unit>({ logger.lifecycle("    Running ${this.className}.${this.name}") }))
        afterSuite(KotlinClosure2<TestDescriptor, TestResult, Unit>({ descriptor, result ->
            if (descriptor.parent == null) {
                logger.lifecycle("Tests run: ${result.testCount}, Failures: ${result.failedTestCount}, Skipped: ${result.skippedTestCount}")
            }
            Unit
        }))
    }
}
