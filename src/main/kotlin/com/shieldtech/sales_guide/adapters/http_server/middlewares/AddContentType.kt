package com.shieldtech.sales_guide.adapters.http_server.middlewares

import com.shieldtech.sales_guide.utils.Constants.HEADER_CONTENT_TYPE
import io.vertx.core.Handler
import io.vertx.rxjava3.ext.web.RoutingContext

class AddContentType : Handler<RoutingContext> {
  override fun handle(ctx: RoutingContext?) {
    ctx?.response()?.putHeader(HEADER_CONTENT_TYPE, ctx.getAcceptableContentType());
    ctx?.next()
  }
}
