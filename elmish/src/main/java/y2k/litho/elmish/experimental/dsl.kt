package y2k.litho.elmish.experimental

import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder
import com.facebook.litho.*
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.litho.fresco.FrescoImage
import com.facebook.litho.widget.*

typealias Contextual<T> = (ComponentContext) -> T

@DslMarker
@Target(AnnotationTarget.TYPE)
annotation class LithoElmishDslMarker

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

fun ComponentLayout.ContainerBuilder.child(c: Contextual<Component.Builder<*, *>>) {
    child(c(innerContext))
}

fun ComponentLayout.ContainerBuilder.layoutChild(c: Contextual<ComponentLayout.Builder>) {
    child(c(innerContext))
}

fun ComponentLayout.ContainerBuilder.editText(f: (@LithoElmishDslMarker EditText.Builder).() -> Unit) {
    child(Views.editText(f))
}

fun ComponentLayout.ContainerBuilder.text(f: (@LithoElmishDslMarker Text.Builder).() -> Unit) {
    child(Views.text(f))
}

fun ComponentLayout.ContainerBuilder.progress(f: (@LithoElmishDslMarker Progress.Builder).() -> Unit) {
    child(Views.progress(f))
}

fun ComponentLayout.ContainerBuilder.column(f: (@LithoElmishDslMarker ContainerBuilder).() -> Unit) {
    layoutChild(Views.column(f))
}

fun ComponentLayout.ContainerBuilder.recyclerView(f: (@LithoElmishDslMarker Recycler.Builder).() -> Unit) {
    child(Views.recyclerView(f))
}

fun ComponentLayout.ContainerBuilder.fresco(f: (@LithoElmishDslMarker FrescoImage.Builder).() -> Unit) {
    child(Views.fresco(f))
}

object Views {

    fun column(f: (@LithoElmishDslMarker ContainerBuilder).() -> Unit): Contextual<ContainerBuilder> =
        { context -> Column.create(context).apply(f) }

    fun progress(f: Progress.Builder.() -> Unit): Contextual<Progress.Builder> =
        { context -> Progress.create(context).apply(f) }

    fun recyclerView(f: (@LithoElmishDslMarker Recycler.Builder).() -> Unit): Contextual<Recycler.Builder> =
        { context -> Recycler.create(context).apply(f) }

    fun editText(f: EditText.Builder.() -> Unit): Contextual<EditText.Builder> =
        { context -> EditText.create(context).also(f) }

    fun text(f: Text.Builder.() -> Unit): Contextual<Text.Builder> =
        { context -> Text.create(context).also(f) }

    fun fresco(f: FrescoImage.Builder.() -> Unit): Contextual<FrescoImage.Builder> =
        { context -> FrescoImage.create(context).apply(f) }
}

@Deprecated("")
fun recycler(f: Recycler.Builder.() -> Unit): Contextual<ComponentLayout> =
    { context -> Recycler.create(context).apply(f).buildWithLayout() }

@Deprecated("")
fun recyclerBuilder(f: Recycler.Builder.() -> Unit): Contextual<Recycler.Builder> =
    { context -> Recycler.create(context).apply(f) }