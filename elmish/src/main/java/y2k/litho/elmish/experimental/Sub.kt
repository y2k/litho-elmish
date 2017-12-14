package y2k.litho.elmish.experimental

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ActorJob
import kotlinx.coroutines.experimental.launch

interface Sub<T> {
    fun start(target: ActorJob<T>): Job

    companion object {
        fun <T> none(): Sub<T> = object : Sub<T> {
            override fun start(target: ActorJob<T>): Job = launch { }
        }
    }
}