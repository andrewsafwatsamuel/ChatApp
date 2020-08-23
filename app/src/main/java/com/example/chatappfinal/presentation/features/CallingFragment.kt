package com.example.chatappfinal.presentation.features

import android.content.Context
import android.media.AudioManager
import android.media.audiofx.AcousticEchoCanceler
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.connectycube.videochat.RTCSession
import com.connectycube.videochat.RTCTypes
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.connectyCube.getUser
import com.example.chatappfinal.domain.connectyCube.incomingRingingManager
import com.example.chatappfinal.domain.connectyCube.initCapturer
import com.example.chatappfinal.domain.connectyCube.rtc.*
import com.example.chatappfinal.domain.connectyCube.switchCamera
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import com.example.chatappfinal.presentation.hide
import com.example.chatappfinal.presentation.show
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_calling.*
import kotlinx.android.synthetic.main.fragment_calling.audio_controller_imageView
import kotlinx.android.synthetic.main.fragment_calling.speaker_imageView
import kotlinx.android.synthetic.main.fragment_calling.stop_call_imageView
import kotlinx.android.synthetic.main.fragment_start_call.*
import timber.log.Timber

class CallingFragment : Fragment() {

    private val audioManager by lazy { requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    private var session: RTCSession? = sessionCallbacks.session

    private val isVideo = session?.conferenceType == RTCTypes.ConferenceType.CONFERENCE_TYPE_VIDEO

    private val speakerSubject = BehaviorSubject.create<Boolean>()

    private val disposable = speakerSubject
        .subscribeOn(Schedulers.computation())
        .subscribe({ if (it) disableSpeaker() else enableSpeaker() }, Throwable::printStackTrace)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(inflateOnType(), container, false)

    private fun inflateOnType() =
        if (isVideo) R.layout.fragment_calling else R.layout.fragment_start_call

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = context?.let {
        super.onViewCreated(view, savedInstanceState)

        incomingRingingManager.stopRinging()

        speaker_imageView.show()
        audio_controller_imageView.show()

        audio_controller_imageView.setOnClickListener {
            audioCallbacks.muteSubject.onNext(audioCallbacks.muteSubject.value?.let { !it }
                ?: false)
        }

        stop_call_imageView.setOnClickListener {
            session?.hangUp(session?.userInfo)
        }
        speakerSubject.onNext(true)
        speaker_imageView.setOnClickListener { speakerSubject.onNext(!speakerSubject.value) }

        onTypeVideo()

    } ?: Unit


    private fun onTypeVideo() = if (isVideo) {

        videoCallbacks.remoteVideoLiveData.observe(viewLifecycleOwner, Observer {
            it?.addRenderer(remote_opponentView)
        })

        videoCallbacks.localVideoLiveData.observe(viewLifecycleOwner, Observer {
            it?.addRenderer(local_opponentView)
            session?.initCapturer()
        })

        video_controller_imageView.setOnClickListener {
            videoCallbacks.muteSubject.onNext(videoCallbacks.muteSubject.value?.let { !it }
                ?: false)
        }

        rotate_camera_imageView.setOnClickListener { switchCamera() }

    } else getUser(getOtherSideId(), createEntityCallbacks({
        start_call_progressBar.hide()
        Glide.with(requireContext())
            .load(it?.avatar)
            .placeholder(R.drawable.ic_person)
            .into(recipient_imageView)
        recipient_name_textView.text = it?.login ?: "UNKNOWN"
    }, Timber::e)
    )

    private fun getOtherSideId() = if (sessionCallbacks.stateSubject.value is CallAccepted) {
        session?.opponents?.get(0)
    } else {
        session?.callerID
    } ?: 0


    private fun enableSpeaker() {
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = true
        AcousticEchoCanceler.create(android.media.AudioDeviceInfo.TYPE_BUILTIN_MIC)
    }

    private fun disableSpeaker() {
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = AudioManager.MODE_IN_CALL
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}

