package y2k.litho.elmish

import com.facebook.litho.*
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.litho.widget.Recycler

typealias Contextual<T> = (ComponentContext) -> T

fun ContainerBuilder.childBuilder(cb: Contextual<Component.Builder<*, *>>) {
    child(cb.invoke(innerContext))
}

fun ContainerBuilder.child(cb: Contextual<ComponentLayout>) {
    child(cb.invoke(innerContext))
}

fun column(f: ContainerBuilder.() -> Unit): Contextual<ComponentLayout> {
    return { context ->
        Column.create(context).apply(f).build()
    }
}

fun recycler(f: Recycler.Builder.() -> Unit): Contextual<ComponentLayout> {
    return { context ->
        Recycler.create(context).apply(f).buildWithLayout()
    }
}

fun recyclerBuilder(f: Recycler.Builder.() -> Unit): Contextual<Recycler.Builder> {
    return { context ->
        Recycler.create(context).apply(f)
    }
}