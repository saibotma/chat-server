package util

open class Optional<T>(val value: T) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Optional<*>

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }
}

fun <T> T.toOptional() = Optional(this)

fun <T> Optional<T>?.fallbackTo(fallback: T): T {
    if (this == null) return fallback
    return this.value
}

fun <In, Out> Optional<In>.map(block: (In) -> Out): Optional<Out> {
    return block(value).toOptional()
}
