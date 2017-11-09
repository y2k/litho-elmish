package y2k.litho.elmish

import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder
import com.facebook.litho.*
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.litho.fresco.FrescoImage
import com.facebook.litho.widget.*

typealias Contextual<T> = (ComponentContext) -> T
typealias LazyComponent = Contextual<ComponentLayout.Builder>
typealias P<TModel, TMsg> = Pair<TModel, Cmd<TMsg>>

fun ComponentLayout.ContainerBuilder.children(vararg xs: Contextual<ComponentLayout.Builder>) =
    xs.forEach(this::child)

inline fun FrescoImage.Builder.fresco(f: PipelineDraweeControllerBuilder.() -> Unit) {
    controller(
        Fresco
            .newDraweeControllerBuilder()
            .also(f)
            .build())
}

fun EditText.Builder.onTextChanged(msgFactory: (String) -> Any) {
    textChangedEventHandler(ElmishApplication.onTextChanged(innerContext, msgFactory))
}

fun Text.Builder.onClick(layout: LayoutFuncCallback, msg: Any) {
    val ctx = innerContext
    layout {
        clickHandler(ElmishApplication.onEventHandle(ctx, msg))
    }
}

fun editText(f: EditText.Builder.() -> Unit): Contextual<ComponentLayout.Builder> =
    { context -> EditText.create(context).also(f).withLayout() }

fun text(f: Text.Builder.(LayoutFuncCallback) -> Unit): Contextual<ComponentLayout.Builder> {
    return { context ->
        val builder = Text.create(context)

        var lf: LayoutFunc? = null
        val lfCallback: LayoutFuncCallback = { lf = it }

        builder
            .also { it.f(lfCallback) }
            .withLayout()
            .also { lb -> lf?.let { lb.it() } }
    }
}

fun recyclerL(f: Recycler.Builder.() -> Unit): Contextual<ComponentLayout.Builder> =
    { context -> Recycler.create(context).also(f).withLayout() }

fun ComponentLayout.ContainerBuilder.child(c: Contextual<ComponentLayout.Builder>) {
    child(c(innerContext))
}

fun ComponentLayout.ContainerBuilder.childEditText(f: EditText.Builder.() -> Unit) {
    child(editText(f))
}

fun ComponentLayout.ContainerBuilder.childText(f: Text.Builder.(LayoutFuncCallback) -> Unit) {
    child(text(f))
}

fun ComponentLayout.ContainerBuilder.childFresco(f: FrescoImage.Builder.() -> Unit) {
    child(fresco(f))
}

fun ContainerBuilder.childBuilder(cb: Contextual<Component.Builder<*, *>>) {
    child(cb.invoke(innerContext))
}

fun ContainerBuilder.child_(cb: Contextual<ComponentLayout>) {
    child(cb.invoke(innerContext))
}

fun column_(f: ContainerBuilder.() -> Unit): Contextual<ComponentLayout> =
    { context -> Column.create(context).apply(f).build() }

fun column(f: ContainerBuilder.() -> Unit): Contextual<ComponentLayout.Builder> =
    { context -> Column.create(context).apply(f) }

fun recycler(f: Recycler.Builder.() -> Unit): Contextual<ComponentLayout> =
    { context -> Recycler.create(context).apply(f).buildWithLayout() }

fun fresco(f: FrescoImage.Builder.() -> Unit): Contextual<ComponentLayout.Builder> =
    { context -> FrescoImage.create(context).apply(f).withLayout() }

fun recyclerBuilder(f: Recycler.Builder.() -> Unit): Contextual<Recycler.Builder> =
    { context -> Recycler.create(context).apply(f) }

typealias LayoutFunc = ComponentLayout.Builder.() -> Unit
typealias LayoutFuncCallback = (LayoutFunc) -> Unit

fun progressL(b: Progress.Builder.(LayoutFuncCallback) -> Unit): Contextual<ComponentLayout.Builder> {
    return { context ->
        val builder = Progress.create(context)

        var lf: LayoutFunc? = null
        val lfCallback: LayoutFuncCallback = { lf = it }

        builder
            .also { it.b(lfCallback) }
            .withLayout()
            .also { lb -> lf?.let { lb.it() } }
    }
}

fun ComponentLayout.ContainerBuilder.childWithLayout(
    cb: Contextual<ComponentLayout.Builder>,
    f: ComponentLayout.Builder.() -> Unit
) {
    val a = cb.invoke(innerContext)
    a.f()
    child(a.build())
}