package io.github.sgpublic.lombokaction.action.task

import com.google.gson.annotations.SerializedName
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
     * @param asBuild 指定版本
     */
    fun hasVersion(asBuild: String): Boolean

    /**
     * 提交插件文件
     * @param file 插件文件
     * @param asBuild Android Studio 平台版本
     * @param asVersions 平台版本下 Android Studio 版本列表
     * @param ideaInfo 提供插件的源 IDEA Ultimate 版本
     */
    fun saveVersion(
        file: File,
        asBuild: String,
        asVersions: LinkedList<AndroidStudioVersionRSS.AndroidVersionItem>,
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
            open.checkout().setName("origin/$branch").call()
        }
        return open
    }

    private fun plugin(asBuild: String, ideaBuild: String): File {
        return File(repository, "plugins/${asBuild}/lombok-$ideaBuild.zip")
    }
    private fun targetInfo(asBuild: String): File {
        return File(repository, "plugins/${asBuild}/target.json")
    }
    override fun hasVersion(asBuild: String): Boolean {
        val root = File(repository, "plugins/${asBuild}")
        return root.listFiles { _: File, name: String ->
            name == "target.json" || (name.startsWith("lombok-") && name.endsWith(".zip"))
        }?.size == 2
    }

    override fun saveVersion(
        file: File,
        asBuild: String,
        asVersions: LinkedList<AndroidStudioVersionRSS.AndroidVersionItem>,
        ideaInfo: IdeaUltimateVersionRSS.IdeaVersionItem,
    ) {
        file.copyTo(plugin(asBuild, ideaInfo.build), true)
        targetInfo(asBuild).writeText(PluginTargetInfo(
            PluginTargetInfo.AndroidStudio(asBuild, asVersions), ideaInfo
        ).toGson())
        log.info("插件导出成功：$asBuild")
    }

    override fun close() {
        repositoryGit.also {
            it.add().addFilepattern(".").call()
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
