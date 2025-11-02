package com.example.kingoftokyo.ui.rules

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import androidx.navigation.fragment.findNavController
import com.example.kingoftokyo.R

class RulesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView: View = inflater.inflate(
            R.layout.fragment_rules,
            container,
            false
        )
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scrollView = view.findViewById<ScrollView>(R.id.rulesScrollView)
        val nextButton = view.findViewById<Button>(R.id.next_button)
        val skipButton = view.findViewById<Button>(R.id.skipButton)

        skipButton.visibility = View.VISIBLE
        nextButton.visibility = View.INVISIBLE

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val viewLast = scrollView.getChildAt(scrollView.childCount - 1)
            val diff = viewLast.bottom - (scrollView.height + scrollView.scrollY)
            if (diff <= 0) {
                nextButton.visibility = View.VISIBLE
                skipButton.visibility = View.INVISIBLE
            }
        }

        nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_rulesFragment_to_playerSetUpFragment)
        }

        skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_rulesFragment_to_playerSetUpFragment)
        }
    }
}