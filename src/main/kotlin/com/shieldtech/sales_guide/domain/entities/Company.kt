package com.shieldtech.sales_guide.domain.entities

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class Company(
  var id: UUID = UUID.randomUUID(),
  var name: String,
  var document: String,
  var logo: String = DEFAULT_LOGO,
  var totalCollaborators: Int = 0,
  var primaryColor: String = DEFAULT_PRIMARY_COLOR,
  var primaryFontColor: String = DEFAULT_PRIMARY_FONT_COLOR,
  var secondaryColor: String = DEFAULT_SECONDARY_COLOR,
  var secondaryFontColor: String = DEFAULT_SECONDARY_FONT_COLOR,
  var createdAt: LocalDateTime = LocalDateTime.now(ZoneId.of(UTC_TIME_ZONE)),
  var updatedAt: LocalDateTime = LocalDateTime.now(ZoneId.of(UTC_TIME_ZONE)),
  var rowVersion: UInt = 0u
) {

  companion object {
    const val DEFAULT_LOGO = "http://www.hsevolutione.com/wp-content/uploads/2020/07/logo-principal.png"
    const val DEFAULT_PRIMARY_COLOR = "#000066"
    const val DEFAULT_PRIMARY_FONT_COLOR = "#cce4ff"
    const val DEFAULT_SECONDARY_COLOR = "#ffffff"
    const val DEFAULT_SECONDARY_FONT_COLOR = "#222"
    const val UTC_TIME_ZONE = "UTC"
  }
}
