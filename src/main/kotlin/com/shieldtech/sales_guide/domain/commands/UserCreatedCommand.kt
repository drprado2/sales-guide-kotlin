package com.shieldtech.sales_guide.domain.commands

import com.shieldtech.sales_guide.domain.events.UserCreatedEvent
import io.opentelemetry.api.trace.Span

data class UserCreatedCommand(val event: UserCreatedEvent, val span: Span?, val amountEmployeesOfCompany: Int? = null) {
}
