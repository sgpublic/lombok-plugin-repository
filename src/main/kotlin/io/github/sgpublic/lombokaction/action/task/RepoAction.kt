package io.github.sgpublic.lombokaction.action.task

import com.google.gson.annotations.SerializedName
import io.github.sgpublic.kotlin.core.util.toGson
import io.github.sgpublic.kotlin.util.log
import io.github.sgpublic.lombokaction.Config
import io.github.sgpublic.lombokaction.action.rss.AndroidStudioVersionRSS
import io.github.sgpublic.lombokaction.action.rss.IdeaUltimateVersionRSS
import io.github.sgpublic.lombokaction.core.AbsConfig
import org.eclipse.jgit.api.Git
import java.io.File
import java.lang.IllegalStateException

/**
 * @author sgpublic
 * @Date 2023/7/29 14:56
 */
interface RepoAction: AutoCloseable {
    val id: String

    /**
     * 检查当前 repo 是否包含指定版本的 Lombok
     * @param version 指定版本
     */
    fun hasVersion(version: String): Boolean

    /**
     * 提交插件文件
     * @param file 插件文件
     * @param ideaInfo 提供插件的源 IDEA Ultimate 版本
     */
    fun saveVersion(
        file: File,
        asInfo: AndroidStudioVersionRSS.AndroidVersionItem,
        ideaInfo: IdeaUltimateVersionRSS.IdeaVersionItem,
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
        File(Config.tempDir, "git/${id}")
    }
    private val repository: File by lazy {
        File(tempDir, "repository")
    }
    private val wiki: File by lazy {
        File(tempDir, "wiki")
    }

    private fun checkout(url: String, target: File, branch: String? = null): Git {
        val open = try {
            val git = Git.open(target)
            val remote = git.repository.config.getString("remote", "origin", "url")
            git.takeIf { remote == repo.gitUrl }
                ?: throw IllegalStateException("此目录不是目标仓库，而是：$remote")
        } catch (e: Exception) {
            log.warn("仓库 $id（${repo.gitUrl}）不存在或检查失败，重新 clone")
            Git.cloneRepository()
                .setURI(repo.gitUrl)
                .setGitDir(target)
                .call()
        }
        if (branch != null) {
            open.checkout().setName(branch).call()
        }
        return open
    }

    private fun plugin(version: String): File {
        return File(repository, "plugins/${version}/lombok-$version.zip")
    }
    private fun targetInfo(version: String): File {
        return File(repository, "plugins/${version}/target.json")
    }
    override fun hasVersion(version: String): Boolean {
        return plugin(version).exists() && targetInfo(version).exists()
    }

    override fun saveVersion(
        file: File,
        asInfo: AndroidStudioVersionRSS.AndroidVersionItem,
        ideaInfo: IdeaUltimateVersionRSS.IdeaVersionItem,
    ) {
        checkout(repo.gitUrl, repository, repo.branch)
        file.copyTo(plugin(ideaInfo.build))
        targetInfo(ideaInfo.build).writeText(PluginTargetInfo(
            asInfo, ideaInfo
        ).toGson())
    }

    override fun close() {

    }
}

data class PluginTargetInfo(
    @SerializedName("android_studio")
    val androidStudio: AndroidStudioVersionRSS.AndroidVersionItem,
    @SerializedName("idea_ultimate")
    val ideaUltimate: IdeaUltimateVersionRSS.IdeaVersionItem,
)