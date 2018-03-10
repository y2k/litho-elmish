package y2k.litho.elmish.experimental

import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder
import com.facebook.litho.*
import com.facebook.litho.Component.ContainerBuilder
import com.facebook.litho.fresco.FrescoImage
import com.facebook.litho.widget.*

typealias Contextual<T> = (ComponentContext) -> T

@DslMarker
@Target(AnnotationTarget.TYPE)
annotation class LithoElmishDslMarker

inline fun <T : Component.Builder<*>> T.style(applyStyle: (T) -> Unit) {
    applyStyle(this)
}

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

fun ContainerBuilder<*>.onClick(msg: Any) {
    EventHandler.attachClickHandler(msg, this, innerContext)
}

fun ContainerBuilder<*>.child(c: Contextual<Component.Builder<*>>) {
    child(c(innerContext))
}

fun ContainerBuilder<*>.layoutChild(c: Contextual<Component.Builder<*>>) {
    child(c(innerContext))
}

fun ContainerBuilder<*>.editText(f: (@LithoElmishDslMarker EditText.Builder).() -> Unit) {
    child(Views.editText(f))
}

fun ContainerBuilder<*>.text(f: (@LithoElmishDslMarker Text.Builder).() -> Unit) {
    child(Views.text(f))
}

fun ContainerBuilder<*>.progress(f: (@LithoElmishDslMarker Progress.Builder).() -> Unit) {
    child(Views.progress(f))
}

fun ContainerBuilder<*>.column(f: (@LithoElmishDslMarker ContainerBuilder<*>).() -> Unit) {
    layoutChild(Views.column(f))
}

fun ContainerBuilder<*>.row(f: (@LithoElmishDslMarker ContainerBuilder<*>).() -> Unit) {
    layoutChild(Views.row(f))
}

fun ContainerBuilder<*>.recyclerView(f: (@LithoElmishDslMarker Recycler.Builder).() -> Unit) {
    child(Views.recyclerView(f))
}

fun ContainerBuilder<*>.fresco(f: (@LithoElmishDslMarker FrescoImage.Builder).() -> Unit) {
    child(Views.fresco(f))
}

object Views {

    fun row(f: (@LithoElmishDslMarker ContainerBuilder<*>).() -> Unit): Contextual<ContainerBuilder<*>> =
        { context -> Row.create(context).apply(f) }

    fun column(f: (@LithoElmishDslMarker ContainerBuilder<*>).() -> Unit): Contextual<ContainerBuilder<*>> =
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