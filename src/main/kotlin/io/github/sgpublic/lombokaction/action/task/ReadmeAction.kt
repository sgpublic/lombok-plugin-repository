package io.github.sgpublic.lombokaction.action.task

import com.charleskorn.kaml.YamlPath.Companion.root
import com.sgpublic.xml.SXMLObject
import io.github.sgpublic.kotlin.core.util.fromGson
import io.github.sgpublic.kotlin.util.log
import io.github.sgpublic.lombokaction.core.AbsConfig
import io.github.sgpublic.lombokaction.core.fullRepository
import io.github.sgpublic.lombokaction.core.itemDownloadUrl
import io.github.sgpublic.lombokaction.core.releaseRepository
import io.github.sgpublic.lombokaction.core.util.exportMarkdown
import java.io.File
import java.util.*

/**
 * @author sgpublic
 * @Date 2023/7/30 11:19
 */

fun File.checkRepository(repo: AbsConfig.Repo) {
    val existList = LinkedList<PluginTargetInfo>()
    for (file in listFiles() ?: return) {
        if (!file.isDirectory) {
            continue
        }
        val target = File(file, "target.json")
        log.debug("读取 target.json：{}", target)
        if (!target.exists()) {
            return
        }
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

    existList.checkMainReadme(this, repo)
    existList.checkRepository(this, repo)
}

private fun LinkedList<PluginTargetInfo>.checkMainReadme(baseFile: File, repo: AbsConfig.Repo) {
    val versionItem = StringJoiner("\n")
    for (item in this) {
        if (!item.androidStudio.isRelease) {
            continue
        }
        versionItem.add("| ${
            item.androidStudio.versionName
        } | ${item.ideaUltimate.version} (${item.ideaUltimate.build}) | [lombok-${
            item.ideaUltimate.build
        }](${
            repo.itemDownloadUrl(item.androidStudio.platformBuild, item.ideaUltimate.build)
        }) |")
    }
    baseFile.exportMarkdown("main") {
        it.replace("%RELEASE%", repo.releaseRepository())
            .replace("%FULL%", repo.fullRepository())
            .replace("%WIKI_URL%", repo.wikiUrl)
            .replace("%VERSIONS%", versionItem.toString())
    }
}

private fun LinkedList<PluginTargetInfo>.checkRepository(baseFile: File, repo: AbsConfig.Repo) {
    File(baseFile, "release")
        .writeText(RepositoryXMLObject(repo, this.filter { it.androidStudio.isRelease }).toString())
    File(baseFile, "full")
        .writeText(RepositoryXMLObject(repo, this).toString())
}

private fun RepositoryXMLObject(repo: AbsConfig.Repo, list: List<PluginTargetInfo>): SXMLObject {
    return SXMLObject().also { root ->
        root.setRootTagName("plugin-repository")
        root.putInnerObject(SXMLObject().also { ff ->
            ff.setRootTagName("ff")
            ff.setInnerData("Tools Integration")
        })
        root.putInnerObject(SXMLObject().also { category ->
            category.setRootTagName("category")
            for (plugin in list) {
                val info = plugin.info ?: continue
                category.putInnerObject(SXMLObject().also { ideaPlugin ->
                    ideaPlugin.setRootTagName("idea-plugin")
                    ideaPlugin.putAttr("download", 0)
                    ideaPlugin.putAttr("size", info.size)
                    ideaPlugin.putAttr("date", info.date)
                    ideaPlugin.putAttr("updatedDate", info.date)
                    ideaPlugin.putAttr("url", repo.repoUrl)
                    ideaPlugin.putInnerObject(SXMLObject().also { name ->
                        name.setRootTagName("name")
                        name.setInnerData("Lombok")
                    })
                    ideaPlugin.putInnerObject(SXMLObject().also { id ->
                        id.setRootTagName("id")
                        id.setInnerData("Lombook Plugin")
                    })
                    ideaPlugin.putInnerObject(SXMLObject().also { description ->
                        description.setRootTagName("description")
                        description.setInnerData("Lombook Plugin")
                    })
                    ideaPlugin.putInnerObject(SXMLObject().also { version ->
                        version.setRootTagName("version")
                        version.setInnerData(plugin.androidStudio.platformBuild)
                    })
                    ideaPlugin.putInnerObject(SXMLObject().also { vendor ->
                        vendor.setRootTagName("vendor")
                        vendor.putAttr("email", "")
                        vendor.putAttr("url", repo.wikiUrl)
                    })
                    ideaPlugin.putInnerObject(SXMLObject().also { rating ->
                        rating.setRootTagName("rating")
                        rating.setInnerData("5.0")
                    })
                    ideaPlugin.putInnerObject(SXMLObject().also { rating ->
                        rating.setRootTagName("rating")
                        rating.setInnerData("5.0")
                    })
                    ideaPlugin.putInnerObject(SXMLObject().also { changeNotes ->
                        changeNotes.setRootTagName("change-notes")
                        changeNotes.setInnerData("")
                    })
                    ideaPlugin.putInnerObject(SXMLObject().also { downloadUrl ->
                        downloadUrl.setRootTagName("download-url")
                        downloadUrl.setInnerData(repo.itemDownloadUrl(
                            plugin.androidStudio.platformBuild, plugin.ideaUltimate.build
                        ))
                    })
                    ideaPlugin.putInnerObject(SXMLObject().also { ideaVersion ->
                        ideaVersion.setRootTagName("idea-version")
                        ideaVersion.putAttr("min", "n/a")
                        ideaVersion.putAttr("max", "n/a")
                        ideaVersion.putAttr("since-build", plugin.androidStudio.platformBuild)
                        ideaVersion.putAttr("until-build", plugin.ideaUltimate.build)
                    })
                })
            }
        })
    }
}


