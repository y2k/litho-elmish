package y2k.litho.elmish.examples

import kotlinx.coroutines.experimental.delay
import y2k.litho.elmish.examples.ListExample.Model
import y2k.litho.elmish.examples.ListExample.Msg
import y2k.litho.elmish.examples.ListExample.Msg.*
import y2k.litho.elmish.experimental.*
import y2k.litho.elmish.experimental.Views.column
import java.util.*

class ListExample : ElmFunctions<Model, Msg> {
    data class Model(
        val rnd: Long,
        val binder: ContextualRecyclerBinder<Unit>)

    sealed class Msg {
        object UpdateMsg : Msg()
        class NowUpdatedMsg(val rnd: Long) : Msg()
        class ItemsMsg(val xs: List<Unit>) : Msg()
    }

    override fun init(): Pair<Model, Cmd<Msg>> {
        val binder = ContextualRecyclerBinder(::viewItem, Service.notComparableStub)
        return Model(0, binder) to Cmd.fromSuspend({ Service.loadData() }, ::ItemsMsg)
    }

    override fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> = when (msg) {
        UpdateMsg -> model to Cmd.fromSuspend({ Service.getNow() }, ::NowUpdatedMsg)
        is NowUpdatedMsg -> model.copy(rnd = msg.rnd) to Cmd.none()
        is ItemsMsg -> model.copy(binder = model.binder.copy(msg.xs)) to Cmd.none()
    }

    override fun view(model: Model) =
        column {
            column {
                backgroundRes(android.R.drawable.btn_default)
                onClick(UpdateMsg)

                text {
                    text("UPDATED (${model.rnd})")
                    textSizeSp(30f)
                }
            }
            recyclerView {
                binder(model.binder)
            }
        }

    private fun viewItem(ignore: Unit) =
        column {
            backgroundRes(android.R.drawable.btn_default)
            onClick(UpdateMsg)

            text {
                text("[BUTTON]")
                textSizeSp(30f)
            }
        }
}

private object Service {

    val notComparableStub: (Any, Any) -> Boolean = { _, _ -> false }

    suspend fun getNow(): Long {
        delay(300)
        return Random().nextInt().toLong()
    }

    suspend fun loadData(): List<Unit> {
        delay(300)
        return List(30) { Unit }
    }
}