package y2k.litho.elmish.examples

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import y2k.litho.elmish.examples.EditExample.Msg
import y2k.litho.elmish.experimental.*
import y2k.litho.elmish.experimental.Views.column

/**
 * Elm origin: http://elm-lang.org/examples/field
 */
object EditExample : ElmFunctions<String, Msg> {

    class Msg(val value: String)

    override fun init(): Pair<String, Cmd<Msg>> =
        "" to Cmd.none()

    override fun update(model: String, msg: Msg): Pair<String, Cmd<Msg>> =
        msg.value to Cmd.none()

    override fun view(model: String) =
        column {
            editText {
                hint("Text to reverse")
                textSizeSp(30f)
                onTextChanged(::Msg)
            }
            text {
                text(model)
                textSizeSp(30f)
            }
        }
}

class EditExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        program(EditExample)
    }
}