package io.github.sgpublic.lombokaction.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AbsConfig(
    @SerialName("debug")
    val debug: Boolean = false,
    @SerialName("cron")
    val cron: String = "0 0 2 * * *",
    @SerialName("temp-dir")
    val tempDir: String = "/tmp/lombok-plugin-repository",
    @SerialName("log-dir")
    val logDir: String = "/var/log/lombok-plugin-repository",
    @SerialName("version-rss")
    val versionRss: VersionRSS = VersionRSS(),
    @SerialName("repos")
    val repos: Map<String, Repo> = mapOf(),
) {
    @Serializable
    data class VersionRSS(
        @SerialName("android-studio")
        val androidStudio: String = "https://jb.gg/android-studio-releases-list.json",
        @SerialName("idea-ultimate")
        val ideaUltimate: String = "https://data.services.jetbrains.com/products?code=IU&fields=releases",
    )
    @Serializable
    data class Repo(
        @SerialName("branch-url")
        val branchUrl: String,
        @SerialName("full-repository")
        val fullRepository: String,
        @SerialName("item-download-url")
        val itemDownloadUrl: String,
        @SerialName("release-repository")
        val releaseRepository: String,
        @SerialName("branch")
        val branch: String,
        @SerialName("git-url")
        val gitUrl: String,
        @SerialName("repo-url")
        val repoUrl: String,
        @SerialName("wiki-git")
        val wikiGit: String,
        @SerialName("wiki-url")
        val wikiUrl: String,
    )
}