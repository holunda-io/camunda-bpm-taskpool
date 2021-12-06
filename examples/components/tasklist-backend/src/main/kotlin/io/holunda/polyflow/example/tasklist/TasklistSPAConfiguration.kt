package io.holunda.polyflow.example.tasklist

import io.holunda.polyflow.example.tasklist.Web.RESOURCE_LOCATION
import io.holunda.polyflow.example.tasklist.Web.ROUTES
import io.holunda.polyflow.example.tasklist.Web.STATIC_RESOURCES_LONG_CACHE
import io.holunda.polyflow.example.tasklist.Web.STATIC_RESOURCES_SHORT_CACHE
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.http.HttpMethod
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import java.util.concurrent.TimeUnit

/**
 * Collection of constants.
 */
object Web {

  const val BASE_PATH = "polyflow"
  const val RESOURCE_LOCATION = "classpath:/static/polyflow/"

  val ROUTES = arrayOf(
    "/${BASE_PATH}", "/${BASE_PATH}/", "/${BASE_PATH}/index.html",
    "/${BASE_PATH}/tasks", "/${BASE_PATH}/tasks/",
    "/${BASE_PATH}/archive", "/${BASE_PATH}/archive/"
  )
  val STATIC_RESOURCES_LONG_CACHE = arrayOf("$BASE_PATH/*.css", "$BASE_PATH/*.js", "$BASE_PATH/*.woff2", "$BASE_PATH/*.woff", "$BASE_PATH/*.ttf")
  val STATIC_RESOURCES_SHORT_CACHE = arrayOf("$BASE_PATH/assets/*.png", "$BASE_PATH/assets/*.jpg", "$BASE_PATH/*.ico", "$BASE_PATH/*.json")
}

/**
 * General Single-Page-Application configuration.
 */
@Configuration
class TasklistSPAConfiguration : WebMvcConfigurer {

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {

    /**
     * Frontend routes.
     */
    registry
      .addResourceHandler(*ROUTES)
      .addResourceLocations("${RESOURCE_LOCATION}index.html")
      .resourceChain(true)
      .addResolver(LocationAwarePathResourceResolver())

    /**
     * SPA parts for long cache.
     */
    registry
      .addResourceHandler(*STATIC_RESOURCES_LONG_CACHE)
      .addResourceLocations(RESOURCE_LOCATION)
      .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))

    /**
     * SPA parts for short cache.
     */
    registry
      .addResourceHandler(*STATIC_RESOURCES_SHORT_CACHE)
      .addResourceLocations(RESOURCE_LOCATION)
      .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))

    /**
     * Swagger UI.
     */
    registry.addResourceHandler("/swagger-ui.html**")
      .addResourceLocations("classpath:/META-INF/resources/")

    /**
     * Webjar support.
     */
    registry.addResourceHandler("/webjars/**")
      .addResourceLocations("classpath:/META-INF/resources/webjars/")
  }

  override fun addCorsMappings(registry: CorsRegistry) {
    // allow ng serve to access the backend
    registry
      .addMapping(Web.BASE_PATH + "/**")
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

  class LocationAwarePathResourceResolver : PathResourceResolver() {
    override fun getResource(resourcePath: String, location: Resource): Resource? {
      return if (location.exists() && location.isReadable) {
        location
      } else {
        null
      }
    }
  }

}
