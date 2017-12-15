package y2k.litho.elmish.experimental

import android.content.Context
import android.content.ContextWrapper
import com.facebook.litho.*
import com.facebook.litho.EventHandler
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.SendChannel
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
    private val functions: ElmFunctions<TModel, TMsg>,
    private val ctx: Context) {
    private var model = functions.init().first

    private val actorLoop = actor<TMsg>(UI, capacity = 32) {
        executeCmdAsync(functions.init().second, channel)

        while (true) {
            val msg = receive()

            val (newModel, cmd) = functions.update(model, msg)
            model = newModel

            reloadSubscriptions(newModel)

            ElmishApplication.reload(reloadContext)

            executeCmdAsync(cmd, channel)
        }
    }

    private fun executeCmdAsync(cmd: Cmd<TMsg>, channel: SendChannel<TMsg>) {
        async {
            val t = cmd.handle(ctx)
            if (t != null) channel.send(t)
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

    private lateinit var reloadContext: ComponentContext

    fun onCreateLayout(c: ComponentContext): ComponentLayout? {
        reloadContext = c
        return functions.view(model).invoke(c).build()
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