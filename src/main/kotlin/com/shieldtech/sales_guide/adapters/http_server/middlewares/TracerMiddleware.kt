package com.shieldtech.sales_guide.adapters.http_server.middlewares

import com.shieldtech.sales_guide.adapters.http_server.utils.TracingTextMapGetter
import com.shieldtech.sales_guide.utils.Constants
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerRequest
import io.vertx.rxjava3.ext.web.RoutingContext

class TracerMiddleware(private val tracer: Tracer) : Handler<RoutingContext> {
  companion object {
    private const val UUID_REGEX = "\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}"
    private const val NUMBER_REGEX = "/\\d+"
    private const val UUID_JOKER = "*"
    private const val NUMBER_JOKER = "/*"
    private val uuidRegex: Regex = Regex(UUID_REGEX)
    private val numberRegex: Regex = Regex(NUMBER_REGEX)
    private val textMapGetter = TracingTextMapGetter()
  }

  override fun handle(router: RoutingContext) {
    val tracerName =
      "${router.request().method()} - ${router.request().path().replace(uuidRegex, UUID_JOKER).replace(numberRegex, NUMBER_JOKER)}"

    val externalContext = GlobalOpenTelemetry.get().propagators.textMapPropagator.extract(Context.current(), router.request(), textMapGetter)

    val span = tracer
      .spanBuilder(tracerName)
      .setParent(externalContext)
      .setSpanKind(SpanKind.SERVER)
      .setAttribute(SemanticAttributes.HTTP_TARGET, router.request().path())
      .setAttribute(SemanticAttributes.HTTP_METHOD, router.request().method().toString())
      .setAttribute(SemanticAttributes.HTTP_SCHEME, "http")
      .setAttribute(SemanticAttributes.HTTP_HOST, router.request().host())
      .startSpan()
    router.put(Constants.SPAN_CTX_KEY, span)
    router.next()
  }
}
