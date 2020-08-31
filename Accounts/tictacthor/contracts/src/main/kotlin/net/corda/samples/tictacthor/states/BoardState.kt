package net.corda.samples.tictacthor.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.samples.tictacthor.contracts.BoardContract

@CordaSerializable
enum class Status {
    GAME_IN_PROGRESS, GAME_OVER
}

@BelongsToContract(BoardContract::class)
@CordaSerializable
data class BoardState(
    val playerO: UniqueIdentifier,
    val playerX: UniqueIdentifier,
    val me: Party,
    val competitor: Party,
    val isPlayerXTurn: Boolean = false,
    val board: Array<CharArray> = Array(3, { charArrayOf('E', 'E', 'E') }),
    val status: Status = Status.GAME_IN_PROGRESS,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
): LinearState {

    override val participants: List<AbstractParty> = listOfNotNull(me, competitor).map { it }

    fun isGameOver() = status != Status.GAME_IN_PROGRESS

    // Returns the party of the current player
    fun getCurrentPlayerParty(): UniqueIdentifier = if (isPlayerXTurn) playerX else playerO

    // Get deep copy of board
    private fun Array<CharArray>.copy() = Array(size) { get(it).clone() }

    // Returns a copy of a BoardState object after a move at Pair<x,y>
    fun returnNewBoardAfterMove(pos: Pair<Int, Int>): BoardState {
        if (pos.first > 2 || pos.second > 2) throw IllegalStateException("Invalid board index.")

        val newBoard = board.copy()
        if (isPlayerXTurn) newBoard[pos.second][pos.first] = 'X'
        else newBoard[pos.second][pos.first] = 'O'

        val newBoardState = copy(board = newBoard, isPlayerXTurn = !isPlayerXTurn)
        if (BoardContract.BoardUtils.isGameOver(newBoardState)) return newBoardState.copy(status = Status.GAME_OVER)
        return newBoardState
    }
}

