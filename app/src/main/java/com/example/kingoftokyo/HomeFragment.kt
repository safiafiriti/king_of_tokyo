package com.example.kingoftokyo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

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
        playButton.setOnClickListener {
            // Example navigation to Page1Fragment (you’ll create it later)
            //findNavController().navigate(R.id.action_homeFragment_to_page1Fragment)
        }
    }
}