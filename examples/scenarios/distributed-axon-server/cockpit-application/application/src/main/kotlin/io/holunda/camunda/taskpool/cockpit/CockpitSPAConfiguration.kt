package io.holunda.camunda.taskpool.cockpit

import io.holunda.camunda.taskpool.cockpit.Web.BASE_PATH
import io.holunda.camunda.taskpool.cockpit.rest.Rest
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
 * SPA config.
 */
@Configuration
class CockpitSPAConfiguration : WebFluxConfigurer {

  /**
   * Constants.
   */
  companion object {
    private const val CSS = "$BASE_PATH/*.css"
    private const val JS = "$BASE_PATH/*.js"
    private const val FONT2 = "$BASE_PATH/*.woff2"
    private const val FONT = "$BASE_PATH/*.woff"
    private const val TTF = "$BASE_PATH/*.ttf"
    private const val PNG = "$BASE_PATH/**/*.png"
    private const val JPG = "$BASE_PATH/**/*.jpg"
    private const val ICO = "$BASE_PATH/*.ico"
    private const val MAP = "$BASE_PATH/*.map"
    private const val JSON = "$BASE_PATH/*.json"

    val STATIC_RESOURCES_LONG_CACHE = arrayOf(CSS, JS, FONT2, FONT, TTF, MAP)
    val STATIC_RESOURCES_SHORT_CACHE = arrayOf(PNG, JPG, ICO, JSON)

    private const val STATIC_LOCATION = "classpath:/static/taskpool-cockpit/"
  }

  @Value("$STATIC_LOCATION/index.html")
  private lateinit var indexHtml: Resource

  /**
   * Webflux router configuration.
   */
  @Bean
  fun cockpitSpaRouter() = router {
    GET("/$BASE_PATH/") {
      ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml)
    }
  }


  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    /**
     * Deliver the platform SPA index for all frontend states.
     */
    registry
      .addResourceHandler("/${BASE_PATH}/*.html")
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
    registry
      .addMapping(Rest.PATH + "/**")
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

