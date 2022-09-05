package com.shieldtech.sales_guide.domain.events

@kotlinx.serialization.Serializable
data class UserCreatedEvent(val cid: String, val companyId: String, val userId: String) {
}
