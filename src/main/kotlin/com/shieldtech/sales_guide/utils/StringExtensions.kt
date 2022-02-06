package com.shieldtech.sales_guide.utils

import net.logstash.logback.argument.StructuredArguments

infix fun String.asLogArg(tag: String) = StructuredArguments.v(tag, this)
