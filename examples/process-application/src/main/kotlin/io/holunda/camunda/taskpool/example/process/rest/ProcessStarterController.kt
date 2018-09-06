package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.example.process.ProcessApproveRequestBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
open class ProcessStarterController {

  companion object {
    const val NUMBER_OF_INSTANCES = 10
  }

  @Autowired
  lateinit var processApproveRequestBean: ProcessApproveRequestBean

  @PostMapping("/approve-request")
  open fun startManyInstances() {
    for (numberOfInstances in processApproveRequestBean.countInstances()..NUMBER_OF_INSTANCES) {
      processApproveRequestBean.startProcess()
    }
  }

  @DeleteMapping("/approve-request")
  open fun deleteAllInstances() {
    processApproveRequestBean.deleteAllInstances()
  }

}
