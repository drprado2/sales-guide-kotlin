package com.shieldtech.sales_guide.adapters.http_server.middlewares

import com.shieldtech.sales_guide.utils.Constants
import io.opentelemetry.api.trace.Tracer
import io.vertx.core.Handler
import io.vertx.rxjava3.ext.web.RoutingContext

class TracerMiddleware(private val tracer: Tracer) : Handler<RoutingContext> {
  companion object {
    private const val UUID_REGEX = "\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}"
    private const val NUMBER_REGEX = "/\\d+"
    private const val UUID_JOKER = "*"
    private const val NUMBER_JOKER = "/*"
    private val uuidRegex: Regex = Regex(UUID_REGEX)
    private val numberRegex: Regex = Regex(NUMBER_REGEX)
  }

  override fun handle(router: RoutingContext) {
    val tracerName =
      "${router.request().method()} - ${router.request().path().replace(uuidRegex, UUID_JOKER).replace(numberRegex, NUMBER_JOKER)}"
    val span = tracer
      .spanBuilder(tracerName)
      .setAttribute("path", router.request().path())
      .setAttribute("method", router.request().method().toString())
      .startSpan()
    router.put(Constants.SPAN_CTX_KEY, span)
    router.next()
  }
}
