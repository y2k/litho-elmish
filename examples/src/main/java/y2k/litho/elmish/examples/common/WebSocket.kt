package y2k.litho.elmish.examples.common

import com.facebook.litho.ComponentContext
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ActorJob
import kotlinx.coroutines.experimental.channels.ProducerJob
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ws.WebSocketCall
import okhttp3.ws.WebSocketListener
import okio.Buffer
import y2k.litho.elmish.experimental.Cmd
import y2k.litho.elmish.experimental.Sub
import java.io.IOException
import kotlin.coroutines.experimental.suspendCoroutine
import okhttp3.Request as R

object Time {

    fun <T> every(period: Long, f: (Long) -> T): Sub<T> = TimeSub(period, f)

    private class TimeSub<T>(
        private val period: Long,
        private val f: (Long) -> T) : Sub<T> {

        override fun hashCode(): Int = period.hashCode()
        override fun equals(other: Any?): Boolean =
            other is TimeSub<*> && period == other.period

        override fun start(target: ActorJob<T>): Job =
            launch {
                while (true) {
                    delay(period)

                    val t = f(System.currentTimeMillis())
                    target.send(t)
                }
            }
    }
}

private fun <E> ProducerJob<E>.zip(ob: ProducerJob<E>, f: (E, E) -> E): ProducerJob<E> = TODO()

object WebSocket {

    fun <T> send(url: String, data: String): Cmd<T> {
        return object : Cmd<T> {
            suspend override fun handle(ctx: ComponentContext): T? {

                suspendCoroutine<Unit> { continuation ->
                    WebSocketCall
                        .create(
                            OkHttpClient.Builder().build(),
                            okhttp3.Request.Builder().url(url).build())
                        .enqueue(object : WebSocketListener {
                            override fun onOpen(webSocket: okhttp3.ws.WebSocket, response: Response?) {
                                webSocket.sendMessage(RequestBody.create(okhttp3.ws.WebSocket.TEXT, data))
                                webSocket.close(0, null)
                                continuation.resume(Unit)
                            }

                            override fun onClose(code: Int, reason: String?) = Unit
                            override fun onFailure(e: IOException, response: Response?) = Unit
                            override fun onMessage(message: ResponseBody) = Unit
                            override fun onPong(payload: Buffer?) = Unit
                        })
                }

                return null
            }
        }
    }

    fun <T> listen(url: String, f: (String) -> T): Sub<T> {
        val producer =
            produce<String>(capacity = 10) {
                WebSocketCall
                    .create(
                        OkHttpClient.Builder().build(),
                        okhttp3.Request.Builder().url(url).build())
                    .enqueue(object : WebSocketListener {
                        override fun onOpen(webSocket: okhttp3.ws.WebSocket, response: Response?) = Unit
                        override fun onClose(code: Int, reason: String?) = Unit
                        override fun onFailure(e: IOException, response: Response?) = Unit
                        override fun onPong(payload: Buffer?) = Unit
                        override fun onMessage(message: ResponseBody) {
                            offer(message.string())
                        }
                    })
            }

        TODO()
    }
}