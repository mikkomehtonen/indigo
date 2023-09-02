package indigo

internal data class Card(val suit: String, val rank: String) {
    override fun toString() = "$rank$suit"

    fun isSameRankOrSuit(other: Card) = other.rank == rank || other.suit == suit
    fun isHighRank() = rank in highRank

    companion object {
        val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val highRank = listOf("A", "10", "J", "Q", "K")
        val suits = listOf("♦", "♥", "♠", "♣")
    }
}

internal class Deck {
    private val cards: MutableList<Card>

    init {
        cards = Card.suits.flatMap { suit -> Card.ranks.map { rank -> Card(rank = rank, suit = suit) } }.toMutableList()
    }

    fun shuffle() {
        cards.shuffle()
    }

    fun take(number: Int): List<Card> {
        if (number !in 0..cards.size) {
            throw IllegalArgumentException()
        }
        val takenCards = cards.take(number)
        cards.removeAll(takenCards)
        return takenCards
    }

    fun isEmpty() = cards.isEmpty()
}
