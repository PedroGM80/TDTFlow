package com.pedrogm.tdtflow.domain.model

data class Program(
    val title: String,
    val description: String? = null,
    val startTime: Long, // Epoch millis
    val endTime: Long,   // Epoch millis
    val channelUrl: String
)

fun Program.isNow(): Boolean {
    val now = System.currentTimeMillis()
    return now in startTime until endTime
}

fun Program.progress(): Float {
    val now = System.currentTimeMillis()
    if (now < startTime) return 0f
    if (now > endTime) return 1f
    return (now - startTime).toFloat() / (endTime - startTime).toFloat()
}
