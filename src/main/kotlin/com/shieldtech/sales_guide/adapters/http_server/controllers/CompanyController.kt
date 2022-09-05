package com.shieldtech.sales_guide.adapters.http_server.controllers

import com.shieldtech.sales_guide.adapters.http_server.middlewares.AddContentType
import com.shieldtech.sales_guide.adapters.http_server.middlewares.AuthMiddleware
import com.shieldtech.sales_guide.adapters.http_server.transformers.CompanyTransformer
import com.shieldtech.sales_guide.adapters.http_server.transformers.InvalidCommandTransformer.toJson
import com.shieldtech.sales_guide.adapters.http_server.utils.endRouterSpan
import com.shieldtech.sales_guide.adapters.http_server.utils.setErrorToSpan
import com.shieldtech.sales_guide.configs.Envs
import com.shieldtech.sales_guide.domain.commands.GetCompanyByIdCommand
import com.shieldtech.sales_guide.domain.errors.CompanyNotFoundException
import com.shieldtech.sales_guide.domain.errors.DatabaseException
import com.shieldtech.sales_guide.domain.errors.InvalidCommandException
import com.shieldtech.sales_guide.domain.usecases.GetCompanyById
import com.shieldtech.sales_guide.logs.CorrelationLogger
import com.shieldtech.sales_guide.utils.Constants.APP_JSON
import com.shieldtech.sales_guide.utils.Constants.COMPANY_ID
import com.shieldtech.sales_guide.utils.asLogArg
import io.reactivex.rxjava3.core.Completable
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.rxjava3.core.Vertx
import io.vertx.rxjava3.ext.web.Router
import io.vertx.rxjava3.ext.web.RoutingContext
import io.vertx.rxjava3.ext.web.handler.TimeoutHandler

class CompanyController(vertx: Vertx, private val getCompanyById: GetCompanyById) {
  companion object {
    private val logger: CorrelationLogger =
      CorrelationLogger.getLogger(com.shieldtech.sales_guide.adapters.http_server.Router::class.java)
    private val unexpectedErrorResponse = JsonObject()
      .put("message", "unexpected error occurred, try later")
      .encode()
    private val companyNotFoundResponse = JsonObject()
      .put("message", "company not found")
      .encode()
    private const val BASE_ROUTE = "/api/v1/companies"
  }

  private val router : Router = Router.router(vertx)

  fun configureRoutes(): CompanyController {
    router
      .route(HttpMethod.GET, "/current")
      .produces(APP_JSON)
      .handler(AddContentType())
      .handler(TimeoutHandler.create(Envs.getRequestMaxTimeout()))
      .handler(AuthMiddleware())
      .handler(this::getCompanyById)

    return this
  }

  fun registerAsSubrouter(mainRouter: Router) {
    mainRouter.mountSubRouter(BASE_ROUTE, router)
  }

  private fun getCompanyById(ctx: RoutingContext) {
    val cmd = CompanyTransformer.buildGetCompanyByIdInput(ctx)
    getCompanyById.handle(cmd)
      .flatMapCompletable {
        logger.info(
          "company with id=${it.id} found", it.cid,
          it.id asLogArg COMPANY_ID
        )
        ctx.response()
          .setStatusCode(200)
          .end(
            if (it.company != null) CompanyTransformer.toCompanyOutput(ctx, it.company)
              .encode() else companyNotFoundResponse
          )
      }
      .onErrorResumeNext {
        ctx.setErrorToSpan(it)
        handleError(it, ctx, cmd)
      }
      .subscribe {
        ctx.endRouterSpan()
      }
  }

  private fun handleError(error: Throwable, ctx: RoutingContext, cmd: GetCompanyByIdCommand): Completable {
    when (error) {
      is DatabaseException -> {
        logger.error("error on get company by id", cmd.cid, null, cmd.id asLogArg COMPANY_ID)
        return ctx.response()
          .setStatusCode(500)
          .end(unexpectedErrorResponse)
      }
      is CompanyNotFoundException -> {
        logger.warn("company not found when find by id", cmd.cid, null, cmd.id asLogArg COMPANY_ID)
        return ctx.response()
          .setStatusCode(404)
          .end(companyNotFoundResponse)
      }
      is InvalidCommandException -> {
        logger.warn("invalid request during get company by id", cmd.cid, null, cmd.id asLogArg COMPANY_ID)
        return ctx.response()
          .setStatusCode(400)
          .end(error.toJson())
      }
      else -> {
        logger.error("error on get company by id", cmd.cid, null, cmd.id asLogArg COMPANY_ID)
        return ctx.response()
          .setStatusCode(500)
          .end(unexpectedErrorResponse)
      }
    }
  }
}
