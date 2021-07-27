package jackson

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod

object DoNotIgnoreIs : PropertyNamingStrategy() {
    override fun nameForGetterMethod(
        config: MapperConfig<*>?,
        method: AnnotatedMethod,
        defaultName: String?
    ): String? {
        return if (method.hasReturnType() && (method.rawReturnType === Boolean::class.java || method.rawReturnType === Boolean::class.javaPrimitiveType)
            && method.name.startsWith("is")
        ) {
            method.name
        } else super.nameForGetterMethod(config, method, defaultName)
    }
}
