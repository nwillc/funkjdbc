import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
    jacoco
    `maven-publish`
    Dependencies.plugins.forEach { (n, v) -> id(n) version v }
}

group = "com.github.nwillc"
version = "0.12.0"

logger.lifecycle("${project.group}.${project.name}@${project.version}")

repositories {
    jcenter()
}

dependencies {
    Dependencies.artifacts(
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core"
    ) { implementation(it) }

    Dependencies.artifacts(
        "org.junit.jupiter:junit-jupiter",
        "org.assertj:assertj-core"
    ) { testImplementation(it) }

    Dependencies.artifacts(
        "com.h2database:h2"
    ) { testRuntimeOnly(it) }
}

ktlint {
    version.set(ToolVersions.ktlint)
    disabledRules.set(setOf("import-ordering"))
}

detekt {
    toolVersion = PluginVersions.detekt
    reports {
        html.enabled = true
        txt.enabled = true
    }
}

jacoco {
    toolVersion = ToolVersions.jacoco
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn("dokka")
    archiveClassifier.set("javadoc")
    from("$projectDir/${Constants.dokkaDir}")
}

val testJar by tasks.registering(Jar::class) {
    archiveClassifier.set("tests")
    from(sourceSets.test.get().output)
}

val testSourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("testSources")
    from(sourceSets.test.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>(Constants.publicationName) {
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"

            artifact(sourcesJar.get())
            artifact(javadocJar.get())
            artifact(testJar.get())
            artifact(testSourcesJar.get())
            from(components["java"])
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
        configuration {
            externalDocumentationLink {
                url = URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/")
                packageListUrl = URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/package-list")
            }
        }
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
