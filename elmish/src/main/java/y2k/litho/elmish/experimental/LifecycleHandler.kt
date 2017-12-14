package y2k.litho.elmish.experimental

import android.content.Context
import android.content.ContextWrapper
import com.facebook.litho.*
import com.facebook.litho.EventHandler
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.actor

object EventHandler {

    fun attachClickHandler(msg: Any, builder: ComponentLayout.Builder, ctx: ComponentContext) {
        eventHandler(ctx, msg)
            .let(builder::clickHandler)
    }

    fun attachClickHandler(msg: Any, builder: Component.Builder<*, *>, ctx: ComponentContext) {
        eventHandler(ctx, msg)
            .let(builder::clickHandler)
    }

    private fun eventHandler(ctx: ComponentContext, msg: Any): EventHandler<ClickEvent> =
        when (ctx.componentScope) {
            is ElmishApplication -> ElmishApplication.onEventHandle(ctx, msg)
            is ElmishItemComponent -> ElmishItemComponent.onItemClicked(ctx, msg)
            else -> error("Unsupported component <${ctx.componentScope}> in layout")
        }
}

class LifecycleHandler<TModel, TMsg>(
    private val functions: ElmFunctions<TModel, TMsg>) {
    private var subModel = functions.init().first
    lateinit var appContext: ComponentContext

    private val actorLoop = actor<TMsg>(UI, capacity = 32) {
        async {
            val t = functions.init().second.handle(appContext)
            if (t != null) channel.send(t)
        }

        while (true) {
            val msg = receive()

            val (model2, cmd2) = functions.update(subModel, msg)
            subModel = model2

            reloadSubscriptions(model2)

            ElmishApplication.reload(c2)

            async {
                val t = cmd2.handle(appContext)
                if (t != null)
                    channel.send(t)
            }
        }
    }

    private var currentSubs: Pair<Job, Sub<*>>? = null

    private fun reloadSubscriptions(newModel: TModel) {
        val newSub = functions.subscriptions(newModel)
        val cs = currentSubs
        if (cs == null || !newSub.isSame(cs.second)) {
            cs?.first?.cancel()
            currentSubs = newSub.start(actorLoop) to newSub
        }
    }

    private lateinit var c2: ComponentContext

    fun onCreateLayout(c: ComponentContext): ComponentLayout? {
        c2 = c
        return functions.view(subModel).invoke(c).build()
    }

    fun onEventHandle(msg: Any) {
        @Suppress("UNCHECKED_CAST")
        actorLoop.offer(msg as TMsg)
    }

    fun onTextChanged(text: String, msgFactory: (String) -> Any) {
        val msg = msgFactory(text)
        @Suppress("UNCHECKED_CAST")
        actorLoop.offer(msg as TMsg)
    }
}

class MyContext(base: Context?, val handler: LifecycleHandler<*, *>) : ContextWrapper(base)

val ComponentContext.sharedLifecycleHandler: LifecycleHandler<*, *>
    get() = (baseContext as MyContext).handler