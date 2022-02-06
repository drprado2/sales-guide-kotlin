package com.shieldtech.sales_guide.adapters.http_server

import com.shieldtech.sales_guide.configs.Bootstraper
import com.shieldtech.sales_guide.configs.Envs
import com.shieldtech.sales_guide.logs.CorrelationLogger
import io.reactivex.rxjava3.core.Completable
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.tracing.TracingPolicy
import io.vertx.rxjava3.core.AbstractVerticle
import net.logstash.logback.argument.StructuredArguments

class HttpServerVerticle(private val router: Router) : AbstractVerticle() {
  companion object {
    val logger: CorrelationLogger = CorrelationLogger.getLogger(HttpServerVerticle::class.java)
  }

  override fun rxStart(): Completable {
    val httpPort = Envs.getHttpPort()

    return vertx
      .createHttpServer(HttpServerOptions()
        .setTracingPolicy(TracingPolicy.IGNORE)
      )
      .requestHandler(router.build(vertx))
      .rxListen(httpPort)
      .doOnSuccess {
        logger.info(
          "http server running at port $httpPort",
          Bootstraper.bootCid,
          StructuredArguments.v("http_port", httpPort)
        )
      }
      .doOnError {
        logger.info(
          "http server running at port $httpPort",
          Bootstraper.bootCid,
          StructuredArguments.v("http_port", httpPort)
        )
      }
      .flatMapCompletable {
        Completable.complete()
      }
  }
}
