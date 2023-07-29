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


object AndroidStudioVersionRSS: RetryWhen {
    override fun retryWhen(req: ForestRequest<*>?, res: ForestResponse<*>?): Boolean {
        return false
    }

    operator fun getValue(thisRef: Any, prop: KProperty<*>): LinkedHashMap<String, LinkedList<AndroidVersionItem>>? {
        try {
            val json = Forest.request(String::class.java)
                .url(Config.versionRss.androidStudio)
                .retryWhen(AndroidStudioVersionRSS)
                .maxRetryCount(5)
                .maxRetryInterval(10_000)
                .sync()
                .execute(String::class.java)
            val list = AndroidVersionItem::class.fromGson(
                JsonObject::class.fromGson(json)
                    .getAsJsonObject("content")
                    .getAsJsonArray("item")
            )
            list.sortByDescending { it.version }

            val result = linkedMapOf<String, LinkedList<AndroidVersionItem>>()
            for (item in list) {
                if (item.version.split(".")[0].toInt() < 202) {
                    // 不适用于 Android Studio version is 4.2.2 (202.*) 及以下
                    continue
                }
                if (result[item.platformBuild] == null) {
                    result[item.platformBuild] = LinkedList()
                }
                result[item.platformBuild]!!.add(item)
            }

            return result
        } catch (e: Exception) {
            log.warn("Android Studio 版本列表获取失败", e)
            return null
        }
    }

    data class AndroidVersionItem(
        /** 2023.1.1.14 */
        @SerializedName("version")
        val version: String,
        /** 231.9225.16 */
        @SerializedName("platformBuild")
        val platformBuild: String,
        /** Android Studio Hedgehog | 2023.1.1 Canary 14 */
        @SerializedName("name")
        val name: String,
        /** Canary/Beta/RC/Release/Patch */
        @SerializedName("channel")
        val channel: Channel,
    ) {
        enum class Channel {
            Canary, Beta, RC, Release, Patch;

            val isRelease: Boolean by lazy {
                when (this) {
                    Release, Patch -> true
                    else -> false
                }
            }
        }
    }
}