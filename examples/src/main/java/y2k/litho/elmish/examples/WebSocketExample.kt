package y2k.litho.elmish.examples

import com.facebook.litho.ComponentLayout.ContainerBuilder
import y2k.litho.elmish.examples.WebSocketExample.Model
import y2k.litho.elmish.examples.WebSocketExample.Msg
import y2k.litho.elmish.examples.WebSocketExample.Msg.*
import y2k.litho.elmish.examples.common.Time
import y2k.litho.elmish.examples.common.WebSocket
import y2k.litho.elmish.experimental.*
import y2k.litho.elmish.experimental.Views.column

private val echoServer = "wss://echo.websocket.org"

/**
 * http://elm-lang.org/examples/websockets
 */
class WebSocketExample : ElmFunctionsWithSubscription<Model, Msg>() {
    data class Model(
        val input: String = "",
        val messages: List<String> = emptyList())

    sealed class Msg {
        class Input(val input: String) : Msg()
        object Send : Msg()
        class NewMessage(val message: Long) : Msg()
    }

    override fun subscriptions(model: Model): Sub<Msg> =
        Time.every(1000, ::NewMessage)

    override fun init(): Pair<Model, Cmd<Msg>> =
        Model() to Cmd.none()

    override fun update2(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> =
        when (msg) {
            is Input ->
                model.copy(input = msg.input) to Cmd.none()
            Send ->
                model.copy(input = "") to WebSocket.send(echoServer, model.input)
            is NewMessage ->
                model.copy(messages = listOf("" + msg.message) + model.messages) to Cmd.none()
        }

    override fun view(model: Model) =
        column {
            editText {
                textSizeSp(20f)
                text(model.input)
                onTextChanged(::Input)
            }

            text {
                text("Send")
                textSizeSp(20f)
                onClick(Send)
            }

            model.messages
                .reversed()
                .map { viewMessage(it) }
        }

    private fun ContainerBuilder.viewMessage(text: String) =
        text {
            text(text)
            textSizeSp(20f)
        }
}