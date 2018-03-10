package y2k.litho.elmish.examples

import com.facebook.litho.Component
import y2k.litho.elmish.examples.WebSocketExample.Model
import y2k.litho.elmish.examples.WebSocketExample.Msg
import y2k.litho.elmish.examples.WebSocketExample.Msg.*
import y2k.litho.elmish.examples.common.WebSocket
import y2k.litho.elmish.experimental.*
import java.net.URL

private val echoServer = URL("wss://echo.websocket.org")

/**
 * http://elm-lang.org/examples/websockets
 */
class WebSocketExample : ElmFunctions<Model, Msg> {

    data class Model(
        val input: String = "",
        val messages: List<String> = emptyList())

    sealed class Msg {
        class Input(val input: String) : Msg()
        object Send : Msg()
        class NewMessage(val message: String) : Msg()
    }

    override fun subscriptions(model: Model): Sub<Msg> =
        WebSocket.listen(echoServer, ::NewMessage)

    override fun init(): Pair<Model, Cmd<Msg>> =
        Model() to Cmd.none()

    override fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> =
        when (msg) {
            is Input ->
                model.copy(input = msg.input) to Cmd.none()
            Send ->
                model.copy(input = "") to WebSocket.send(echoServer, model.input)
            is NewMessage ->
                model.copy(messages = listOf("" + msg.message) + model.messages) to Cmd.none()
        }

    override fun Component.ContainerBuilder<*>.view(model: Model) {
        editText {
            textSizeSp(20f)
            text(model.input)
            isSingleLine(true)
            onTextChanged(::Input)
        }

        column {
            backgroundRes(R.drawable.button_bg)
            widthDip(60f)
            heightDip(60f)
            onClick(Send)
        }

        model.messages
            .reversed()
            .map { viewMessage(it) }
    }

    private fun Component.ContainerBuilder<*>.viewMessage(text: String) =
        text {
            text(text)
            textSizeSp(20f)
        }
}