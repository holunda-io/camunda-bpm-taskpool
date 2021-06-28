package io.holunda.polyflow.view.simple.service

import com.tngtech.jgiven.junit.ScenarioTest
import org.junit.Test

class DataPoolServiceTest : ScenarioTest<DataPoolGivenStage<*>, DataPoolWhenStage<*>, DataPoolThenStage<*>>() {

  @Test
  fun `filters to an empty list`() {
    given()
      .entries_exist(13)

    `when`()
      .data_queried(listOf("property=value"))

    then()
      .dataEntriesAreReturned()
  }

  @Test
  fun `retrieves all`() {
    given()
      .entries_exist(13)

    `when`()
      .data_queried(listOf("my-property=myValue"))

    then()
      .dataEntriesAreReturned(*then().dataEntries.toTypedArray())
  }

  @Test
  fun `retrieves only fifth by payload value`() {
    given()
      .entries_exist(13)

    `when`()
      .data_queried(listOf("entryId=entry-5"))

    then()
      .dataEntriesAreReturned(then().dataEntries.toTypedArray()[5])
  }

  @Test
  fun `retrieves only fifth by data entry attribute`() {
    given()
      .entries_exist(13)

    `when`()
      .data_queried(listOf("data.entryId=entry-5"))

    then()
      .dataEntriesAreReturned(then().dataEntries.toTypedArray()[5])
  }

}
