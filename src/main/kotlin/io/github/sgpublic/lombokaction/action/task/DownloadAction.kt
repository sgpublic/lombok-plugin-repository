package io.github.sgpublic.lombokaction.action.task

import com.dtflys.forest.Forest
import io.github.sgpublic.kotlin.core.util.closeQuietly
import io.github.sgpublic.kotlin.util.Loggable
import io.github.sgpublic.kotlin.util.log
import io.github.sgpublic.lombokaction.Config
import io.github.sgpublic.lombokaction.action.rss.IdeaUltimateVersionRSS
import kotlinx.coroutines.*
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.language.bm.Lang
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.lang.IllegalStateException
import java.net.URI
import kotlin.math.max

/**
 * @author sgpublic
 * @Date 2023/7/29 15:35
 */
interface DownloadAction: Loggable {
    fun download(): File?

    companion object {
        fun of(info: IdeaUltimateVersionRSS.IdeaVersionItem, retry: Int = 3): DownloadAction {
            return DownloadActionImp(info, retry)
        }
    }
}



class DownloadActionImp(
    private val info: IdeaUltimateVersionRSS.IdeaVersionItem,
    private val retry: Int,
): DownloadAction {
    override fun download(): File? {
        log.info("开始下载 IDEA Ultimate：${info.build}（${info.downloads.windowsZip.link}）")
        try {
            if (tempFile.exists() && tempFile.isFile && checkSum()) {
                return tempFile
            }
            for (index in 0 until max(1, retry)) {
                try {
                    realDownload()
                    log.info("第 ${index + 1} 次尝试下载完成，校验文件完整性...")
                    if (checkSum()) {
                        return tempFile
                    } else {
                        throw IllegalStateException("文件校验不完整")
                    }
                } catch (e: Exception) {
                    log.warn("第 ${index + 1} 次尝试下载失败", e)
                }
            }
            log.warn("下载任务失败")
            return null
        } finally {
            log.info("下载任务结束")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun realDownload() {
        tempFile.deleteRecursively()
        tempFile.createNewFile()
        val url = URI.create(info.downloads.windowsZip.link)
        val resp = HttpClients.createDefault().execute(HttpGet(url))
        runBlocking {
            var downloaded = 0
            val downloadJob = GlobalScope.async {
                val buffer = ByteArray(4096)
                resp.entity.content.use { input ->
                    BufferedOutputStream(tempFile.outputStream()).use { output ->
                        var length: Int
                        while (input.read(buffer).also { length = it } > 0) {
                            downloaded += length
                            output.write(buffer, 0, length)
                        }
                    }
                }
            }
            val tickJob = GlobalScope.async {
                while (downloadJob.isActive) {
                    log.info("下载中（${info.build}）：${downloaded / info.downloads.windowsZip.size}%")
                }
            }
            downloadJob.await()
            tickJob.cancel()
        }
    }

    private val targetSha256: String by lazy {
        Forest.request(String::class.java)
            .url(info.downloads.windowsZip.checksumLink)
            .execute().toString()
    }

    private val tempFile: File by lazy {
        File(Config.tempDir, "idea/idea-${info.build}.zip")
    }

    private fun checkSum(): Boolean {
        val sha256 = tempFile.inputStream().use {
            DigestUtils.sha256Hex(it)
        }
        return if (sha256 == targetSha256) {
            log.debug("文件校验成功：{}", tempFile)
            true
        } else {
            log.debug("文件校验失败：{}", tempFile)
            false
        }
    }
}