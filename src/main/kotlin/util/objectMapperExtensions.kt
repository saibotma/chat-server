package util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

fun ObjectMapper.convertToStringMap(map: Map<String, Any>): Map<String, String> {
    val string = this.writeValueAsString(map)
    return readValue(string)
}
