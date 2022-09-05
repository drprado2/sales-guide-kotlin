package com.shieldtech.sales_guide.adapters.aws

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.shieldtech.sales_guide.configs.Envs

object Bootstraper {
  fun getSqsAsyncClient(): AmazonSQSAsync {
    return AmazonSQSAsyncClientBuilder.standard()
      .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(Envs.getAwsEndpoint(), Envs.getAwsRegion()))
      .build()
  }
}
