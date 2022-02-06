package com.shieldtech.sales_guide.adapters.http_server.middlewares

import com.shieldtech.sales_guide.configs.Envs
import com.shieldtech.sales_guide.domain.models.LocationData
import com.shieldtech.sales_guide.utils.Constants
import io.vertx.core.Handler
import io.vertx.rxjava3.ext.web.RoutingContext
import java.time.ZoneOffset

class LocationMiddleware : Handler<RoutingContext> {
  companion object {
    const val TIMEZONE_HEADER = "X-Timezone"
    const val TIMEZONE_OFFSET_HEADER = "X-Timezone-Offset"
  }

  override fun handle(ctx: RoutingContext) {
    val timezone = ctx.request().getHeader(TIMEZONE_HEADER) ?: Envs.getDefaultTimezone()
    val timezoneOffset = ctx.request().getHeader(TIMEZONE_OFFSET_HEADER)?.toIntOrNull() ?: Envs.getDefaultTimezoneOffset()
    val offset = ZoneOffset.ofHours(timezoneOffset)

    ctx.put(Constants.LOCATION_CTX_KEY, LocationData(timezone, timezoneOffset, offset))
    ctx.next()
  }
}
