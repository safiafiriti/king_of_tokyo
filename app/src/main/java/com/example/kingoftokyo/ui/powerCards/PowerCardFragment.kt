package com.example.kingoftokyo.ui.powerCards

import android.os.Bundle
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels // Important pour partager les données
import androidx.navigation.fragment.findNavController
import com.example.kingoftokyo.R
import com.example.kingoftokyo.game.viewmodel.GameViewModel

class PowerCardFragment : DialogFragment(R.layout.fragment_power_card) {

    // On récupère le ViewModel partagé avec l'activité (le même que celui du GameFragment)
    private val viewModel: GameViewModel by activityViewModels()

    private lateinit var cardViews: List<ImageView>
    private var selectedIndex: Int? = null

    override fun onStart() {
        super.onStart()
        // Rend le fond de la fenêtre transparent pour voir le jeu derrière
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_power_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Animation d'entrée
        val popup = view.findViewById<View>(R.id.popup_container)
        val anim = android.view.animation.AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.popup_enter
        )
        popup.startAnimation(anim)

        // Récupération des vues
        val card1: ImageView = view.findViewById(R.id.card_image_1)
        val card2: ImageView = view.findViewById(R.id.card_image_2)
        val card3: ImageView = view.findViewById(R.id.card_image_3)
        cardViews = listOf(card1, card2, card3)

        val cancelButton: Button = view.findViewById(R.id.button_cancel)
        val buyButton: Button = view.findViewById(R.id.button_buy)

        // --- OBSERVER LES CARTES DISPONIBLES ---
        // Dès que la liste des cartes change dans le ViewModel, on met à jour les images ici
        viewModel.marketCards.observe(viewLifecycleOwner) { cards ->
            cards.forEachIndexed { index, card ->
                if (index < cardViews.size) {
                    if (card != null) {
                        // Affiche l'image de la carte (nécessite l'étape 1 faite dans Card.kt)
                        cardViews[index].setImageResource(card.imageResId)
                        cardViews[index].visibility = View.VISIBLE
                        cardViews[index].isClickable = true
                        cardViews[index].alpha = 1.0f // Reset opacité si réutilisé
                    } else {
                        // Si l'emplacement est vide (plus de cartes dans le deck)
                        cardViews[index].visibility = View.INVISIBLE
                        cardViews[index].isClickable = false
                    }
                }
            }
            // Si on a acheté une carte, on reset la sélection
            selectedIndex = null
        }

        // Gestion du clic sur une carte (Sélection visuelle)
        cardViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                onCardClicked(index)
            }
        }

        // --- BOUTON ANNULER ---
        cancelButton.setOnClickListener {
            // On prévient le jeu qu'on ferme la boutique (passe au joueur suivant)
            viewModel.onCloseShop()
            findNavController().popBackStack()
        }

        // --- BOUTON ACHETER ---
        buyButton.setOnClickListener {
            selectedIndex?.let { index ->
                // On tente d'acheter la carte sélectionnée via le ViewModel
                viewModel.onBuyCard(index)

                // Optionnel : Si tu veux fermer la boutique après un achat, décommente la ligne ci-dessous.
                // Sinon, le joueur peut acheter d'autres cartes tant qu'il a de l'énergie.
                // findNavController().popBackStack()
            }
        }
    }

    private fun onCardClicked(index: Int) {
        selectedIndex = index
        // Effet visuel : la carte sélectionnée reste opaque, les autres deviennent semi-transparentes
        cardViews.forEachIndexed { i, imageView ->
            imageView.alpha = if (i == index) 1.0f else 0.5f
        }
    }
}