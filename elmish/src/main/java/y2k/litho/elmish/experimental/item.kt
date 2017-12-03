package y2k.litho.elmish.experimental

import com.facebook.litho.ClickEvent
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.Recycler
import com.facebook.litho.widget.RecyclerBinder
import com.facebook.litho.widget.innerContext

private fun <T> createItemComponent(f: (T) -> Contextual<ComponentLayout.Builder>): (x: T) -> Contextual<Component<*>> =
    { x ->
        { context ->
            val provider = ElmishItemProvider { f(x).invoke(it).build() }
            ElmishItemComponent.create(context).item(provider).build()
        }
    }

class ElmishItemProvider(
    val func: (ComponentContext) -> ComponentLayout)

@LayoutSpec
object ElmishItemComponentSpec {

    @JvmStatic
    @OnCreateLayout
    fun onCreateLayout(c: ComponentContext, @Prop item: ElmishItemProvider): ComponentLayout =
        item.func(c)

    @OnEvent(ClickEvent::class)
    @JvmStatic
    fun onItemClicked(c: ComponentContext, @Param msg: Any) =
        c.TODO_NAME.onEventHandle(c, msg)
}

fun Recycler.Builder.binder(b: ContextualRecyclerBinder<*>) {
    binder(b.getBinder(innerContext))
}

//@Deprecated("")
//fun recyclerView(f: (@LithoElmishDslMarker Recycler.Builder).() -> Unit): Contextual<ComponentLayout.Builder> {
//    return { context ->
//        Recycler.create(context)
//            .apply(f)
//            .withLayout()
//    }
//}

class ContextualRecyclerBinder<T>(
    compFactory: (T) -> Contextual<ComponentLayout.Builder>,
    private val compareId: (T, T) -> Boolean,
    private val factory: RecyclerBinder.Builder.() -> Unit = {}) {
    private val func = createItemComponent(compFactory)
    private var wrapper: BinderWrapper<T>? = null

    fun getBinder(context: ComponentContext): RecyclerBinder {
        val w = wrapper
        return if (w != null) w.binder else {
            val b = RecyclerBinder.Builder().apply(factory).build(context)
            wrapper = BinderWrapper(b, emptyList(), context)
            b
        }
    }

    fun copy(newItems: List<T>): ContextualRecyclerBinder<T> {
        val w = wrapper
        if (w != null) {
            val binder = w.binder
            val oldItems = w.items
            val context = w.context

            binder.applyDiff(
                oldItems,
                newItems,
                { func(it)(context) },
                compareId)

            wrapper = w.copy(items = newItems)
        }
        return this
    }

    data class BinderWrapper<T>(
        val binder: RecyclerBinder,
        val items: List<T>,
        val context: ComponentContext)
}