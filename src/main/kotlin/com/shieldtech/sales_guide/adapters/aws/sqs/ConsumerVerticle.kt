package com.shieldtech.sales_guide.adapters.aws.sqs

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.ReceiveMessageResult
import com.shieldtech.sales_guide.adapters.aws.sqs.Constants.RECEIPT_HANDLE
import com.shieldtech.sales_guide.domain.consumers.EventMessage
import com.shieldtech.sales_guide.domain.consumers.UserCreatedConsumer
import com.shieldtech.sales_guide.domain.events.UserCreatedEvent
import com.shieldtech.sales_guide.logs.CorrelationLogger
import com.shieldtech.sales_guide.utils.AwsAsyncHandler
import com.shieldtech.sales_guide.utils.Constants
import com.shieldtech.sales_guide.utils.asLogArg
import io.reactivex.rxjava3.core.Completable
import io.vertx.rxjava3.core.AbstractVerticle
import io.vertx.rxjava3.core.eventbus.EventBus
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ConsumerVerticle(private val asyncClient: AmazonSQSAsync, private val consume: ConsumeRegistry) :
  AbstractVerticle() {

  companion object {
    private val logger: CorrelationLogger = CorrelationLogger.getLogger(this::class.java)
    private const val consumeCid = "consume_default_cid"
  }

  private var consumerId: Long? = null
  private var eventBus: EventBus? = null

  override fun rxStart(): Completable {
    val eb = vertx.eventBus()
    eventBus = eb

    return eb.consumer<EventMessage>(consume.name) {
      consume.handler.consume(it.body())
    }.completionHandler()
  }

  fun startConsuming() {
    consumerId = vertx.setPeriodic(consume.poolInterval) {
      val req = ReceiveMessageRequest(consume.queueUrl)
        .withWaitTimeSeconds(consume.waitTimeSeconds)
        .withMaxNumberOfMessages(consume.maxNumberOfMessages)

      val future = asyncClient.receiveMessageAsync(
        req,
        AwsAsyncHandler<ReceiveMessageRequest, ReceiveMessageResult>(
          { ipt, output ->
            output.messages.forEach {
              val message = Json.encodeToString(EventMessage(consume.queueUrl, it.messageId, null, mutableMapOf(RECEIPT_HANDLE to it.receiptHandle as Object), it.body))
              eventBus?.publish(consume.name, message)
            }
          },
          { err: Exception ->
            logger.error("error to consume message from SQS queue", consumeCid, err,
              consume.name asLogArg "consumer",
              consume.queueUrl asLogArg "queue",
              )
          },
        )
      )
      Completable
        .fromFuture(future)
        .subscribe()
    }
  }
}
