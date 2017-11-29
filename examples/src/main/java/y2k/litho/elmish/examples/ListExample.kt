package y2k.litho.elmish.examples

import android.app.Activity
import android.os.Bundle
import kotlinx.coroutines.experimental.delay
import y2k.litho.elmish.examples.ListExampleScreen.Model
import y2k.litho.elmish.examples.ListExampleScreen.Msg
import y2k.litho.elmish.examples.ListExampleScreen.Msg.*
import y2k.litho.elmish.experimental.*
import java.util.*

object ListExampleScreen : ElmFunctions<Model, Msg> {
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
            children(
                column {
                    backgroundRes(android.R.drawable.btn_default)
                    onClick(UpdateMsg)
                    childText {
                        text("UPDATED (${model.rnd})")
                        textSizeSp(30f)
                    }
                },
                recyclerView {
                    binder(model.binder)
                })
        }

    private fun viewItem(ignore: Unit) =
        column {
            onClick(UpdateMsg)
            backgroundRes(android.R.drawable.btn_default)
            child(
                text {
                    text("[BUTTON]")
                    textSizeSp(30f)
                })
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

class ListExampleActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        program(ListExampleScreen)
    }
}