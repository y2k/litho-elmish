# Litho-Elmish

Port of Elm architecture to Kotlin with Litho

# Simple example

```kotlin
object Screen : ElmFunctions<Int, Screen.Msg> {

    enum class Msg { Increase, Decrease }

    override fun init(): Pair<Int, Msg> = 0 to Cmd.none()

    override fun update(model: Int, msg: Msg): Pair<Int, Msg> = when (msg) {
        Increase -> (model + 1) to Cmd.none()
        Decrease -> (model - 1) to Cmd.none()
    }

    override fun view(model: Int) =
        column {
            childText { layout ->
                text("-")
                textSizeSp(45f)
                onClick(layout, Decrease)
            }
            childText {
                text("$model")
                textSizeSp(45f)
            }
            childText { layout ->
                text("+")
                textSizeSp(45f)
                onClick(layout, Increase)
            }
        }
}
```
