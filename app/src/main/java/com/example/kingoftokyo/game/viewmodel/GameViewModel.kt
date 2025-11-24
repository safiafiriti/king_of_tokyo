package com.example.kingoftokyo.game.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kingoftokyo.game.engine.GameManager
import com.example.kingoftokyo.game.model.Card
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

    // LiveData pour la boutique
    private val _marketCards = MutableLiveData<List<Card?>>()
    val marketCards: LiveData<List<Card?>> = _marketCards

    // Evénement pour ouvrir la popup (True = ouvrir)
    private val _showShopEvent = MutableLiveData<Boolean>()
    val showShopEvent: LiveData<Boolean> = _showShopEvent


    fun startNewGame(playerNames: List<String>) {
        gameManager.initGame(playerNames)
        gameManager.startTurn()
        _rollsThisTurn.value = 0
        refreshState()
    }

    fun onValidateClicked() {

        // 0️⃣ Lock ALL the dice visually + logically
        gameManager.lockAllDice()
        refreshState()

        // 1 On résout les dés (soins, dégâts, VP, gains d'énergie)
        gameManager.resolveDice()
        refreshState()

        // 2 Si le jeu est fini on arrête tout
        if (gameManager.gameEnded) {
            _gameEnded.value = true
            _winnerId.value = gameManager.winnerId
            return
        }

        val player = gameManager.currentPlayer

        // 3 LOGIQUE BOUTIQUE :
        // Si c'est un humain et qu'il a assez d'énergie (>= 3)-> on ouvre la boutique
        // Sinonon termine le tour
        if (player.isHuman && player.energy >= 3) {
            _marketCards.value = gameManager.marketCards.toList()
            _showShopEvent.value = true
        } else {
            endTurnProperly()
        }
    }


    fun onBuyCard(index: Int) {
        val player = gameManager.currentPlayer
        val success = gameManager.buyCard(player, index)
        if (success) {
            refreshState()
            // Met à jour la boutique pour voir la nouvelle carte (si le joueur veut en acheter une autre)
            _marketCards.value = gameManager.marketCards.toList()
        }
    }

    fun onCloseShop() {
        _showShopEvent.value = false
        endTurnProperly()
    }

    private fun endTurnProperly() {
        gameManager.endTurn()
        gameManager.startTurn()
        _rollsThisTurn.value = 0
        refreshState()
    }
    fun startGameWithSelectedMonster(selectedMonster: String) {
        humanPlayerSelectedMonster = selectedMonster
        // Assign the human player to the selected monster
        val monsters = listOf("rexmaw", "meko", "grumpjaw", "venomjunch")
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



    private fun refreshState() {
        _players.value = gameManager.players.toList()
        _dice.value = gameManager.dice.map { it.symbol }
        _diceLocked.value = gameManager.dice.map { it.locked }
        _currentPlayerIndex.value = gameManager.currentPlayerIndex
        _gameEnded.value = gameManager.gameEnded
        _winnerId.value = gameManager.winnerId
    }
}
