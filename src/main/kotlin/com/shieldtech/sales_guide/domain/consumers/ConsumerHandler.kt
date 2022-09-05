package com.shieldtech.sales_guide.domain.consumers

import io.reactivex.rxjava3.core.Completable

interface ConsumerHandler {
  fun consume(message: EventMessage) : Completable
}
