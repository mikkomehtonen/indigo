package indigo

import kotlin.random.Random

internal interface Player {
    val name: String
    val handCards: List<Card>
    val wonCards: List<Card>

    fun playTurn(topCard: Card?): Card
    fun addWonCards(cards: List<Card>)
    fun addHandCards(cards: List<Card>)
    fun cardsString() = "$name ${wonCards.size}"
}

internal abstract class BasePlayer : Player {
    private val _handCards = mutableListOf<Card>()
    override val handCards: List<Card>
        get() = _handCards

    private val _wonCards = mutableListOf<Card>()
    override val wonCards: List<Card>
        get() = _wonCards

    override fun addWonCards(cards: List<Card>) {
        _wonCards.addAll(cards)
    }

    override fun addHandCards(cards: List<Card>) {
        _handCards.addAll(cards)
    }

    protected fun removeHandCardAt(index: Int) = _handCards.removeAt(index)
    protected fun removeFirstHandCard() = _handCards.removeFirst()
    protected fun removeFromHandCards(card: Card) = _handCards.remove(card)
}

internal class HumanPlayer : BasePlayer() {
    override val name = "Player"

    override fun playTurn(topCard: Card?): Card {
        println("Cards in hand: ${handCards.indexedCardsString()}")
        var cardNumber = 0
        do {
            println("Choose a card to play (1-${handCards.size}):")
            cardNumber = try {
                val input = readln()
                if (input == "exit") {
                    throw GameOverException()
                }
                input.toInt()
            } catch (e: NumberFormatException) {
                continue
            }
        } while (cardNumber !in 1..handCards.size)
        return removeHandCardAt(cardNumber - 1)
    }
}

internal class AiPlayer : BasePlayer() {
    override val name = "Computer"

    override fun playTurn(topCard: Card?): Card {
        println(handCards.cardsString()) // For testing

        val candidateCards = topCard?.let { handCards.filter { it.isSameRankOrSuit(topCard) } } ?: emptyList()
        val card = when {
            // Case 1: Only 1 card in hand
            handCards.size == 1 -> {
                removeFirstHandCard()
            }
            // Case 2: Only 1 candidate card
            candidateCards.size == 1 -> {
                val card = candidateCards.first()
                removeFromHandCards(card)
                card
            }
            // Case 3 & 4: No cards on table or no candidate cards
            topCard == null || candidateCards.isEmpty() -> {
                val multipleSuits = handCards.groupBy { it.suit }.filter { it.value.size > 1 }.flatMap {  it.value }
                val multipleRanks = handCards.groupBy { it.rank }.filter { it.value.size > 1 }.flatMap { it.value }
                val cardsToChooseFrom = when {
                    multipleSuits.isNotEmpty() -> multipleSuits
                    multipleRanks.isNotEmpty() -> multipleRanks
                    else -> handCards
                }
                val card = cardsToChooseFrom[Random.nextInt(cardsToChooseFrom.size)]
                removeFromHandCards(card)
                card
            }
            // Case 5: Two or more candidate cards
            else -> {
                val multipleSuits = handCards
                    .filter { it.suit == topCard.suit }
                    .groupBy { it.suit }
                    .filter { it.value.size > 1 }
                    .flatMap { it.value }
                val multipleRanks = handCards
                    .filter { it.rank == topCard.rank }
                    .groupBy { it.rank }
                    .filter { it.value.size > 1 }
                    .flatMap { it.value }
                val cardsToChooseFrom = when {
                    multipleSuits.isNotEmpty() -> multipleSuits
                    multipleRanks.isNotEmpty() -> multipleRanks
                    else -> candidateCards
                }
                val card = cardsToChooseFrom[Random.nextInt(cardsToChooseFrom.size)]
                removeFromHandCards(card)
                card
            }
        }
        println("Computer plays $card")
        return card
    }
}

internal fun List<Card>.indexedCardsString() = this.mapIndexed { i, c -> "${i + 1})$c" }.joinToString(separator = " ")
