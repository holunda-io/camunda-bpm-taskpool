package io.holunda.polyflow.bus.jackson

import com.fasterxml.jackson.annotation.JsonAutoDetect

/**
 * Allow serialization of all fields. (Also private fields!)
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class JsonAutoDetectAnyVisibility
