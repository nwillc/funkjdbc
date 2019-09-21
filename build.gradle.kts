
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

val jvmTargetVersion = JavaVersion.VERSION_1_8.toString()
val publicationName = "gpr"
val gprUser = System.getenv("GPR_USER")
val gprKey = System.getenv("GPR_KEY")

val assertjVarsion: String by project
val detektToolVersion: String by project
val h2Version: String by project
val jacocoToolVersion: String by project
val jupiterVersion: String by project
val ktlintVersion: String by project

plugins {
    kotlin("jvm") version "1.3.50"
    jacoco
    `maven-publish`
    id("org.jetbrains.dokka") version "0.9.18"
    id("org.jlleitschuh.gradle.ktlint") version "8.2.0"
    id("com.github.nwillc.vplugin") version "3.0.1"
    id("io.gitlab.arturbosch.detekt") version "1.0.1"
}

group = "com.github.nwillc"
version = "0.8.3"

logger.lifecycle("${project.group}.${project.name}@${project.version}")

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
    testImplementation("org.assertj:assertj-core:$assertjVarsion")

    testRuntime("com.h2database:h2:$h2Version")
}

ktlint {
    version.set(ktlintVersion)
}

detekt {
    toolVersion = detektToolVersion
}

jacoco {
    toolVersion = jacocoToolVersion
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn("dokka")
    classifier = "javadoc"
    from("$buildDir/javadoc")
}

publishing {
    repositories {
        maven() {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/$gprUser/${project.name}")
            credentials {
                username = gprUser
                password = gprKey
            }
        }
    }
    publications {
        create<MavenPublication>(publicationName) {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            pom
            from(components["java"])
//            artifact(sourcesJar.get())
//            artifact(javadocJar.get())
        }
    }
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
    withType<JacocoReport> {
        dependsOn("test")
        reports {
            xml.apply {
                isEnabled = true
            }
            html.apply {
                isEnabled = true
            }
        }
    }
}
