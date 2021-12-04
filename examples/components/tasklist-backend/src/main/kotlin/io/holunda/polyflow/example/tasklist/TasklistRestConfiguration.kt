package io.holunda.polyflow.example.tasklist

import io.holunda.polyflow.view.auth.UnknownUserException

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@Configuration
@ControllerAdvice
//@EnableSwagger2
class TasklistRestConfiguration {

//  @Bean
//  fun tasklistRestApi() = Docket(DocumentationType.SWAGGER_2)
//    .groupName("polyflow process platform")
//    .select()
//    .apis(RequestHandlerSelectors.basePackage("io.holunda.polyflow.example.tasklist"))
//    .paths(PathSelectors.any())
//    .build()


  @ExceptionHandler(UnknownUserException::class)
  fun unknownUserException(e: UnknownUserException) = ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
}
