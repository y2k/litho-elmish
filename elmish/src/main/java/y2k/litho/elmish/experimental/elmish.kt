package y2k.litho.elmish.experimental

import android.content.Context
import com.facebook.litho.ComponentContext
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

interface Cmd<out T> {

    suspend fun handle(ctx: ComponentContext): T?

    companion object {
        fun <T> fromSuspend(f: suspend () -> Unit): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: ComponentContext): T? {
                    f()
                    return null
                }
            }

        fun <T> fromSuspend_(f: suspend (Context) -> Unit): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: ComponentContext): T? {
                    f(ctx)
                    return null
                }
            }

        /**
         * batch : List (Cmd msg) -> Cmd msg
         */
        fun <T> batch(vararg commands: Cmd<T>): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: ComponentContext): T? {
                    var last: T? = null
                    for (cmd in commands) {
                        last = cmd.handle(ctx)
                    }
                    return last
                }
            }

        fun <T> none(): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: ComponentContext): T? = null
            }

        fun <R, T> fromSuspend(f: suspend () -> R, fOk: (R) -> T): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: ComponentContext): T = fOk(f())
            }

        fun <R, T> fromSuspend_(f: suspend (Context) -> R, fOk: (R) -> T): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: ComponentContext): T = fOk(f(ctx))
            }

        fun <R, T> fromSuspend(f: suspend () -> R, fOk: (R) -> T, fError: () -> T): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: ComponentContext): T =
                    try {
                        fOk(f())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        fError()
                    }
            }

        fun <T, R> fromContext(f: ComponentContext.() -> R, fOk: (R) -> T): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: ComponentContext): T? = fOk(ctx.f())
            }

        fun <T> fromContext(f: ComponentContext.() -> Unit): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(ctx: ComponentContext): T? {
                    ctx.f()
                    return null
                }
            }
    }
}

object Elmish {
    fun <TMsg, TModel> handle(ctx: ComponentContext,
                              init: () -> Pair<TModel, Cmd<TMsg>>,
                              update: (TModel, TMsg) -> Pair<TModel, Cmd<TMsg>>,
                              reload: (TModel) -> Unit) = launch(UI) {
        val (model, cmd) = init()

        reload(model)

        val msg = cmd.handle(ctx) ?: return@launch
        val (model2, cmd2) = update(model, msg)
        reload(model2)

        val msg2 = cmd2.handle(ctx) ?: return@launch
        val (model3, _) = update(model2, msg2)
        reload(model3)
    }
}