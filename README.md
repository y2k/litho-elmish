# Litho-Elmish

Port of Elm architecture to Kotlin with Litho

![Release](https://jitpack.io/v/y2k/litho-elmish.svg)
(https://jitpack.io/#y2k/litho-elmish)

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

    override fun ContainerBuilder.view(model: Int) {
        text {
            text("-")
            onClick(Decrease)
        }
        text {
            text("$model")
        }
        text {
            text("+")
            onClick(Increase)
        }
    }
}
```

# Example applications

- https://github.com/y2k/SmsToTelegram
- https://github.com/y2k/LithoApp
- https://github.com/y2k/OfflineTube
