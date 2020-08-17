package com.example.chatappfinal.domain.connectyCube.rtc

import com.connectycube.videochat.RTCAudioTrack
import com.connectycube.videochat.RTCSession
import com.connectycube.videochat.callbacks.RTCClientAudioTracksCallback
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import timber.log.Timber

val audioCallbacks by lazy { AudioCallbacks() }

class AudioCallbacks(
    private val disposables: CompositeDisposable = CompositeDisposable(),
    val muteSubject: BehaviorSubject<Boolean> = BehaviorSubject.create()
) : RTCClientAudioTracksCallback<RTCSession> {

    override fun onRemoteAudioTrackReceive(p0: RTCSession?, p1: RTCAudioTrack?, p2: Int?) {

    }

    override fun onLocalAudioTrackReceive(p0: RTCSession?, p1: RTCAudioTrack?) {
        muteSubject.onNext(p1?.enabled())
        observeOnMute(p1)
    }

    private fun observeOnMute(track: RTCAudioTrack?): Unit =
        muteSubject.subscribeOn(AndroidSchedulers.mainThread())
            .subscribe({ if (it != null) track?.setEnabled(it) }, Throwable::printStackTrace)
            .let { disposables.add(it) }

    fun dispose() {
        disposables.dispose()
    }
}