package y2k.litho.elmish.examples

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import y2k.litho.elmish.examples.Domain.WAITING_GIF
import y2k.litho.elmish.examples.Domain.getRandomGif
import y2k.litho.elmish.examples.HttpExample.Model
import y2k.litho.elmish.examples.HttpExample.Msg
import y2k.litho.elmish.examples.HttpExample.Msg.MorePlease
import y2k.litho.elmish.examples.HttpExample.Msg.NewGif
import y2k.litho.elmish.examples.common.*
import y2k.litho.elmish.experimental.*

/**
 * Elm origin: http://elm-lang.org/examples/http
 */
object HttpExample : ElmFunctions<Model, Msg> {

    data class Model(val topic: String, val gifUrl: String)
    sealed class Msg {
        object MorePlease : Msg()
        class NewGif(val result: Result<String, String>) : Msg()
    }

    override fun init(): Pair<Model, Cmd<Msg>> {
        val topic = "cats"
        return Model(topic, WAITING_GIF) to getRandomGif(topic)
    }

    override fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> = when (msg) {
        MorePlease -> model to getRandomGif(model.topic)
        is NewGif -> when (msg.result) {
            is Ok -> model.copy(gifUrl = msg.result.value) to Cmd.none()
            is Error -> model to Cmd.none()
        }
    }

    override fun view(model: Model) =
        column {
            childText {
                text(model.topic)
                textSizeSp(40f)
            }
            childFresco {
                fresco {
                    setUri(model.gifUrl)
                    autoPlayAnimations = true
                }
            }
            childText { layout ->
                text("More Please!")
                textSizeSp(30f)
                onClick(layout, MorePlease)
            }
        }
}

private object Domain {

    fun getRandomGif(topic: String): Cmd<Msg> {
        val url = "https://api.giphy.com/v1/gifs/random?api_key=dc6zaTOxFJmzC&tag=$topic"
        return Http.send(::NewGif, Http.get(url, decodeGifUrl()))
    }

    fun decodeGifUrl() =
        Decode.at(listOf("data", "image_url"), Decode.string())

    const val WAITING_GIF = "https://camo.githubusercontent.com/6ed028acbf67707d622344e0ef1bc3b098425b50/687474703a2f2f662e636c2e6c792f6974656d732f32473146315a304d306b306832553356317033392f535650726f67726573734855442e676966"
}

class HttpExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        program(HttpExample)
    }
}