package y2k.litho.elmish

import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

interface Cmd<out T> {

    suspend fun handle(): T?

    companion object {
        fun <T> none(): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(): T? = null
            }

        fun <R, T> fromSuspend(f: suspend () -> R, fOk: (R) -> T): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(): T = fOk(f())
            }

        fun <R, T> fromSuspend(f: suspend () -> R, fOk: (R) -> T, fError: () -> T): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(): T =
                    try {
                        fOk(f())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        fError()
                    }
            }
    }
}

object Elmish {
    fun <TMsg, TModel> handle(init: () -> Pair<TModel, Cmd<TMsg>>,
                              update: (TModel, TMsg) -> Pair<TModel, Cmd<TMsg>>,
                              reload: (TModel) -> Unit) = launch(UI) {
        val (model, cmd) = init()

        reload(model)

        val msg = cmd.handle() ?: return@launch
        val (model2, cmd2) = update(model, msg)
        reload(model2)

        val msg2 = cmd2.handle() ?: return@launch
        val (model3, _) = update(model2, msg2)
        reload(model3)
    }
}