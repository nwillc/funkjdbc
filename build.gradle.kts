import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    jacoco
    `maven-publish`
    Libs.plugins.forEach { (n, v) -> id(n) version v }
}

group = "com.github.nwillc"
version = "0.10.3-SNAPSHOT"

logger.lifecycle("${project.group}.${project.name}@${project.version}")

repositories {
    jcenter()
}

dependencies {
    Libs.implementations.forEach { (n, v) -> implementation("$n:$v") }
    Libs.testImplementations.forEach { (n, v) -> testImplementation("$n:$v") }
    Libs.testRuntimeOnly.forEach { (n, v) -> testRuntimeOnly("$n:$v") }
}

ktlint {
    version.set(ToolVersions.ktlint)
    ignoreFailures.set(true)
}

detekt {
    reports {
        html.enabled = true
    }
}

jacoco {
    toolVersion = ToolVersions.jacoco
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.convention("sources")
    from(sourceSets["main"].allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn("dokka")
    archiveClassifier.convention("javadoc")
    from("$projectDir/${Constants.dokkaDir}")
}

publishing {
    publications {
        create<MavenPublication>(Constants.publicationName) {
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"

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
    setPublications(Constants.publicationName)
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = Constants.publicationName
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
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
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
        outputDirectory = "$projectDir/${Constants.dokkaDir}"
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
                logger.lifecycle("Version ${project.version} is not a release version - skipping upload.")
                false
            } else {
                true
            }
        }
    }
}
