package com.example.kingoftokyo.ui.game

import android.os.Bundle
import android.view.View
import com.example.kingoftokyo.game.viewmodel.GameViewModel
import com.example.kingoftokyo.game.engine.GameManager
import com.example.kingoftokyo.game.model.Location
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.kingoftokyo.R
import com.example.kingoftokyo.game.model.DiceSymbol
import com.example.kingoftokyo.game.model.Player

class GameFragment : Fragment(R.layout.fragment_game) {

    private val viewModel: GameViewModel by activityViewModels()

    private lateinit var roundTitle: TextView
    private lateinit var rollButton: Button
    private lateinit var validateButton: Button
    private lateinit var diceViews: List<ImageView>

    // REXMAW stats
    private lateinit var rexHeart: TextView
    private lateinit var rexStar: TextView
    private lateinit var rexEnergy: TextView

    // MEKO stats
    private lateinit var mekoHeart: TextView
    private lateinit var mekoStar: TextView
    private lateinit var mekoEnergy: TextView

    // GRUMPJAW stats
    private lateinit var grumpHeart: TextView
    private lateinit var grumpStar: TextView
    private lateinit var grumpEnergy: TextView

    // VENOMJUNCH stats
    private lateinit var venomHeart: TextView
    private lateinit var venomStar: TextView
    private lateinit var venomEnergy: TextView


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
        // REXMAW
        rexHeart = view.findViewById<LinearLayout>(R.id.rexmaw_heart).findViewById(R.id.stat_value)
        rexStar = view.findViewById<LinearLayout>(R.id.rexmaw_star).findViewById(R.id.stat_value)
        rexEnergy = view.findViewById<LinearLayout>(R.id.rexmaw_energy).findViewById(R.id.stat_value)

        // MEKO
        mekoHeart = view.findViewById<LinearLayout>(R.id.meko_heart).findViewById(R.id.stat_value)
        mekoStar = view.findViewById<LinearLayout>(R.id.meko_star).findViewById(R.id.stat_value)
        mekoEnergy = view.findViewById<LinearLayout>(R.id.meko_energy).findViewById(R.id.stat_value)

        // GRUMPJAW
        grumpHeart = view.findViewById<LinearLayout>(R.id.grumpJaw_heart).findViewById(R.id.stat_value)
        grumpStar = view.findViewById<LinearLayout>(R.id.grumpJaw_star).findViewById(R.id.stat_value)
        grumpEnergy = view.findViewById<LinearLayout>(R.id.grumpJaw_energy).findViewById(R.id.stat_value)

        // VENOMJUNCH
        venomHeart = view.findViewById<LinearLayout>(R.id.venomJunch_heart).findViewById(R.id.stat_value)
        venomStar = view.findViewById<LinearLayout>(R.id.venomJunch_star).findViewById(R.id.stat_value)
        venomEnergy = view.findViewById<LinearLayout>(R.id.venomJunch_energy).findViewById(R.id.stat_value)


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

        viewModel.players.observe(viewLifecycleOwner) { players ->
            if (players.isNullOrEmpty()) return@observe

            // Reset before updating (optional but clean)
            updateStats(null, rexHeart, rexStar, rexEnergy)
            updateStats(null, mekoHeart, mekoStar, mekoEnergy)
            updateStats(null, grumpHeart, grumpStar, grumpEnergy)
            updateStats(null, venomHeart, venomStar, venomEnergy)

            players.forEach { player ->
                when (player.name) {
                    "rexmaw" -> updateStats(player, rexHeart, rexStar, rexEnergy)
                    "meko" -> updateStats(player, mekoHeart, mekoStar, mekoEnergy)
                    "grumpjaw" -> updateStats(player, grumpHeart, grumpStar, grumpEnergy)
                    "venomjunch" -> updateStats(player, venomHeart, venomStar, venomEnergy)
                }
            }
        }

        viewModel.players.observe(viewLifecycleOwner) { players ->

            players.forEach { player ->
                when (player.name.lowercase()) {
                    "rexmaw" -> toggleTokyoBadge(
                        player.location == Location.TOKYO,
                        R.id.rexmaw_tokyo
                    )
                    "meko" -> toggleTokyoBadge(
                        player.location == Location.TOKYO,
                        R.id.meko_tokyo
                    )
                    "grumpjaw" -> toggleTokyoBadge(
                        player.location == Location.TOKYO,
                        R.id.grumJaw_tokyo
                    )
                    "venomjunch" -> toggleTokyoBadge(
                        player.location == Location.TOKYO,
                        R.id.venomJunch_tokyo
                    )
                }
            }
        }


    }

    private fun updateStats(
        player: Player?,
        heartView: TextView,
        starView: TextView,
        energyView: TextView
    ) {
        if (player == null) {
            heartView.text = "-"
            starView.text = "-"
            energyView.text = "-"
            return
        }

        heartView.text = player.health.toString()
        starView.text = player.victoryPoints.toString()
        energyView.text = player.energy.toString()
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

    private fun toggleTokyoBadge(isInTokyo: Boolean, viewId: Int) {
        val badge = view?.findViewById<TextView>(viewId) ?: return
        badge.visibility = if (isInTokyo) View.VISIBLE else View.GONE
    }

}
