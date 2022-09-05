package com.shieldtech.sales_guide.utils

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode

fun Span.setError(err: Throwable) : Span {
  this.setStatus(StatusCode.ERROR, err.message ?: "")?.recordException(err)
  return this
}
