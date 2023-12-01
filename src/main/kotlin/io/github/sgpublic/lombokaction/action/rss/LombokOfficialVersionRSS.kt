package io.github.sgpublic.lombokaction.action.rss

import com.dtflys.forest.Forest
import com.dtflys.forest.callback.RetryWhen
import com.dtflys.forest.http.ForestRequest
import com.dtflys.forest.http.ForestResponse
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.github.sgpublic.kotlin.core.util.fromGson
import io.github.sgpublic.kotlin.util.log
import io.github.sgpublic.lombokaction.Config
import java.util.LinkedList
import kotlin.reflect.KProperty


object LombokOfficialVersionRSS: RetryWhen {
    override fun retryWhen(req: ForestRequest<*>?, res: ForestResponse<*>?): Boolean {
        return false
    }

    operator fun getValue(thisRef: Any, prop: KProperty<*>): HashSet<String>? {
        try {
            val result = HashSet<String>()

            var pageIndex = 0
            while (true) {
                pageIndex += 1
                val json = Forest.request(String::class.java)
                        .url(Config.versionRss.lombokOfficial)
                        .addQuery("size", 20)
                        .addQuery("page", pageIndex)
                        .retryWhen(LombokOfficialVersionRSS)
                        .maxRetryCount(5)
                        .maxRetryInterval(5_000)
                        .sync()
                        .execute(String::class.java)
                val list = LombokVersionItem::class.fromGson(
                        JsonArray::class.fromGson(json)
                ).apply {
                    sortByDescending { it.version }
                }
                if (list.isEmpty()) {
                    break
                }
                for (lombokVersionItem in list) {
                    if (lombokVersionItem.version.startsWith("0.") ||
                            lombokVersionItem.compatibleVersions.ANDROID_STUDIO == null) {
                        continue
                    }
                    result.add(lombokVersionItem.version)
                }
            }

            return result
        } catch (e: Exception) {
            log.warn("Lombok 版本列表获取失败", e)
            return null
        }
    }

    data class LombokVersionItem(
        /** 211.7142.45 */
        @SerializedName("version")
        val version: String,
        @SerializedName("compatibleVersions")
        val compatibleVersions: CompatibleVersions,
    ) {
        data class CompatibleVersions(
            @SerializedName("ANDROID_STUDIO")
            val ANDROID_STUDIO: String? = null
        )
    }
}