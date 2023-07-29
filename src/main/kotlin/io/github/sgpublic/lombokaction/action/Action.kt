package io.github.sgpublic.lombokaction.action

import io.github.sgpublic.kotlin.util.Loggable
import org.quartz.Job
import org.quartz.JobExecutionContext

object Action: Loggable, Job {
    override fun execute(context: JobExecutionContext?) {

    }
}