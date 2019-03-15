package io.holunda.camunda.taskpool.example.process.web

import io.holunda.camunda.taskpool.example.process.web.Web.RESOURCE_LOCATION
import org.springframework.beans.factory.annotation.Value
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
 * Configuration of the base URL for th content delivered by this module.
 */
object Web {

  const val RESOURCE_LOCATION = "classpath:/static/process-forms"

  const val START = "/start"
  const val TASKS = "/tasks" // see process-forms/src/app/app-routing.module.ts
  const val ANY = "/**"

  private const val CSS = "/**/*.css"
  private const val JS = "/**/*.js"
  private const val FONT2 = "/**/*.woff2"
  private const val FONT = "/**/*.woff"
  private const val TTF = "/**/*.ttf"
  private const val PNG = "/**/*.png"
  private const val JPG = "/**/*.jpg"
  private const val ICO = "/**/*.ico"
  private const val JSON = "/**/*.json"

  val STATIC_RESOURCES_LONG_CACHE = arrayOf(CSS, JS, FONT2, FONT, TTF)
  val STATIC_RESOURCES_SHORT_CACHE = arrayOf(PNG, JPG, ICO, JSON)

}

@Configuration
open class ProcessApproveRequestSPAConfiguration(
// see process-forms/src/app/index.html#base@href
  @Value("\${spring.application.name}") val applicationName: String
) : WebMvcConfigurer {

  /**
   * https://github.com/mpalourdio/SpringBootAngularHTML5
   * https://stackoverflow.com/questions/24837715/spring-boot-with-angularjs-html5mode/44850886#44850886
   * http://joshlong.com/jl/blogPost/simplified_web_configuration_with_spring.html
   */
  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {

    /**
     * Deliver the (renamed) SPA index for all task deep links.
     */
    registry
      .addResourceHandler(
        "/$applicationName" + Web.TASKS + Web.ANY,
        "/$applicationName" + Web.START
      )
      .addResourceLocations("$RESOURCE_LOCATION/index.html")
      .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
      .resourceChain(true)
      .addResolver(ResourcePathResolver())

    /**
     * Deliver static resources.
     */
    val staticResourcesLong = Web.STATIC_RESOURCES_LONG_CACHE.map { "/$applicationName$it" }.toTypedArray()
    registry
      .addResourceHandler(*staticResourcesLong)
      .addResourceLocations("$RESOURCE_LOCATION/")
      .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))

    val staticResourcesShort = Web.STATIC_RESOURCES_SHORT_CACHE.map { "/$applicationName$it" }.toTypedArray()
    registry
      .addResourceHandler(*staticResourcesShort)
      .addResourceLocations("$RESOURCE_LOCATION/")
      .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
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

class ResourcePathResolver : PathResourceResolver() {
  override fun getResource(resourcePath: String, location: Resource): Resource? {
    return if (location.exists() && location.isReadable) {
      location
    } else {
      null
    }
  }
}


