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
import com.example.kingoftokyo.game.model.Player
import com.example.kingoftokyo.game.viewmodel.GameViewModel

class PlayerCardsFragment : DialogFragment(R.layout.fragment_player_cards) {

    private val viewModel: GameViewModel by activityViewModels()
    private lateinit var container: LinearLayout
    private lateinit var emptyText: TextView

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

        container = view.findViewById(R.id.cards_container)
        emptyText = view.findViewById(R.id.empty_text)
        val closeButton = view.findViewById<Button>(R.id.close_button)

        closeButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Observer les joueurs pour mettre à jour la liste des cartes du joueur humain
        viewModel.players.observe(viewLifecycleOwner) { players ->
            updateCardsDisplay(players)
        }
    }

    private fun updateCardsDisplay(players: List<Player>) {
        // CORRECTION : On cherche spécifiquement le joueur humain pour afficher SES cartes
        // Dans ton GameManager, le joueur humain est toujours le premier (index 0) ou celui avec isHuman=true
        val humanPlayer = players.firstOrNull { it.isHuman } ?: players.getOrNull(0)

        container.removeAllViews()
        container.addView(emptyText)

        if (humanPlayer != null && humanPlayer.cards.isNotEmpty()) {
            emptyText.visibility = View.GONE

            humanPlayer.cards.forEach { card ->
                val imageView = ImageView(context)
                imageView.adjustViewBounds = true
                imageView.setImageResource(card.imageResId)

                val params = LinearLayout.LayoutParams(
                    // Keep MATCH_PARENT for width
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    // CHANGE: Use WRAP_CONTENT so the height is defined by the image itself
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                // REMOVE: params.height = 600

                // Keep the margin for the small space between cards (32dp margin)
                params.setMargins(0, 0, 0, 32)

                imageView.layoutParams = params
                // CHANGE: Use FIT_XY if you want the card to fill the width and respect the aspect ratio
                // Or just leave it as FIT_CENTER if you prefer, but WRAP_CONTENT is the key change.
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER

                container.addView(imageView)
            }
        } else {
            emptyText.visibility = View.VISIBLE
        }
    }
}