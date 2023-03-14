import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/IDEA-262280

plugins {
	id("java-library")
	id("maven-publish")
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
	api(libs.network) {
		exclude("org.checkerframework", "checker-qual")
	}

	api(libs.gson)
	api(libs.snakeyaml)
	api(libs.guava)

	api(libs.bundles.log4j)
	api(libs.bundles.terminal) {
		exclude("org.jline", "jline-reader")
		exclude("org.apache.logging.log4j", "log4j-core")
	}

	compileOnly(libs.lombok)
	annotationProcessor(libs.lombok)
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

application {
	mainClass.set("org.distril.beengine.Bootstrap")
}

tasks {
	compileJava {
		options.encoding = Charsets.UTF_8.name()
		options.compilerArgs.add("--enable-preview")
	}

	shadowJar {
		manifest.attributes["Multi-Release"] = "true"

		transform(Log4j2PluginsCacheFileTransformer())

		// Backwards compatible jar directory
		destinationDirectory.set(file("$projectDir/target"))
		archiveClassifier.set("")
	}
}
