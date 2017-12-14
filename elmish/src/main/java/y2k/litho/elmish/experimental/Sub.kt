package y2k.litho.elmish.experimental

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch

interface Sub<out T> {

    fun start(target: SendChannel<T>): Job
    fun isSame(other: Sub<*>): Boolean

    private object NoneSub : Sub<Nothing> {
        override fun start(target: SendChannel<Nothing>): Job = launch { }
        override fun isSame(other: Sub<*>): Boolean = other is NoneSub
    }

    companion object {
        fun <T> none(): Sub<T> = NoneSub
    }
}