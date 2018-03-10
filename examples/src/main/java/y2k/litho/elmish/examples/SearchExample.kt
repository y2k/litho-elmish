package y2k.litho.elmish.examples

import com.facebook.litho.Component.ContainerBuilder
import com.facebook.yoga.YogaEdge
import kotlinx.coroutines.experimental.delay
import y2k.litho.elmish.examples.SearchExample.Model
import y2k.litho.elmish.examples.SearchExample.Msg
import y2k.litho.elmish.examples.SearchExample.Msg.*
import y2k.litho.elmish.examples.Services.SearchEngine
import y2k.litho.elmish.examples.Services.SearchEngine.Google
import y2k.litho.elmish.examples.Services.SearchEngine.Yandex
import y2k.litho.elmish.examples.common.button
import y2k.litho.elmish.examples.common.editTextWithLabel
import y2k.litho.elmish.examples.common.fullscreenProgress
import y2k.litho.elmish.experimental.Cmd
import y2k.litho.elmish.experimental.ElmFunctions
import y2k.litho.elmish.experimental.column
import y2k.litho.elmish.experimental.text

class SearchExample : ElmFunctions<Model, Msg> {

    data class Model(
        val query: String = "",
        val isFlight: Boolean = false,
        val searchResults: List<String> = emptyList(),
        val error: String? = null)

    sealed class Msg {
        class QueryChanged(val query: String) : Msg()
        class SearchRequest(val engine: SearchEngine) : Msg()
        class SearchSuccess(val result: List<String>) : Msg()
        class SearchFailed(val result: Exception) : Msg()
    }

    override fun init(): Pair<Model, Cmd<Msg>> = Model() to Cmd.none()

    override fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> =
        when (msg) {
            is QueryChanged ->
                model.copy(query = msg.query) to Cmd.none()
            is SearchRequest ->
                model.copy(isFlight = true) to Cmd.fromContext(
                    { Services.search(model.query, msg.engine) }, ::SearchSuccess, ::SearchFailed)
            is SearchSuccess ->
                model.copy(isFlight = false, searchResults = msg.result, error = null) to Cmd.none()
            is SearchFailed ->
                model.copy(isFlight = false, error = msg.result.message) to Cmd.none()
        }

    override fun ContainerBuilder<*>.view(model: Model) {
        column {
            paddingDip(YogaEdge.ALL, 4f)

            editTextWithLabel(
                hint = "Search query",
                msg = ::QueryChanged,
                error = model.error)

            button(
                title = "Search in Google",
                msg = SearchRequest(Google))
            button(
                title = "Search in Yandex",
                msg = SearchRequest(Yandex))

            text {
                textSizeSp(20f)
                text(model.searchResults.joinToString(separator = "\n"))
            }
        }

        if (model.isFlight)
            fullscreenProgress()
    }
}

object Services {

    enum class SearchEngine { Google, Yandex }

    suspend fun search(query: String, engine: SearchEngine): List<String> {
        delay((500 + Math.random() * 1500).toLong())
        // XXX: в 30% случаев падаем с исключением
        if (Math.random() < 0.3) throw Exception("Network Error (test)")
        // XXX: фейковые данные
        return List((Math.random() * 16).toInt()) {
            "$engine ($query) #" + (10000 * Math.random()).toInt()
        }
    }
}