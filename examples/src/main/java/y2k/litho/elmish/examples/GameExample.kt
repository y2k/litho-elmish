package y2k.litho.elmish.examples

import android.graphics.Color
import android.graphics.Typeface
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.yoga.YogaEdge
import y2k.litho.elmish.examples.GameExample.Action.*
import y2k.litho.elmish.examples.GameExample.Model
import y2k.litho.elmish.examples.GameExample.Msg
import y2k.litho.elmish.examples.GameExample.Msg.Clicked
import y2k.litho.elmish.examples.Games.Game
import y2k.litho.elmish.examples.Games.Hand
import y2k.litho.elmish.experimental.*
import y2k.litho.elmish.experimental.Views.column

class GameExample : ElmFunctions<Model, Msg> {
    data class Model(val g: Game)
    sealed class Msg {
        class Clicked(val action: Action) : Msg()
    }

    enum class Action { Deal, Hit, Stay }

    override fun init(): Pair<Model, Cmd<Msg>> =
        Model(Games.deal()) to Cmd.none()

    override fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> =
        when (msg) {
            is Clicked -> when (msg.action) {
                Deal -> model.copy(g = Games.deal()) to Cmd.none()
                Hit -> model.copy(g = Games.hit(model.g)) to Cmd.none()
                Stay -> model.copy(g = Games.stay(model.g)) to Cmd.none()
            }
        }

    override fun view(model: Model) =
        column {
            backgroundColor(0xFFF0F0F0.toInt())

            buttonBar()

            text {
                textSizeSp(35f)
                text("Blackjack")
            }
            row {
                handUi(model.g.ph)
                handUi(model.g.dh)
            }
        }

    private fun ContainerBuilder.buttonBar() {
        row {
            button("Deal", Clicked(Deal))
            button("Hit", Clicked(Hit))
            button("Stay", Clicked(Stay))
        }
    }

    private fun ContainerBuilder.button(title: String, msg: Msg) {
        text {
            marginDip(YogaEdge.RIGHT, 4f)
            paddingDip(YogaEdge.ALL, 8f)
            backgroundRes(R.drawable.button_simple)
            text(title)
            textSizeSp(30f)
            onClick(msg)
        }
    }

    private fun ContainerBuilder.handUi(h: Hand) {
        column {
            backgroundColor(Color.CYAN)
            marginDip(YogaEdge.RIGHT, 4f)
            minWidthDip(150f)
            minHeightDip(150f)

            text {
                textSizeSp(25f)
                textStyle(Typeface.BOLD)
                text("${h.name} Hand")
            }
            column {
                h.cards.forEach {
                    text {
                        textSizeSp(25f)
                        text(it.name)
                    }
                }
            }
            text {
                textSizeSp(25f)
                textStyle(Typeface.BOLD)
                text("${h.points} Point")
            }
        }
    }
}

object Games {
    data class Game(val ph: Hand, val dh: Hand)
    data class Hand(val cards: List<Card>, val name: String, val points: Int)
    data class Card(val name: String)

    fun deal() =
        Game(
            ph = Hand(emptyList(), "Player", 12),
            dh = Hand(emptyList(), "Dialer", 20))

    fun hit(g: Game) =
        g.copy(ph = g.ph.copy(cards = g.ph.cards + Card("" + Math.random())))

    fun stay(g: Game) =
        g.copy(ph = g.ph.copy(cards = g.ph.cards + Card("" + Math.random())))
}