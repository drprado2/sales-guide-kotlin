package com.shieldtech.sales_guide.adapters.http_server.healthcheck

import io.vertx.core.Vertx
import io.vertx.ext.healthchecks.HealthChecks
import io.vertx.ext.healthchecks.Status
import io.vertx.rxjava3.ext.web.client.WebClient
import io.vertx.rxjava3.pgclient.PgPool

class HealthChecker(private val vertx: Vertx, private val webClient: WebClient, private val pool: PgPool) {
  private val readiness = HealthChecks.create(vertx)
  private val liveness = HealthChecks.create(vertx)

  companion object {
    private const val HEALTH_CHECK_QUERY = """
      SELECT
        sum(heap_blks_read) as heap_read,
        sum(heap_blks_hit)  as heap_hit,
        sum(heap_blks_hit) / (sum(heap_blks_hit) + sum(heap_blks_read)) as ratio
      FROM
        pg_statio_user_tables;
    """
  }

  fun configReadiness() {
    readiness.register(
      "cep-service",
      2000,
    ) { promise ->
      webClient
        .get(5051, "localhost", "/api/ceps/health")
        .rxSend()
        .doOnSuccess {
          promise.complete(Status.OK())
        }
        .doOnError {
          promise.fail(it)
        }
        .subscribe()
    }

    readiness.register(
      "user-service",
      2000,
    ) { promise ->
      webClient
        .get(5051, "localhost", "/api/users/health")
        .rxSend()
        .doOnSuccess() {
          promise.complete(Status.OK())
        }
        . doOnError {
          promise.fail(it)
        }
        .subscribe()
    }

    readiness.register(
      "postgress",
      2000,
    ) { promise ->
    pool
      .rxGetConnection()
      .flatMap { conn ->
        conn
          .query(HEALTH_CHECK_QUERY)
          .rxExecute()
          .doAfterTerminate {
            conn.close().subscribe()
          }
      }
      .doOnSuccess {
        if (it.size() > 0) {
          promise.complete(Status.OK())
        } else {
          promise.fail(Exception("invalid response from DB"))
        }
      }
      .doOnError {
        promise.fail(it)
      }
      .subscribe()
    }
  }
}
