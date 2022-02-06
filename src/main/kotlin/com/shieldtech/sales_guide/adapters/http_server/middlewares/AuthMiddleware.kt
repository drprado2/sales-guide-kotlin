package com.shieldtech.sales_guide.adapters.http_server.middlewares

import com.shieldtech.sales_guide.adapters.http_server.utils.getCidOrDefault
import com.shieldtech.sales_guide.adapters.http_server.utils.getResourceLogArgs
import com.shieldtech.sales_guide.domain.models.UserData
import com.shieldtech.sales_guide.logs.CorrelationLogger
import com.shieldtech.sales_guide.utils.Constants.USER_CTX_KEY
import io.vertx.core.Handler
import io.vertx.rxjava3.ext.web.RoutingContext

class AuthMiddleware : Handler<RoutingContext> {
  companion object {
    private val logger: CorrelationLogger = CorrelationLogger.getLogger(AuthMiddleware::class.java)
    private const val USER_ID_HEADER = "X-User-Id"
    private const val COMPANY_ID_HEADER = "X-Tenant"
    private const val EMAIL_HEADER = "X-Email"
    const val DEFAULT_STATUS_CODE = 401
  }

  override fun handle(ctx: RoutingContext) {
    val userId = ctx.request().getHeader(USER_ID_HEADER)
    val companyId = ctx.request().getHeader(COMPANY_ID_HEADER)
    val email = ctx.request().getHeader(EMAIL_HEADER)
    val cid = ctx.getCidOrDefault()
    if (userId.isNullOrEmpty() || companyId.isNullOrEmpty() || email.isNullOrEmpty()) {
      logger.warn("Received request without authentication headers", cid, null, *ctx.getResourceLogArgs())

      ctx
        .response()
        .setStatusCode(DEFAULT_STATUS_CODE)
        .end()
        .subscribe()
        return
    }

    ctx.put(USER_CTX_KEY, UserData(userId, companyId, email))
    ctx.next()
  }
}
