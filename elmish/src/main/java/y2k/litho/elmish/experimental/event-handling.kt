package y2k.litho.elmish.experimental

import android.content.Context
import android.content.ContextWrapper
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

object EventHandler {

    fun sendClick(msg: Any, builder: ComponentLayout.Builder, ctx: ComponentContext) {
        val handler = when (ctx.componentScope) {
            is ElmishApplication -> ElmishApplication.onEventHandle(ctx, msg)
            is ElmishItemComponent -> ElmishItemComponent.onItemClicked(ctx, msg)
            else -> error("Unsupported component <${ctx.componentScope}> in layout")
        }
        builder.clickHandler(handler)
    }
}

class LifecycleHandler(
    private val functions: ElmFunctions<Any, Any>,
    private var subModel: Any) {

    private var initialized = false
    private lateinit var appContext: ComponentContext

    fun onCreateLayout(c: ComponentContext): ComponentLayout? {
        if (!initialized) {
            initialized = true
            createInitialState(c)
        }
        return functions.view(subModel).invoke(c).build()
    }

    private fun createInitialState(c: ComponentContext) {
        appContext = c
        Elmish.handle(
            c,
            functions::init,
            functions::update,
            { invalidateTree(it) })
    }

    fun onEventHandle(c: ComponentContext, msg: Any) {
        launch(UI) {
            val (model2, cmd2) = functions.update(subModel, msg)
            invalidateTree(model2)

            val msg2 = cmd2.handle(c) ?: return@launch
            val (model3, _) = functions.update(subModel, msg2)
            invalidateTree(model3)
        }
    }

    fun onTextChanged(c: ComponentContext, text: String, msgFactory: (String) -> Any) {
        launch(UI) {
            val msg = msgFactory(text)

            val (model2, cmd2) = functions.update(subModel, msg)
            invalidateTree(model2)

            val msg2 = cmd2.handle(c) ?: return@launch
            val (model3, _) = functions.update(subModel, msg2)
            invalidateTree(model3)
        }
    }

    private fun invalidateTree(newModel: Any) {
        subModel = newModel
        ElmishApplication.reload(appContext)
    }
}

class MyContext(base: Context?, val handler: LifecycleHandler) : ContextWrapper(base)

val ComponentContext.TODO_NAME: LifecycleHandler
    get() = (baseContext as MyContext).handler