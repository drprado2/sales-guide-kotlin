package com.shieldtech.sales_guide.configs

import io.opentelemetry.api.GlobalOpenTelemetry
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
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes.SERVICE_NAME


class TracerBuilder {
  private val exporters = mutableListOf<SpanExporter>()

  fun withZipkin() : TracerBuilder {
    val exporter: ZipkinSpanExporter = ZipkinSpanExporter.builder()
      .setEndpoint(Envs.getZipkinEndpoint())
      .build()
    this.exporters.add(exporter)
    return this
  }

  fun withJaeger() : TracerBuilder {
    val jaegerExporter = JaegerGrpcSpanExporter.builder()
      .setEndpoint(Envs.getJaegerEndpoint())
      .build()

    exporters.add(jaegerExporter)
    return this
  }

  fun build(): Tracer {
    val sdkTracerProvider = SdkTracerProvider.builder()
      .setResource(Resource.create(Attributes.of(SERVICE_NAME, Envs.getServiceName())))

    exporters.forEach {
      sdkTracerProvider.addSpanProcessor(BatchSpanProcessor.builder(it).build())
    }

    val openTelemetry: OpenTelemetry = OpenTelemetrySdk.builder()
      .setTracerProvider(sdkTracerProvider.build())
      .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
      .buildAndRegisterGlobal()

    return openTelemetry.getTracer("instrumentation-library-name", "1.0.0")
  }
}

