package com.shieldtech.sales_guide.domain.commands

import am.ik.yavi.builder.ValidatorBuilder
import com.shieldtech.sales_guide.domain.entities.Company
import com.shieldtech.sales_guide.domain.errors.InvalidCommandException
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer

data class GetCompanyByIdCommand(val id: String, val cid: String, val parentSpan: Span?, val company: Company?=null) {
  companion object {
    val validator = ValidatorBuilder.of<GetCompanyByIdCommand>()
      .constraint(GetCompanyByIdCommand::id, "company_id") { c -> c.notEmpty().uuid() }
      .constraint(GetCompanyByIdCommand::cid, "company_id") { c -> c.notEmpty() }
      .build()
  }

  fun validate() {
    val violations = validator.validate(this)
    if (!violations.isValid) {
      throw InvalidCommandException(violations)
    }
  }
}
