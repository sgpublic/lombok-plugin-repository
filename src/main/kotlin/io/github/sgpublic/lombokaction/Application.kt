package io.github.sgpublic.lombokaction

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import com.charleskorn.kaml.Yaml
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.github.sgpublic.kotlin.core.util.GSON
import io.github.sgpublic.kotlin.util.Loggable
import io.github.sgpublic.lombokaction.action.Action
import io.github.sgpublic.lombokaction.core.AbsConfig
import io.github.sgpublic.lombokaction.core.util.asDate
import org.apache.commons.cli.*
import org.quartz.JobKey
import org.quartz.impl.JobDetailImpl
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.triggers.CronTriggerImpl
import java.io.File
import java.io.IOException


object Application: Loggable {
    @JvmStatic
    fun main(args: Array<String>) {
        val cmd = cmd(args) ?: return

        val path = if (cmd.hasOption("config")) {
            cmd.getOptionValue("config")
        } else {
            "./config.yaml"
        }
        val file = File(path)

        if (!file.isFile) {
            throw IllegalStateException("配置文件不可用：$path")
        }
        val configPath: String = try {
            file.canonicalPath
        } catch (e: IOException) {
            file.path
        }
        try {
            config = file.inputStream().use {
                Yaml.default.decodeFromStream(AbsConfig.serializer(), it)
            }
            initLogback()
        } catch (e: Exception) {
            throw IllegalStateException("配置文件无效：$path", e)
        }
        log.info("lombok-plugin-repository 启动！")
        log.info("使用配置文件：$configPath")

        GSON = GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

        start(cmd.hasOption("now"), cmd.hasOption("force"))
    }

    private fun start(once: Boolean, force: Boolean) {
        if (once) {
            log.info("单次运行模式...")
            Action.realExecute(force)
            return
        }
        val factory = StdSchedulerFactory()
        val scheduler = factory.scheduler
        val cron = CronTriggerImpl().apply {
            cronExpression = Config.cron
            name = "cron"
        }
        val job = JobDetailImpl().apply {
            jobClass = Action::class.java
            key = JobKey("job")
        }
        scheduler.scheduleJob(job, cron)
        scheduler.start()
        log.info("服务模式...")
        Thread.currentThread().join()
    }

    private fun cmd(args: Array<String>): CommandLine? {
        return try {
            val options = Options()
            options.addOption(
                    Option.builder("c")
                            .longOpt("config")
                            .argName("配置文件路径")
                            .type(String::class.java)
                            .hasArg()
                            .build()
            )
            options.addOption(
                    Option.builder("n")
                            .longOpt("now")
                            .argName("单次运行")
                            .type(Boolean::class.java)
                            .build()
            )
            options.addOption(
                    Option.builder("f")
                            .longOpt("force")
                            .argName("强制更新")
                            .type(Boolean::class.java)
                            .build()
            )
            val parser: CommandLineParser = DefaultParser()
            parser.parse(options, args)
        } catch (e: Exception) {
            log.warn("参数解析出错", e)
            null
        }
    }

    private fun initLogback() {
        val context = org.slf4j.LoggerFactory.getILoggerFactory() as LoggerContext
        val configurator = JoranConfigurator()
        configurator.context = context
        context.reset()
        configurator.doConfigure(javaClass.classLoader.getResourceAsStream("logback.xml"))
    }
}

private lateinit var config: AbsConfig

val Config: AbsConfig get() = config
