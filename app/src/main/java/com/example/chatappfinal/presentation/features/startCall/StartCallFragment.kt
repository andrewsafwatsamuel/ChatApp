package com.example.chatappfinal.presentation.features.startCall

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.core.helper.StringifyArrayList
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.connectyCube.pushNotifications.CALL
import com.example.chatappfinal.domain.connectyCube.pushNotifications.composeChatNotification
import com.example.chatappfinal.domain.connectyCube.pushNotifications.createEvent
import com.example.chatappfinal.presentation.enable
import com.example.chatappfinal.presentation.hide
import com.example.chatappfinal.presentation.show
import kotlinx.android.synthetic.main.fragment_start_call.*

class StartCallFragment : Fragment() {

    private val args by lazy { StartCallFragmentArgs.fromBundle(requireArguments()) }
    private val recipientId by lazy { args.recepientId }
    private val flag by lazy { args.flag }
    private val dialog:ConnectycubeChatDialog by lazy { args.dialog }

    private val viewModel by lazy {
        StartCallViewModelFactory(recipientId, flag).let { ViewModelProvider(this,it)[StartCallViewModel::class.java] }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_start_call, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.stateLiveData.observe(viewLifecycleOwner, Observer {it?.let { drawStates(it) }})
        createEvent(StringifyArrayList<Int>().apply { add(recipientId) }, composeChatNotification(CALL,dialog.dialogId,dialog.name))
        stop_call_imageView.setOnClickListener { endCall() }
    }

    private fun drawStates(state:StartCallState) = when(state){
        is LoadingState ->onLoading()
        is SuccessUserState ->successUser(state.user)
        is ErrorState ->endCall()
        is SuccessCall -> successCall()
    }

    private fun onLoading(){
        start_call_progressBar.show()
    }

    private fun endCall(){
    viewModel.session?.hangUp(viewModel.session?.userInfo)
    }

    private fun successCall(){
        findNavController().navigate(R.id.action_startCallFragment_to_callingFragment)
    }

    @SuppressLint("SetTextI18n")
    private fun successUser(user:ConnectycubeUser?)=user?.run{
        start_call_progressBar.hide()
        stop_call_imageView.enable()
        Glide.with(requireContext())
            .load(avatar)
            .placeholder(R.drawable.ic_person)
            .into(recipient_imageView)
        recipient_name_textView.text = "Calling $login"
        call_type_textView.text = flag
    }

}
