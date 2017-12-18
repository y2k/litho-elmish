package y2k.litho.elmish.examples.common

import android.os.AsyncTask
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.types.Result
import kotlinx.types.Result.Error
import kotlinx.types.Result.Ok
import org.json.JSONObject
import y2k.litho.elmish.examples.common.SocketLifetime.WebSocketCmd.*
import y2k.litho.elmish.examples.common.WebSocket.Cmd.*
import y2k.litho.elmish.examples.common.WebSocket.SocketWrapper.SocketCmd.*
import y2k.litho.elmish.examples.common.WebSocket.SocketWrapper.SocketState.*
import y2k.litho.elmish.experimental.Cmd
import y2k.litho.elmish.experimental.Sub
import java.lang.System.currentTimeMillis
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
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
                        Ok(t)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Error("$e")
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

object WebSocket {

    private class SocketWrapper(
        val url: URL,
        private val sendToParent: (String) -> Unit) {

        private sealed class SocketCmd {
            class SocketWrite(val data: String) : SocketCmd()
            object FlushBuffer : SocketCmd()
            class SocketSubCmd(val subCmd: SocketLifetime.WebSocketCmd) : SocketCmd()
        }

        private sealed class SocketState {
            object NilSocket : SocketState()
            object PendingSocket : SocketState()
            class ReadySocket(val socket: NWebSocket) : SocketState()
        }

        private val aktor = actor<SocketCmd>(capacity = 128) {
            var socket: SocketState = NilSocket
            val buffer: Queue<String> = LinkedList<String>()

            try {
                while (true) {
                    val cmd = receive()
                    when (cmd) {
                        is SocketWrite -> {
                            buffer += cmd.data
                            channel.send(FlushBuffer)
                        }
                        FlushBuffer -> {
                            when (socket) {
                                NilSocket -> {
                                    socket = PendingSocket
                                    SocketLifetime.start(url, ::SocketSubCmd, channel)
                                }
                                is ReadySocket -> {
                                    while (true) {
                                        val msg = buffer.peek() ?: break
                                        if (socket.socket.sendMessage(msg) is Ok)
                                            buffer.poll()
                                        else {
                                            channel.send(FlushBuffer)
                                            break
                                        }
                                    }
                                }
                            }
                        }
                        is SocketSubCmd -> {
                            val subCmd = cmd.subCmd
                            when (subCmd) {
                                is WebSocketRead -> sendToParent(subCmd.data)
                                is WebSocketConnected -> {
                                    socket = ReadySocket(subCmd.socket)
                                    channel.send(FlushBuffer)
                                }
                                WebSocketDisconnected -> {
                                    socket = PendingSocket
                                    delay(5000)
                                    SocketLifetime.start(url, ::SocketSubCmd, channel)
                                }
                            }
                        }
                    }
                }
            } finally {
                (socket as? ReadySocket)?.socket?.closeQuietly()
            }
        }

        suspend fun send(data: String) = aktor.send(SocketWrite(data))

        fun close() {
            aktor.close()
        }
    }

    private sealed class Cmd {
        class SendData(val url: URL, val data: String) : Cmd()
        class UserConnected(val url: URL, val offer: (String) -> Unit, val id: Any) : Cmd()
        class UserDisconnected(val id: Any) : Cmd()
    }

    private val aktor = actor<Cmd>(capacity = 128) {
        val wrappers = ArrayList<SocketWrapper>()
        val users = ArrayList<UserConnected>()

        while (true) {
            val cmd = receive()
            when (cmd) {
                is SendData ->
                    wrappers.find { it.url == cmd.url }?.send(cmd.data)
                is UserConnected -> {
                    if (wrappers.none { it.url == cmd.url })
                        wrappers.add(SocketWrapper(cmd.url, cmd.offer))
                    users += cmd
                }
                is UserDisconnected -> {
                    users.removeAll { it.id == cmd.id }
                    wrappers.removeAll({ w -> users.none { it.url == w.url } }, SocketWrapper::close)
                }
            }
        }
    }

    suspend fun send(url: URL, data: String) =
        aktor.send(SendData(url, data))

    fun <T> listen(url: URL, convert: (String) -> T, callback: SendChannel<T>): Job =
        launch {
            try {
                aktor.send(UserConnected(url, { callback.offer(convert(it)) }, callback))
                delay(Long.MAX_VALUE)
            } catch (e: CancellationException) {
                aktor.offer(UserDisconnected(callback))
            }
        }
}