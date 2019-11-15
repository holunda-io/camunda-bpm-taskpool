package io.holunda.camunda.taskpool.example.process.web;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.holunda.camunda.taskpool.enricher.FilterType;
import io.holunda.camunda.taskpool.enricher.ProcessVariablesFilter;
import io.holunda.camunda.taskpool.enricher.TaskVariableFilter;
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest;
import org.springframework.context.annotation.Bean;

import java.util.List;

public class TestClass {

  @Bean
  public ProcessVariablesFilter myProcessVariablesFilter() {

    return new ProcessVariablesFilter(
      // define a variable filter for every process
      new TaskVariableFilter[]{
        // for every process definition
        new TaskVariableFilter(
          ProcessApproveRequest.KEY,
          // filter type
          FilterType.INCLUDE,
          ImmutableMap.<String, List<String>>builder()
            // define a variable filter for every task
            .put(ProcessApproveRequest.Elements.APPROVE_REQUEST, Lists.newArrayList(
              ProcessApproveRequest.Variables.REQUEST_ID,
              ProcessApproveRequest.Variables.ORIGINATOR)
            )
            // and again
            .put(ProcessApproveRequest.Elements.AMEND_REQUEST, Lists.newArrayList(
              ProcessApproveRequest.Variables.REQUEST_ID,
              ProcessApproveRequest.Variables.COMMENT,
              ProcessApproveRequest.Variables.APPLICANT)
            ).build()
        )
      }
    );
  }

}
