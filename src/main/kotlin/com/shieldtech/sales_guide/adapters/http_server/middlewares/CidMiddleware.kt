package com.shieldtech.sales_guide.adapters.http_server.middlewares

import com.shieldtech.sales_guide.utils.Constants
import io.vertx.core.Handler
import io.vertx.rxjava3.ext.web.RoutingContext
import java.util.UUID

class CidMiddleware : Handler<RoutingContext> {
  companion object {
    private const val CID_HEADER = "x-cid"
  }

  override fun handle(ctx: RoutingContext) {
    val cid = ctx.request().getHeader(CID_HEADER) ?: UUID.randomUUID().toString()
    ctx.put(Constants.CID_CTX_KEY, cid)
    ctx.next()
  }
}
