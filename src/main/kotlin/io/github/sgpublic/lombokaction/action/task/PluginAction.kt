package io.github.sgpublic.lombokaction.action.task

import io.github.sgpublic.kotlin.util.log
import io.github.sgpublic.lombokaction.Config
import io.github.sgpublic.lombokaction.action.rss.AndroidStudioVersionRSS
import io.github.sgpublic.lombokaction.action.rss.IdeaUltimateVersionRSS
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * @author sgpublic
 * @Date 2023/7/29 15:16
 */

interface PluginAction: AutoCloseable {
    /**
     * 检查所有仓库中是否有至少一个仓库缺少指定版本的 Lombok
     * @param targetInfo 目标版本信息
     */
    fun needAddVersion(
        targetInfo: PluginTargetInfo
    ): Boolean

    /**
     * 开始缺少指定版本 Lombok 的仓库提交插件文件
     * @param targetInfo 目标版本信息
     */
    fun postVersion(
        targetInfo: PluginTargetInfo
    )

    companion object {
        fun create(list: List<RepoAction>): PluginAction {
            return PluginActionImpl(list)
        }
    }
}

class PluginActionImpl(
    private val list: List<RepoAction>
): PluginAction {
    override fun needAddVersion(
        targetInfo: PluginTargetInfo
    ): Boolean {
        for (item in list) {
            if (!item.hasVersion(targetInfo)) {
                return true
            }
        }
        return false
    }

    override fun postVersion(
        targetInfo: PluginTargetInfo,
    ) {
        val plugin: File? = extractPlugin(
            targetInfo.androidStudio.platformBuild,
            targetInfo.ideaUltimate
        )
        if (plugin == null) {
            log.warn("IDEA Ultimate 版本 ${targetInfo.ideaUltimate.build} 下载失败")
            return
        }
        for (item in list) {
            if (item.hasVersion(targetInfo)) {
                continue
            }
            try {
                item.saveVersion(plugin, targetInfo)
            } catch (e: Exception) {
                log.warn("插件版本 ${targetInfo.ideaUltimate.build}（目标 Android Studio 版本 ${
                    targetInfo.androidStudio.platformBuild
                }）保存失败，仓库 ID：${item.id}", e)
            }
        }
    }

    private fun extractPlugin(
        asBuild: String,
        ideaInfo: IdeaUltimateVersionRSS.IdeaVersionItem,
    ): File? {
        val idea = DownloadAction.of(ideaInfo).download() ?: return null
        val tempDir = File(Config.tempDir, "lombok/lombok-${ideaInfo.build}.zip")
        tempDir.deleteRecursively()
        tempDir.parentFile.mkdirs()
        tempDir.createNewFile()
        ZipOutputStream(BufferedOutputStream(tempDir.outputStream())).use { zipOut ->
            ZipInputStream(BufferedInputStream(idea.inputStream())).use { zipIn ->
                var tmp: ZipEntry?
                while (zipIn.nextEntry.also { tmp = it } != null) {
                    val entity = tmp ?: continue
                    if (!entity.name.startsWith("plugins/lombok/")) {
                        continue
                    }
                    zipOut.putNextEntry(ZipEntry(entity.name.replace("plugins/", "")))

                    val buffer = ByteArray(4096)
                    var length: Int

                    val wrappedInput = zipIn.takeIf {
                        !entity.name.endsWith("lombok.jar")
                    } ?: zipIn.wrap(asBuild, ideaInfo)
                    while (wrappedInput.read(buffer).also { length = it } > 0) {
                        zipOut.write(buffer, 0, length)
                    }
                }
            }
        }
        return tempDir
    }

    override fun close() {
        for (repoAction in list) {
            repoAction.close()
        }
    }
}