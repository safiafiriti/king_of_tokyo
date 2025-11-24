package com.example.kingoftokyo.game.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kingoftokyo.game.engine.GameManager
import com.example.kingoftokyo.game.model.DiceSymbol
import com.example.kingoftokyo.game.model.Player

class GameViewModel : ViewModel() {

    private val gameManager = GameManager()

    private val _players = MutableLiveData<List<Player>>()
    val players: LiveData<List<Player>> = _players

    private var humanPlayerSelectedMonster: String? = null

    private val _dice = MutableLiveData<List<DiceSymbol>>()
    val dice: LiveData<List<DiceSymbol>> = _dice

    private val _currentPlayerIndex = MutableLiveData<Int>()
    val currentPlayerIndex: LiveData<Int> = _currentPlayerIndex

    private val _gameEnded = MutableLiveData<Boolean>()
    val gameEnded: LiveData<Boolean> = _gameEnded

    private val _winnerId = MutableLiveData<Int?>()
    val winnerId: LiveData<Int?> = _winnerId

    private val _rollsThisTurn = MutableLiveData<Int>()
    val rollsThisTurn: LiveData<Int> = _rollsThisTurn

    private val _diceLocked = MutableLiveData<List<Boolean>>()
    val diceLocked: LiveData<List<Boolean>> = _diceLocked

    init {
        gameManager.initGame(listOf("Player 1", "Bot 1", "Bot 2", "Bot 3"))
        gameManager.startTurn()
        _rollsThisTurn.value = 0
        refreshState()
    }

    fun startNewGame(playerNames: List<String>) {
        gameManager.initGame(playerNames)
        gameManager.startTurn()
        _rollsThisTurn.value = 0
        refreshState()
    }

    fun startGameWithSelectedMonster(selectedMonster: String) {
        humanPlayerSelectedMonster = selectedMonster
        // Assign the human player to the selected monster
        val monsters = listOf("Rexmaw", "Meko", "GrumpJaw", "VenomJunch")
        val remainingMonsters = monsters - selectedMonster
        gameManager.initGame(listOf(selectedMonster) + remainingMonsters)
        gameManager.startTurn()
        refreshState()
    }

    fun onRollClicked() {
        gameManager.rollDice()
        val current = _rollsThisTurn.value ?: 0
        _rollsThisTurn.value = current + 1
        refreshState()
    }

    fun onToggleDice(index: Int) {
        gameManager.toggleLock(index)
        refreshState()
    }

    fun onValidateClicked() {
        gameManager.resolveDice()
        refreshState()

        if (gameManager.gameEnded) {
            _gameEnded.value = true
            _winnerId.value = gameManager.winnerId
            return
        }

        gameManager.endTurn()
        gameManager.startTurn()
        _rollsThisTurn.value = 0
        refreshState()
    }


    private fun refreshState() {
        _players.value = gameManager.players.toList()
        _dice.value = gameManager.dice.map { it.symbol }
        _diceLocked.value = gameManager.dice.map { it.locked }
        _currentPlayerIndex.value = gameManager.currentPlayerIndex
        _gameEnded.value = gameManager.gameEnded
        _winnerId.value = gameManager.winnerId
    }
}
