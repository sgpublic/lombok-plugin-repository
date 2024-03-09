import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	application
	val kotlin = "1.9.21"
	kotlin("jvm") version kotlin
	kotlin("plugin.serialization") version kotlin

	id("com.bmuschko.docker-remote-api") version "9.4.0"
}

group = "io.github.sgpublic"
version = "1.1.0"

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

application {
	mainClass = "io.github.sgpublic.lombokaction.Application"
}

repositories {
	mavenCentral()

	findProperty("publishing.gitlab.host")?.toString()?.let {
		// TODO 将 uniktx 发布到 mavenCentral
		maven("https://$it/api/v4/projects/11/packages/maven")
	}
}

dependencies {
	testImplementation("junit:junit:4.13.1")

	implementation("com.google.code.gson:gson:2.10.1")

	implementation("org.eclipse.jgit:org.eclipse.jgit:6.8.0.202311291450-r")
	implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:6.6.0.202305301015-r") {
		// com.jcraft.jsch.JSchException: invalid privatekey: xxx
		exclude("com.jcraft", "jsch")
	}
	// https://github.com/mwiede/jsch#by-replacing-a-direct-maven-dependency
	implementation("com.github.mwiede:jsch:0.2.16")

	implementation("com.charleskorn.kaml:kaml:0.54.0")
	implementation("io.github.sgpublic:SimplifyXMLObject:1.2.2")
	implementation("commons-cli:commons-cli:1.5.0")
	implementation("org.quartz-scheduler:quartz:2.3.2")

	implementation("ch.qos.logback:logback-classic:1.4.14")
	implementation("com.dtflys.forest:forest-core:1.5.36")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
	implementation("commons-codec:commons-codec:1.16.0")

	val uniktx = "1.0.0-beta01"
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

tasks {
	val clean by getting
	val assembleDist by getting
	val installDist by getting

	val dockerCreateDockerfile by creating(Dockerfile::class) {
		group = "docker"
		from("openjdk:17-slim-bullseye")
		workingDir("/app")
		copy {
			copyFile("./install/lombok-plugin-repository", "/app")
		}
		runCommand(listOf(
				"useradd -u 1000 runner",
				"apt-get update",
				"apt-get install findutils -y",
				"chown -R runner:runner /app"
		).joinToString(" &&\\\n "))
		user("runner")
		volume("/app/config.yaml")
		entryPoint("/app/bin/lombok-plugin-repository")
	}

	val tag = "mhmzx/lombok-plugin-repository"
	val dockerBuildImage by creating(DockerBuildImage::class) {
		group = "docker"
		dependsOn(assembleDist, installDist, dockerCreateDockerfile)
		inputDir = project.file("./build")
		dockerFile = dockerCreateDockerfile.destFile
		images.add("$tag:$version")
		images.add("$tag:latest")
		noCache = true
	}

	val dockerPushImageOfficial by creating(DockerPushImage::class) {
		group = "docker"
		dependsOn(dockerBuildImage)
		images.add("$tag:$version")
		images.add("$tag:latest")
	}
}

docker {
	registryCredentials {
		username = findProperty("publishing.docker.username")!!.toString()
		password = findProperty("publishing.docker.password")!!.toString()
		email = findProperty("publishing.developer.email")!!.toString()
	}
}
