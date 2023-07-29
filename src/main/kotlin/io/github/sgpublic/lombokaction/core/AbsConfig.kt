package io.github.sgpublic.lombokaction.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.eclipse.jgit.api.GitCommand
import org.eclipse.jgit.api.TransportCommand
import org.eclipse.jgit.transport.*

@Serializable
data class AbsConfig(
    @SerialName("debug")
    val debug: Boolean = false,
    @SerialName("cron")
    val cron: String = "0 0 2 * * ?",
    @SerialName("temp-dir")
    val tempDir: String = "/tmp/lombok-plugin-repository",
    @SerialName("log-dir")
    val logDir: String = "/var/log/lombok-plugin-repository",
    @SerialName("version-rss")
    val versionRss: VersionRSS = VersionRSS(),
    @SerialName("download-retry")
    val downloadRetry: Int = 3,
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

        @SerialName("repo-url")
        val repoUrl: String,
        @SerialName("wiki-url")
        val wikiUrl: String,

        @SerialName("plugin-repo")
        val gitRepo: PluginRepository,
        @SerialName("wiki-repo")
        val wikiRepo: WikiRepository,
    ) {
        sealed interface GitUrl {
            val gitUrl: String
            val auth: PasswordAuth?
        }
        @Serializable
        data class PluginRepository(
            @SerialName("branch")
            val branch: String,
            @SerialName("git-url")
            override val gitUrl: String,
            @SerialName("auth")
            override val auth: PasswordAuth? = null,
        ): GitUrl
        @Serializable
        data class WikiRepository(
            @SerialName("git-url")
            override val gitUrl: String,
            @SerialName("auth")
            override val auth: PasswordAuth? = null,
        ): GitUrl

        @Serializable
        data class PasswordAuth(
            @SerialName("username")
            val username: String,
            @SerialName("token")
            val token: String,
        )
    }
}

fun <T: GitCommand<R>, R> TransportCommand<T, R>.applyAuth(auth: AbsConfig.Repo.PasswordAuth?): T {
    if (auth != null) {
        setCredentialsProvider(
            UsernamePasswordCredentialsProvider(auth.username, auth.token)
        )
    }
    @Suppress("UNCHECKED_CAST")
    return this as T
}