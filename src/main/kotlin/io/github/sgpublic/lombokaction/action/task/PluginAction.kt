package io.github.sgpublic.lombokaction.action.task

import io.github.sgpublic.kotlin.core.util.closeQuietly
import io.github.sgpublic.kotlin.util.log
import io.github.sgpublic.lombokaction.Config
import io.github.sgpublic.lombokaction.action.rss.AndroidStudioVersionRSS
import io.github.sgpublic.lombokaction.action.rss.IdeaUltimateVersionRSS
import java.io.*
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
     * @param asBuild 指定版本
     */
    fun needAddVersion(asBuild: String): Boolean

    /**
     * 开始缺少指定版本 Lombok 的仓库提交插件文件
     * @param asBuild Android Studio 平台版本
     * @param asVersions 平台版本下 Android Studio 版本列表
     * @param ideaInfo 提供插件的源 IDEA Ultimate 版本
     */
    fun postVersion(
        asBuild: String,
        asVersions: LinkedList<AndroidStudioVersionRSS.AndroidVersionItem>,
        ideaInfo: IdeaUltimateVersionRSS.IdeaVersionItem,
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
    override fun needAddVersion(asBuild: String): Boolean {
        for (item in list) {
            if (!item.hasVersion(asBuild)) {
                return true
            }
        }
        return false
    }

    override fun postVersion(
        asBuild: String,
        asVersions: LinkedList<AndroidStudioVersionRSS.AndroidVersionItem>,
        ideaInfo: IdeaUltimateVersionRSS.IdeaVersionItem,
    ) {
        val plugin: File? = extractPlugin(asBuild, ideaInfo)
        if (plugin == null) {
            log.warn("IDEA Ultimate 版本 ${ideaInfo.version}（${ideaInfo.build}）下载失败")
            return
        }
        for (item in list) {
            if (item.hasVersion(asBuild)) {
                continue
            }
            try {
                item.saveVersion(plugin, asBuild, asVersions, ideaInfo)
            } catch (e: Exception) {
                log.warn("插件版本 ${ideaInfo.build}（目标 Android Studio 版本 $asBuild）保存失败，仓库 ID：${item.id}", e)
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
                    zipOut.putNextEntry(ZipEntry(entity.name.replace("plugins/lombok/", "")))

                    val buffer = ByteArray(4096)
                    var length: Int

                    val wrappedInput = zipIn.wrap(asBuild, ideaInfo)
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