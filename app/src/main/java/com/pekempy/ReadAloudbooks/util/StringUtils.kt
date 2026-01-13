package com.pekempy.ReadAloudbooks.util

object StringUtils {
    fun normalizeTitle(title: String?): String {
        if (title.isNullOrBlank()) return ""
        val lowercase = title.trim().lowercase()
        return when {
            lowercase.startsWith("the ") -> title.substring(4)
            lowercase.startsWith("a ") -> title.substring(2)
            lowercase.startsWith("an ") -> title.substring(3)
            else -> title
        }
    }
}
