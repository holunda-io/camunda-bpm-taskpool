package io.holunda.camunda.taskpool.example.tasklist

import io.holunda.camunda.taskpool.example.tasklist.Web.RESOURCE_LOCATION
import io.holunda.camunda.taskpool.example.tasklist.Web.ROUTES
import io.holunda.camunda.taskpool.example.tasklist.Web.STATIC_RESOURCES_LONG_CACHE
import io.holunda.camunda.taskpool.example.tasklist.Web.STATIC_RESOURCES_SHORT_CACHE
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.TEXT_HTML
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.PathMatchConfigurer
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.router
import java.util.concurrent.TimeUnit


object Web {

  const val BASE_PATH = "taskpool"
  const val RESOURCE_LOCATION = "classpath:/static/taskpool/"

  val ROUTES = arrayOf("/${BASE_PATH}/", "/${BASE_PATH}/index.html", "/${BASE_PATH}/tasks", "/${BASE_PATH}/archive")
  val STATIC_RESOURCES_LONG_CACHE = arrayOf("$BASE_PATH/*.css", "$BASE_PATH/*.js", "$BASE_PATH/*.woff2", "$BASE_PATH/*.woff", "$BASE_PATH/*.ttf")
  val STATIC_RESOURCES_SHORT_CACHE = arrayOf("$BASE_PATH/**/*.png", "$BASE_PATH/**/*.jpg", "$BASE_PATH/*.ico", "$BASE_PATH/*.json")

}

@Configuration
class TasklistSPAConfiguration(
  @Value("${RESOURCE_LOCATION}index.html") val indexHtml: Resource
) : WebFluxConfigurer {

  @Bean
  fun tasklistSpaRouter() = router {
    ROUTES.forEach {
      GET(it) { ok().contentType(TEXT_HTML).bodyValue(indexHtml) }
    }
  }

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    registry
      .addResourceHandler(*STATIC_RESOURCES_LONG_CACHE)
      .addResourceLocations(RESOURCE_LOCATION)
      .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))

    registry
      .addResourceHandler(*STATIC_RESOURCES_SHORT_CACHE)
      .addResourceLocations(RESOURCE_LOCATION)
      .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))

    registry.addResourceHandler("/swagger-ui.html**")
      .addResourceLocations("classpath:/META-INF/resources/");

    registry.addResourceHandler("/webjars/**")
      .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  override fun configurePathMatching(configurer: PathMatchConfigurer) {
    configurer.setUseTrailingSlashMatch(true)
  }

  override fun addCorsMappings(registry: CorsRegistry) {
    // allow ng serve to access the backend
    registry.addMapping(Web.BASE_PATH + "/**")
      .allowedOrigins("http://localhost:4200")
      .allowedMethods(
        HttpMethod.GET.name,
        HttpMethod.HEAD.name,
        HttpMethod.POST.name,
        HttpMethod.DELETE.name,
        HttpMethod.OPTIONS.name,
        HttpMethod.PATCH.name,
        HttpMethod.PUT.name)
  }

}
