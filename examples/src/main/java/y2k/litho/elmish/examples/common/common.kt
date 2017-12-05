package y2k.litho.elmish.examples.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask.THREAD_POOL_EXECUTOR
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.litho.ComponentLayout
import com.facebook.soloader.SoLoader
import dalvik.system.DexFile
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.run
import kotlinx.types.Result
import kotlinx.types.Result.Error
import kotlinx.types.Result.Ok
import org.json.JSONObject
import y2k.litho.elmish.examples.BuildConfig
import y2k.litho.elmish.examples.Functions
import y2k.litho.elmish.experimental.*
import java.io.Serializable
import java.net.URL

object Log {
    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> log(e: Exception, x: T): T {
        if (BuildConfig.DEBUG) e.printStackTrace()
        return x
    }
}

object Navigation {

    inline suspend fun <reified T : Activity> open(arg: Serializable, ctx: Context) =
        Intent(ctx, T::class.java)
            .putExtra("arg", arg)
            .let(ctx::startActivity)

    fun getArgument(intent: Intent): Functions.Example? =
        intent.getSerializableExtra("arg") as? Functions.Example
}

object ClassAnalyzer {

    suspend fun getClassesInPackage(context: Context, pkg: Package): Sequence<Class<*>> {
        val regex = pkg.name
            .let(Regex.Companion::escape)
            .let { Regex("$it\\.\\w+") }

        val dexFile = DexFile(context.packageCodePath)
        try {
            return dexFile
                .entries()
                .asSequence()
                .filter(regex::matches)
                .map(javaClass.classLoader::loadClass)
        } finally {
            dexFile.close()
        }
    }
}

fun ComponentLayout.ContainerBuilder.editTextWithLabel(
    hint: String, cmd: (String) -> Any, error: String?) {
    column {
        editText {
            hint(hint)
            textSizeSp(30f)
            isSingleLine(true)

            onTextChanged(cmd)
        }
        text {
            textColor(Color.RED)
            textSizeSp(20f)
            text(error)
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T, R> T.zipOption(other: R): Pair<T, R>? =
    if (this == null || other == null) null else this to other

inline fun <T1, T2, T3, R> Pair<Pair<T1?, T2?>?, T3?>?.map3Option(f: (T1, T2, T3) -> R): R? {
    val (a, b) = this?.first ?: return null
    val c = second
    if (a == null || b == null || c == null) return null
    return f(a, b, c)
}

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