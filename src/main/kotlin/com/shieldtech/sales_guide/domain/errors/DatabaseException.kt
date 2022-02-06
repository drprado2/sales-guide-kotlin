package com.shieldtech.sales_guide.domain.errors

class DatabaseException(message: String?, cause: Throwable?) : Exception(message, cause) {
}
