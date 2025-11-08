package com.example.kingoftokyo.ui.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kingoftokyo.R

class GameFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView: View = inflater.inflate(
            R.layout.fragment_game,
            container,
            false
        )
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val playButton: Button = view.findViewById(R.id.PlayButton)
//
//        playButton.setOnClickListener {
//            findNavController().navigate(R.id.action_homeFragment_to_rulesFragment)
//        }
    }
}