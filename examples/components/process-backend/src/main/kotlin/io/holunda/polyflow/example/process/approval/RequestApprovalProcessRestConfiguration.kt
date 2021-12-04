package io.holunda.polyflow.example.process.approval

import io.holunda.polyflow.view.auth.UnknownUserException
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@Configuration
// @EnableSwagger2
@ControllerAdvice
class RequestApprovalProcessRestConfiguration {

//  @Bean
//  fun requestApprovalRestApi() = Docket(DocumentationType.SWAGGER_2)
//    .groupName("example-request-approval")
//    .select()
//    .apis(RequestHandlerSelectors.any())
//    .paths(PathSelectors.any())
//    .build()

  @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Unknown user.")
  @ExceptionHandler(value = [UnknownUserException::class])
  fun forbiddenException() = Unit

  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Element not found.")
  @ExceptionHandler(value = [NoSuchElementException::class])
  fun notFoundException() = Unit

}
