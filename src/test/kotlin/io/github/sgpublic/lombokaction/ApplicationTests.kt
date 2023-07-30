package io.github.sgpublic.lombokaction

import org.junit.Test

class ApplicationTests {
    @Test
    fun read() {
        assert(ClassLoader.getSystemClassLoader().getResource("readme-template/cn/main.md") != null)
    }
}
