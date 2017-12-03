package y2k.litho.elmish.experimental

import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder
import com.facebook.litho.*
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.litho.fresco.FrescoImage
import com.facebook.litho.widget.*

typealias Contextual<T> = (ComponentContext) -> T
typealias LazyComponent = Contextual<ComponentLayout.Builder>
typealias P<TModel, TMsg> = Pair<TModel, Cmd<TMsg>>

@DslMarker
@Target(AnnotationTarget.TYPE)
annotation class LithoElmishDslMarker

@Deprecated("")
fun ComponentLayout.ContainerBuilder.children(vararg xs: Contextual<ComponentLayout.Builder>?) =
    xs.filterNotNull().forEach(this::child)

inline fun FrescoImage.Builder.frescoController(f: (@LithoElmishDslMarker PipelineDraweeControllerBuilder).() -> Unit) {
    controller(
        Fresco
            .newDraweeControllerBuilder()
            .also(f)
            .build())
}

fun EditText.Builder.onTextChanged(msgFactory: (String) -> Any) {
    textChangedEventHandler(ElmishApplication.onTextChanged(innerContext, msgFactory))
}

fun Text.Builder.onClick(msg: Any) =
    EventHandler.attachClickHandler(msg, this, innerContext)

fun ContainerBuilder.onClick(msg: Any) {
    EventHandler.attachClickHandler(msg, this, innerContext)
}

@Deprecated("")
fun recyclerL(f: Recycler.Builder.() -> Unit): Contextual<ComponentLayout.Builder> =
    { context -> Recycler.create(context).also(f).withLayout() }

fun ComponentLayout.ContainerBuilder.child(c: Contextual<ComponentLayout.Builder>) {
    child(c(innerContext))
}

@Deprecated("Use editText")
fun ComponentLayout.ContainerBuilder.childEditText(f: (@LithoElmishDslMarker EditText.Builder).() -> Unit) {
    child(Views.editText(f))
}

fun ComponentLayout.ContainerBuilder.editText(f: (@LithoElmishDslMarker EditText.Builder).() -> Unit) {
    child(Views.editText(f))
}

@Deprecated("Use text")
fun ComponentLayout.ContainerBuilder.childText(f: (@LithoElmishDslMarker Text.Builder).(LayoutFuncCallback) -> Unit) {
    child(Views.text(f))
}

fun ComponentLayout.ContainerBuilder.text(f: (@LithoElmishDslMarker Text.Builder).(LayoutFuncCallback) -> Unit) {
    child(Views.text(f))
}

fun ComponentLayout.ContainerBuilder.column(f: (@LithoElmishDslMarker ContainerBuilder).() -> Unit) {
    child(Views.column(f))
}

fun ComponentLayout.ContainerBuilder.recyclerView(f: (@LithoElmishDslMarker Recycler.Builder).() -> Unit) {
    child(Views.recyclerView(f))
}

@Deprecated("Use fresco")
fun ComponentLayout.ContainerBuilder.childFresco(f: (@LithoElmishDslMarker FrescoImage.Builder).() -> Unit) {
    child(Views.fresco(f))
}

fun ComponentLayout.ContainerBuilder.fresco(f: (@LithoElmishDslMarker FrescoImage.Builder).() -> Unit) {
    child(Views.fresco(f))
}

@Deprecated("Use children(...)")
fun ContainerBuilder.childBuilder(cb: Contextual<Component.Builder<*, *>>) {
    child(cb.invoke(innerContext))
}

@Deprecated("")
fun ContainerBuilder.child_(cb: Contextual<ComponentLayout>) {
    child(cb.invoke(innerContext))
}

@Deprecated("")
fun column_(f: ContainerBuilder.() -> Unit): Contextual<ComponentLayout> =
    { context -> Column.create(context).apply(f).build() }

fun recycler(f: Recycler.Builder.() -> Unit): Contextual<ComponentLayout> =
    { context -> Recycler.create(context).apply(f).buildWithLayout() }

object Views {

    fun column(f: (@LithoElmishDslMarker ContainerBuilder).() -> Unit): Contextual<ComponentLayout.Builder> =
        { context -> Column.create(context).apply(f) }

    fun recyclerView(f: (@LithoElmishDslMarker Recycler.Builder).() -> Unit): Contextual<ComponentLayout.Builder> {
        return { context ->
            Recycler.create(context)
                .apply(f)
                .withLayout()
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

    fun fresco(f: FrescoImage.Builder.() -> Unit): Contextual<ComponentLayout.Builder> =
        { context -> FrescoImage.create(context).apply(f).withLayout() }
}

fun recyclerBuilder(f: Recycler.Builder.() -> Unit): Contextual<Recycler.Builder> =
    { context -> Recycler.create(context).apply(f) }

typealias LayoutFunc = ComponentLayout.Builder.() -> Unit
typealias LayoutFuncCallback = (LayoutFunc) -> Unit

@Deprecated("")
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

@Deprecated("")
fun ComponentLayout.ContainerBuilder.childWithLayout(
    cb: Contextual<ComponentLayout.Builder>,
    f: ComponentLayout.Builder.() -> Unit
) {
    val a = cb.invoke(innerContext)
    a.f()
    child(a.build())
}