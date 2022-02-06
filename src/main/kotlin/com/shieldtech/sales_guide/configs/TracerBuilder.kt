package com.shieldtech.sales_guide.configs

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes.SERVICE_NAME


object TracerBuilder {
  fun getInstance(): Tracer {
    val exporter: ZipkinSpanExporter = ZipkinSpanExporter.builder()
      .setEndpoint(Envs.getSpanReportEndpoint())
      .build()

    val jaegerExporter = JaegerGrpcSpanExporter.builder()
      .setEndpoint("http://localhost:14250")
      .build()

    val sdkTracerProvider: SdkTracerProvider = SdkTracerProvider.builder()
      .setResource(Resource.create(Attributes.of(SERVICE_NAME, Envs.getServiceName())))
      .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
      .addSpanProcessor(BatchSpanProcessor.builder(jaegerExporter).build())
      .build()

    val openTelemetry: OpenTelemetry = OpenTelemetrySdk.builder()
      .setTracerProvider(sdkTracerProvider)
      .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
      .buildAndRegisterGlobal()

    val tracer: Tracer = openTelemetry.getTracer("instrumentation-library-name", "1.0.0")
    return tracer
  }
}

