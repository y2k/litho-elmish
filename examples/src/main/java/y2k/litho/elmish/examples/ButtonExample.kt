package y2k.litho.elmish.examples

import com.facebook.litho.ComponentLayout.ContainerBuilder
import y2k.litho.elmish.examples.ButtonExample.Msg
import y2k.litho.elmish.examples.ButtonExample.Msg.Decrease
import y2k.litho.elmish.examples.ButtonExample.Msg.Increase
import y2k.litho.elmish.examples.common.Styles
import y2k.litho.elmish.experimental.*

/**
 * Elm origin: http://elm-lang.org/examples/buttons
 */
class ButtonExample : ElmFunctions<Int, Msg> {

    enum class Msg { Increase, Decrease }

    override fun init(): Pair<Int, Cmd<Msg>> = 0 to Cmd.none()

    override fun update(model: Int, msg: Msg): Pair<Int, Cmd<Msg>> = when (msg) {
        Increase -> (model + 1) to Cmd.none()
        Decrease -> (model - 1) to Cmd.none()
    }

    override fun ContainerBuilder.view(model: Int) {
        text {
            style(Styles::label)
            text("-")
            onClick(Decrease)
        }
        text {
            style(Styles::label)
            text("$model")
        }
        text {
            style(Styles::label)
            text("+")
            onClick(Increase)
        }
    }
}