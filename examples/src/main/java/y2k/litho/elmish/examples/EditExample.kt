package y2k.litho.elmish.examples

import com.facebook.litho.Component.ContainerBuilder
import y2k.litho.elmish.examples.EditExample.Msg
import y2k.litho.elmish.experimental.*

/**
 * Elm origin: http://elm-lang.org/examples/field
 */
class EditExample : ElmFunctions<String, Msg> {

    class Msg(val value: String)

    override fun init(): Pair<String, Cmd<Msg>> =
        "" to Cmd.none()

    override fun update(model: String, msg: Msg): Pair<String, Cmd<Msg>> =
        msg.value to Cmd.none()

    override fun ContainerBuilder<*>.view(model: String) {
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