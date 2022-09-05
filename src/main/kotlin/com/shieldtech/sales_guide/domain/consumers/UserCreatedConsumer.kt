package com.shieldtech.sales_guide.domain.consumers

import com.shieldtech.sales_guide.domain.commands.UserCreatedCommand
import com.shieldtech.sales_guide.domain.errors.CompanyNotFoundException
import com.shieldtech.sales_guide.domain.events.UserCreatedEvent
import com.shieldtech.sales_guide.domain.usecases.UserCreated
import com.shieldtech.sales_guide.logs.CorrelationLogger
import com.shieldtech.sales_guide.utils.Constants
import com.shieldtech.sales_guide.utils.asLogArg
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import io.reactivex.rxjava3.core.Completable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class UserCreatedConsumer(private val tracer: Tracer, private val userCreated: UserCreated, private val queueManager: QueueManager) :
  ConsumerHandler {
  companion object {
    private val logger: CorrelationLogger = CorrelationLogger.getLogger(UserCreatedConsumer::class.java)
    private val tracerName = "consumer:${UserCreatedConsumer::javaClass.name}:${UserCreatedEvent::javaClass.name}"
    private const val consumeCid = "consume_default_cid"
  }

  override fun consume(message: EventMessage): Completable {
    try {
      val event = Json.decodeFromString<UserCreatedEvent>(message.content)
      val span = tracer
        .spanBuilder(tracerName)
        .setSpanKind(SpanKind.CONSUMER)
        .setAttribute("queue", message.queue)
        .setAttribute("message_id", message.messageId)
        .setAttribute("company_id", event.companyId)
        .setAttribute("user_id", event.userId)
        .startSpan()

      val cmd = UserCreatedCommand(event, span)

      return userCreated
        .handle(cmd)
        .flatMapCompletable {
          logger.info(
            "UserCreatedEvent consumed successfully", it.event.cid,
            it.event.companyId asLogArg Constants.COMPANY_ID,
            it.event.userId asLogArg Constants.USER_ID,
          )
          queueManager
            .setMessageAsConsumed(message, true, it.event.cid, it.span)
        }
        .onErrorResumeNext {
          logger.error(
            "UserCreatedEvent failed to consume event", cmd.event.cid, it,
            cmd.event.companyId asLogArg Constants.COMPANY_ID,
            cmd.event.userId asLogArg Constants.USER_ID,
          )

          if (it is CompanyNotFoundException) {
            return@onErrorResumeNext queueManager
              .setMessageAsConsumed(message, false, cmd.event.cid, span)
          }

          return@onErrorResumeNext queueManager
            .returnMessageToQueue(message, cmd.event.cid, span)
        }
        .doOnTerminate {
          span.end()
        }

    } catch(err: Exception) {
      logger.error(
        "Error to decode JSON of userCreated eventt", consumeCid, err,
        message.queue asLogArg "queue",
        message.messageId asLogArg "message_id",
      )
      return Completable.error(err)
    }
  }
}
