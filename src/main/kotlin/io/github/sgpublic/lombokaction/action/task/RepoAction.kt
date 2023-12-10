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
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.errors.RepositoryNotFoundException
import java.io.File
import java.util.*

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

    /**
     * 检查并更新 README 和 wiki
     */
    fun checkRepository()

    companion object {
        fun of(id: String, force: Boolean, repo: AbsConfig.Repo): RepoAction {
            return RepoActionImpl(id, force, repo)
        }
    }
}

class RepoActionImpl internal constructor(
    override val id: String,
    force: Boolean,
    private val repo: AbsConfig.Repo
): RepoAction {
    private val tempDir: File = File(Config.tempDir, "git/$id")
    private val repository: File = File(tempDir, "repository")
    private val repositoryGit = checkout(repo.gitRepo, repository, repo.gitRepo.branch)
    private val wiki: File = File(tempDir, "wiki")
    private val wikiGit: Git = checkout(repo.wikiRepo, wiki)

    init {
        if (force) {
            File(repository, "plugins").deleteRecursively()
        }
    }

    private fun checkout(gitUrl: AbsConfig.Repo.GitUrl, target: File, branch: String? = null): Git {
        val open: Git = try {
            Git.open(target).also { git ->
                val remote = git.repository.config.getString("remote", "origin", "url")
                if (remote != gitUrl.gitUrl) {
                    git.close()
                    throw IllegalStateException("此目录不是目标仓库，而是：$remote")
                }
                if (branch != null) {
                    git.checkout()
                        .setName(branch)
                        .setStartPoint("origin/$branch")
                        .call()
                }
                git.fetch().call()
                git.reset().setRef("HEAD").setMode(ResetCommand.ResetType.HARD).call()
            }
        } catch (e: Exception) {
            when (e) {
                is RepositoryNotFoundException -> {
                    log.debug("仓库 $id（${gitUrl.gitUrl}）不存在，重新 clone")
                }
                is IllegalStateException -> {
                    log.warn("目标仓库不匹配，重新 clone")
                }
                else -> {
                    log.warn("仓库 $id（${gitUrl.gitUrl}）检查失败，重新 clone")
                    log.debug("错误信息：${e.message}", e)
                }
            }
            target.deleteRecursively()
            target.mkdirs()
            Git.cloneRepository()
                .setURI(gitUrl.gitUrl)
                .setDirectory(target)
                .also {
                    if (branch != null) {
                        it.setBranch(branch)
                    }
                }
                .applyAuth(gitUrl.auth)
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
            val exist = PluginTargetInfo::class.fromGson(target.readText())
            var forceUpdate = false
            if (exist.info == null) {
                forceUpdate = true
                exist.info = PluginTargetInfo.FileInfo(
                    size = plugin.length(),
                    date = plugin.lastModified(),
                )
                log.warn("文件信息缺失，已补充。")
            }
            if (targetInfo.androidStudio != exist.androidStudio || forceUpdate) {
                log.info("更新版本信息：${targetInfo.androidStudio.platformBuild}")
                targetInfo.info = exist.info
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
        targetInfo.info = PluginTargetInfo.FileInfo(file.length(), System.currentTimeMillis())
        targetInfo(targetInfo.androidStudio.platformBuild).writeText(targetInfo.toGson())
    }

    override fun checkRepository() {
        val existList = LinkedList<PluginTargetInfo>()
        for (file in File(repository, "plugins").listFiles() ?: return) {
            if (!file.isDirectory) {
                continue
            }
            val target = File(file, "target.json")
            if (!target.exists()) {
                log.warn("target.json 不存在：{}", target)
                continue
            }
            log.debug("读取 target.json：{}", target)
            existList.add(
                try {
                    PluginTargetInfo::class.fromGson(target.readText())
                } catch (e: Exception) {
                    log.warn("解析 target.json 出错（${target}）", e)
                    continue
                }
            )
        }
        if (existList.isEmpty()) {
            log.warn("当前仓库不包含任何 Lombok 插件信息")
            return
        }

        existList.sortByDescending {
            it.androidStudio.platformBuild
        }

        repository.checkRepository(existList, repo)
        wiki.checkWiki(existList, repo)
    }

    override fun close() {
        repositoryGit.autoClose()
        wikiGit.autoClose()
    }

    private fun Git.autoClose() {
        add().addFilepattern(".").call()
        if (status().call().hasUncommittedChanges()) {
            commit().setMessage("auto update").call()
        }
        push().setForce(true).call()
        close()
    }
}

data class PluginTargetInfo(
    @SerializedName("android_studio")
    val androidStudio: AndroidStudio,
    @SerializedName("idea_ultimate")
    val ideaUltimate: IdeaUltimateVersionRSS.IdeaVersionItem,
    @SerializedName("info")
    var info: FileInfo? = null,
) {
    data class AndroidStudio(
        @SerializedName("platform_build")
        val platformBuild: String,
        @SerializedName("versions")
        val versions: LinkedList<AndroidStudioVersionRSS.AndroidVersionItem>
    )
    data class FileInfo(
        @SerializedName("size")
        val size: Long,
        @SerializedName("date")
        val date: Long,
    )
}
val PluginTargetInfo.isWrapped: Boolean get() {
    return androidStudio.platformBuild != ideaUltimate.build
}
val PluginTargetInfo.AndroidStudio.isRelease: Boolean get() {
    for (version in versions) {
        if (version.channel.isRelease) {
            return true
        }
    }
    return false
}
val PluginTargetInfo.AndroidStudio.versionName: String get() {
    for (version in versions) {
        if (version.channel == AndroidStudioVersionRSS.AndroidVersionItem.Channel.Release) {
            return version.name
        }
    }
    throw IllegalStateException("此版本暂未发布 Release")
}