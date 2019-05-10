package io.holunda.camunda.taskpool.example.tasklist

import io.holunda.camunda.taskpool.example.tasklist.Web.STATIC_LOCATION
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
  const val BASE_PATH = "tasklist"

  private const val CSS = "$BASE_PATH/*.css"
  private const val JS = "$BASE_PATH/*.js"
  private const val FONT2 = "$BASE_PATH/*.woff2"
  private const val FONT = "$BASE_PATH/*.woff"
  private const val TTF = "$BASE_PATH/*.ttf"
  private const val PNG = "$BASE_PATH/**/*.png"
  private const val JPG = "$BASE_PATH/**/*.jpg"
  private const val ICO = "$BASE_PATH/*.ico"
  private const val JSON = "$BASE_PATH/*.json"

  val STATIC_RESOURCES_LONG_CACHE = arrayOf(CSS, JS, FONT2, FONT, TTF)
  val STATIC_RESOURCES_SHORT_CACHE = arrayOf(PNG, JPG, ICO, JSON)

  const val STATIC_LOCATION = "classpath:/static/tasklist-angular/"
}

@Configuration
class TasklistSPAConfiguration : WebFluxConfigurer {

  @Value("${STATIC_LOCATION}index.html")
  private lateinit var indexHtml: Resource

  @Bean
  fun tasklistSpaRouter() = router {
    GET("/${Web.BASE_PATH}/") {
      ok().contentType(TEXT_HTML).syncBody(indexHtml)
    }
  }

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {

    /**
     * Deliver the platform SPA index for all frontend states.
     */
    registry
      .addResourceHandler("${Web.BASE_PATH}/*.html")
      .addResourceLocations(STATIC_LOCATION)
      .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))

    registry
      .addResourceHandler(*STATIC_RESOURCES_LONG_CACHE)
      .addResourceLocations(STATIC_LOCATION)
      .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))

    registry
      .addResourceHandler(*STATIC_RESOURCES_SHORT_CACHE)
      .addResourceLocations(STATIC_LOCATION)
      .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))

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

  override fun configurePathMatching(configurer: PathMatchConfigurer) {
    configurer.setUseTrailingSlashMatch(true)
  }
}
