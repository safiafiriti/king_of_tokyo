package com.example.kingoftokyo.ui.powerCards

import android.os.Bundle
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels // Important pour partager les données
import androidx.navigation.fragment.findNavController
import com.example.kingoftokyo.R
import com.example.kingoftokyo.game.model.Card
import com.example.kingoftokyo.game.viewmodel.GameViewModel

class PowerCardFragment : DialogFragment(R.layout.fragment_power_card) {

    // On récupère le ViewModel partagé avec l'activité (le même que celui du GameFragment)
    private val viewModel: GameViewModel by activityViewModels()

    private lateinit var cardViews: List<ImageView>
    private lateinit var buyButton: Button
    private lateinit var cancelButton: Button
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

        // EMPECHER LA FERMETURE PAR RETOUR ARRIERE (force l'usage des boutons)
        isCancelable = false

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

        cancelButton = view.findViewById(R.id.button_cancel)
        buyButton = view.findViewById(R.id.button_buy)

        // GESTION DES BOUTONS SELON LA PHASE
        viewModel.isBuyPhase.observe(viewLifecycleOwner) { isBuyPhase ->
            if (isBuyPhase) {
                // Phase d'achat (Fin de tour)
                buyButton.visibility = View.VISIBLE
                cancelButton.text = getString(R.string.cancel)
            } else {
                // Phase de consultation (Pendant le tour)
                buyButton.visibility = View.INVISIBLE
                cancelButton.text = "Close"
            }
            selectedIndex = null
            updateSelectionVisuals()
        }

        // --- OBSERVER LES CARTES DISPONIBLES ---
        // Dès que la liste des cartes change dans le ViewModel, on met à jour les images ici
        viewModel.marketCards.observe(viewLifecycleOwner) { cards ->
            updateCardsImages(cards)
        }

        // Gestion du clic sur une carte (Sélection visuelle)
        cardViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                val isBuyPhase = viewModel.isBuyPhase.value == true

                // Si on n'est pas en phase d'achat, on ne sélectionne pas
                if (!isBuyPhase) return@setOnClickListener

                val cards = viewModel.marketCards.value
                val currentEnergy = viewModel.getCurrentPlayerEnergy()
                val card = cards?.getOrNull(index)

                if (card != null) {
                    if (currentEnergy >= card.cost) {
                        onCardClicked(index)
                    } else {
                        Toast.makeText(context, "Not enough energy!", Toast.LENGTH_SHORT).show()
                    }
                }
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

                val energyLeft = viewModel.getCurrentPlayerEnergy()
                // Optionnel : Fermeture auto si < 3 énergies
                if (energyLeft < 3) {
                    viewModel.onCloseShop()
                    findNavController().popBackStack()
                } else {
                    selectedIndex = null
                    updateSelectionVisuals()
                }
            }
        }
    }

    private fun updateCardsImages(cards: List<Card?>) {
        cardViews.forEachIndexed { i, imageView ->
            val card = cards.getOrNull(i)
            if (card != null) {
                imageView.setImageResource(card.imageResId)
                imageView.visibility = View.VISIBLE
                imageView.alpha = 1.0f // Toujours visible
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
                    imageView.alpha = 1.0f
                    imageView.scaleX = 1.1f
                    imageView.scaleY = 1.1f
                } else {
                    imageView.alpha = 0.5f
                    imageView.scaleX = 1.0f
                    imageView.scaleY = 1.0f
                }
            } else {
                imageView.alpha = 1.0f
                imageView.scaleX = 1.0f
                imageView.scaleY = 1.0f
            }
        }
        buyButton.isEnabled = selectedIndex != null
        buyButton.alpha = if (selectedIndex != null) 1.0f else 0.5f
    }
}