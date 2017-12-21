package y2k.litho.elmish.examples

import com.facebook.litho.ComponentLayout.ContainerBuilder
import y2k.litho.elmish.examples.ButtonExample.Msg
import y2k.litho.elmish.examples.ButtonExample.Msg.Decrease
import y2k.litho.elmish.examples.ButtonExample.Msg.Increase
import y2k.litho.elmish.experimental.Cmd
import y2k.litho.elmish.experimental.ElmFunctions
import y2k.litho.elmish.experimental.onClick
import y2k.litho.elmish.experimental.text

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
            text("-")
            textSizeSp(45f)
            onClick(Decrease)
        }
        text {
            text("$model")
            textSizeSp(45f)
        }
        text {
            text("+")
            textSizeSp(45f)
            onClick(Increase)
        }
    }
}