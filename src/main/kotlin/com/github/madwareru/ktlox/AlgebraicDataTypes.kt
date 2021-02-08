package com.github.madwareru.ktlox

sealed class Option<out T> {
    data class Some<out T>(val value: T) : Option<T>()
    class None<out T>: Option<T>()
}

inline fun<T> some(x: () -> T) = Option.Some(x())
fun<T> none() = Option.None<T>()

val<T> Option<T>.isNone get() = this is Option.None<T>
inline fun<T0, T1>
    Option<T0>.map(mapper: (T0) -> T1): Option<T1> =
    when (this) {
        is Option.None -> none()
        is Option.Some -> some { mapper(this.value) }
    }

inline fun<T0, T1>
    Option<T0>.flatMap(mapper: (T0) -> Option<T1>) =
    when (this) {
        is Option.None -> none()
        is Option.Some -> mapper(this.value)
    }

fun<T, TErrorReason> Option<T>.castToErr(
    errorReasonOnNone: TErrorReason
): Result<T, TErrorReason> =
    when (this) {
        is Option.None -> err { errorReasonOnNone }
        is Option.Some -> ok { this.value }
    }

fun <T> Option<T>.unwrap() = (this as Option.Some).value

sealed class Result<out T, TErrorReason> {
    data class Ok<out T, TErrorReason>(val value: T) : Result<T, TErrorReason>()
    data class Err<out T, TErrorReason>(val reason: TErrorReason) : Result<T, TErrorReason>()
}

inline fun<T, TErrorReason> ok(x: () -> T) = Result.Ok<T, TErrorReason>(x())
inline fun<T, TErrorReason> err(x: () -> TErrorReason) = Result.Err<T, TErrorReason>(x())

val<T, TErrorReason> Result<T, TErrorReason>.isErr get() = this is Result.Err<T, TErrorReason>
inline fun<T0, T1, TErrorReason>
    Result<T0, TErrorReason>.map(mapper: (T0) -> T1): Result<T1, TErrorReason> =
    when (this) {
        is Result.Err -> err { this.reason }
        is Result.Ok -> ok { mapper(this.value) }
    }

inline fun<T0, T1, TErrorReason>
    Result<T0, TErrorReason>.flatMap(mapper: (T0) -> Result<T1, TErrorReason>): Result<T1, TErrorReason> =
    when (this) {
        is Result.Err -> err { this.reason }
        is Result.Ok -> mapper(this.value)
    }

fun <T, TErrorReason> Result<T, TErrorReason>.unwrap() = (this as Result.Ok).value