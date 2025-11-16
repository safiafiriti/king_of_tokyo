package com.example.kingoftokyo.ui.game

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

    private val _dice = MutableLiveData<List<DiceSymbol>>()
    val dice: LiveData<List<DiceSymbol>> = _dice

    private val _currentPlayerIndex = MutableLiveData<Int>()
    val currentPlayerIndex: LiveData<Int> = _currentPlayerIndex

    private val _gameEnded = MutableLiveData<Boolean>()
    val gameEnded: LiveData<Boolean> = _gameEnded

    private val _winnerId = MutableLiveData<Int?>()
    val winnerId: LiveData<Int?> = _winnerId

    init {
        gameManager.initGame(listOf("Human", "Bot 1", "Bot 2", "Bot 3"))
        gameManager.startTurn()
        refreshState()
    }

    fun onRollClicked() {
        gameManager.rollDice()
        refreshState()
    }

    fun onRerollAll() {
        gameManager.rerollDice(listOf(0, 1, 2, 3, 4, 5))
        refreshState()
    }

    fun onValidateClicked() {
        gameManager.resolveDice()
        refreshState()

        if (gameManager.gameEnded) {
            _gameEnded.value = true
            _winnerId.value = gameManager.winnerId
        } else {
            gameManager.endTurn()
            gameManager.startTurn()
            refreshState()
        }
    }

    private fun refreshState() {
        _players.value = gameManager.players.toList()
        _dice.value = gameManager.dice.toList()
        _currentPlayerIndex.value = gameManager.currentPlayerIndex
        _gameEnded.value = gameManager.gameEnded
        _winnerId.value = gameManager.winnerId
    }


}