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
import y2k.litho.elmish.examples.common.Сmd
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

    fun init(): Pair<Model, Сmd<Msg>> = Model() to null

    fun update(model: Model, msg: Msg): Pair<Model, Сmd<Msg>> = when (msg) {
        is InputChanged ->
            model.copy(input = msg.input, isInputValid = FlowDomain.validate(model.stage, msg.input))
        is ToNextStage -> when (msg.current) {
            Home ->
                Model(Addresses, itemNumbers = model.input.toInt())
            Addresses ->
                model.copy(
                    stage = if (model.shippingAddresses.size + 1 == model.itemNumbers) Billing else Addresses,
                    shippingAddresses = model.shippingAddresses + model.input,
                    input = "")
            Billing ->
                model.copy(stage = Home, billingAddress = model.input, input = "")
        }
    } to null

    fun ContainerBuilder.view(model: Model) {
        paddingDip(YogaEdge.ALL, 8f)
        justifyContent(YogaJustify.CENTER)

        when (model.stage) {
            Home -> homeView(model)
            Addresses -> addressesView(model)
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
            text(FlowDomain.makeBillingMessage(model))
        }
    }

    private fun ContainerBuilder.addressesView(model: Model) {
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
}

object FlowDomain {

    private val numberRegex = Regex("^([1-9]|[1-9][0-9])$")
    private val notEmptyRegex = Regex("^[^ ]+$")

    fun validate(stage: FlowExample.Stage, x: String): Boolean = when (stage) {
        Home -> numberRegex
        Addresses -> notEmptyRegex
        Billing -> notEmptyRegex
    }.let(x::matches)

    fun makeBillingMessage(model: Model): String = when (model.itemNumbers) {
        0 -> "Buy something"
        1 -> "Item will be shipped to ${model.shippingAddresses.single()} and billed to ${model.billingAddress}"
        else -> model.shippingAddresses.joinToString(
            prefix = "Items will be shipped to ",
            postfix = " accordingly. All ${model.itemNumbers} items will be billed to ${model.billingAddress}.")
    }
}

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