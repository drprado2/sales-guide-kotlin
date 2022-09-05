package com.shieldtech.sales_guide.domain.consumers

import io.opentelemetry.api.trace.Span
import kotlinx.serialization.Serializable

@Serializable
data class EventMessage(val queue: String, val messageId: String, val parentSpan: Span?, val extraInfo: MutableMap<String, Object>, val content: String)
