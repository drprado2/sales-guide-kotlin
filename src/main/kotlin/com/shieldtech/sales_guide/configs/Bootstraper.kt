package com.shieldtech.sales_guide.configs

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.shieldtech.sales_guide.adapters.http_server.HttpServerVerticle
import com.shieldtech.sales_guide.adapters.http_server.Router
import com.shieldtech.sales_guide.adapters.http_server.controllers.CompanyController
import com.shieldtech.sales_guide.adapters.pgsql.PoolBuilder
import com.shieldtech.sales_guide.adapters.repositories.pgsql.CompanyRepositoryImpl
import com.shieldtech.sales_guide.domain.usecases.GetCompanyById
import com.shieldtech.sales_guide.logs.CorrelationLogger
import io.reactivex.rxjava3.core.Single
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.rxjava3.core.Vertx


object Bootstraper {
  private val logger: CorrelationLogger = CorrelationLogger.getLogger(Bootstraper::class.java)
  const val bootCid = "boot"

  fun configureModules() {
    DatabindCodec.mapper().registerModule(JavaTimeModule())
    DatabindCodec.mapper().registerModule(Jdk8Module())
    DatabindCodec.mapper().registerModule(ParameterNamesModule())
    DatabindCodec.mapper().findAndRegisterModules()
    DatabindCodec.mapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  }

  fun startServer() : Single<String> {
    val vertx = Vertx.vertx()
    val pgPool = PoolBuilder.build()
    val tracer = TracerBuilder.getInstance()
    val companyRepository = CompanyRepositoryImpl(pgPool, tracer)
    val getByCompanyById = GetCompanyById(companyRepository, tracer)
    val companyController = CompanyController(vertx, getByCompanyById)
    val router = Router(companyController, tracer)

    return vertx
      .deployVerticle(HttpServerVerticle(router), getDefaultVerticleOptions())
      .doOnSuccess {
        logger.info("server deployed successfully", bootCid)
      }
      .doOnError {
        logger.error("error to deploy server", bootCid, it)
      }
  }

  private fun getDefaultVerticleOptions(): DeploymentOptions = DeploymentOptions().setWorkerPoolSize(10)
}
