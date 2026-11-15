package com.github.gezimos.inkos

import android.app.Application
import io.opentelemetry.android.OpenTelemetryRum
import io.opentelemetry.android.agent.OpenTelemetryRumInitializer
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span

/** Debug builds: OpenTelemetry Android agent exporting OTLP/HTTP to a Sigiro on localhost:4318. */
object Telemetry {
    private var rum: OpenTelemetryRum? = null

    fun init(app: Application) {
        rum = OpenTelemetryRumInitializer.initialize(app) {
            httpExport { baseUrl = "http://127.0.0.1:4318" }
            resource { put("service.name", "inkos") }
            diskBuffering { enabled(false) }
            // agent 1.7 slow-render listener calls Duration.toSeconds(), absent before API 31 (fixed in 1.8)
            instrumentations { slowRenderingReporter { enabled(false) } }
        }
    }

    inline fun <T> span(name: String, attrs: Map<String, Any> = emptyMap(), block: (Span) -> T): T {
        val span = tracer().spanBuilder(name).startSpan()
        for ((k, v) in attrs) when (v) {
            is Long -> span.setAttribute(AttributeKey.longKey(k), v)
            is Int -> span.setAttribute(AttributeKey.longKey(k), v.toLong())
            is Boolean -> span.setAttribute(AttributeKey.booleanKey(k), v)
            else -> span.setAttribute(AttributeKey.stringKey(k), v.toString())
        }
        try {
            return block(span).also { if (it is Collection<*>) span.setAttribute(AttributeKey.longKey("result.count"), it.size.toLong()) }
        } finally { span.end() }
    }

    fun tracer() = (rum?.openTelemetry ?: io.opentelemetry.api.OpenTelemetry.noop()).getTracer("inkos")
}
