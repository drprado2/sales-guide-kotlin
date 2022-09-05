package com.shieldtech.sales_guide.domain.repositories

import com.shieldtech.sales_guide.domain.entities.Company
import io.opentelemetry.api.trace.Span
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe

interface CompanyRepository {
  fun getCompanyById(id: String, parentSpan: Span?) : Maybe<Company>

  fun increaseCompanyEmployeesCount(companyId: String, amountToIncrease: Int, parentSpan: Span?) : Maybe<Int>
}
