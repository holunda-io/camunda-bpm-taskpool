package io.holunda.polyflow.example.process.approval

import io.holunda.polyflow.example.process.approval.Web.RESOURCE_LOCATION
import io.holunda.polyflow.example.process.approval.Web.ROUTES
import io.holunda.polyflow.example.process.approval.Web.STATIC_RESOURCES_LONG_CACHE
import io.holunda.polyflow.example.process.approval.Web.STATIC_RESOURCES_SHORT_CACHE
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

  const val RESOURCE_LOCATION = "classpath:/static/example-process-approval/"

  private const val START = "/start"                        // see process-forms/src/app/app-routing.module.ts
  private const val TASKS = "/tasks"                        // see process-forms/src/app/app-routing.module.ts
  private const val APPROVAL_REQUEST = "/approval-request"  // see process-forms/src/app/app-routing.module.ts
  internal const val ANY = "/**"

  val ROUTES = arrayOf(
    START, // /start?userId=...
    TASKS + ANY, // /tasks/...id...?userId=...
    APPROVAL_REQUEST + ANY // /approval-request/...id...?userId=...
  )
  val STATIC_RESOURCES_LONG_CACHE = arrayOf("/*.css", "/*.js", "/*.woff2", "/*.woff", "/*.ttf")
  val STATIC_RESOURCES_SHORT_CACHE = arrayOf("/*/*.png", "/*/*.jpg", "/*/*.ico", "/*/*.json")


}

@Configuration
class ProcessApproveRequestSPAConfiguration(
  @Value("\${spring.application.name:example-process-approval}") val applicationName: String
) : WebMvcConfigurer {

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {

    /**
     * Frontend routes
     */
    val routes = ROUTES.map { "/$applicationName$it" }.toTypedArray()
    registry
      .addResourceHandler(*routes)
      .addResourceLocations("${RESOURCE_LOCATION}index.html")
      .resourceChain(true)
      .addResolver(LocationAwarePathResourceResolver())


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
      .allowedOrigins("http://localhost:4300")
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
