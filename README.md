# Litho-Elmish

Port of Elm architecture to Kotlin with Litho

# Simple example

Original Elm code: http://elm-lang.org/examples/buttons

```kotlin
object Screen : ElmFunctions<Int, Msg> {

    enum class Msg { Increase, Decrease }

    override fun init(): Pair<Int, Cmd<Msg>> = 0 to Cmd.none()

    override fun update(model: Int, msg: Msg): Pair<Int, Cmd<Msg>> = when (msg) {
        Increase -> (model + 1) to Cmd.none()
        Decrease -> (model - 1) to Cmd.none()
    }

    override fun view(model: Int) =
        column {
            text {
                text("-")
                textSizeSp(45f)
                onClick(Decrease)
            }
            text {
                text("$model")
                textSizeSp(45f)
            }
            text {
                text("+")
                textSizeSp(45f)
                onClick(Increase)
            }
        }
}
```

# Example applications

- https://github.com/y2k/SmsToTelegram
- https://github.com/y2k/LithoApp
- https://github.com/y2k/OfflineTube
