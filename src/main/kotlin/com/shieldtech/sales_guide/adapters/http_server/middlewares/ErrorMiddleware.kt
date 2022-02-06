package com.shieldtech.sales_guide.adapters.http_server.middlewares

import com.shieldtech.sales_guide.adapters.http_server.utils.getCidOrDefault
import com.shieldtech.sales_guide.logs.CorrelationLogger
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.rxjava3.ext.web.RoutingContext
import net.logstash.logback.argument.StructuredArguments

class ErrorMiddleware : Handler<RoutingContext> {
  companion object {
    private val logger: CorrelationLogger = CorrelationLogger.getLogger(ErrorMiddleware::class.java)
    private const val ERROR_STATUS_CODE = 500
    private val defaultReply = JsonObject().put("message", "something wrong happened, try again later").encode()
  }

  override fun handle(ctx: RoutingContext) {
    val err = ctx.failure()
    val cid = ctx.getCidOrDefault()
    logger.error("Request failure", cid, err, StructuredArguments.v("path", ctx.request().path()))

    ctx
      .response()
      .setStatusCode(if (ctx.statusCode() > 399) ctx.statusCode() else ERROR_STATUS_CODE)
      .end(defaultReply)
      .subscribe()
  }
}
