package com.example.chatappfinal.presentation.features.receiveCall

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.connectycube.videochat.RTCSession
import com.connectycube.videochat.RTCTypes
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.connectyCube.getUser
import com.example.chatappfinal.domain.connectyCube.rtc.NewCall
import com.example.chatappfinal.domain.connectyCube.rtc.SessionClosed
import com.example.chatappfinal.domain.connectyCube.rtc.sessionCallbacks
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import com.example.chatappfinal.presentation.features.chat.AUDIO_CALL
import com.example.chatappfinal.presentation.features.chat.VIDEO_CALL
import com.example.chatappfinal.presentation.hide
import kotlinx.android.synthetic.main.fragment_receive_call.*
import kotlinx.android.synthetic.main.fragment_receive_call.call_type_textView
import kotlinx.android.synthetic.main.fragment_receive_call.recipient_imageView
import kotlinx.android.synthetic.main.fragment_receive_call.recipient_name_textView
import kotlinx.android.synthetic.main.fragment_receive_call.stop_call_imageView

class ReceiveCallFragment : Fragment() {
    private var session :RTCSession? =null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_receive_call, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionCallbacks.observeOnSession {
            when(it){
                is SessionClosed -> findNavController().navigateUp()
                is NewCall -> {
                    session = it.session
                    getUser(session?.callerID?:0, createEntityCallbacks ({user->

                        context?.let {
                            receive_call_progressBar.hide()
                            Glide.with(requireContext())
                            .load(user?.avatar)
                            .placeholder(R.drawable.ic_person)
                            .into(recipient_imageView)
                            recipient_name_textView.text = "Call from ${user?.login}"
                            call_type_textView.text = if (session?.conferenceType == RTCTypes.ConferenceType.CONFERENCE_TYPE_AUDIO) AUDIO_CALL else VIDEO_CALL
                            stop_call_imageView.setOnClickListener { session?.rejectCall(session?.userInfo) }
                            accept_call_imageView.setOnClickListener { session?.acceptCall(session?.userInfo);findNavController().navigate(R.id.action_receiveCallFragment_to_callingFragment) }
                        }
                    },{}))
                }
            }
        }


    }


}
