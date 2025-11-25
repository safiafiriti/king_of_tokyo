package com.example.kingoftokyo.ui.powerCards

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.kingoftokyo.R
import com.example.kingoftokyo.game.model.Card
import com.example.kingoftokyo.game.viewmodel.GameViewModel

class PowerCardFragment : DialogFragment(R.layout.fragment_power_card) {

    // On récupère le ViewModel partagé
    private val viewModel: GameViewModel by activityViewModels()

    // --- DECLARATION DES VARIABLES ---
    private lateinit var cardViews: List<ImageView>
    private lateinit var buyButton: Button
    private lateinit var cancelButton: Button
    private var selectedIndex: Int? = null

    override fun onStart() {
        super.onStart()
        // Rend le fond de la fenêtre transparent
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_power_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Empêcher la fermeture par retour arrière pour forcer l'usage des boutons
        isCancelable = false

        // Animation d'entrée
        val popup = view.findViewById<View>(R.id.popup_container)
        val anim = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.popup_enter)
        popup.startAnimation(anim)

        // --- RECUPERATION DES VUES (CORRECTION ICI) ---
        // On utilise findViewById(R.id.xxx) sans "id ="
        val card1: ImageView = view.findViewById(R.id.card_image_1)
        val card2: ImageView = view.findViewById(R.id.card_image_2)
        val card3: ImageView = view.findViewById(R.id.card_image_3)
        cardViews = listOf(card1, card2, card3)

        cancelButton = view.findViewById(R.id.button_cancel)
        buyButton = view.findViewById(R.id.button_buy)

        // --- LOGIQUE DU FRAGMENT ---

        // Gestion de l'affichage selon la phase (Achat ou Consultation)
        viewModel.isBuyPhase.observe(viewLifecycleOwner) { isBuyPhase ->
            if (isBuyPhase) {
                buyButton.visibility = View.VISIBLE
                cancelButton.text = getString(R.string.cancel)
            } else {
                buyButton.visibility = View.INVISIBLE
                cancelButton.text = "Fermer"
            }
            // Reset sélection quand on change de mode
            selectedIndex = null
            updateSelectionVisuals()
        }

        // Observer les cartes disponibles pour les afficher
        viewModel.marketCards.observe(viewLifecycleOwner) { cards ->
            updateCardsImages(cards)
        }

        // Clic sur une carte
        cardViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                val isBuyPhase = viewModel.isBuyPhase.value == true

                // Si on n'est pas en phase d'achat, on ne fait rien
                if (!isBuyPhase) return@setOnClickListener

                val cards = viewModel.marketCards.value
                val currentEnergy = viewModel.getCurrentPlayerEnergy()
                val card = cards?.getOrNull(index)

                if (card != null) {
                    if (currentEnergy >= card.cost) {
                        onCardClicked(index)
                    } else {
                        Toast.makeText(context, "Pas assez d'énergie (${card.cost} requis)", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Bouton Acheter
        buyButton.setOnClickListener {
            selectedIndex?.let { index ->
                viewModel.onBuyCard(index)

                val energyLeft = viewModel.getCurrentPlayerEnergy()
                // Si plus assez d'argent (< 3), on peut fermer (optionnel)
                if (energyLeft < 3) {
                    viewModel.onCloseShop()
                    findNavController().popBackStack()
                } else {
                    selectedIndex = null
                    updateSelectionVisuals()
                }
            }
        }

        // Bouton Annuler / Fermer
        cancelButton.setOnClickListener {
            viewModel.onCloseShop()
            findNavController().popBackStack()
        }
    }

    private fun updateCardsImages(cards: List<Card?>) {
        cardViews.forEachIndexed { i, imageView ->
            val card = cards.getOrNull(i)
            if (card != null) {
                imageView.setImageResource(card.imageResId)
                imageView.visibility = View.VISIBLE
                imageView.alpha = 1.0f
            } else {
                imageView.visibility = View.INVISIBLE
            }
        }
        updateSelectionVisuals()
    }

    private fun onCardClicked(index: Int) {
        if (selectedIndex == index) {
            selectedIndex = null
        } else {
            selectedIndex = index
        }
        updateSelectionVisuals()
    }

    private fun updateSelectionVisuals() {
        cardViews.forEachIndexed { i, imageView ->
            if (selectedIndex != null) {
                if (i == selectedIndex) {
                    // Carte sélectionnée : mise en valeur
                    imageView.alpha = 1.0f
                    imageView.scaleX = 1.1f
                    imageView.scaleY = 1.1f
                } else {
                    // Les autres : en retrait
                    imageView.alpha = 0.5f
                    imageView.scaleX = 1.0f
                    imageView.scaleY = 1.0f
                }
            } else {
                // Aucune sélection : état normal
                imageView.alpha = 1.0f
                imageView.scaleX = 1.0f
                imageView.scaleY = 1.0f
            }
        }

        // Le bouton acheter n'est actif que si une sélection est faite
        buyButton.isEnabled = selectedIndex != null
        buyButton.alpha = if (selectedIndex != null) 1.0f else 0.5f
    }
}