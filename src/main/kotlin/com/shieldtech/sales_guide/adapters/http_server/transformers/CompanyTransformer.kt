package com.shieldtech.sales_guide.adapters.http_server.transformers

import com.shieldtech.sales_guide.domain.commands.GetCompanyByIdCommand
import com.shieldtech.sales_guide.domain.entities.Company
import com.shieldtech.sales_guide.domain.models.LocationData
import com.shieldtech.sales_guide.domain.models.UserData
import com.shieldtech.sales_guide.utils.Constants
import com.shieldtech.sales_guide.utils.Constants.CID_CTX_KEY
import com.shieldtech.sales_guide.utils.Constants.USER_CTX_KEY
import io.opentelemetry.api.trace.Span
import io.vertx.core.json.JsonObject
import io.vertx.rxjava3.ext.web.RoutingContext
import java.time.format.DateTimeFormatter

object CompanyTransformer {
  fun buildGetCompanyByIdInput(ctx: RoutingContext) : GetCompanyByIdCommand {
    val userData = ctx.get<UserData>(USER_CTX_KEY)
    val cid = ctx.get<String>(CID_CTX_KEY)
    val parentSpan = ctx.get<Span>(Constants.SPAN_CTX_KEY)
    return GetCompanyByIdCommand(userData.companyId, cid, parentSpan)
  }

  fun toCompanyOutput(ctx: RoutingContext, company: Company) : JsonObject {
    val locationData = ctx.get<LocationData>(Constants.LOCATION_CTX_KEY)

    return JsonObject()
      .put("company_id", company.id)
      .put("name", company.name)
      .put("document", company.document)
      .put("logo", company.logo)
      .put("total_collaborators", company.totalCollaborators)
      .put("primary_color", company.primaryColor)
      .put("primary_font_color", company.primaryFontColor)
      .put("secondary_color", company.secondaryColor)
      .put("secondary_font_color", company.secondaryFontColor)
      .put("created_at", DateTimeFormatter.ISO_INSTANT.format(company.createdAt.toInstant(locationData.zoneOffset)))
      .put("updated_at", DateTimeFormatter.ISO_INSTANT.format(company.updatedAt.toInstant(locationData.zoneOffset)))
      .put("row_version", company.rowVersion.toInt())
  }
}
