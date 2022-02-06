package com.shieldtech.sales_guide.adapters.http_server.transformers

import com.shieldtech.sales_guide.domain.errors.InvalidCommandException
import io.vertx.core.json.JsonObject

object InvalidCommandTransformer {
  fun InvalidCommandException.toJson() : String {
    val result = mutableListOf<Map<String, String>>()
    this.violations.map {
      result.add(mapOf("field" to it.name(), "message" to it.message()))
    }

    return JsonObject()
      .put("message", "invalid input")
      .put("errors", result)
      .encode()
  }
}
