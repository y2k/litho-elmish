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
                suspend override fun handle(ctx: Context): T? {
                    f()
                    return null
                }
            }

        fun <T> fromContext(f: suspend (Context) -> Unit): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: Context): T? {
                    f(ctx)
                    return null
                }
            }

        /**
         * batch : List (Cmd msg) -> Cmd msg
         */
        fun <T> batch(vararg commands: Cmd<T>): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: Context): T? {
                    var last: T? = null
                    for (cmd in commands) {
                        last = cmd.handle(ctx)
                    }
                    return last
                }
            }

        fun <T> none(): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: Context): T? = null
            }

        @Deprecated("")
        fun <R, T> fromSuspend(f: suspend () -> R, fOk: (R) -> T): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: Context): T = fOk(f())
            }

        fun <R, T> fromContext(f: suspend (Context) -> R, callback: (Result<R, Exception>) -> T): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: Context): T =
                    try {
                        callback(Ok(f(ctx)))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback(Error(e))
                    }
            }

        fun <R, T> fromSuspend(f: suspend () -> R, fOk: (R) -> T, fError: () -> T): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: Context): T =
                    try {
                        fOk(f())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        fError()
                    }
            }
    }
}