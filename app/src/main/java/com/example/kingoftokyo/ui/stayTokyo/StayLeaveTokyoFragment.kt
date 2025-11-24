package com.example.kingoftokyo.ui.stayTokyo

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.kingoftokyo.R

class StayLeaveTokyoFragment : DialogFragment(R.layout.fragment_stay_leave_tokyo) {

    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stay_leave_tokyo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val popup = view.findViewById<View>(R.id.popup_container)

        val anim = android.view.animation.AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.popup_enter
        )
        popup.startAnimation(anim)

        val stayBtn = view.findViewById<Button>(R.id.button_stay)
        val leaveBtn = view.findViewById<Button>(R.id.button_leave)

        stayBtn.setOnClickListener {
            parentFragmentManager.setFragmentResult("tokyoDecision", Bundle().apply {
                putBoolean("leave", false)
            })
            dismiss()
        }

        leaveBtn.setOnClickListener {
            parentFragmentManager.setFragmentResult("tokyoDecision", Bundle().apply {
                putBoolean("leave", true)
            })
            dismiss()
        }
    }

}