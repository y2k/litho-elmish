package y2k.litho.elmish.examples

import android.view.inputmethod.EditorInfo
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.yoga.YogaEdge
import com.facebook.yoga.YogaJustify
import y2k.litho.elmish.examples.FlowExample.Model
import y2k.litho.elmish.examples.FlowExample.Msg
import y2k.litho.elmish.examples.FlowExample.Msg.InputChanged
import y2k.litho.elmish.examples.FlowExample.Msg.ToNextStage
import y2k.litho.elmish.examples.FlowExample.Stage.*
import y2k.litho.elmish.examples.common.button
import y2k.litho.elmish.experimental.*

object FlowExample {

    data class Model(
        val stage: Stage = Home,
        val isInputValid: Boolean = false,
        val input: String = "",
        val itemNumbers: Int = 0,
        val shippingAddresses: List<String> = emptyList(),
        val billingAddress: String = "")

    enum class Stage { Home, Addresses, Billing }

    sealed class Msg {
        class InputChanged(val input: String) : Msg()
        class ToNextStage(val current: Stage) : Msg()
    }

    fun init(): Pair<Model, Cmd2<Msg>> = Model() to null

    fun update(model: Model, msg: Msg): Pair<Model, Cmd2<Msg>> = when (msg) {
        is InputChanged -> model.copy(input = msg.input, isInputValid = msg.input.isNotBlank())
        is ToNextStage -> when (msg.current) {
            Home -> model.input
                .tryToIntRange(1..99)
                ?.let { Model(Addresses, itemNumbers = it) } ?: model
            Addresses -> model
                .copy(shippingAddresses = model.shippingAddresses + model.input, input = "")
                .let { it.copy(stage = if (it.shippingAddresses.size == it.itemNumbers) Billing else Addresses) }
            Billing -> model.copy(stage = Home, billingAddress = model.input, input = "")
        }
    } to null

    fun ContainerBuilder.view(model: Model) {
        paddingDip(YogaEdge.ALL, 8f)
        justifyContent(YogaJustify.CENTER)

        when (model.stage) {
            Home -> homeView(model)
            Addresses -> addressView(model)
            Billing -> billingView(model)
        }
    }

    private fun ContainerBuilder.homeView(model: Model) {
        editText {
            textSizeSp(16f)
            inputType(EditorInfo.TYPE_CLASS_NUMBER)
            hint("How many items are you going go buy?")

            onTextChanged(::InputChanged)
        }
        button(
            title = "Begin",
            disabled = !model.isInputValid,
            msg = ToNextStage(Home))

        text {
            textSizeSp(16f)
            text(model.makeBillingMessage())
        }
    }

    private fun ContainerBuilder.addressView(model: Model) {
        editText {
            textSizeSp(16f)
            hint("Shipping address for item #${model.shippingAddresses.size + 1}")

            text(model.input)
            onTextChanged(::InputChanged)
        }
        button(
            title = "Next",
            disabled = !model.isInputValid,
            msg = ToNextStage(Addresses))
    }

    private fun ContainerBuilder.billingView(model: Model) {
        editText {
            textSizeSp(16f)
            hint("Billing address")
            onTextChanged(::InputChanged)
        }
        button(
            title = "Next",
            disabled = !model.isInputValid,
            msg = ToNextStage(Billing))
    }

    private fun String.tryToIntRange(range: IntRange): Int? =
        toIntOrNull()
            ?.let { if (it in range) it else null }

    private fun Model.makeBillingMessage(): String = when (itemNumbers) {
        0 -> "Buy something"
        1 -> "Item will be shipped to ${shippingAddresses.single()} and billed to $billingAddress"
        else -> shippingAddresses.joinToString(
            prefix = "Items will be shipped to ",
            postfix = " accordingly. All $itemNumbers items will be billed to $billingAddress.")
    }
}

typealias Cmd2<T> = (suspend () -> T)?

@Suppress("unused")
class FlowExampleWrapper : ElmFunctions<Model, Msg> {
    override fun init(): Pair<Model, Cmd<Msg>> = FlowExample.init().first to Cmd.none()
    override fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> = FlowExample.update(model, msg).first to Cmd.none()
    override fun ContainerBuilder.view(model: Model) {
        val x = this
        with(FlowExample) {
            x.view(model)
        }
    }
}