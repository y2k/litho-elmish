package com.example.examples

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.examples.MainScreen.Msg.Decrease
import com.example.examples.MainScreen.Msg.Increase
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.Text
import y2k.litho.elmish.*

fun Text.Builder.click(msg: Any) {

//    this.clickHandler(MainPage.onItemClicked(null, Increase))

    TODO()
}

object MainScreen {

    data class Model(var count: Int)
    sealed class Msg {
        object Increase : Msg()
        object Decrease : Msg()
    }

    fun init(): Pair<Model, Cmd<Msg>> = Model(0) to Cmd.none()
    fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> =
        when (msg) {
            Increase -> model.copy(count = model.count + 1) to Cmd.none()
            Decrease -> model.copy(count = model.count - 1) to Cmd.none()
        }

    fun view(model: Model): LazyComponent =
        columnL {
            childLayout(
                textL {
                    text("+")
//                    clickHandler(MainPage.onItemClicked(null, Increase))
                    click(Increase)
                }
            )
            childLayout(
                textL { text("${model.count}") }
            )
            childLayout(
                textL {
                    text("-")
//                    clickHandler(MainPage.onItemClicked(null, Decrease))
                    click(Decrease)
                }
            )
        }
}

@LayoutSpec
class MainPageSpec {
    companion object {

        @OnCreateInitialState
        @JvmStatic
        fun createInitialState(c: ComponentContext, state: StateValue<MainScreen.Model>) {
            state.set(MainScreen.init().first)
            Elmish.handle(
                { MainScreen.init() },
                { model, msg -> MainScreen.update(model, msg) },
                { MainPage.reload(c, it) })
        }

        @OnCreateLayout
        @JvmStatic
        fun onCreateLayout(c: ComponentContext, @State state: MainScreen.Model): ComponentLayout? =
            MainScreen.view(state).invoke(c).build()

        @OnUpdateState
        @JvmStatic
        fun reload(state: StateValue<MainScreen.Model>, @Param newState: MainScreen.Model) =
            state.set(newState)

        @OnEvent(ClickEvent::class)
        @JvmStatic
        fun onItemClicked(c: ComponentContext, @Param item: MainScreen.Msg): Unit = TODO()
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = ComponentContext(this)
        val component = MainPage.create(context).build()
        setContentView(LithoView.create(context, component))
    }
}