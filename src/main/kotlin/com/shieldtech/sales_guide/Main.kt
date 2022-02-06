package com.shieldtech.sales_guide

import com.shieldtech.sales_guide.configs.Bootstraper

fun main() {
  Bootstraper.configureModules()
  Bootstraper.startServer().subscribe()
}
