package com.shieldtech.sales_guide.logs

import com.shieldtech.sales_guide.utils.Constants.CID_TAG
import com.shieldtech.sales_guide.utils.asLogArg
import net.logstash.logback.argument.StructuredArgument
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CorrelationLogger(private val logger: Logger) {

  companion object {
    fun getLogger(clazz: Class<*>) : CorrelationLogger {
      return CorrelationLogger(LoggerFactory.getLogger(clazz))
    }
  }

  fun debug(message: String, cid: String, vararg fields: StructuredArgument) {
    this.logger.debug(message, cid asLogArg CID_TAG, *fields)
  }

  fun info(message: String, cid: String, vararg fields: StructuredArgument) {
    this.logger.info(message, cid asLogArg CID_TAG, *fields)
  }

  fun warn(message: String, cid: String, error: Throwable?=null, vararg fields: StructuredArgument) {
    this.logger.atWarn().setCause(error).log(message, cid asLogArg CID_TAG, *fields)
  }

  fun error(message: String, cid: String, error: Throwable?=null, vararg fields: StructuredArgument) {
    this.logger.atError().setCause(error).log(message, cid asLogArg CID_TAG, *fields)
  }
}
