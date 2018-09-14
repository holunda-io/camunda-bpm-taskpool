package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequestBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
open class ProcessStarterController {

  @Autowired
  lateinit var processApproveRequestBean: ProcessApproveRequestBean

  @PostMapping("/request/start/{count}")
  open fun startManyInstances(@PathVariable("count") count: Int): ResponseEntity<List<String>> {
    val instances = mutableListOf<String>()
    for (numberOfInstances in processApproveRequestBean.countInstances()..count) {
      instances.add(processApproveRequestBean.startProcess().processInstanceId)
    }
    return ResponseEntity.ok(instances)
  }

  @DeleteMapping("/request")
  open fun deleteAllInstances() {
    processApproveRequestBean.deleteAllInstances()
  }

  @PostMapping("/request/{id}/decision/{decision}")
  open fun approve(@PathVariable("id") id: String, @PathVariable("decision") decision: String) {
    processApproveRequestBean.approve(id, decision)
  }

  @PostMapping("/request/{id}/action/{action}")
  open fun amend(@PathVariable("id") id: String, @PathVariable("action") action: String) {
    processApproveRequestBean.amend(id, action)
  }

}
