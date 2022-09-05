package com.shieldtech.sales_guide.utils

import com.amazonaws.AmazonWebServiceRequest
import com.amazonaws.handlers.AsyncHandler
import kotlin.Exception

class AwsAsyncHandler<TInput : AmazonWebServiceRequest?, TOutput>(private val onSuccessAction: (input: TInput, output: TOutput) -> Unit, private val onErrorAction: (Exception) -> Unit) : AsyncHandler<TInput, TOutput> {
  override fun onError(err: Exception) {
    onErrorAction(err)
  }

  override fun onSuccess(input: TInput, output: TOutput) {
    onSuccessAction(input, output)
  }
}
