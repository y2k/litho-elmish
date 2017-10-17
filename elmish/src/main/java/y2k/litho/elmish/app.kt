package y2k.litho.elmish

import android.app.Activity
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.TextChangedEvent
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

fun <TModel, TMsg> Activity.program(functions: ElmFunctions<TModel, TMsg>) {
    @Suppress("UNCHECKED_CAST")
    val provider = ElmProvider(
        functions as ElmFunctions<Any, Any>,
        functions.init().first)

    val context = ComponentContext(this)
    val component = ElmishApplication.create(context).provider(provider).build()
    setContentView(LithoView.create(context, component))
}

class ElmProvider(val functions: ElmFunctions<Any, Any>, var subModel: Any)

interface ElmFunctions<TModel, TMsg> {
    fun init(): Pair<TModel, Cmd<TMsg>>
    fun update(model: TModel, msg: TMsg): Pair<TModel, Cmd<TMsg>>
    fun view(model: TModel): Contextual<ComponentLayout.Builder>
}

@LayoutSpec
class ElmishApplicationSpec {
    companion object {

        @OnCreateInitialState
        @JvmStatic
        fun createInitialState(c: ComponentContext, model: StateValue<ElmProvider>, @Prop provider: ElmProvider) {
            model.set(provider)
            Elmish.handle(
                { provider.functions.init() },
                { newModel, msg -> provider.functions.update(newModel, msg) },
                { ElmishApplication.reload(c, it) })
        }

        @OnCreateLayout
        @JvmStatic
        fun onCreateLayout(c: ComponentContext, @State model: ElmProvider): ComponentLayout? {
            return model.functions.view(model.subModel).invoke(c).build()
        }

        @OnUpdateState
        @JvmStatic
        fun reload(model: StateValue<ElmProvider>, @Param subModel: Any) {
            val oldModel = model.get()
            oldModel.subModel = subModel
        }

        @OnEvent(ClickEvent::class)
        @JvmStatic
        fun onEventHandle(c: ComponentContext, @Param msg: Any, @State model: ElmProvider) {
            launch(UI) {
                val (model2, cmd2) = model.functions.update(model.subModel, msg)
                ElmishApplication.reload(c, model2)

                val msg2 = cmd2.handle() ?: return@launch
                val (model3, _) = model.functions.update(model.subModel, msg2)
                ElmishApplication.reload(c, model3)
            }
        }

        @OnEvent(TextChangedEvent::class)
        @JvmStatic
        fun onTextChanged(c: ComponentContext, @FromEvent text: String, @Param msgFactory: (String) -> Any, @State model: ElmProvider) {
            launch(UI) {
                val msg = msgFactory(text)

                val (model2, cmd2) = model.functions.update(model.subModel, msg)
                ElmishApplication.reload(c, model2)

                val msg2 = cmd2.handle() ?: return@launch
                val (model3, _) = model.functions.update(model.subModel, msg2)
                ElmishApplication.reload(c, model3)
            }
        }
    }
}