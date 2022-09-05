package com.shieldtech.sales_guide.domain.consumers

import io.opentelemetry.api.trace.Span
import io.reactivex.rxjava3.core.Completable

interface QueueManager {
  fun setMessageAsConsumed(message: EventMessage, success: Boolean, cid: String, parentSpan: Span?) : Completable
  fun returnMessageToQueue(message: EventMessage, cid: String, parentSpan: Span?) : Completable
  fun increaseConsumerTimeout(message: EventMessage, milliseconds: Int, cid: String, parentSpan: Span?) : Completable
}
