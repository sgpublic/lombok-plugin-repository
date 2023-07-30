package io.github.sgpublic.lombokaction.core.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author sgpublic
 * @Date 2023/7/30 16:53
 */

val String.asDate: Date get() {
    val formatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
    return formatter.parse(this)
}