package io.holunda.camunda.taskpool.example.process.service

import io.holunda.camunda.datapool.sender.DataEntryCommandSender
import io.holunda.camunda.taskpool.example.users.UserStoreService
import io.holunda.camunda.taskpool.view.auth.UserService
import org.camunda.bpm.engine.variable.Variables
import org.springframework.stereotype.Component

@Component
class UserNotificationService(
  private val sender: DataEntryCommandSender,
  private val userStoreService: UserStoreService,
  private val userService: UserService
) {

  fun publishAll() {
    userStoreService.getUserIdentifiers().forEach{
      val user = userService.getUser(it.key)
      sender.sendDataEntryCommand(entryType = BusinessDataEntry.USER, entryId = it.key, payload = Variables.putValue("user", RichUserObject(username = user.username)))
    }
  }

  data class RichUserObject(val username: String)
}
