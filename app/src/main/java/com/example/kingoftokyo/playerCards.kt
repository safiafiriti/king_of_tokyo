package com.example.kingoftokyo.ui.playerCards

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.kingoftokyo.R
import com.example.kingoftokyo.game.viewmodel.GameViewModel

class PlayerCardsFragment : DialogFragment(R.layout.fragment_player_cards) {

    private val viewModel: GameViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_cards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val popup = view.findViewById<View>(R.id.popup_container)
        popup.startAnimation(android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.popup_enter))

        val container = view.findViewById<LinearLayout>(R.id.cards_container)
        val emptyText = view.findViewById<TextView>(R.id.empty_text)
        val closeButton = view.findViewById<Button>(R.id.close_button)

        closeButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // On récupère le joueur actuel pour voir ses cartes
        val currentPlayerIndex = viewModel.currentPlayerIndex.value ?: 0
        val players = viewModel.players.value
        val currentPlayer = players?.getOrNull(currentPlayerIndex)

        if (currentPlayer != null && currentPlayer.cards.isNotEmpty()) {
            emptyText.visibility = View.GONE

            // Pour chaque carte possédée, on crée une ImageView
            currentPlayer.cards.forEach { card ->
                val imageView = ImageView(context)
                imageView.adjustViewBounds = true
                imageView.setImageResource(card.imageResId)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 24) // Marge entre les cartes
                imageView.layoutParams = params

                container.addView(imageView)
            }
        } else {
            emptyText.visibility = View.VISIBLE
        }
    }
}