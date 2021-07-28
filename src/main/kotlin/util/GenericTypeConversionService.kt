package util

import io.ktor.util.*
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Serves as a workaround because the default conversion services of the conversion feature
 * can not parse generic types.
 */
class GenericTypeConversionService(private val klass: KClass<*>) : ConversionService {
    private var decoder: ((values: List<String>, type: Type) -> Any?)? = null
    private var encoder: ((value: Any?) -> List<String>)? = null

    /**
     * Configure decoder function. Only one decoder could be supplied
     * @throws IllegalStateException
     */
    fun decode(converter: (values: List<String>, type: Type) -> Any?) {
        if (decoder != null) throw IllegalStateException("Decoder has already been set for type '$klass'")
        decoder = converter
    }

    override fun fromValues(values: List<String>, type: Type): Any? {
        val decoder = decoder ?: throw DataConversionException("Decoder was not specified for class '$klass'")
        return decoder(values, type)
    }

    override fun toValues(value: Any?): List<String> {
        val encoder = encoder ?: throw DataConversionException("Encoder was not specified for class '$klass'")
        return encoder(value)
    }
}
