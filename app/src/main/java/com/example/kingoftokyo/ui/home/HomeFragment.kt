package com.example.kingoftokyo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kingoftokyo.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView: View = inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val playButton: Button = view.findViewById(R.id.PlayButton)

        // Start the floating animation
        val floatAnim = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.floating)
        playButton.startAnimation(floatAnim)

        playButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_rulesFragment)
        }
    }
}