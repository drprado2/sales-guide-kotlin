package com.shieldtech.sales_guide.domain.usecases

import com.shieldtech.sales_guide.domain.commands.GetCompanyByIdCommand
import com.shieldtech.sales_guide.domain.errors.CompanyNotFoundException
import com.shieldtech.sales_guide.domain.errors.DatabaseException
import com.shieldtech.sales_guide.domain.repositories.CompanyRepository
import com.shieldtech.sales_guide.logs.CorrelationLogger
import com.shieldtech.sales_guide.utils.asLogArg
import com.shieldtech.sales_guide.utils.setError
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.reactivex.rxjava3.core.Maybe

class GetCompanyById(private val companyRepository: CompanyRepository, private val tracer: Tracer) {
  companion object {
    private val logger: CorrelationLogger = CorrelationLogger.getLogger(GetCompanyById::class.java)
    private const val SPAN_NAME = "GetCompanyById.handle"
  }

  fun handle(cmd: GetCompanyByIdCommand): Maybe<GetCompanyByIdCommand> {
    var span: Span? = null

    return Maybe.just(cmd)
      .map {
        span = tracer.spanBuilder(SPAN_NAME).setParent(Context.current().with(cmd.parentSpan)).startSpan()
        it.copy(parentSpan = span)
      }
      .doOnSuccess { it.validate() }
      .flatMap {
        this.companyRepository.getCompanyById(it.id, it.parentSpan)
          .onErrorResumeNext {
            logger.error("error trying to getting company by id", cmd.cid, it, cmd.id asLogArg "company_id")
            Maybe.error(DatabaseException("problem to get company", it))
          }
          .switchIfEmpty(Maybe.error(CompanyNotFoundException()))
          .flatMap { company ->
            Maybe.just(cmd.copy(company = company))
          }
      }
      .doOnError {
        span?.setError(it)
      }
      .doOnTerminate { span?.end() }
  }
}
