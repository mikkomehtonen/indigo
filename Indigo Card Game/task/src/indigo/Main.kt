package indigo

internal class GameOverException : RuntimeException()

fun main() {
    println("Indigo Card Game")

    // Prepare game
    val deck = Deck()
    deck.shuffle()
    val players = listOf(HumanPlayer(), AiPlayer())
    val tableCards = mutableListOf<Card>()
    tableCards.addAll(deck.take(INITIAL_CARDS_NBR))
    val firstPlayer = decideFirstPlayer()
    var prevWinningPlayer: Player? = null
    var turn = firstPlayer

    try {
        println("Initial cards on the table: ${tableCards.cardsString()}")
        while (dealCards(players, deck)) {
            println()
            printTableCards(tableCards)
            val player = players[turn.ordinal]
            val card = player.playTurn(tableCards.lastOrNull())
            if (tableCards.isNotEmpty() && card.isSameRankOrSuit(tableCards.last())) {
                println("${player.name} wins cards")
                val wonCards = tableCards + card
                tableCards.clear()
                player.addWonCards(wonCards)
                prevWinningPlayer = player
                printScore(players, false, firstPlayer)
            } else {
                tableCards.add(card)
            }
            turn = nextPlayer(turn)
        }
        printTableCards(tableCards)
        if (tableCards.isNotEmpty()) {
            prevWinningPlayer?.addWonCards(tableCards) ?: {
                players[firstPlayer.ordinal].addWonCards(tableCards)
            }
        }
        printScore(players, true, firstPlayer)
    } catch (_: GameOverException) { }
    println("Game Over")
}

private fun decideFirstPlayer(): Turn {
    var playFirst: String
    do {
        println("Play first?")
        playFirst = readln()
    } while (playFirst.lowercase() !in listOf("yes", "no"))
    return when (playFirst) {
        "yes" -> Turn.Player
        else -> Turn.Computer
    }
}

/**
 * Return true unless game is finished
 */
private fun dealCards(players: List<Player>, deck: Deck): Boolean {
    if (players.all { it.handCards.isEmpty() }) {
        // Deal more cards
        if (deck.isEmpty()) {
            // No more cards left
            return false
        } else {
            players.forEach { it.addHandCards(deck.take(DEAL_CARDS_NBR)) }
        }
    }
    return true
}

private fun printTableCards(tableCards: List<Card>) {
    if (tableCards.isEmpty()) {
        println("No cards on the table")
    } else {
        println("${tableCards.size} cards on the table, and the top card is ${tableCards.last()}")
    }
}

private fun nextPlayer(turn: Turn): Turn = when (turn) {
    Turn.Player -> Turn.Computer
    else -> Turn.Player
}

private fun printScore(players: List<Player>, final: Boolean, firstPlayer: Turn) {
    val score = calculateScore(players, final, firstPlayer)
    println("Score: ${players[0].name} ${score[0]} - ${players[1].name} ${score[1]}")
    println("Cards: ${players[0].cardsString()} - ${players[1].cardsString()}")
}

private fun calculateScore(players: List<Player>, final: Boolean, firstPlayer: Turn): List<Int> {
    val scores = players.map { player -> player.wonCards.count { card -> card.isHighRank()} }.toMutableList()
    if (final) {
        val cardCount = players.map { it.wonCards.count() }
        when {
            cardCount[0] == cardCount[1] -> scores[firstPlayer.ordinal] += MOST_CARDS_POINTS
            cardCount[0] > cardCount[1] -> scores[0] += MOST_CARDS_POINTS
            cardCount[0] < cardCount[1] -> scores[1] += MOST_CARDS_POINTS
        }
    }
    return scores
}

private enum class Turn {
    Player,
    Computer,
}

private const val MOST_CARDS_POINTS = 3
private const val INITIAL_CARDS_NBR = 4
private const val DEAL_CARDS_NBR = 6

internal fun List<Card>.cardsString() = this.joinToString(separator = " ")
