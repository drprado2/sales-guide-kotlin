package com.shieldtech.sales_guide.adapters.aws.sqs

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.shieldtech.sales_guide.domain.consumers.EventMessage
import com.shieldtech.sales_guide.domain.consumers.QueueManager
import com.shieldtech.sales_guide.logs.CorrelationLogger
import com.shieldtech.sales_guide.utils.asLogArg
import com.shieldtech.sales_guide.utils.setError
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.reactivex.rxjava3.core.Completable

class QueueManagerImpl(private val asyncClient: AmazonSQSAsync, private val tracer: Tracer) : QueueManager {
  companion object {
    private val logger: CorrelationLogger = CorrelationLogger.getLogger(this::class.java)
  }

  override fun setMessageAsConsumed(
    message: EventMessage,
    success: Boolean,
    cid: String,
    parentSpan: Span?
  ): Completable {
    val span = tracer.spanBuilder("QueueManagerImpl.setMessageAsConsumed").setParent(Context.current().with(parentSpan))
      .startSpan()

    val receiptHandle = message.receiptHandleOrNull(logger, cid)
      ?: return Completable.error(InvalidReceiptHandleException())

    val request = DeleteMessageRequest(message.queue, receiptHandle)
    return Completable.fromFuture(asyncClient.deleteMessageAsync(request))
      .doOnError {
        logger.error(
          "Error to delete SQS message", cid, it,
          message.queue asLogArg "queue",
          message.messageId asLogArg "message_id",
        )
        span.setError(it)
      }
      .doOnTerminate {
        span.end()
      }
  }

  override fun returnMessageToQueue(message: EventMessage, cid: String, parentSpan: Span?): Completable {
    val span = tracer.spanBuilder("QueueManagerImpl.returnMessageToQueue").setParent(Context.current().with(parentSpan))
      .startSpan()

    val receiptHandle = message.receiptHandleOrNull(logger, cid)
      ?: return Completable.error(InvalidReceiptHandleException())

    return Completable.fromFuture(
      asyncClient.changeMessageVisibilityAsync(message.queue, receiptHandle, 0)
    )
      .doOnError {
        logger.error(
          "Error changing visibility timeout to 0 of SQS message", cid, it,
          message.queue asLogArg "queue",
          message.messageId asLogArg "message_id",
        )
        span.setError(it)
      }
      .doOnTerminate {
        span.end()
      }
  }

  override fun increaseConsumerTimeout(
    message: EventMessage,
    milliseconds: Int,
    cid: String,
    parentSpan: Span?
  ): Completable {
    val span =
      tracer.spanBuilder("QueueManagerImpl.increaseConsumerTimeout").setParent(Context.current().with(parentSpan))
        .startSpan()

    val receiptHandle = message.receiptHandleOrNull(logger, cid)
      ?: return Completable.error(InvalidReceiptHandleException())

    return Completable.fromFuture(
      asyncClient.changeMessageVisibilityAsync(message.queue, receiptHandle, milliseconds)
    )
      .doOnError {
        logger.error(
          "Error changing visibility timeout of SQS message", cid, it,
          message.queue asLogArg "queue",
          message.messageId asLogArg "message_id",
        )
        span.setError(it)
      }
      .doOnTerminate {
        span.end()
      }
  }
}

fun EventMessage.receiptHandleOrNull(logger: CorrelationLogger, cid: String): String? {
  val receiptHandle = this.extraInfo[Constants.RECEIPT_HANDLE]?.toString()
  if (receiptHandle == null) {
    logger.error(
      "SQS message doesn't contains receipt handle", cid, InvalidReceiptHandleException(),
      this.queue asLogArg "queue",
      this.messageId asLogArg "message_id",
    )
  }
  return receiptHandle
}
