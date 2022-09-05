package com.shieldtech.sales_guide.adapters.repositories.pgsql

import com.shieldtech.sales_guide.domain.entities.Company
import com.shieldtech.sales_guide.domain.repositories.CompanyRepository
import com.shieldtech.sales_guide.utils.setError
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.vertx.rxjava3.pgclient.PgPool
import io.vertx.rxjava3.sqlclient.Row
import io.vertx.rxjava3.sqlclient.SqlConnection
import io.vertx.rxjava3.sqlclient.Tuple

class CompanyRepositoryImpl(private val pool: PgPool, private val tracer: Tracer) : CompanyRepository {
  companion object {
    const val COMPANY_BY_ID_QUERY = """
      SELECT id,
            name,
            document,
            logo,
            total_colaborators,
            primary_color,
            primary_font_color,
            secondary_color,
            secondary_font_color,
            created_at,
            updated_at,
            xmin as row_version
        FROM company
       WHERE id=$1
      """

    const val INCREASE_COMPANY_EMPLOYEES_COUNTER = """
        UPDATE company
        SET total_colaborators = total_colaborators + $1
       WHERE id=$2
       RETURNING total_colaborators
      """
  }

  override fun getCompanyById(id: String, parentSpan: Span?): Maybe<Company> {
    var span: Span? = null

    return Single.just(id)
      .flatMap {
        span = tracer.spanBuilder("CompanyRepositoryImpl.getCompanyById").setParent(Context.current().with(parentSpan))
          .startSpan()
        pool.rxGetConnection()
      }
      .flatMap { conn ->
        conn
          .preparedQuery(COMPANY_BY_ID_QUERY)
          .rxExecute(Tuple.of(id))
          .doAfterTerminate {
            conn.close().subscribe()
          }
      }
      .flatMapMaybe { rows ->
        val register = rows.firstOrNull()
        if (register != null) Maybe.just(fromRowToCompany(register)) else Maybe.empty()
      }
      .doOnError {
        span?.setError(it)
      }
      .doOnTerminate {
        span?.end()
      }
  }

  override fun increaseCompanyEmployeesCount(companyId: String, amountToIncrease: Int, parentSpan: Span?): Maybe<Int> {
    var span: Span? = null

    return Single.create<SqlConnection> {
      span = tracer.spanBuilder("CompanyRepositoryImpl.increaseCompanyEmployeesCount").setParent(Context.current().with(parentSpan))
        .startSpan()

      pool.rxGetConnection()
    }
      .flatMap { conn ->
        conn
          .preparedQuery(INCREASE_COMPANY_EMPLOYEES_COUNTER)
          .rxExecute(Tuple.of(amountToIncrease, companyId))
          .doAfterTerminate {
            conn.close().subscribe()
          }
      }
      .flatMapMaybe { rows ->
        val register = rows.firstOrNull()
        if (register != null) Maybe.just(register.getInteger("total_colaborators")) else Maybe.empty()
      }
      .doOnError {
        span?.setError(it)
      }
      .doOnTerminate {
        span?.end()
      }
  }

  private fun fromRowToCompany(row: Row): Company {
    return Company(
      id = row.getUUID("id"),
      name = row.getString("name"),
      document = row.getString("document"),
      logo = row.getString("logo"),
      totalCollaborators = row.getInteger("total_colaborators"),
      primaryColor = row.getString("primary_color"),
      primaryFontColor = row.getString("primary_font_color"),
      secondaryColor = row.getString("secondary_color"),
      secondaryFontColor = row.getString("secondary_font_color"),
      createdAt = row.getLocalDateTime("created_at"),
      updatedAt = row.getLocalDateTime("updated_at"),
      rowVersion = row.getString("row_version").toUInt()
    )
  }
}
