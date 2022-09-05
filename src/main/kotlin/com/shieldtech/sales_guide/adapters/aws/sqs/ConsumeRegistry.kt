package com.shieldtech.sales_guide.adapters.aws.sqs

import com.shieldtech.sales_guide.domain.consumers.ConsumerHandler

data class ConsumeRegistry(val name: String, val queueUrl: String, val poolInterval: Long, val waitTimeSeconds: Int, val maxNumberOfMessages: Int, val handler: ConsumerHandler)
