package com.shieldtech.sales_guide.domain.models

import java.time.ZoneOffset

data class LocationData(val timezone: String, val timezoneOffset: Int, val zoneOffset: ZoneOffset)
