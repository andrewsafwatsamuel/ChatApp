package com.example.chatappfinal.domain.connectyCube.rtc

import androidx.lifecycle.MutableLiveData
import com.connectycube.videochat.RTCSession
import com.connectycube.videochat.callbacks.RTCClientVideoTracksCallback
import com.connectycube.videochat.view.RTCVideoTrack
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

val videoCallbacks by lazy { VideoCallbacks() }

class VideoCallbacks(

    private val disposables: CompositeDisposable = CompositeDisposable(),
    val localVideoLiveData: MutableLiveData<RTCVideoTrack> = MutableLiveData(),
    val remoteVideoLiveData: MutableLiveData<RTCVideoTrack> = MutableLiveData(),
    val muteSubject: BehaviorSubject<Boolean> = BehaviorSubject.create()
) : RTCClientVideoTracksCallback<RTCSession> {

    override fun onLocalVideoTrackReceive(p0: RTCSession?, p1: RTCVideoTrack?) {
        localVideoLiveData.value = p1
        muteSubject.onNext(p1?.enabled())
        observeOnMute(p1)
    }

    private fun observeOnMute(track: RTCVideoTrack?): Unit =
        muteSubject.subscribeOn(AndroidSchedulers.mainThread())
            .subscribe({ if (it != null) track?.setEnabled(it) }, Throwable::printStackTrace)
            .let { disposables.add(it) }

    override fun onRemoteVideoTrackReceive(p0: RTCSession?, p1: RTCVideoTrack?, p2: Int?) {
        remoteVideoLiveData.value = p1
    }

    fun dispose() {
        localVideoLiveData.value = null
        remoteVideoLiveData.value = null
        disposables.dispose()
    }

}