package io.github.sgpublic.lombokaction.action

import io.github.sgpublic.kotlin.util.Loggable
import io.github.sgpublic.lombokaction.Config
import io.github.sgpublic.lombokaction.action.rss.AndroidStudioVersionRSS
import io.github.sgpublic.lombokaction.action.rss.IdeaUltimateVersionRSS
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.util.*

object Action: Loggable, Job {
    private val asRss: LinkedList<AndroidStudioVersionRSS.AndroidVersionItem>? by AndroidStudioVersionRSS
    private val ideaRss: LinkedList<IdeaUltimateVersionRSS.IdeaVersionItem>? by IdeaUltimateVersionRSS

    override fun execute(context: JobExecutionContext?) {
        try {
            if (Config.repos.isEmpty()) {
                log.warn("没有配置仓库信息，跳过此次更新。")
                return
            }

            val asRss = this.asRss ?: return
            val ideaRss = this.ideaRss ?: return

        } finally {
            log.info("更新结束")
        }
    }
}