package com.shieldtech.sales_guide.domain.usecases

import com.shieldtech.sales_guide.domain.commands.UserCreatedCommand
import com.shieldtech.sales_guide.domain.errors.CompanyNotFoundException
import com.shieldtech.sales_guide.domain.errors.DatabaseException
import com.shieldtech.sales_guide.domain.consumers.QueueManager
import com.shieldtech.sales_guide.domain.repositories.CompanyRepository
import com.shieldtech.sales_guide.logs.CorrelationLogger
import com.shieldtech.sales_guide.utils.asLogArg
import com.shieldtech.sales_guide.utils.setError
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

class UserCreated(private val companyRepository: CompanyRepository, private val tracer: Tracer) {
  companion object {
    private val logger: CorrelationLogger = CorrelationLogger.getLogger(UserCreated::class.java)
    private const val SPAN_NAME = "UserCreated.handle"
  }

  fun handle(cmd: UserCreatedCommand): Single<UserCreatedCommand> {
    var span: Span? = null

    return Single.just(cmd)
      .map {
        span = tracer.spanBuilder(SPAN_NAME).setParent(Context.current().with(it.span)).startSpan()
        it.copy(span = span)
      }
      .flatMap(increaseCompanyEmployees)
      .doOnError {
        span?.setError(it)
      }
      .doOnTerminate { span?.end() }
  }

  private val increaseCompanyEmployees = fun (cmd: UserCreatedCommand) : Single<UserCreatedCommand> {
    return this.companyRepository.increaseCompanyEmployeesCount(cmd.event.companyId, 1, cmd.span)
      .onErrorResumeNext {
        logger.error("error trying to increase employees count", cmd.event.cid, it
          , cmd.event.companyId asLogArg "company_id"
          , cmd.event.userId asLogArg "user_id"
        )
        Maybe.error(DatabaseException("problem during write operation", it))
      }
      .switchIfEmpty(Single.error(CompanyNotFoundException()))
      .map {
        cmd.copy(amountEmployeesOfCompany = it)
      }
  }
}
