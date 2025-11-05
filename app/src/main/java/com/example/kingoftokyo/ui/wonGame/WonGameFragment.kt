package com.example.kingoftokyo.ui.wonGame

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.kingoftokyo.R


class WonGameFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView: View = inflater.inflate(
            R.layout.fragment_won_game,
            container,
            false
        )
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val replayButton: Button = view.findViewById(R.id.replay_button_won)
        val leaveButton: Button = view.findViewById(R.id.leave_button_won)

        replayButton.setOnClickListener {
            findNavController().navigate(R.id.action_wonGameFragment_to_playerSetUpFragment)
        }

        leaveButton.setOnClickListener {
            requireActivity().finishAffinity()
        }

    }
}