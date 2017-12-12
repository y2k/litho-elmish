package y2k.litho.elmish.examples

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.facebook.litho.ComponentLayout
import com.facebook.yoga.YogaEdge.ALL
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.run
import kotlinx.types.Result
import kotlinx.types.Result.Error
import kotlinx.types.Result.Ok
import y2k.litho.elmish.examples.ExampleList.Model
import y2k.litho.elmish.examples.ExampleList.Msg
import y2k.litho.elmish.examples.ExampleList.Msg.ExampleLoaded
import y2k.litho.elmish.examples.ExampleList.Msg.OpenExample
import y2k.litho.elmish.examples.Functions.Example
import y2k.litho.elmish.examples.common.ClassAnalyzer
import y2k.litho.elmish.examples.common.Log.log
import y2k.litho.elmish.examples.common.Navigation
import y2k.litho.elmish.experimental.*
import y2k.litho.elmish.experimental.Views.column
import java.io.Serializable
import java.lang.reflect.Modifier

object ExampleList : ElmFunctions<Model, Msg> {

    class Model(val x: List<Example>)
    sealed class Msg {
        class ExampleLoaded(val x: Result<List<Example>, Exception>) : Msg()
        class OpenExample(val e: Example) : Msg()
    }

    override fun init(): Pair<Model, Cmd<Msg>> =
        Model(emptyList()) to Cmd.context({ Functions.findExamples(it) }, ::ExampleLoaded)

    override fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> =
        when (msg) {
            is ExampleLoaded -> when (msg.x) {
                is Ok -> Model(msg.x.value) to Cmd.none()
                is Error -> log(msg.x.error, model to Cmd.none())
            }
            is OpenExample ->
                model to Cmd.fromSuspend_({ Navigation.openActivity(ExampleActivity::class, msg.e, it) })
        }

    override fun view(model: Model): Contextual<ComponentLayout.Builder> =
        column {
            for (x in model.x) {
                text {
                    paddingDip(ALL, 4f)
                    marginDip(ALL, 2f)
                    backgroundRes(R.drawable.button_simple)

                    text(x.name)
                    textSizeSp(24f)
                    onClick(OpenExample(x))
                }
            }
        }
}

object Functions {

    class Example(val name: String, val cls: Class<ElmFunctions<*, *>>) : Serializable

    suspend fun findExamples(context: Context): List<Example> =
        run(CommonPool) {
            ClassAnalyzer
                .getClassesInPackage(context, javaClass.`package`)
                .filter(::checkItElmFunctions)
                .filterIsInstance<Class<ElmFunctions<*, *>>>()
                .map { Example(name = getDisplayName(it), cls = it) }
                .toList()
        }

    private fun getDisplayName(cls: Class<*>): String =
        "${cls.simpleName}.kt"

    private fun checkItElmFunctions(cls: Class<*>): Boolean =
        ElmFunctions::class.java.isAssignableFrom(cls) &&
            Modifier.isPublic(cls.getDeclaredConstructor().modifiers)
}

class ExampleListActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        program(ExampleList)
    }
}

class ExampleActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val example = Navigation.getArgument(intent)
        if (example != null) program(example.cls.newInstance())
        else finish()
    }
}