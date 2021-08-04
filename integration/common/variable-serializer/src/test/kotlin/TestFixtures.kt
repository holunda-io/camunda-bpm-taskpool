package io.holunda.polyflow.variable.serializer

import java.time.Instant
import java.time.OffsetDateTime

data class Pojo1(
  val key: String,
  val anotherKey: List<Pojo2>
)

data class Pojo2(
  val keyZUZUZ: String,
  var children: List<Pojo1> = listOf()
)

data class Pojo3(
  val key: String,
  val anotherKey: Int
)

data class Pojo4(
  val key: String,
  val anotherKey: List<Int>
)

data class Pojo5(
  val key: String,
  val ts: Instant,
  val date: OffsetDateTime
)
