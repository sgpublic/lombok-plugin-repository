package io.github.sgpublic.lombokaction.core.logback

import ch.qos.logback.core.PropertyDefinerBase
import io.github.sgpublic.lombokaction.Config

class LogPathDefiner: PropertyDefinerBase() {
    override fun getPropertyValue(): String {
        return Config.logDir
    }
}