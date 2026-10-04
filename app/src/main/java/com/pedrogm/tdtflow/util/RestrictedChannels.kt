package com.pedrogm.tdtflow.util

import java.text.Normalizer

/**
 * Channels from Atresmedia and Mediaset do not allow their streams to be used in
 * third-party apps. Users often search for them and leave negative reviews when
 * nothing shows up, so the empty search state explains why instead.
 */
object RestrictedChannels {

    private val TERMS = listOf(
        // Atresmedia
        "antena 3", "antena3", "a3", "la sexta", "lasexta", "la 6", "neox", "nova",
        "mega", "atreseries", "atresplayer",
        // Mediaset
        "telecinco", "tele 5", "tele5", "t5", "cuatro", "fdf", "energy",
        "divinity", "be mad", "bemad", "boing", "mitele"
    )

    private val DIACRITICS = "\\p{InCombiningDiacriticalMarks}+".toRegex()

    private fun normalize(text: String): String =
        Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
            .replace(DIACRITICS, "")
            .replace("\\s+".toRegex(), " ")

    /** True when [query] looks like a search for a restricted channel. */
    fun matches(query: String): Boolean {
        val q = normalize(query)
        if (q.length < 2) return false
        return TERMS.any { term -> term == q || (q.length >= 3 && term.startsWith(q)) || q.startsWith("$term ") }
    }
}
