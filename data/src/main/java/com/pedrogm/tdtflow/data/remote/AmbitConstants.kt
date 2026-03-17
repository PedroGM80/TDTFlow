package com.pedrogm.tdtflow.data.remote

/**
 * Constants for ambit (channel category) names from the TDT API
 */
object AmbitConstants {
    // TV Categories
    const val GENERALISTAS = "Generalistas"
    const val INFORMATIVOS = "Informativos"
    const val DEPORTIVOS = "Deportivos"
    const val INFANTILES = "Infantiles"
    const val EVENTUALES = "Eventuales"
    const val STREAMING = "Streaming"

    // Radio Categories
    const val MUSICALES = "Musicales"
    const val POPULARES = "Populares"

    // Regional categories (Autonomous Communities)
    val REGIONAL_AMBITS = setOf(
        "Andalucía", "Aragón", "Asturias", "Canarias", "Cantabria",
        "Castilla-La Mancha", "Castilla y León", "Cataluña", "Ceuta",
        "C. Valenciana", "Extremadura", "Galicia", "Islas Baleares",
        "La Rioja", "Madrid", "Melilla", "Murcia", "Navarra", "País Vasco"
    )
}
