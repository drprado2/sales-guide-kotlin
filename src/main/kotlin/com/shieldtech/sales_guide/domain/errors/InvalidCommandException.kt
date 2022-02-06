package com.shieldtech.sales_guide.domain.errors

import am.ik.yavi.core.ConstraintViolations

class InvalidCommandException(val violations: ConstraintViolations) : Exception("invalid command") {
}
