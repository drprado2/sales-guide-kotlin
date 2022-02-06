package com.shieldtech.sales_guide.configs

class Envs {
  companion object {
    fun getHttpPort() : Int {
      return System.getenv("HTTP_PORT")?.toIntOrNull() ?: 5050
    }

    fun getPgPort() : Int {
      return System.getenv("PG_PORT")?.toIntOrNull() ?: 5432
    }

    fun getPgHost() : String {
      return System.getenv("PG_HOST") ?: "localhost"
    }

    fun getPgDbName() : String {
      return System.getenv("PG_DB_NAME") ?: "sales-guide"
    }

    fun getPgUser() : String {
      return System.getenv("PG_USER") ?: "admin"
    }

    fun getPgPass() : String {
      return System.getenv("PG_PASS") ?: "Postgres2019!"
    }

    fun getPgPoolSize() : Int {
      return System.getenv("PG_POOL_SIZE")?.toIntOrNull() ?: 10
    }

    fun getPgReconnectAttempts() : Int {
      return System.getenv("PG_RECONNECT_ATTEMPTS")?.toIntOrNull() ?: 2
    }

    fun getPgReconnectInterval() : Long {
      return System.getenv("PG_RECONNECT_INTERVAL")?.toLongOrNull() ?: 1000
    }

    fun getRequestMaxTimeout() : Long {
      return System.getenv("REQUEST_MAX_TIMEOUT")?.toLongOrNull() ?: 20000
    }

    fun getDefaultTimezone() : String {
      return System.getenv("DEFAULT_TIMEZONE") ?: "America/Sao_Paulo"
    }

    fun getDefaultTimezoneOffset() : Int {
      return System.getenv("DEFAULT_TIMEZONE_OFFSET")?.toIntOrNull() ?: -3
    }

    fun getServiceName() : String {
      return System.getenv("SERVICE_NAME") ?: "sales-guide"
    }

    fun getSpanReportEndpoint() : String {
      return System.getenv("SPAN_REPORT_ENDPOINT") ?: "http://localhost:9411/api/v2/spans"
    }
  }
}
