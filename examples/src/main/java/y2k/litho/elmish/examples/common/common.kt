package y2k.litho.elmish.examples.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.soloader.SoLoader
import dalvik.system.DexFile
import y2k.litho.elmish.examples.BuildConfig
import y2k.litho.elmish.examples.Functions
import y2k.litho.elmish.experimental.column
import y2k.litho.elmish.experimental.editText
import y2k.litho.elmish.experimental.onTextChanged
import y2k.litho.elmish.experimental.text
import java.io.Serializable

inline fun <T> ContainerBuilder.viewStaticList(examples: List<T>, f: ContainerBuilder.(T) -> Unit) {
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

fun ContainerBuilder.editTextWithLabel(
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

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
        SoLoader.init(this, false)
    }
}