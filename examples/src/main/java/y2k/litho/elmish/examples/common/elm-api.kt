package y2k.litho.elmish.examples.common

import android.content.Context
import android.os.AsyncTask
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.types.Result
import okhttp3.OkHttpClient
import okhttp3.Request.Builder
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ws.WebSocketCall
import okhttp3.ws.WebSocketListener
import okio.Buffer
import org.json.JSONObject
import y2k.litho.elmish.examples.common.WebSocket.ActorCmd.*
import y2k.litho.elmish.experimental.Cmd
import y2k.litho.elmish.experimental.Sub
import java.io.IOException
import java.lang.System.currentTimeMillis
import java.net.URL
import kotlin.coroutines.experimental.suspendCoroutine
import okhttp3.Request as R
import okhttp3.ws.WebSocket as NWebSocket

typealias Decoder<T> = (String) -> T
class Request<out T>(val url: String, val decoder: Decoder<T>)

object Decode {

    fun string(): Decoder<String> = { it }

    fun <T> at(parts: List<String>, d: Decoder<T>): Decoder<T> {
        return { data ->
            val json = JSONObject(data)
            val x = parts
                .dropLast(1)
                .fold(json, JSONObject::getJSONObject)
                .getString(parts.last())
            d(x)
        }
    }
}

object Http {

    fun <T> get(url: String, decoder: Decoder<T>): Request<T> =
        Request(url, decoder)

    fun <T, TMsg> send(msgFactory: (Result<T, String>) -> TMsg, request: Request<T>): Cmd<TMsg> {
        return Cmd
            .fromSuspend({
                kotlinx.coroutines.experimental.run(AsyncTask.THREAD_POOL_EXECUTOR.asCoroutineDispatcher()) {
                    try {
                        val json = URL(request.url).readText()
                        val t = request.decoder(json)
                        Result.Ok(t)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Result.Error("$e")
                    }
                }
            }, msgFactory)
    }
}

object Time {

    fun <T> every(period: Long, f: (Long) -> T): Sub<T> = TimeSub(period, f)

    private class TimeSub<T>(
        private val period: Long,
        private val f: (Long) -> T) : Sub<T> {

        override fun isSame(other: Sub<*>): Boolean =
            other is TimeSub<*> && period == other.period

        override fun start(target: SendChannel<T>): Job =
            launch {
                while (true) {
                    delay(period)

                    val t = f(currentTimeMillis())
                    target.send(t)
                }
            }
    }
}

private typealias Callback = suspend (String) -> Unit

object WebSocket {

    private sealed class ActorCmd {
        class SendCmd(val url: String, val body: String) : ActorCmd()
        class AddListenerCmd(val url: String, val f: Callback) : ActorCmd()
        class RemoveListenerCmd(val f: Callback) : ActorCmd()
        class SocketUpdateCmd(val url: String, val body: String) : ActorCmd()
    }

    private val actor = actor<ActorCmd>(capacity = 32) {
        val sockets = HashMap<String, NWebSocket>()
        val urlListeners = HashMap<String, HashSet<Callback>>()

        while (true) {
            val cmd = receive()
            when (cmd) {
                is SendCmd -> {
                    try {
                        val socket = sockets.getOrPut(cmd.url) { createSocket(cmd.url) }
                        socket.sendMessage(RequestBody.create(NWebSocket.TEXT, cmd.body))
                    } catch (e: Exception) {
                        sockets.remove(cmd.url)?.closeQuietly()
                        channel.offer(cmd)
                    }
                }
                is AddListenerCmd -> {
                    urlListeners.getOrPut(cmd.url, { HashSet() }).add(cmd.f)
                    sockets.getOrPut(cmd.url) { createSocket(cmd.url) }
                }
                is RemoveListenerCmd -> {
                    urlListeners
                        .toList()
                        .forEach { (url, listeners) ->
                            listeners.remove(cmd.f)
                            if (listeners.isEmpty()) {
                                urlListeners.remove(url)
                                sockets.remove(url)?.closeQuietly()
                            }
                        }
                }
                is SocketUpdateCmd -> {
                    urlListeners
                        .getOrElse(cmd.url, { emptySet<Callback>() })
                        .forEach { it(cmd.body) }
                }
            }
        }
    }

    private suspend fun createSocket(url: String): NWebSocket =
        suspendCoroutine { continuation ->
            WebSocketCall
                .create(
                    OkHttpClient.Builder().build(),
                    Builder().url(url).build())
                .enqueue(object : WebSocketListener {
                    override fun onOpen(webSocket: NWebSocket, response: Response?) {
                        continuation.resume(webSocket)
                    }

                    override fun onFailure(e: IOException, response: Response?) {
                        e.printStackTrace()
                    }

                    override fun onMessage(message: ResponseBody) {
                        actor.offer(SocketUpdateCmd(url, message.string()))
                    }

                    override fun onClose(code: Int, reason: String?) = Unit
                    override fun onPong(payload: Buffer?) = Unit
                })
        }

    fun <T> send(url: String, data: String): Cmd<T> =
        object : Cmd<T> {
            suspend override fun handle(ctx: Context): T? {
                actor.send(SendCmd(url, data))
                return null
            }
        }

    fun <T> listen(url: String, f: (String) -> T): Sub<T> = WebSocketSub(f, url)

    private class WebSocketSub<T>(
        private val f: (String) -> T,
        private val url: String) : Sub<T> {

        override fun isSame(other: Sub<*>): Boolean =
            other is WebSocketSub<*> && url == other.url

        override fun start(target: SendChannel<T>): Job = launch {
            val f: suspend (String) -> Unit = { target.send(f(it)) }
            try {
                actor.send(AddListenerCmd(url, f))
                delay(Long.MAX_VALUE)
            } finally {
                actor.send(RemoveListenerCmd(f))
            }
        }
    }
}