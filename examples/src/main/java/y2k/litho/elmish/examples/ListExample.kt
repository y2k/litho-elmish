package y2k.litho.elmish.examples

import com.facebook.litho.Component.ContainerBuilder
import kotlinx.coroutines.experimental.delay
import y2k.litho.elmish.examples.ListExample.Model
import y2k.litho.elmish.examples.ListExample.Msg
import y2k.litho.elmish.examples.ListExample.Msg.*
import y2k.litho.elmish.examples.common.Log.log
import y2k.litho.elmish.examples.common.Styles
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
        class ErrorMsg(val e: Exception) : Msg()
    }

    override fun init(): Pair<Model, Cmd<Msg>> {
        val binder = ContextualRecyclerBinder(::viewItem, Service.notComparableStub)
        return Model(0, binder) to Cmd.fromSuspend({ Service.loadData() }, ::ItemsMsg, ::ErrorMsg)
    }

    override fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> = when (msg) {
        UpdateMsg -> model to Cmd.fromSuspend({ Service.getNow() }, ::NowUpdatedMsg, ::ErrorMsg)
        is NowUpdatedMsg -> model.copy(rnd = msg.rnd) to Cmd.none()
        is ItemsMsg -> model.copy(binder = model.binder.copy(msg.xs)) to Cmd.none()
        is ErrorMsg -> log(msg.e, model) to Cmd.none()
    }

    override fun ContainerBuilder<*>.view(model: Model) {
        text {
            style(Styles::label)
            text("[${model.rnd}]")
            onClick(UpdateMsg)
        }
        recyclerView {
            binder(model.binder)
        }
    }

    private fun viewItem(ignore: Unit) =
        column {
            text {
                style(Styles::label)
                text("[BUTTON]")
                onClick(UpdateMsg)
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