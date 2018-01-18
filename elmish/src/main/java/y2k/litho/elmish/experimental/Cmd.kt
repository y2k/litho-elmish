package y2k.litho.elmish.experimental

import android.content.Context
import kotlinx.types.Result
import kotlinx.types.Result.Error
import kotlinx.types.Result.Ok

interface Cmd<out T> {

    suspend fun handle(ctx: Context): T?

    companion object {

        fun <T> fromSuspend(f: suspend () -> Unit): Cmd<T> =
            object : Cmd<T> {
                override suspend fun handle(ctx: Context): T? {
                    f()
                    return null
                }
            }

        fun <T> fromContext(f: suspend (Context) -> Unit): Cmd<T> =
            object : Cmd<T> {
                override suspend fun handle(ctx: Context): T? {
                    f(ctx)
                    return null
                }
            }

        /**
         * batch : List (Cmd msg) -> Cmd msg
         */
        fun <T> batch(vararg commands: Cmd<T>): Cmd<T> =
            object : Cmd<T> {
                override suspend fun handle(ctx: Context): T? {
                    var last: T? = null
                    for (cmd in commands) {
                        last = cmd.handle(ctx)
                    }
                    return last
                }
            }

        private val instanceOfNone = object : Cmd<Nothing> {
            override suspend fun handle(ctx: Context): Nothing? = null
        }

        fun <T> none(): Cmd<T> = instanceOfNone

        fun <R, T> fromContext(f: suspend (Context) -> R, callback: (Result<R, Exception>) -> T): Cmd<T> =
            object : Cmd<T> {
                override suspend fun handle(ctx: Context): T =
                    try {
                        callback(Ok(f(ctx)))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback(Error(e))
                    }
            }

        fun <R, T> fromContext(f: suspend (Context) -> R, fOk: (R) -> T, fError: (Exception) -> T): Cmd<T> =
            object : Cmd<T> {
                override suspend fun handle(ctx: Context): T =
                    try {
                        fOk(f(ctx))
                    } catch (e: Exception) {
                        fError(e)
                    }
            }

        fun <R, T> fromSuspend(f: suspend () -> R, fOk: (R) -> T, fError: (Exception) -> T): Cmd<T> =
            object : Cmd<T> {
                override suspend fun handle(ctx: Context): T =
                    try {
                        fOk(f())
                    } catch (e: Exception) {
                        fError(e)
                    }
            }
    }
}