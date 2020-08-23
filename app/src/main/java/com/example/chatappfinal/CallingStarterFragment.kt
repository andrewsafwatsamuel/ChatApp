package com.example.chatappfinal

import android.os.Bundle
import android.telecom.Call
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

class CallingStarterFragment : Fragment() {

    private val activity by lazy { requireActivity() as CallingActivity }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_calling_starter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startNav()
    }

    fun startNav() = findNavController().navigate(
        if (activity.flag != null) navigateToStartCall()
        else navigateToReceiveCall()
    )

    fun navigateToReceiveCall() = CallingStarterFragmentDirections
        .actionCallingStarterFragmentToReceiveCallFragment()

    fun navigateToStartCall() = CallingStarterFragmentDirections
        .actionCallingStarterFragmentToStartCallFragment(
            activity.recipientId,
            activity.flag!!,
            activity.dialog
        )
}
