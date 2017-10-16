package com.example.examples

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.examples.Example1Screen.Msg.Decrease
import com.example.examples.Example1Screen.Msg.Increase
import y2k.litho.elmish.*

object Example1Screen : ElmFunctions<Int, Example1Screen.Msg> {

    enum class Msg { Increase, Decrease }

    override fun init(): P<Int, Msg> = 0 to Cmd.none()

    override fun update(model: Int, msg: Msg): P<Int, Msg> = when (msg) {
        Increase -> (model + 1) to Cmd.none()
        Decrease -> (model - 1) to Cmd.none()
    }

    override fun view(model: Int) =
        column {
            childText { layout ->
                text("-")
                textSizeSp(45f)
                onClick(layout, Decrease)
            }
            childText {
                text("$model")
                textSizeSp(45f)
            }
            childText { layout ->
                text("+")
                textSizeSp(45f)
                onClick(layout, Increase)
            }
        }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        program(Example1Screen)
    }
}