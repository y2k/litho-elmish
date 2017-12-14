package y2k.litho.elmish.experimental

import android.app.Activity
import com.facebook.litho.ClickEvent
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.LithoView
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.TextChangedEvent

fun <TModel, TMsg> Activity.program(functions: ElmFunctions<TModel, TMsg>) {
    val handler = LifecycleHandler(functions)

    val context = ComponentContext(MyContext(this, handler))
    handler.appContext = context

    val component = ElmishApplication.create(context).build()
    setContentView(LithoView.create(context, component))
}

interface ElmFunctions<TModel, TMsg> {
    fun init(): Pair<TModel, Cmd<TMsg>>
    fun update(model: TModel, msg: TMsg): Pair<TModel, Cmd<TMsg>>
    fun view(model: TModel): Contextual<ComponentLayout.Builder>
    fun subscriptions(model: TModel): Sub<TMsg> = Sub.none()
}

@LayoutSpec
object ElmishApplicationSpec {

    @OnCreateLayout
    @JvmStatic
    fun onCreateLayout(c: ComponentContext): ComponentLayout? =
        c.sharedLifecycleHandler.onCreateLayout(c)

    @OnUpdateState
    @JvmStatic
    fun reload() = Unit

    @OnEvent(ClickEvent::class)
    @JvmStatic
    fun onEventHandle(c: ComponentContext, @Param msg: Any) =
        c.sharedLifecycleHandler.onEventHandle(msg)

    @OnEvent(TextChangedEvent::class)
    @JvmStatic
    fun onTextChanged(c: ComponentContext, @FromEvent text: String, @Param msgFactory: (String) -> Any) =
        c.sharedLifecycleHandler.onTextChanged(text, msgFactory)
}