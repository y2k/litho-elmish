package y2k.litho.elmish.examples

import com.facebook.litho.Component.ContainerBuilder
import y2k.litho.elmish.experimental.Cmd
import y2k.litho.elmish.experimental.ElmFunctions

@Suppress("unused")
class FlowExampleWrapper : ElmFunctions<FlowExample.Model, FlowExample.Msg> {
    override fun init(): Pair<FlowExample.Model, Cmd<FlowExample.Msg>> = FlowExample.init().first to Cmd.none()
    override fun update(model: FlowExample.Model, msg: FlowExample.Msg): Pair<FlowExample.Model, Cmd<FlowExample.Msg>> = FlowExample.update(model, msg).first to Cmd.none()
    override fun ContainerBuilder<*>.view(model: FlowExample.Model) {
        val x = this
        with(FlowExample) {
            x.view(model)
        }
    }
}