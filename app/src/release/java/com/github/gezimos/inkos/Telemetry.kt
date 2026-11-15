package com.github.gezimos.inkos

import android.app.Application

/** Release builds: no telemetry, no INTERNET permission. */
object Telemetry {
    @Suppress("UNUSED_PARAMETER") fun init(app: Application) {}
    inline fun <T> span(name: String, attrs: Map<String, Any> = emptyMap(), block: (Unit) -> T): T = block(Unit)
}
