package com.shieldtech.sales_guide.adapters.http_server.middlewares

import com.shieldtech.sales_guide.adapters.http_server.utils.getCidOrDefault
import com.shieldtech.sales_guide.adapters.http_server.utils.getResourceLogArgs
import com.shieldtech.sales_guide.logs.CorrelationLogger
import io.vertx.core.Handler
import io.vertx.rxjava3.ext.web.RoutingContext

class LogMiddleware : Handler<RoutingContext> {
  companion object {
    private val logger: CorrelationLogger = CorrelationLogger.getLogger(LogMiddleware::class.java)
  }

  override fun handle(ctx: RoutingContext) {
    val request = ctx.request()
    val cid = ctx.getCidOrDefault()
    logger.info("Receive request at ${request.method().name()} - ${request.path()}",
      cid,
      *ctx.getResourceLogArgs()
    )
    ctx.next()
  }
}
