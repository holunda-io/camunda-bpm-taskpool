package io.holunda.camunda.taskpool.example.process.service

import java.math.BigDecimal


data class Request(
  val id: String,
  val applicant: String,
  val subject: String,
  val amount: BigDecimal,
  val currency: String
)
