package y2k.litho.elmish.examples

import android.graphics.Color
import android.graphics.Typeface
import com.facebook.litho.Component.ContainerBuilder
import com.facebook.yoga.YogaEdge
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.experimental.delay
import kotlinx.types.Result
import kotlinx.types.Result.Error
import kotlinx.types.Result.Ok
import y2k.litho.elmish.examples.Blackjack.Game
import y2k.litho.elmish.examples.Blackjack.Hand
import y2k.litho.elmish.examples.GameExample.Action.*
import y2k.litho.elmish.examples.GameExample.Msg
import y2k.litho.elmish.examples.GameExample.Msg.Clicked
import y2k.litho.elmish.examples.GameExample.Msg.NewGame
import y2k.litho.elmish.examples.common.Log
import y2k.litho.elmish.examples.common.button
import y2k.litho.elmish.examples.common.subList
import y2k.litho.elmish.experimental.*
import java.util.*

class GameExample : ElmFunctions<Game, Msg> {

    sealed class Msg {
        class NewGame(val game: Result<Game, Exception>) : Msg()
        class Clicked(val action: Action) : Msg()
    }

    enum class Action { Deal, Hit, Stay }

    override fun init(): Pair<Game, Cmd<Msg>> =
        Blackjack.createStub() to Cmd.fromContext({ Blackjack.deal() }, ::NewGame)

    override fun update(model: Game, msg: Msg): Pair<Game, Cmd<Msg>> = when (msg) {
        is NewGame -> when (msg.game) {
            is Ok -> msg.game.value to Cmd.none()
            is Error -> Log.log(msg.game.error, model) to Cmd.none()
        }
        is Clicked -> when (msg.action) {
            Deal -> Blackjack.createStub() to Cmd.fromContext({ Blackjack.deal() }, ::NewGame)
            Hit -> Blackjack.hit(model) to Cmd.none()
            Stay -> Blackjack.stay(model) to Cmd.none()
        }
    }

    override fun ContainerBuilder<*>.view(model: Game) {
        backgroundColor(0xFFF0F0F0.toInt())
        text {
            textSizeSp(35f)
            text("Blackjack")
        }

        if (!Blackjack.isStub(model)) {
            buttonBar(model)

            row {
                marginDip(YogaEdge.TOP, 4f)

                handUi(model.ph)
                handUi(model.dh)
            }
        }
    }

    private fun ContainerBuilder<*>.buttonBar(g: Game) =
        row {
            button("Deal", Clicked(Deal))
            button("Hit", Clicked(Hit), !Blackjack.isCanHit(g))
            button("Stay", Clicked(Stay), !Blackjack.isCanStay(g))
        }

    private fun ContainerBuilder<*>.handUi(h: Hand) =
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
                text("${h.cards.sumBy { it.value }} Point")
            }
        }
}

object Blackjack {

    data class Game(val ph: Hand, val dh: Hand, val deck: ImmutableList<Card>)
    data class Hand(val cards: ImmutableList<Card>, val name: String)
    data class Card(val name: String, val value: Int)

    fun createStub(): Game =
        Game(Hand(immutableListOf(), ""), Hand(immutableListOf(), ""), immutableListOf())

    suspend fun deal(): Game {
        delay(300) // Fake delay for shuffle
        return deal(System.currentTimeMillis())
    }

    private fun deal(random: Long): Game {
        val deck = createDeck(random)
        return Game(
            ph = Hand(deck.subList(0, 2), "Player"),
            dh = Hand(deck.subList(2).subList(0, 2), "Dialer"),
            deck = deck.subList(4))
    }

    private val numberOfDecks = 8
    private val suits = listOf("Spades", "Diamonds", "Hearts", "Clubs")
    private val ranks = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace")
    private val value = listOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 1)

    private fun createDeck(random: Long): ImmutableList<Card> =
        (1..numberOfDecks)
            .flatMap {
                suits.flatMap { suit ->
                    ranks.map { rank ->
                        Card(
                            name = rank + " of " + suit,
                            value = value[ranks.indexOf(rank)])
                    }
                }
            }
            .shuffled(Random(random))
            .toImmutableList()

    fun hit(game: Game): Game {
        if (isStub(game) || game.ph.isWinOrLost || game.dh.isWin) return game

        val playerCard = game.deck.subList(0, 1)
        val newPlayerCards = game.ph.cards.addAll(playerCard)

        if (newPlayerCards.isLost || game.isDealerNeedStop || game.dh.sum >= newPlayerCards.sum)
            return game.copy(
                ph = game.ph.copy(cards = newPlayerCards),
                deck = game.deck.subList(1))

        val dealerCard = game.deck.subList(1, 2)
        return game.copy(
            ph = game.ph.copy(cards = newPlayerCards),
            dh = game.dh.copy(cards = game.dh.cards.addAll(dealerCard)),
            deck = game.deck.subList(2))
    }

    fun stay(game: Game): Game {
        if (isStub(game)
            || game.ph.isWinOrLost
            || game.dh.isWinOrLost
            || game.dh.sum >= game.ph.sum) return game
        val dealerCard = game.deck.subList(0, 1)
        return game
            .copy(
                dh = game.dh.copy(cards = game.dh.cards.addAll(dealerCard)),
                deck = game.deck.subList(1))
            .let(::stay)
    }

    fun isCanHit(g: Game): Boolean = g != hit(g)
    fun isCanStay(g: Game): Boolean = g != stay(g)
    fun isStub(game: Game) = game.deck.isEmpty()

    private val Hand.isWinOrLost get() = isWin || isLost
    private val Hand.isWin: Boolean get() = cards.sum == 21
    private val Hand.sum: Int get() = cards.sum
    private val Game.isDealerNeedStop get() = dh.cards.sum > 17
    private val Hand.isLost get() = cards.isLost
    private val List<Card>.isLost get() = sum > 21
    private val List<Card>.sum get() = sumBy(Card::value)
}