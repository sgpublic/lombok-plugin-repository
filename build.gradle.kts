import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	val kotlin = "1.8.22"
	kotlin("jvm") version kotlin
	kotlin("plugin.serialization") version kotlin
}

group = "io.github.sgpublic"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()

	findProperty("publishing.gitlab.host")?.toString()?.let {
		// TODO 将 uniktx 发布到 mavenCentral
		maven("https://$it/api/v4/projects/11/packages/maven")
	}
}

dependencies {
	implementation("com.google.code.gson:gson:2.10.1")

	implementation("org.eclipse.jgit:org.eclipse.jgit:6.6.0.202305301015-r")
	implementation("com.charleskorn.kaml:kaml:0.54.0")
	implementation("commons-cli:commons-cli:1.5.0")
	implementation("org.quartz-scheduler:quartz:2.3.2")

	implementation("ch.qos.logback:logback-classic:1.4.8")
	implementation("com.dtflys.forest:forest-core:1.5.32")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")

	val uniktx = "1.0.0-alpha04"
	implementation("io.github.sgpublic:uniktx-kotlin-common:$uniktx")
	implementation("io.github.sgpublic:uniktx-kotlin-logback:$uniktx")
	implementation("io.github.sgpublic:uniktx-kotlin-forest:$uniktx")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
