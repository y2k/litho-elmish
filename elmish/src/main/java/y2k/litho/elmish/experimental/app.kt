package y2k.litho.elmish.experimental

import android.app.Activity
import android.os.Bundle
import com.facebook.litho.ClickEvent
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.LithoView
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.TextChangedEvent

@Deprecated("")
fun <TModel, TMsg> Activity.program(functions: ElmFunctions<TModel, TMsg>) {
    val handler = LifecycleHandler(functions, this)
    val context = ComponentContext(MyContext(this, handler))
    val component = ElmishApplication.create(context).build()
    setContentView(LithoView.create(context, component))
}

inline fun <reified T : ElmFunctions<*, *>> Activity.program() =
    program(T::class.java)

fun <T : ElmFunctions<*, *>> Activity.program(clazz: Class<T>) {
    if (fragmentManager.findFragmentByTag("main") == null) {
        fragmentManager
            .beginTransaction()
            .add(android.R.id.content, LCFragment().apply {
                arguments = Bundle().apply { putSerializable("f", clazz) }
            }, "main")
            .commit()
    }
}

interface ElmFunctions<TModel, TMsg> {
    fun init(): Pair<TModel, Cmd<TMsg>>
    fun update(model: TModel, msg: TMsg): Pair<TModel, Cmd<TMsg>>
    fun ComponentLayout.ContainerBuilder.view(model: TModel)
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