package io.holunda.camunda.taskpool.example.process.service

import java.math.BigDecimal
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "APP_APPROVAL_REQUEST")
class Request(

  @Id
  val id: String = UUID.randomUUID().toString(),
  val applicant: String,
  val subject: String,
  @Column(name = "amount", precision = 10, scale = 2)
  val amount: BigDecimal,
  val currency: String
) {
  constructor() : this("", "", "", BigDecimal.ZERO, "")


  override fun toString(): String {
    return "Request(id='$id', applicant='$applicant', subject='$subject', amount=$amount, currency='$currency')"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Request

    if (id != other.id) return false
    if (applicant != other.applicant) return false
    if (subject != other.subject) return false
    if (amount != other.amount) return false
    if (currency != other.currency) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + applicant.hashCode()
    result = 31 * result + subject.hashCode()
    result = 31 * result + amount.hashCode()
    result = 31 * result + currency.hashCode()
    return result
  }
}
