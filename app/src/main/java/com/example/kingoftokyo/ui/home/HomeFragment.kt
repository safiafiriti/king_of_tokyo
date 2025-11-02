package com.example.kingoftokyo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kingoftokyo.R
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

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
        val konfettiView = view.findViewById<KonfettiView>(R.id.konfettiView)

        // Start the floating animation
        val floatAnim = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.floating)
        playButton.startAnimation(floatAnim)

        // Sparkles around the play button
        konfettiView.start(
            Party(
                speed = 2f,
                maxSpeed = 8f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xFFE7C301.toInt(), 0xFFFFFFFF.toInt(), 0xFF3AC769.toInt()),
                emitter = Emitter(duration = 1, TimeUnit.SECONDS).perSecond(10),
                position = Position.Relative(0.5, 0.1)
            )
        )

        playButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_rulesFragment)
        }
    }
}