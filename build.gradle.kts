import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val jvmTargetVersion = JavaVersion.VERSION_1_8.toString()
val publicationName = "maven"
val dokkaDir = "$projectDir/docs/dokka"

val assertjVarsion: String by project
val awaitilityVersion: String by project
val coroutinesVersion: String by project
val detektToolVersion: String by project
val h2Version: String by project
val jacocoToolVersion: String by project
val jupiterVersion: String by project
val ktlintVersion: String by project

plugins {
    kotlin("jvm") version "1.3.71"
    jacoco
    `maven-publish`
    id("org.jetbrains.dokka") version "0.10.1"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    id("com.github.nwillc.vplugin") version "3.0.3"
    id("io.gitlab.arturbosch.detekt") version "1.7.0"
    id("com.jfrog.bintray") version "1.8.4"
}

group = "com.github.nwillc"
version = "0.10.2"

logger.lifecycle("${project.group}.${project.name}@${project.version}")

repositories {
    jcenter()
}

dependencies {
    listOf(
        kotlin("stdlib-jdk8"),
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
    ).forEach { implementation(it) }

    listOf(
        "org.junit.jupiter:junit-jupiter:$jupiterVersion",
        "org.assertj:assertj-core:$assertjVarsion"
    ).forEach { testImplementation(it) }

    testRuntimeOnly("com.h2database:h2:$h2Version")
}

ktlint {
    version.set(ktlintVersion)
    ignoreFailures.set(true)
}

detekt {
    toolVersion = detektToolVersion
}

jacoco {
    toolVersion = jacocoToolVersion
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.convention("sources")
    from(sourceSets["main"].allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn("dokka")
    archiveClassifier.convention("javadoc")
    from(dokkaDir)
}

publishing {
    publications {
        create<MavenPublication>(publicationName) {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
        }
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_API_KEY")
    dryRun = false
    publish = true
    setPublications(publicationName)
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = publicationName
        name = project.name
        desc = "Functional Kotlin JDBC Extensions."
        websiteUrl = "https://github.com/nwillc/${project.name}"
        issueTrackerUrl = "https://github.com/nwillc/${project.name}/issues"
        vcsUrl = "https://github.com/nwillc/${project.name}.git"
        version.vcsTag = "v${project.version}"
        setLicenses("ISC")
        setLabels("kotlin", "JDBC")
        publicDownloadNumbers = true
    })
}

tasks {
    named<Jar>("jar") {
        manifest.attributes["Automatic-Module-Name"] = "${project.group}.${project.name}"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = jvmTargetVersion
    }
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            events("passed", "failed", "skipped")
        }
    }
    withType<DokkaTask> {
        outputFormat = "html"
        outputDirectory = dokkaDir
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
    withType<BintrayUploadTask> {
        onlyIf {
            if (project.version.toString().contains('-')) {
                logger.lifecycle("Version v${project.version} is not a release version - skipping upload.")
                false
            } else {
                true
            }
        }
    }
}
