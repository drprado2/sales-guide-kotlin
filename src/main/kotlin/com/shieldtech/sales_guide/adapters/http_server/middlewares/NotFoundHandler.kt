package com.shieldtech.sales_guide.adapters.http_server.middlewares

import com.shieldtech.sales_guide.adapters.http_server.utils.getCidOrDefault
import com.shieldtech.sales_guide.logs.CorrelationLogger
import com.shieldtech.sales_guide.utils.asLogArg
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.rxjava3.ext.web.RoutingContext

class NotFoundHandler : Handler<RoutingContext> {
  companion object {
    private val logger: CorrelationLogger = CorrelationLogger.getLogger(NotFoundHandler::class.java)
    private val response = JsonObject()
      .put("message", "Resource not found")
      .encode()
  }

  override fun handle(router: RoutingContext) {
    val cid = router.getCidOrDefault()
    logger.warn("Handle request with resource not found", cid, null, router.request().path() asLogArg "path", router.request().method().toString() asLogArg  "method")
    router.response().setStatusCode(404).end(response).subscribe()
  }
}
