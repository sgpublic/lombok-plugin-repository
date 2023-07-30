package io.github.sgpublic.lombokaction

import io.github.sgpublic.kotlin.util.Loggable
import io.github.sgpublic.lombokaction.core.util.asDate
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ApplicationTests: Loggable {
    @Test
    fun read() {
        log.info("July 25, 2023".asDate.toString())
    }
}
