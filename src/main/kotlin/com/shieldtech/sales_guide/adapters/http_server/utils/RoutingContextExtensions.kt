package com.shieldtech.sales_guide.adapters.http_server.utils

import com.shieldtech.sales_guide.utils.Constants
import com.shieldtech.sales_guide.utils.Constants.CID_CTX_KEY
import com.shieldtech.sales_guide.utils.Constants.METHOD_KEY
import com.shieldtech.sales_guide.utils.Constants.PATH_KEY
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.vertx.rxjava3.ext.web.RoutingContext
import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments
import java.util.*

fun RoutingContext.getCidOrDefault() : String {
  return this.get(CID_CTX_KEY, UUID.randomUUID().toString())
}

fun RoutingContext.getResourceLogArgs() : Array<StructuredArgument> {
  return arrayOf(StructuredArguments.v(PATH_KEY, this.request().path()), StructuredArguments.v(METHOD_KEY, this.request().method().toString()))
}

fun RoutingContext.setErrorToSpan(err: Throwable) {
  this.get<Span>(Constants.SPAN_CTX_KEY)
    .setStatus(StatusCode.ERROR, err.message ?: "")
    .recordException(err)
}

fun RoutingContext.endSpan() {
  this.get<Span>(Constants.SPAN_CTX_KEY)
    .setAttribute("path", this.request().path())
    .setAttribute("method", this.request().method().toString())
    .setAttribute("http_status_code", this.response().statusCode.toLong())
    .end()
}
