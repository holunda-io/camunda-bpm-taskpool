package io.holunda.camunda.taskpool.example.tasklist

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.ControllerAdvice
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@ControllerAdvice
@EnableSwagger2
class TasklistRestConfiguration {

  @Bean
  fun tasklistRestApi() = Docket(DocumentationType.SWAGGER_2)
    .groupName("polyflow process platform")
    .select()
    .apis( RequestHandlerSelectors.basePackage( "io.holunda.camunda.taskpool.example.tasklist" ) )
    .paths(PathSelectors.any())
    .build()

}
