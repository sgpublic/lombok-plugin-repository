package io.github.sgpublic.lombokaction.core.util

import java.io.File

/**
 * @author sgpublic
 * @Date 2023/7/30 12:14
 */


private fun loadResource(name: String): String {
    return ClassLoader.getSystemClassLoader().getResourceAsStream(name)!!.reader().readText()
}

fun File.exportMarkdown(
    template: String,
    translate: Boolean = true,
    basename: String = "README",
    replacement: ((String) -> String) = { it }
) {
    if (translate) {
        File(this, "$basename.md").writeText(
            replacement(loadResource("readme-template/cn/$template.md"))
        )
        File(this, "$basename.EN.md").writeText(
            replacement(loadResource("readme-template/en/$template.md"))
        )
    } else {
        File(this, "$basename.md").writeText(
            replacement(loadResource("readme-template/$template.md"))
        )
    }
}