package logging

import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.origin
import io.ktor.server.request.RequestAlreadyConsumedException
import io.ktor.server.request.httpMethod
import io.ktor.server.request.httpVersion
import io.ktor.server.request.path
import io.ktor.server.request.receive
import io.ktor.util.AttributeKey
import org.apache.logging.log4j.kotlin.logger

private const val name = "LoggingPlugin"
val LoggingPlugin = createApplicationPlugin(name = name, createConfiguration = ::LoggingPluginConfig) {
    val log = logger(name)
    val startTimeKey = AttributeKey<Long>("startTime")

    val shouldLogHeaders = pluginConfig.shouldLogHeaders
    val shouldLogBody = pluginConfig.shouldLogBody
    val shouldLogFullUrl = pluginConfig.shouldLogFullUrl

    onCall { call ->
        call.attributes.put(startTimeKey, System.currentTimeMillis())
        log.info(
            StringBuilder().apply {
                appendLine("Received request:")
                appendLine("Call id: ${call.callId}")
                val requestURI = if (shouldLogFullUrl) call.request.origin.uri else call.request.path()
                appendLine(call.request.origin.run { "${method.value} $scheme://$host:$port$requestURI $version" })

                if (shouldLogHeaders) {
                    call.request.headers.forEach { header, values ->
                        appendLine("$header: ${values.firstOrNull()}")
                    }
                }

                if (shouldLogBody) {
                    try {
                        // new line before body as in HTTP request
                        appendLine()
                        // have to receive ByteArray for DoubleReceive to work
                        // new line after body because in the log there might be additional info after "log message"
                        appendLine(String(call.receive<ByteArray>()))
                    } catch (e: RequestAlreadyConsumedException) {
                        log.error(
                            "Logging payloads requires DoubleReceive feature " +
                                "to be installed with receiveEntireContent=true",
                            e
                        )
                    }
                }
            }.toString()
        )
    }

    onCallRespond { call ->
        val startTime = call.attributes.getOrNull(startTimeKey)
        if (startTime != null) {
            val duration = System.currentTimeMillis() - startTime
            val method = call.request.httpMethod.value
            val requestURI = if (shouldLogFullUrl) call.request.origin.uri else call.request.path()
            val url = call.request.origin.run { "$scheme://$host:$port$requestURI" }

            log.info("$duration ms - ${call.callId} - $method $url")
        }

        transformBody {
            log.info(
                StringBuilder().apply {
                    appendLine("Sent response:")
                    appendLine("Call id: ${call.callId}")
                    appendLine("${call.request.httpVersion} ${call.response.status()}")
                    if (shouldLogHeaders) {
                        call.response.headers.allValues().forEach { header, values ->
                            appendLine("$header: ${values.firstOrNull()}")
                        }
                    }
                    if (shouldLogBody) {
                        // new line before body as in HTTP response
                        appendLine()
                        // new line after body because in the log there might be additional info after "log message"
                        appendLine(it.toString())
                    }
                    // do not log warning if  subject is not OutgoingContent.ByteArrayContent
                    // as we could possibly spam warnings without any option to disable them
                }.toString()
            )
            it
        }
    }
}

class LoggingPluginConfig(
    var shouldLogHeaders: Boolean = true,
    var shouldLogBody: Boolean = true,
    var shouldLogFullUrl: Boolean = true,
)
