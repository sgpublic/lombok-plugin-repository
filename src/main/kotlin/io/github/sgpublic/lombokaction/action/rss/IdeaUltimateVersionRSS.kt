package io.github.sgpublic.lombokaction.action.rss

import com.dtflys.forest.Forest
import com.dtflys.forest.callback.RetryWhen
import com.dtflys.forest.http.ForestRequest
import com.dtflys.forest.http.ForestResponse
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.github.sgpublic.kotlin.core.util.fromGson
import io.github.sgpublic.kotlin.util.log
import io.github.sgpublic.lombokaction.Config
import java.util.LinkedList
import kotlin.reflect.KProperty


object IdeaUltimateVersionRSS: RetryWhen {
    override fun retryWhen(req: ForestRequest<*>?, res: ForestResponse<*>?): Boolean {
        return false
    }

    operator fun getValue(thisRef: Any, prop: KProperty<*>): LinkedList<IdeaVersionItem>? {
        try {
            val json = Forest.request(String::class.java)
                .url(Config.versionRss.ideaUltimate)
                .retryWhen(IdeaUltimateVersionRSS)
                .maxRetryCount(5)
                .maxRetryInterval(10_000)
                .sync()
                .execute(String::class.java)
            val list = IdeaVersionItem::class.fromGson(
                JsonObject::class.fromGson(json)
                    .getAsJsonObject("content")
                    .getAsJsonArray("item")
            ).apply {
                sortByDescending {
                    it.build
                }
            }

            return list
        } catch (e: Exception) {
            log.warn("IntaliJ IDEA 版本列表获取失败", e)
            return null
        }
    }

    data class IdeaVersionItem(
        /** 211.7142.45 */
        @SerializedName("build")
        val build: String,
        /** 2021.1.1 */
        @SerializedName("version")
        val version: String,
        @SerializedName("downloads")
        val downloads: DownloadUrl,
    ) {
        data class DownloadUrl(
            @SerializedName("windowsZip")
            val windowsZip: DownloadUrlItem
        ) {
            data class DownloadUrlItem(
                /** https://...win.zip */
                @SerializedName("link")
                val link: String,
                /** 804167192 */
                @SerializedName("size")
                val size: Long,
                /** https://...win.zip.sha256 */
                @SerializedName("checksumLink")
                val checksumLink: String,
            )
        }
    }
}