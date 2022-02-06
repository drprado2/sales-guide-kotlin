package com.shieldtech.sales_guide.adapters.http_server

import com.shieldtech.sales_guide.adapters.http_server.controllers.CompanyController
import com.shieldtech.sales_guide.adapters.http_server.middlewares.*
import com.shieldtech.sales_guide.utils.Constants
import io.opentelemetry.api.trace.Tracer
import io.vertx.core.http.HttpMethod
import io.vertx.rxjava3.ext.web.handler.CorsHandler
import io.vertx.rxjava3.core.Vertx
import io.vertx.rxjava3.ext.web.Router as R

class Router(private val companyController: CompanyController, private val tracer: Tracer) {
  fun build(vertx: Vertx): R {
    val router = R.router(vertx)
    val cors = CorsHandler.create("*")
      .allowedMethod(HttpMethod.DELETE)
      .allowedMethod(HttpMethod.POST)
      .allowedMethod(HttpMethod.PUT)
      .allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.OPTIONS)
      .allowedHeader(HttpHeaders.ACCEPT)
      .allowedHeader(HttpHeaders.CONTENT_TYPE)
      .allowedHeader(HttpHeaders.AUTHORIZATION)
      .allowedHeader(HttpHeaders.TENANT)
      .allowedHeader(HttpHeaders.PROTOCOL)

    router
      .route()
      .handler(cors)
      .handler(CidMiddleware())
      .handler(TracerMiddleware(tracer))
      .handler(LogMiddleware())
      .handler(LocationMiddleware())
      .failureHandler(ErrorMiddleware())

    this.companyController
      .configureRoutes()
      .registerAsSubrouter(router)

    router.route()
      .produces(Constants.APP_JSON)
      .handler(AddContentType())
      .handler(NotFoundHandler())

    return router
  }
}
