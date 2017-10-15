package y2k.litho.elmish

import com.facebook.litho.*
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.litho.widget.Progress
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