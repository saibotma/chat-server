package util

import org.apache.commons.lang3.StringUtils
import java.io.InputStream
import java.net.URL

fun String.asResource(): InputStream {
    return {}.javaClass.classLoader.getResourceAsStream(this)!!
}

fun String.asResourceUrl(): URL {
    return {}.javaClass.classLoader.getResource(this)!!
}

fun String.snakeToCamelCase(): String {
    return split("_").joinToString(separator = "") { it.capitalize() }.decapitalize()
}
