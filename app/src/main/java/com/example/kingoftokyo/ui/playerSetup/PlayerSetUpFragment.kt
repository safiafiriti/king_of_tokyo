package com.example.kingoftokyo.ui.playerSetup

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.kingoftokyo.game.viewmodel.GameViewModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.example.kingoftokyo.R

class PlayerSetUpFragment : Fragment() {

    private val viewModel: GameViewModel by activityViewModels()

    private lateinit var selectedCard: CardView
    private var selectedAvatar: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView: View = inflater.inflate(
            R.layout.fragment_player_set_up,
            container,
            false
        )
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val cards = listOf(
            view.findViewById<CardView>(R.id.card_rexmax) to "rexmax",
            view.findViewById<CardView>(R.id.card_meko) to "meko",
            view.findViewById<CardView>(R.id.card_grumpjaw) to "grumpjaw",
            view.findViewById<CardView>(R.id.card_venomjunch) to "venomjunch"
        )

        // Setup click listeners for each card
        for ((card, name) in cards) {
            card.setOnClickListener {
                // Remove highlight from previous selection
                selectedCard.setCardBackgroundColor(Color.WHITE)
                selectedCard.cardElevation = 4f

                // Highlight the selected card
                card.setCardBackgroundColor(Color.parseColor("#FFF176")) // yellow highlight
                card.cardElevation = 10f

                selectedCard = card
                selectedAvatar = name
            }
        }

        // Preselect the first card (optional)
        selectedCard = view.findViewById(R.id.card_rexmax)
        selectedCard.setCardBackgroundColor(Color.parseColor("#FFF176"))
        selectedAvatar = "rexmax"

        // Handle Next button
        val nextButton = view.findViewById<Button>(R.id.next_button)
        nextButton.setOnClickListener {
            if (selectedAvatar != null) {
                Toast.makeText(requireContext(), "Selected: $selectedAvatar", Toast.LENGTH_SHORT).show()
                viewModel.startGameWithSelectedMonster(selectedAvatar!!)
                // NAVIGATE
                findNavController().navigate(R.id.action_playerSetUpFragment_to_gameFragment)
            } else {
                Toast.makeText(requireContext(), "Please select an avatar", Toast.LENGTH_SHORT).show()
            }
        }
    }

}