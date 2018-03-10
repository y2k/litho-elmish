package y2k.litho.elmish.examples.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.litho.Component.ContainerBuilder
import com.facebook.litho.widget.Text
import com.facebook.soloader.SoLoader
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.facebook.yoga.YogaJustify
import com.facebook.yoga.YogaPositionType
import dalvik.system.DexFile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.withContext
import kotlinx.types.Result
import okhttp3.*
import okhttp3.Request
import okhttp3.ws.WebSocket
import okhttp3.ws.WebSocketCall
import okhttp3.ws.WebSocketListener
import okio.Buffer
import y2k.litho.elmish.examples.BuildConfig
import y2k.litho.elmish.examples.Functions
import y2k.litho.elmish.examples.R
import y2k.litho.elmish.experimental.*
import java.io.IOException
import java.io.Serializable
import java.net.URL

typealias Сmd<T> = (suspend () -> T)?

object Styles {

    fun label(builder: Text.Builder) {
        builder.backgroundRes(R.drawable.button_simple)
        builder.marginDip(YogaEdge.ALL, 2f)
        builder.textSizeSp(40f)
    }
}

fun ContainerBuilder<*>.fullscreenProgress() {
    column {
        backgroundRes(R.color.colorPrimary)
        positionType(YogaPositionType.ABSOLUTE)
        positionDip(YogaEdge.ALL, 0f)
        alignItems(YogaAlign.CENTER)
        justifyContent(YogaJustify.CENTER)

        progress {
            widthDip(100f)
            heightDip(100f)
        }
    }
}

fun ContainerBuilder<*>.button(title: String, msg: Any, disabled: Boolean = false) {
    text {
        marginDip(YogaEdge.ALL, 2f)
        paddingDip(YogaEdge.ALL, 4f)
        backgroundRes(R.drawable.button_simple)
        textColor(if (disabled) Color.LTGRAY else Color.BLACK)
        textSizeSp(30f)
        text(title)

        if (!disabled) onClick(msg)
    }
}

fun <E> ImmutableList<E>.subList(fromIndex: Int): ImmutableList<E> =
    subList(fromIndex, size)

object SocketLifetime {

    sealed class WebSocketCmd {
        class WebSocketRead(val data: String) : WebSocketCmd()
        class WebSocketConnected(val socket: WebSocket) : WebSocketCmd()
        object WebSocketDisconnected : WebSocketCmd()
    }

    fun <T> start(url: URL, convert: (WebSocketCmd) -> T, channel: SendChannel<T>) =
        WebSocketCall
            .create(
                OkHttpClient.Builder().build(),
                Request.Builder().url(url).build())
            .enqueue(object : WebSocketListener {
                override fun onOpen(webSocket: WebSocket, response: Response?) {
                    if (channel.offerResult(convert(WebSocketCmd.WebSocketConnected(webSocket))) is Result.Error)
                        webSocket.closeQuietly()
                }

                override fun onFailure(e: IOException, response: Response?) {
                    channel.offerResult(convert(WebSocketCmd.WebSocketDisconnected))
                }

                override fun onMessage(message: ResponseBody) {
                    channel.offerResult(convert(WebSocketCmd.WebSocketRead(message.string())))
                }

                override fun onClose(code: Int, reason: String?) = Unit
                override fun onPong(payload: Buffer?) = Unit
            })
}

fun <T> SendChannel<T>.offerResult(data: T): Result<Boolean, Exception> =
    try {
        Result.Ok(offer(data))
    } catch (e: Exception) {
        Result.Error(e)
    }

fun WebSocket.sendMessage(msg: String): Result<Unit, Exception> =
    try {
        Result.Ok(sendMessage(RequestBody.create(WebSocket.TEXT, msg)))
    } catch (e: Exception) {
        Result.Error(e)
    }

fun <T> ArrayList<T>.removeAll(predicate: (T) -> Boolean, deleteAction: (T) -> Unit) {
    removeAll {
        if (predicate(it)) {
            deleteAction(it)
            true
        } else false
    }
}

inline fun <T> ContainerBuilder<*>.viewStaticList(examples: List<T>, f: ContainerBuilder<*>.(T) -> Unit) {
    for (x in examples) {
        f(x)
    }
}

fun okhttp3.ws.WebSocket.closeQuietly() {
    try {
        close(0, null)
    } catch (e: Exception) {
    }
}

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

    suspend fun getClassesInPackage(context: Context, pkg: Package): List<Class<*>> =
        withContext(DefaultDispatcher) {
            val regex = pkg.name
                .let(Regex.Companion::escape)
                .let { Regex("$it\\.\\w+") }

            @Suppress("DEPRECATION")
            val dexFile = DexFile(context.packageCodePath)
            try {
                dexFile
                    .entries()
                    .asSequence()
                    .filter(regex::matches)
                    .map(javaClass.classLoader::loadClass)
                    .toList()
            } finally {
                dexFile.close()
            }
        }
}

fun ContainerBuilder<*>.editTextWithLabel(
    hint: String, msg: (String) -> Any, error: String?) {
    column {
        editText {
            hint(hint)
            textSizeSp(30f)
            isSingleLine(true)

            onTextChanged(msg)
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

fun <T, E> Result<T, E>.valueOrDefault(f: (E) -> T): T = when (this) {
    is Result.Ok -> value
    is Result.Error -> f(error)
}

@Suppress("unused")
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
        SoLoader.init(this, false)
    }
}