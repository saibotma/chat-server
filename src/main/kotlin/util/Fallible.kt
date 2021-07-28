package util

sealed class Fallible<L, R> {
    data class Failure<L, R>(val failure: L) : Fallible<L, R>()
    data class Success<L, R>(val success: R) : Fallible<L, R>()

    fun isFailure(): Boolean {
        return this is Failure
    }

    fun isSuccess(): Boolean {
        return this is Success
    }

    inline fun <NL> onFailure(block: L.() -> NL): Fallible<NL, R> {
        return when (this) {
            is Failure -> Failure(block(failure))
            is Success -> Success(success)
        }
    }

    inline fun <T> fold(ifFailure: (L) -> T, ifSuccess: (R) -> T): T {
        return when (this) {
            is Failure -> ifFailure(this.failure)
            is Success -> ifSuccess(this.success)
        }
    }
}

fun <L, R> R.success(): Fallible<L, R> {
    return Fallible.Success(this)
}

fun <L, R> L.failure(): Fallible<L, R> {
    return Fallible.Failure(this)
}
