package persistence.jooq

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.jooq.Converter
import org.jooq.ConverterProvider
import org.jooq.JSON
import org.jooq.exception.DataTypeException
import org.jooq.impl.DefaultConverterProvider
import java.io.StringWriter

class JacksonKotlinConverterProvider(private val objectMapper: ObjectMapper) : ConverterProvider {
    private val delegate: ConverterProvider = DefaultConverterProvider()

    override fun <T : Any?, U : Any?> provide(tType: Class<T>?, uType: Class<U>?): Converter<T, U>? {
        if (tType == JSON::class.java) {
            return Converter.ofNullable(tType as Class<T>, uType,
                { t: T ->
                    try {
                        return@ofNullable objectMapper.readValue((t as JSON).data(), uType)
                    } catch (e: Exception) {
                        throw DataTypeException("JSON mapping error", e)
                    }
                },
                { u: U ->
                    try {
                        val w = StringWriter()
                        val g = JsonFactory().createGenerator(w)
                        objectMapper.writeValue(g, u)
                        return@ofNullable JSON.valueOf(w.toString()) as T
                    } catch (e: Exception) {
                        throw DataTypeException("JSON mapping error", e)
                    }
                }
            )
        } else
            return delegate.provide(tType, uType)
    }
}
