package com.example.chatappfinal.domain.connectyCube.rtc

import com.connectycube.videochat.BaseSession
import com.connectycube.videochat.RTCSession
import com.connectycube.videochat.callbacks.RTCSessionStateCallback
import timber.log.Timber

val sessionStateCallbacks by lazy { SessionStateCallbacks() }

class SessionStateCallbacks : RTCSessionStateCallback<RTCSession> {
    override fun onDisconnectedFromUser(p0: RTCSession?, p1: Int?) {
        Timber.i("onDisconnectedFromUser")
    }

    override fun onConnectedToUser(p0: RTCSession?, p1: Int?) {
        Timber.i("onConnectedToUser")
    }

    override fun onConnectionClosedForUser(p0: RTCSession?, p1: Int?) {
        Timber.i("onConnectionClosedForUser")
    }

    override fun onStateChanged(p0: RTCSession?, p1: BaseSession.RTCSessionState?) {
        Timber.i("onStateChanged")
    }
}