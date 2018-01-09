package y2k.litho.elmish.examples

import android.graphics.Color
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.yoga.YogaEdge.ALL
import kotlinx.coroutines.experimental.delay
import kotlinx.types.Result
import y2k.litho.elmish.examples.FormFunctions.FeedbackResponse
import y2k.litho.elmish.examples.FormFunctions.FeedbackResponse.*
import y2k.litho.elmish.examples.FormFunctions.toDomainModel
import y2k.litho.elmish.examples.FormFunctions.validateForEmpty
import y2k.litho.elmish.examples.FormScreen.Model
import y2k.litho.elmish.examples.FormScreen.Msg
import y2k.litho.elmish.examples.FormScreen.Msg.*
import y2k.litho.elmish.examples.common.editTextWithLabel
import y2k.litho.elmish.examples.common.map3Option
import y2k.litho.elmish.examples.common.valueOrDefault
import y2k.litho.elmish.examples.common.zipOption
import y2k.litho.elmish.experimental.*
import java.util.*

class FormScreen : ElmFunctions<Model, Msg> {

    sealed class Msg {
        class MessageChanged(val x: String) : Msg()
        class FullNameChanged(val x: String) : Msg()
        class EmailChanged(val x: String) : Msg()
        object Send : Msg()
        class SendResult(val result: Result<FeedbackResponse, *>) : Msg()
    }

    data class Model(
        val message: String? = null,
        val fullName: String? = null,
        val email: String? = null,
        val inProgress: Boolean = false,
        val finishStatus: FeedbackResponse? = null)

    override fun init(): Pair<Model, Cmd<Msg>> = Model() to Cmd.none()

    override fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> = when (msg) {
        is MessageChanged ->
            model.copy(message = validateForEmpty(msg.x)) to Cmd.none()
        is FullNameChanged ->
            model.copy(fullName = validateForEmpty(msg.x)) to Cmd.none()
        is EmailChanged ->
            model.copy(email = validateForEmpty(msg.x)) to Cmd.none()
        Send -> {
            val sendModel = toDomainModel(model)
            if (sendModel == null) model to Cmd.none()
            else {
                model.copy(finishStatus = null, inProgress = true) to
                    Cmd.fromContext({ FormFunctions.sendFeedback(sendModel) }, ::SendResult)
            }
        }
        is SendResult -> model.copy(
            finishStatus = msg.result.valueOrDefault { UnknownError },
            inProgress = false) to Cmd.none()
    }

    override fun ContainerBuilder.view(model: Model) {
        paddingDip(ALL, 4f)

        editTextWithLabel(
            "Message",
            ::MessageChanged,
            "Field required".takeIf { model.message == null })
        editTextWithLabel(
            "Full name",
            ::FullNameChanged,
            "Field required".takeIf { model.fullName == null })
        editTextWithLabel(
            "Email",
            ::EmailChanged,
            "Field required".takeIf { model.email == null })

        if (model.inProgress)
            progress {
                widthDip(60f)
                heightDip(60f)
            }
        else
            column {
                backgroundRes(R.drawable.button_bg)
                widthDip(60f)
                heightDip(60f)
                onClick(Send)
            }

        when (model.finishStatus) {
            null -> column { }
            Success ->
                text {
                    text("Feedback was successfully sent")
                    textSizeSp(20f)
                }
            else ->
                text {
                    text(model.finishStatus.toUserMessage())
                    textSizeSp(20f)
                    textColor(Color.RED)
                }

        }
    }

    private fun FeedbackResponse.toUserMessage() = when (this) {
        is FailureNetwork -> "Network error"
        is FailureServer -> "Server error"
        Success -> "Success"
        UnknownError -> "Unknown error"
    }
}

object FormFunctions {

    fun validateForEmpty(x: String?): String? =
        if (x.isNullOrBlank()) null else x

    sealed class FeedbackResponse {
        object Success : FeedbackResponse()
        class FailureNetwork(val message: String) : FeedbackResponse()
        class FailureServer(val message: String) : FeedbackResponse()
        object UnknownError : FeedbackResponse()
    }

    class FeedbackSendModel(
        val message: String,
        val fullName: String,
        val email: String)

    fun toDomainModel(presentModel: Model): FeedbackSendModel? =
        presentModel.message
            .zipOption(presentModel.fullName)
            .zipOption(presentModel.email)
            .map3Option(::FeedbackSendModel)

    suspend fun sendFeedback(x: FeedbackSendModel): FeedbackResponse {
        println("Send feedback: ${x.message}, ${x.fullName}, ${x.email}")
        delay(1000)
        return when (Random().nextInt(4)) {
            0 -> FailureNetwork("...")
            1 -> FailureServer("...")
            2 -> throw Exception()
            else -> Success
        }
    }
}