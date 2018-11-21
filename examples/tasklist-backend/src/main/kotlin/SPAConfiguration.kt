package io.holunda.camunda.taskpool.example.tasklist

import io.holunda.camunda.taskpool.example.tasklist.Web.STATIC_RESOURCES_LONG_CACHE
import io.holunda.camunda.taskpool.example.tasklist.Web.STATIC_RESOURCES_SHORT_CACHE
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.http.HttpMethod
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import java.util.concurrent.TimeUnit


object Web {
  const val BASE_PATH = "tasklist"

  const val CSS = "$BASE_PATH/**/*.css"
  const val JS = "$BASE_PATH/**/*.js"
  const val FONT2 = "$BASE_PATH/**/*.woff2"
  const val FONT = "$BASE_PATH/**/*.woff"
  const val TTF = "$BASE_PATH/**/*.ttf"
  const val PNG = "$BASE_PATH/**/*.png"
  const val JPG = "$BASE_PATH/**/*.jpg"
  const val ICO = "$BASE_PATH/**/*.ico"
  const val JSON = "$BASE_PATH/**/*.json"

  val STATIC_RESOURCES_LONG_CACHE = arrayOf(CSS, JS, FONT2, FONT, TTF)
  val STATIC_RESOURCES_SHORT_CACHE = arrayOf(PNG, JPG, ICO, JSON)
}


@Configuration
open class TasklistSPAConfiguration : WebMvcConfigurer {

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    /**
     * Deliver the platform SPA index for all frontend states.
     */
    registry
      .addResourceHandler("${Web.BASE_PATH}/")
      .addResourceLocations("classpath:/static/tasklist-angular/index.html")
      .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
      .resourceChain(true)
      .addResolver(ResourcePathResolver())

    registry
      .addResourceHandler(*STATIC_RESOURCES_LONG_CACHE)
      .addResourceLocations("classpath:/static/tasklist-angular/")
      .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
    registry
      .addResourceHandler(*STATIC_RESOURCES_SHORT_CACHE)
      .addResourceLocations("classpath:/static/tasklist-angular/")
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
