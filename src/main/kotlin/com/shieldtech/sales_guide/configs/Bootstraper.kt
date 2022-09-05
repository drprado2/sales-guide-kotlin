package com.shieldtech.sales_guide.configs

import com.amazonaws.SDKGlobalConfiguration
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.shieldtech.sales_guide.adapters.aws.sqs.ConsumeRegistry
import com.shieldtech.sales_guide.adapters.aws.sqs.ConsumerVerticle
import com.shieldtech.sales_guide.adapters.aws.sqs.QueueManagerImpl
import com.shieldtech.sales_guide.adapters.http_server.HttpServerVerticle
import com.shieldtech.sales_guide.adapters.http_server.Router
import com.shieldtech.sales_guide.adapters.http_server.controllers.CompanyController
import com.shieldtech.sales_guide.adapters.pgsql.PoolBuilder
import com.shieldtech.sales_guide.adapters.repositories.pgsql.CompanyRepositoryImpl
import com.shieldtech.sales_guide.domain.consumers.UserCreatedConsumer
import com.shieldtech.sales_guide.domain.events.UserCreatedEvent
import com.shieldtech.sales_guide.domain.usecases.GetCompanyById
import com.shieldtech.sales_guide.domain.usecases.UserCreated
import com.shieldtech.sales_guide.logs.CorrelationLogger
import io.reactivex.rxjava3.core.Single
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.rxjava3.core.Vertx
import java.util.concurrent.TimeUnit

object Bootstraper {
  private val logger: CorrelationLogger = CorrelationLogger.getLogger(Bootstraper::class.java)
  const val bootCid = "boot"

  fun configureProperties() {
    System.setProperty(SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY, "true")
  }

  fun configureModules() {
    DatabindCodec.mapper().registerModule(JavaTimeModule())
    DatabindCodec.mapper().registerModule(Jdk8Module())
    DatabindCodec.mapper().registerModule(ParameterNamesModule())
    DatabindCodec.mapper().findAndRegisterModules()
    DatabindCodec.mapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  }

  fun startServer() : Single<String> {
    val vertx = Vertx.vertx()
    val tracer = TracerBuilder()
      .withZipkin()
      .withJaeger()
      .build()
    val pgPool = PoolBuilder.build()
    val companyRepository = CompanyRepositoryImpl(pgPool, tracer)
    val getByCompanyById = GetCompanyById(companyRepository, tracer)
    val companyController = CompanyController(vertx, getByCompanyById)
    val router = Router(companyController, tracer)
    val userCreatedUseCase = UserCreated(companyRepository, tracer)
    val amazonSqsClient = AmazonSQSAsyncClientBuilder
      .standard()
      .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(Envs.getAwsEndpoint(), Envs.getAwsRegion()))
      .build()
    val awsSqsQueueManager = QueueManagerImpl(amazonSqsClient, tracer)
    val userCreatedConsumer = UserCreatedConsumer(tracer, userCreatedUseCase, awsSqsQueueManager)
    val userCreatedVerticle = ConsumerVerticle(amazonSqsClient,
      ConsumeRegistry("userCreatedConsumer", Envs.getUserCreatedSqsUrl(), Envs.getSqsPoolInterval(), Envs.getSqsWaitSecondsInterval(), Envs.getSqsMessagePerPool(), userCreatedConsumer)
    )

    return vertx
      .deployVerticle(HttpServerVerticle(router), getDefaultVerticleOptions())
      .flatMap {
        vertx.deployVerticle(userCreatedVerticle, getWorkerVerticleOptions())
      }
      .doAfterSuccess {
        userCreatedVerticle.startConsuming()
      }
      .doOnSuccess {
        logger.info("server deployed successfully", bootCid)
      }
      .doOnError {
        logger.error("error to deploy server", bootCid, it)
      }
  }

  private fun getDefaultVerticleOptions(): DeploymentOptions = DeploymentOptions().setWorkerPoolSize(10)

  private fun getWorkerVerticleOptions(): DeploymentOptions = DeploymentOptions().setWorker(true).setMaxWorkerExecuteTime(6000).setMaxWorkerExecuteTimeUnit(TimeUnit.MILLISECONDS).setWorkerPoolSize(1)
}
