package io.github.sgpublic.lombokaction.action

import com.dtflys.forest.Forest
import com.dtflys.forest.callback.RetryWhen
import com.dtflys.forest.exceptions.ForestRetryException
import com.dtflys.forest.http.ForestRequest
import com.dtflys.forest.http.ForestResponse
import com.dtflys.forest.retryer.ForestRetryer
import com.google.gson.JsonObject
import io.github.sgpublic.kotlin.core.util.fromGson
import io.github.sgpublic.lombokaction.Config
import java.util.LinkedList


object AndroidStudioVersionRSS: RetryWhen {
    override fun retryWhen(req: ForestRequest<*>?, res: ForestResponse<*>?): Boolean {
        return false
    }

    operator fun getValue(thisRef: Any): LinkedList<AndroidVersionItem> {
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
        ).apply {
            sortBy {
                it.platformBuild
            }
        }
        println(list)
    }

    data class AndroidVersionItem(
        val platformBuild: String,
        val platformVersion: String,
        val name: String,
        val version: String,
        val download: List<DownloadUrl>,
    ) {
        data class DownloadUrl(
            val link: String,
            val checksum: String,
        )
    }
}