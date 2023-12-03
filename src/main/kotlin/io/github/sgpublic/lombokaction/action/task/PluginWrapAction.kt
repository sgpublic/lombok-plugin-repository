package io.github.sgpublic.lombokaction.action.task

import io.github.sgpublic.lombokaction.Config
import io.github.sgpublic.lombokaction.action.Action.Companion.log
import io.github.sgpublic.lombokaction.action.rss.IdeaUltimateVersionRSS
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * @author sgpublic
 * @Date 2023/7/29 18:12
 */
fun InputStream.wrap(
    asBuild: String,
    ideaInfo: IdeaUltimateVersionRSS.IdeaVersionItem,
): InputStream {
    log.info("检查插件版本")
    if (asBuild == ideaInfo.build) {
        log.info("插件版本与平台一致，跳过修改")
        return this
    }

    log.warn("插件版本与平台不一致，开始修改")

    val tempDir = File(Config.tempDir, "./wrap/${asBuild}")
    val origin = File(tempDir, "lombok-${ideaInfo.build}.jar").also {
        it.deleteRecursively()
        it.parentFile.mkdirs()
        it.createNewFile()
    }
    val wrapped = File(tempDir, "lombok-${ideaInfo.build}.wrapped.jar").also {
        it.deleteRecursively()
        it.parentFile.mkdirs()
        it.createNewFile()
    }

    val buffer = ByteArray(4096)
    var length: Int

    log.info("将 jar 文件解压至缓存文件夹...")
    origin.outputStream().use { temp ->
        while (read(buffer).also { length = it } > 0) {
            temp.write(buffer, 0, length)
        }
    }

    log.info("打开 jar 修改版本信息...")
    ZipInputStream(origin.inputStream()).use { zipIn ->
        ZipOutputStream(wrapped.outputStream()).use { zipOut ->
            var changed = false

            var tmp: ZipEntry?
            while (zipIn.nextEntry.also { tmp = it } != null) {
                val entity = tmp ?: continue
                if (entity.name != "META-INF/plugin.xml") {
                    zipOut.putNextEntry(entity)
                    while (zipIn.read(buffer).also { length = it } > 0) {
                        zipOut.write(buffer, 0, length)
                    }
                    continue
                }

                zipOut.putNextEntry(ZipEntry(entity.name))
                var pluginXml = zipIn.reader().readText()
                val sinceBuild = "since-build=\"${ideaInfo.build}\""
                if (!pluginXml.contains(sinceBuild)) {
                    log.warn("未在 META-INF/plugin.xml 中找到 \"$sinceBuild\" 字样")
                } else {
                    changed = true
                    pluginXml = pluginXml.replace(sinceBuild, "since-build=\"${asBuild}\"")
                }
                zipOut.writer().let {
                    it.write(pluginXml)
                    it.flush()
                }
            }

            if (!changed) {
                throw IllegalStateException("未完成 META-INF/plugin.xml 文件的修改！")
            }
        }
    }

    return wrapped.inputStream()
}