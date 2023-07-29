package io.github.sgpublic.lombokaction.core.logback

import io.github.sgpublic.kotlin.core.logback.filter.ConsoleFilter
import io.github.sgpublic.lombokaction.Config

open class ConsoleFilter: ConsoleFilter(
    debug = Config.debug,
    baseName = "io.github.sgpublic.lombokaction"
)