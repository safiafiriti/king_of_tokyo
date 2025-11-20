package com.example.kingoftokyo.ui.powerCards

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.kingoftokyo.R

class PowerCardFragment : DialogFragment(R.layout.fragment_power_card) {

    private lateinit var cardViews: List<ImageView>
    private var selectedIndex: Int? = null

    override fun onStart() {
        super.onStart()

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
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_power_card,
            container,
            false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val popup = view.findViewById<View>(R.id.popup_container)

        val anim = android.view.animation.AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.popup_enter
        )
        popup.startAnimation(anim)

        val card1: ImageView = view.findViewById(R.id.card_image_1)
        val card2: ImageView = view.findViewById(R.id.card_image_2)
        val card3: ImageView = view.findViewById(R.id.card_image_3)
        cardViews = listOf(card1, card2, card3)

        val cancelButton: Button = view.findViewById(R.id.button_cancel)
        val buyButton: Button = view.findViewById(R.id.button_buy)

        cardViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                onCardClicked(index)
            }
        }

        cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }

        buyButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun onCardClicked(index: Int) {
        selectedIndex = index

        cardViews.forEachIndexed { i, imageView ->
            imageView.alpha = if (i == index) 1.0f else 0.5f
        }
    }
}