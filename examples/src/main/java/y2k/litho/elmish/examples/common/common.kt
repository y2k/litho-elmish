package y2k.litho.elmish.examples.common

import android.app.Application
import android.os.AsyncTask.THREAD_POOL_EXECUTOR
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.soloader.SoLoader
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.run
import org.json.JSONObject
import y2k.litho.elmish.Cmd
import java.net.URL

sealed class Result<out T, out E>
class Ok<out T>(val value: T) : Result<T, Nothing>()
class Error<out E>(val error: E) : Result<Nothing, E>()

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
                run(THREAD_POOL_EXECUTOR.asCoroutineDispatcher()) {
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

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
        SoLoader.init(this, false)
    }
}