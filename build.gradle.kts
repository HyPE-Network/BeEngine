import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
	kotlin("jvm") version "1.9.0"
	id("java-library")
	id("application")
	alias(libs.plugins.shadow)
}

group = "org.distril.beengine"
version = "1.0.0"

repositories {
	mavenLocal()
	mavenCentral()
	maven("https://repo.opencollab.dev/maven-releases")
	maven("https://repo.opencollab.dev/maven-snapshots")
}

dependencies {
	implementation(libs.network) { exclude("org.checkerframework", "checker-qual") }

	implementation(libs.configurate)

	implementation(libs.gson)
	implementation(libs.coroutines)

	implementation(libs.bundles.log4j)
	implementation(libs.bundles.terminal) {
		exclude("org.jline", "jline-reader")
		exclude("org.apache.logging.log4j", "log4j-core")
	}
}

kotlin { jvmToolchain(17) }

java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

application { mainClass.set("org.distril.beengine.BootstrapKt") }

tasks {
	compileJava {
		options.encoding = Charsets.UTF_8.name()
		options.compilerArgs.addAll(arrayOf("-Xlint:unchecked", "-Xlint:deprecation"))
		options.isIncremental = true
	}

	shadowJar {
		manifest.attributes["Multi-Release"] = "true"

		transform(Log4j2PluginsCacheFileTransformer())

		destinationDirectory.set(file("$projectDir/target"))
		archiveClassifier.set("")
	}
}
