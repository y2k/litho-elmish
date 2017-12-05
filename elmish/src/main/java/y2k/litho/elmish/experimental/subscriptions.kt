package y2k.litho.elmish.experimental

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ActorJob
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch

fun initializeSubscriptionPrototype(functions: ElmFunctions<*, *>, handle: LifecycleHandler) {
    val subf = functions as? ElmFunctionsWithSubscription<*, *> ?: return
    subf.handle = handle
}

abstract class ElmFunctionsWithSubscription<TModel, TMsg> : ElmFunctions<TModel, TMsg> {

    internal lateinit var handle: LifecycleHandler

    abstract fun subscriptions(model: TModel): Sub<TMsg>

    final override fun update(model: TModel, msg: TMsg): Pair<TModel, Cmd<TMsg>> {
        val (newModel, newCmd) = update2(model, msg)
        reloadSubscriptions(newModel)
        return newModel to newCmd
    }

    private var currentSubs: Job? = null

    private fun reloadSubscriptions(newModel: TModel) {
        currentSubs?.cancel()
        currentSubs = subscriptions(newModel).start(actorLoop)
    }

    private val actorLoop = actor<TMsg>(UI, capacity = 32) {
        while (true) {
            val msg = receive()

            val (model2, cmd2) = update(getCurrentModel(), msg)
            invalidateTree(model2)

            async {
                val t = cmd2.handle(handle.appContext)
                if (t != null)
                    channel.send(t)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getCurrentModel(): TModel = handle.subModel as TModel

    private fun invalidateTree(model2: TModel) =
        handle.invalidateTree(model2!!)

    abstract fun update2(model: TModel, msg: TMsg): Pair<TModel, Cmd<TMsg>>
}

interface Sub<T> {
    fun start(target: ActorJob<T>): Job

    companion object {
        fun <T> none(): Sub<T> = object : Sub<T> {
            override fun start(target: ActorJob<T>): Job = launch { }
        }
    }
}