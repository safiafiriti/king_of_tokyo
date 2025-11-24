package com.example.kingoftokyo.ui.game

import android.os.Bundle
import android.view.View
import com.example.kingoftokyo.game.viewmodel.GameViewModel
import com.example.kingoftokyo.game.engine.GameManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.kingoftokyo.R
import com.example.kingoftokyo.game.model.DiceSymbol

class GameFragment : Fragment(R.layout.fragment_game) {

    private val viewModel: GameViewModel by activityViewModels()

    private lateinit var roundTitle: TextView
    private lateinit var rollButton: Button
    private lateinit var validateButton: Button
    private lateinit var diceViews: List<ImageView>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        roundTitle = view.findViewById(R.id.round_title_text)
        rollButton = view.findViewById(R.id.rollButton)
        validateButton = view.findViewById(R.id.validateButton)

        diceViews = listOf(
            view.findViewById(R.id.dice1),
            view.findViewById(R.id.dice2),
            view.findViewById(R.id.dice3),
            view.findViewById(R.id.dice4),
            view.findViewById(R.id.dice5),
            view.findViewById(R.id.dice6)
        )

        if (viewModel.players.value.isNullOrEmpty()) {
            viewModel.startNewGame(
                listOf(
                    "You",
                    "Bot 1",
                    "Bot 2",
                    "Bot 3"
                )
            )
        }

        rollButton.setOnClickListener {
            viewModel.onRollClicked()
        }

        validateButton.setOnClickListener {
            viewModel.onValidateClicked()
        }

        diceViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                val rolls = viewModel.rollsThisTurn.value ?: 0
                val ended = viewModel.gameEnded.value == true

                if (rolls == 0 || ended) return@setOnClickListener

                viewModel.onToggleDice(index)
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.dice.observe(viewLifecycleOwner) { diceSymbols ->
            if (diceSymbols.isNullOrEmpty()) return@observe

            diceSymbols.forEachIndexed { index, symbol ->
                if (index < diceViews.size) {
                    diceViews[index].setImageResource(symbolToDrawable(symbol))
                }
            }
        }

        viewModel.diceLocked.observe(viewLifecycleOwner) { lockedList ->
            lockedList.forEachIndexed { index, locked ->
                if (index < diceViews.size) {
                    diceViews[index].alpha = if (locked) 0.5f else 1f
                }
            }
        }

        viewModel.currentPlayerIndex.observe(viewLifecycleOwner) { index ->
            val players = viewModel.players.value
            val name = players?.getOrNull(index)?.name ?: "Player ${index + 1}"
            roundTitle.text = "Turn: $name"
        }

        viewModel.rollsThisTurn.observe(viewLifecycleOwner) { rolls ->
            rollButton.text = if (rolls == 0) "Roll" else "Reroll"

            val canRoll = rolls < 3 && viewModel.gameEnded.value != true
            rollButton.isEnabled = canRoll
            rollButton.alpha = if (canRoll) 1f else 0.5f

            val canValidate = rolls > 0 && viewModel.gameEnded.value != true
            validateButton.isEnabled = canValidate
            validateButton.alpha = if (canValidate) 1f else 0.5f
        }

        viewModel.gameEnded.observe(viewLifecycleOwner) { ended ->
            if (ended == true) {
                rollButton.isEnabled = false
                validateButton.isEnabled = false

                val winnerId = viewModel.winnerId.value
                val winnerName = viewModel.players.value
                    ?.firstOrNull { it.id == winnerId }
                    ?.name ?: "Unknown"

                Toast.makeText(
                    requireContext(),
                    "Game over! Winner: $winnerName",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun symbolToDrawable(symbol: DiceSymbol): Int {
        return when (symbol) {
            DiceSymbol.CLAW   -> R.drawable.dice_claw
            DiceSymbol.HEART  -> R.drawable.dice_heart
            DiceSymbol.ENERGY -> R.drawable.dice_energy
            DiceSymbol.ONE    -> R.drawable.dice_one
            DiceSymbol.TWO    -> R.drawable.dice_two
            DiceSymbol.THREE  -> R.drawable.dice_three
        }
    }
}
