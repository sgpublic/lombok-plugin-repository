import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.2"
//	id("org.graalvm.buildtools.native") version "0.9.23"

	val kotlin = "1.8.22"
	kotlin("jvm") version kotlin
	kotlin("plugin.spring") version kotlin
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
		maven("https://$it/api/v4/projects/11/packages/maven")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
//	developmentOnly("org.springframework.boot:spring-boot-devtools")
//	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	implementation("com.google.code.gson:gson")

	implementation("org.eclipse.jgit:org.eclipse.jgit:6.6.0.202305301015-r")
	implementation("com.charleskorn.kaml:kaml:0.54.0")
	implementation("commons-cli:commons-cli:1.5.0")

	val uniktx = "1.0.0-alpha04"
	implementation("io.github.sgpublic:uniktx-kotlin-common:$uniktx")
	implementation("io.github.sgpublic:uniktx-kotlin-logback:$uniktx")
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
