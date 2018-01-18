package y2k.litho.elmish.examples

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.yoga.YogaEdge.ALL
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
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
import y2k.litho.elmish.examples.common.viewStaticList
import y2k.litho.elmish.experimental.*
import java.io.Serializable
import java.lang.reflect.Modifier

class ExampleList : ElmFunctions<Model, Msg> {

    class Model(val examples: List<Example>)
    sealed class Msg {
        class ExampleLoaded(val x: Result<List<Example>, Exception>) : Msg()
        class OpenExample(val e: Example) : Msg()
    }

    override fun init(): Pair<Model, Cmd<Msg>> =
        Model(emptyList()) to Cmd.fromContext({ Functions.findExamples(it) }, ::ExampleLoaded)

    override fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> =
        when (msg) {
            is ExampleLoaded -> when (msg.x) {
                is Ok -> Model(msg.x.value) to Cmd.none()
                is Error -> log(msg.x.error, model to Cmd.none())
            }
            is OpenExample ->
                model to Cmd.fromContext({ Navigation.open<ExampleActivity>(msg.e, it) })
        }

    override fun ContainerBuilder.view(model: Model) =
        column {
            viewStaticList(model.examples) {
                viewItem(it)
            }
        }

    private fun ContainerBuilder.viewItem(x: Example) {
        text {
            paddingDip(ALL, 8f)
            marginDip(ALL, 2f)
            backgroundRes(R.drawable.button_simple)

            text(x.name)
            textSizeSp(24f)
            onClick(OpenExample(x))
        }
    }
}

object Functions {

    class Example(val name: String, val cls: Class<ElmFunctions<*, *>>) : Serializable

    suspend fun findExamples(context: Context): List<Example> =
        withContext(CommonPool) {
            ClassAnalyzer
                .getClassesInPackage(context, javaClass.`package`)
                .let(::filterExamples)
        }

    private fun filterExamples(classes: List<Class<*>>): List<Example> =
        classes
            .filter(::checkItElmFunctions)
            .filterIsInstance<Class<ElmFunctions<*, *>>>()
            .map { Example(name = getDisplayName(it), cls = it) }
            .toList()

    private fun checkItElmFunctions(cls: Class<*>): Boolean =
        ElmFunctions::class.java.isAssignableFrom(cls)
            && cls.constructors.any { it.parameterTypes.isEmpty() }
            && Modifier.isPublic(cls.getDeclaredConstructor().modifiers)

    private fun getDisplayName(cls: Class<*>): String =
        "${cls.simpleName}.kt"
}

class ExampleListActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StrictMode.enableDefaults()
//        program<ExampleList>()
        program<SearchExample>()
    }
}

class ExampleActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StrictMode.enableDefaults()
        val example = Navigation.getArgument(intent)
        if (example != null) program(example.cls)
        else finish()
    }
}