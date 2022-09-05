package com.shieldtech.sales_guide.adapters.http_server.utils

import io.opentelemetry.context.propagation.TextMapGetter
import io.vertx.rxjava3.core.http.HttpServerRequest

class TracingTextMapGetter : TextMapGetter<HttpServerRequest> {
  override fun keys(req: HttpServerRequest): MutableIterable<String> {
    return req.headers().names()
  }

  override fun get(req: HttpServerRequest?, key: String): String? {
    return req?.headers()?.get(key) ?: ""
  }
}
