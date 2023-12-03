package io.github.sgpublic.lombokaction.action

import io.github.sgpublic.kotlin.util.Loggable
import io.github.sgpublic.lombokaction.Config
import io.github.sgpublic.lombokaction.action.rss.AndroidStudioVersionRSS
import io.github.sgpublic.lombokaction.action.rss.IdeaUltimateVersionRSS
import io.github.sgpublic.lombokaction.action.rss.LombokOfficialVersionRSS
import io.github.sgpublic.lombokaction.action.task.PluginAction
import io.github.sgpublic.lombokaction.action.task.PluginTargetInfo
import io.github.sgpublic.lombokaction.action.task.RepoAction
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.min

class Action: Job {
    private val asRss: LinkedHashMap<String, LinkedList<AndroidStudioVersionRSS.AndroidVersionItem>>? by AndroidStudioVersionRSS
    private val ideaRss: LinkedList<IdeaUltimateVersionRSS.IdeaVersionItem>? by IdeaUltimateVersionRSS
    private val lombokRss: HashSet<String>? by LombokOfficialVersionRSS

    fun realExecute(force: Boolean = false) {
        try {
            if (Config.repos.isEmpty()) {
                log.warn("没有配置仓库信息，跳过此次更新。")
                return
            }

            val asRss = this.asRss ?: return
            val ideaRss = this.ideaRss ?: return
            val lombokRss = this.lombokRss ?: return

            try {
                PluginAction.create(
                    Config.repos.map {
                        log.debug("检查仓库：${it.key}")
                        RepoAction.of(it.key, force, it.value)
                    }
                )
            } catch (e: Exception) {
                log.error("仓库检查出错！", e)
                return
            }.use { actions ->
                for ((asBuild, asInfo) in asRss) {
                    log.debug("检查版本：$asBuild")
                    val target = PluginTargetInfo(
                        PluginTargetInfo.AndroidStudio(asBuild, asInfo),
                        ideaRss.findTargetVersion(asBuild) ?: continue,
                    )
                    try {
                        if (lombokRss.contains(asBuild)) {
                            log.info("版本 $asBuild 由官方插件商城提供支持，跳过导出")
                            continue
                        }
                        if (!force && !actions.needAddVersion(target)) {
                            continue
                        }
                        log.info("开始导出插件：$asBuild（源版本 ${target.ideaUltimate.build}）")
                        actions.postVersion(target)
                    } catch (e: Exception) {
                        log.warn("版本导出失败：$asBuild", e)
                    }
                }
                log.info("开始写入介绍")
                actions.checkRepository()
            }
        } catch (e: Exception) {
            log.error("未捕获的错误！", e)
        } finally {
            log.info("更新结束")
        }
    }

    override fun execute(context: JobExecutionContext?) {
        realExecute()
    }

    private fun List<IdeaUltimateVersionRSS.IdeaVersionItem>.findTargetVersion(
        version: String
    ): IdeaUltimateVersionRSS.IdeaVersionItem? {
        val iterator = LinkedList(this).sortedBy { it.build }.iterator()
        val comparableVersion = Version(version)
        while (iterator.hasNext()) {
            val next = iterator.next()
            val nextBuild = next.build
            if (nextBuild == version) {
                log.debug("找到 $version 的原生版本：${nextBuild}")
                return next
            }
            if (Version(nextBuild) > comparableVersion) {
                log.debug("找到 $version 的替代版本：${nextBuild}")
                return next
            }
        }
        log.warn("未找到合适的版本：${version}")
        return null
    }


    private class Version(
        private val version: String
    ): Comparable<Version> {
        override fun compareTo(other: Version): Int {
            val thisVer = version.split(".").map { it.toInt() }
            val otherVer = other.version.split(".").map { it.toInt() }
            val length = min(thisVer.size, otherVer.size)
            for (index in 0 until length) {
                val compare = thisVer[index].compareTo(otherVer[index])
                if (compare != 0) {
                    return compare
                }
            }
            return 0
        }

        override fun equals(other: Any?): Boolean {
            return if (other !is Version) {
                false
            } else {
                version == other.version
            }
        }

        override fun hashCode(): Int {
            return version.hashCode()
        }
    }

    companion object: Loggable
}