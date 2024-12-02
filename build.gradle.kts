import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "dev.notro"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }

    // IntelliJ Platform testing for other IDEs
    // Should consider read it in the future
    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-testing-extension.html
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")

    intellijPlatform {
        val version = providers.gradleProperty("platformVersion")
        val type = providers.gradleProperty("platformType")
        create(type, version)

        bundledPlugin("com.intellij.java")

        pluginVerifier()
        zipSigner()
        instrumentationTools()

        testFramework(TestFrameworkType.Platform)
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    test { // still not sure if we need it
        useJUnitPlatform()
    }
}
