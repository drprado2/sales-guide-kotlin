package com.shieldtech.sales_guide.adapters.pgsql

import com.shieldtech.sales_guide.configs.Envs
import io.vertx.pgclient.PgConnectOptions
import io.vertx.rxjava3.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions


object PoolBuilder {

  fun build() : PgPool {
    var connectOptions = PgConnectOptions()
      .setPort(Envs.getPgPort())
      .setHost(Envs.getPgHost())
      .setDatabase(Envs.getPgDbName())
      .setUser(Envs.getPgUser())
      .setPassword(Envs.getPgPass())
      .setReconnectAttempts(Envs.getPgReconnectAttempts())
      .setReconnectInterval(Envs.getPgReconnectInterval())

    var poolOptions = PoolOptions()
      .setMaxSize(Envs.getPgPoolSize())

    return PgPool.pool(connectOptions, poolOptions)
  }
}
