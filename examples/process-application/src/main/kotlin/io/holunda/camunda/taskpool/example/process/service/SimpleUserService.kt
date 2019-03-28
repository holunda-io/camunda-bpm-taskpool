package io.holunda.camunda.taskpool.example.process.service

import io.holunda.camunda.taskpool.api.sender.DataEntryCommandSender
import org.springframework.stereotype.Component

@Component
class SimpleUserService(
  private val sender: DataEntryCommandSender
) {

  private val muppetUsers = setOf(
    "kermit",
    "piggy",
    "gonzo",
    "fozzy"
  )

  fun getAllUsers(): List<String> = muppetUsers.toList()

  fun notify(username: String) {
    if (muppetUsers.contains(username)) {
      sender.sendDataEntryCommand(entryType = BusinessDataEntry.USER, entryId = username, payload = mapOf("username" to username))
    }
  }


  data class RichUserObject(private val username: String)
}
