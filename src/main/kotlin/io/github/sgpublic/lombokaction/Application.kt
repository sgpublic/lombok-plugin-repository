package io.github.sgpublic.lombokaction

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import io.github.sgpublic.kotlin.util.Loggable
import io.github.sgpublic.lombokaction.core.AbsConfig
import org.apache.commons.cli.*
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ApplicationContext
import java.io.File
import java.io.IOException
import kotlin.reflect.KClass
import kotlin.system.exitProcess


@SpringBootApplication
class Application {

    companion object: Loggable {
        const val APPLICATION_BASE_NAME = "lombok-plugin-repository"

        private lateinit var context: ApplicationContext
        val Context: ApplicationContext get() = context

        @JvmStatic
        fun main(args: Array<String>) {
            val cmd = cmd(args) ?: return

            val path = if (cmd.hasOption("config")) {
                cmd.getOptionValue("config")
            } else {
                "./config/config.yaml"
            }
            val file = File(path)

            if (!file.isFile) {
                log.error("配置文件不可用：$path")
                exitProcess(-1)
            }
            val configPath: String = try {
                file.canonicalPath
            } catch (e: IOException) {
                file.path
            }
            log.info("使用配置文件：$configPath")

            config = file.inputStream().use {
                Yaml.default.decodeFromStream(AbsConfig.serializer(), it)
            }

            context = Bootstrap(Application::class.java)
                    .setDebug(Config.debug)
                    .setLogPath(Config.logDir)
                    .run(args)
        }

        private fun cmd(args: Array<String>): CommandLine? {
            try {
                val options = Options()
                val configOption = Option.builder("c")
                        .longOpt("config")
                        .argName("配置文件路径")
                        .hasArg()
                        .build()
                options.addOption(configOption)
                val parser: CommandLineParser = DefaultParser()
                return parser.parse(options, args)
            } catch (e: Exception) {
                log.warn("参数解析出错", e)
                return null
            }
        }

        inline fun <reified T> getBean(): T {
            return Context.getBean(T::class.java)
        }

        val <T: Any> KClass<T>.Bean: T get() {
            return Context.getBean(java)
        }
    }
}


private class Bootstrap(clazz: Class<*>) {
    private val application: SpringApplication = SpringApplication(clazz)
    private val properties: MutableMap<String, Any> = HashMap()

    fun setDatasource(
            dbPath: String, dbUsername: String, dbPassword: String
    ): Bootstrap {
        properties["spring.datasource.username"] = dbUsername
        properties["spring.datasource.password"] = dbPassword
        properties["spring.datasource.url"] = "jdbc:h2:file:$dbPath/${Application.APPLICATION_BASE_NAME}"
        return this
    }

    fun setDebug(isDebug: Boolean): Bootstrap {
        if (isDebug) {
            properties["spring.profiles.active"] = "dev"
        } else {
            properties["spring.profiles.active"] = "prod"
        }
        return this
    }

    fun setLogPath(path: String): Bootstrap {
        properties["application.logging.path"] = path
        return this
    }

    fun run(args: Array<String>): ApplicationContext {
        application.setDefaultProperties(properties)
        return application.run(*args)
    }
}


private lateinit var config: AbsConfig

val Config: AbsConfig get() = config
