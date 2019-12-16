package io.holunda.camunda.taskpool.example.process

import io.holunda.camunda.taskpool.example.process.Web.RESOURCE_LOCATION
import io.holunda.camunda.taskpool.example.process.Web.ROUTES
import io.holunda.camunda.taskpool.example.process.Web.STATIC_RESOURCES_LONG_CACHE
import io.holunda.camunda.taskpool.example.process.Web.STATIC_RESOURCES_SHORT_CACHE
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.router
import java.util.concurrent.TimeUnit


/**
 * Configuration of the base URL for th content delivered by this module.
 */
object Web {

  const val RESOURCE_LOCATION = "classpath:/static/example-process-approval/"

  private const val START = "/start"                        // see process-forms/src/app/app-routing.module.ts
  private const val TASKS = "/tasks"                        // see process-forms/src/app/app-routing.module.ts
  private const val APPROVAL_REQUEST = "/approval-request"  // see process-forms/src/app/app-routing.module.ts
  internal const val ANY = "/**"

  val ROUTES = arrayOf(START + ANY, TASKS + ANY, APPROVAL_REQUEST + ANY)
  val STATIC_RESOURCES_LONG_CACHE = arrayOf("$ANY/*.css", "$ANY/*.js", "$ANY/*.woff2", "$ANY/*.woff", "$ANY/*.ttf")
  val STATIC_RESOURCES_SHORT_CACHE = arrayOf("$ANY/*.png", "$ANY/*.jpg", "$ANY/*.ico", "$ANY/*.json")


}

@Configuration
class ProcessApproveRequestSPAConfiguration(
  @Value("\${spring.application.name:example-process-approval}") val applicationName: String,
  @Value("${RESOURCE_LOCATION}index.html") val indexHtml: Resource
) : WebFluxConfigurer {

  @Bean
  fun processSpaRouter() = router {
    ROUTES.forEach {
      GET("/$applicationName$it") { ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml) }
    }
  }

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {

    /**
     * Deliver static resources.
     */
    val staticResourcesLong = STATIC_RESOURCES_LONG_CACHE.map { "/$applicationName$it" }.toTypedArray()
    registry
      .addResourceHandler(*staticResourcesLong)
      .addResourceLocations(RESOURCE_LOCATION)
      .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))

    val staticResourcesShort = STATIC_RESOURCES_SHORT_CACHE.map { "/$applicationName$it" }.toTypedArray()
    registry
      .addResourceHandler(*staticResourcesShort)
      .addResourceLocations(RESOURCE_LOCATION)
      .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))

    /**
     * Deliver Swagger UI
     */
    registry.addResourceHandler("/swagger-ui.html**")
      .addResourceLocations("classpath:/META-INF/resources/")
      .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))

    /**
     * Webjar support
     */
    registry.addResourceHandler("/webjars/**")
      .addResourceLocations("classpath:/META-INF/resources/webjars/")
      .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
  }

  override fun addCorsMappings(registry: CorsRegistry) {
    // allow ng serve to access the backend
    registry.addMapping("/$applicationName" + Web.ANY)
      .allowedOrigins("http://localhost:4200")
      .allowedMethods(
        HttpMethod.GET.name,
        HttpMethod.HEAD.name,
        HttpMethod.POST.name,
        HttpMethod.DELETE.name,
        HttpMethod.OPTIONS.name,
        HttpMethod.PATCH.name,
        HttpMethod.PUT.name
      )
  }
}
