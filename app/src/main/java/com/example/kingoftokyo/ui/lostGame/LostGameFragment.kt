package com.example.kingoftokyo.ui.lostGame

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.kingoftokyo.R


class LostGameFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView: View = inflater.inflate(
            R.layout.fragment_lost_game,
            container,
            false
        )
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val replayButton: Button = view.findViewById(R.id.replay_button)
        val leaveButton: Button = view.findViewById(R.id.leave_button)

        replayButton.setOnClickListener {
            findNavController().navigate(R.id.action_lostGameFragment_to_playerSetUpFragment)
        }

        leaveButton.setOnClickListener {
            requireActivity().finishAffinity()
        }

    }
}