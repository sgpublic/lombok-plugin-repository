package io.github.sgpublic.lombokaction.action.task

import com.google.gson.annotations.SerializedName
import io.github.sgpublic.kotlin.core.util.fromGson
import io.github.sgpublic.kotlin.core.util.toGson
import io.github.sgpublic.kotlin.util.log
import io.github.sgpublic.lombokaction.Config
import io.github.sgpublic.lombokaction.action.rss.AndroidStudioVersionRSS
import io.github.sgpublic.lombokaction.action.rss.IdeaUltimateVersionRSS
import io.github.sgpublic.lombokaction.core.AbsConfig
import io.github.sgpublic.lombokaction.core.applyAuth
import org.apache.http.auth.UsernamePasswordCredentials
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
import java.lang.IllegalStateException
import java.util.LinkedList

/**
 * @author sgpublic
 * @Date 2023/7/29 14:56
 */
interface RepoAction: AutoCloseable {
    val id: String

    /**
     * 检查当前 repo 是否包含适用于指定 Android Studio 版本的 Lombok
     * @param targetInfo 目标版本信息
     */
    fun hasVersion(
        targetInfo: PluginTargetInfo,
    ): Boolean

    /**
     * 提交插件文件
     * @param file 插件文件
     * @param targetInfo 目标版本信息
     */
    fun saveVersion(
        file: File,
        targetInfo: PluginTargetInfo,
    )

    companion object {
        fun of(id: String, repo: AbsConfig.Repo): RepoAction {
            return RepoActionImpl(id, repo)
        }
    }
}

class RepoActionImpl internal constructor(
    override val id: String,
    private val repo: AbsConfig.Repo
): RepoAction {
    private val tempDir: File by lazy {
        File(Config.tempDir, "git/$id")
    }
    private val repository: File by lazy {
        File(tempDir, "repository")
    }
    private val repositoryGit = checkout(repo.gitRepo, repository, repo.gitRepo.branch)
    private val wiki: File by lazy {
        File(tempDir, "wiki")
    }
    private val wikiGit: Lazy<Git> = lazy {
        checkout(repo.wikiRepo, wiki)
    }

    private fun checkout(gitUrl: AbsConfig.Repo.GitUrl, target: File, branch: String? = null): Git {
        val open = try {
            val git = Git.open(target)
            val remote = git.repository.config.getString("remote", "origin", "url")
            git.takeIf { remote == gitUrl.gitUrl }
                ?: throw IllegalStateException("此目录不是目标仓库，而是：$remote")
        } catch (e: Exception) {
            log.warn("仓库 $id（${gitUrl.gitUrl}）不存在或检查失败，重新 clone")
            Git.cloneRepository()
                .setURI(gitUrl.gitUrl)
                .setDirectory(target)
                .applyAuth(gitUrl.auth)
                .call()
        }
        if (branch != null) {
            open.checkout()
                .setName(branch)
                .setCreateBranch(true)
                .setStartPoint("origin/$branch")
                .call()
        }
        return open
    }

    private fun plugin(asBuild: String, ideaBuild: String): File {
        return File(repository, "plugins/${asBuild}/lombok-$ideaBuild.zip")
    }
    private fun targetInfo(asBuild: String): File {
        return File(repository, "plugins/${asBuild}/target.json")
    }
    override fun hasVersion(
        targetInfo: PluginTargetInfo,
    ): Boolean {
        val root = File(repository, "plugins/${targetInfo.androidStudio.platformBuild}")
        val plugin = File(root, "lombok-${targetInfo.ideaUltimate.build}.zip")
        val target = File(root, "target.json")
        return if (plugin.exists() && target.exists()) {
            if (targetInfo != PluginTargetInfo::class.fromGson(target.readText())) {
                log.info("更新版本信息：${targetInfo.androidStudio.platformBuild}")
                target.writeText(targetInfo.toGson())
            }
            true
        } else {
            false
        }
    }

    override fun saveVersion(
        file: File,
        targetInfo: PluginTargetInfo,
    ) {
        file.copyTo(plugin(
            targetInfo.androidStudio.platformBuild,
            targetInfo.ideaUltimate.build
        ), true)
        targetInfo(targetInfo.androidStudio.platformBuild).writeText(targetInfo.toGson())
        log.info("插件导出成功：${targetInfo.androidStudio.platformBuild}")
    }

    override fun close() {
        repositoryGit.also {
            it.add().addFilepattern(".").call()
            it.commit().setMessage("auto update").call()
            it.push().setForce(true).call()
            it.close()
        }
        if (wikiGit.isInitialized()) {
            wikiGit.value.also {
                it.add().addFilepattern(".").call()
                it.push().setForce(true).call()
                it.close()
            }
        }
    }
}

data class PluginTargetInfo(
    @SerializedName("android_studio")
    val androidStudio: AndroidStudio,
    @SerializedName("idea_ultimate")
    val ideaUltimate: IdeaUltimateVersionRSS.IdeaVersionItem,
) {
    data class AndroidStudio(
        @SerializedName("platform_build")
        val platformBuild: String,
        @SerializedName("versions")
        val versions: LinkedList<AndroidStudioVersionRSS.AndroidVersionItem>
    )
}
val PluginTargetInfo.isWrapped: Boolean get() {
    return androidStudio.platformBuild != ideaUltimate.build
}
