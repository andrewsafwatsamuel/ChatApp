package com.example.chatappfinal.domain.connectyCube.conference

import com.connectycube.videochat.BaseSession
import com.connectycube.videochat.callbacks.RTCSessionStateCallback
import com.connectycube.videochat.conference.ConferenceSession
import com.connectycube.videochat.conference.WsException
import com.connectycube.videochat.conference.callbacks.ConferenceEntityCallback
import com.connectycube.videochat.conference.callbacks.ConferenceSessionCallbacks
import timber.log.Timber
import java.util.ArrayList

val conferenceEntityCallbacks by lazy { ConferenceEntityCallbacks() }
val conferenceSessionStates by lazy { ConferenceSessionStates() }
val conferenceCallbacks by lazy { ConferenceCallbacks() }

class ConferenceEntityCallbacks : ConferenceEntityCallback<ConferenceSession>{

    override fun onSuccess(p0: ConferenceSession?) {
        Timber.i("onSuccess")
    }

    override fun onError(p0: WsException?) {
        Timber.i("onError")
    }
}

class ConferenceSessionStates: RTCSessionStateCallback<ConferenceSession> {
    override fun onDisconnectedFromUser(p0: ConferenceSession?, p1: Int?) {
        Timber.i("onDisconnectedFromUser")
    }

    override fun onConnectedToUser(p0: ConferenceSession?, p1: Int?) {
        Timber.i("onConnectedToUser")
    }

    override fun onConnectionClosedForUser(p0: ConferenceSession?, p1: Int?) {
        Timber.i("onConnectionClosedForUser")
    }

    override fun onStateChanged(p0: ConferenceSession?, p1: BaseSession.RTCSessionState?) {
        Timber.i("onStateChanged")
    }
}

class ConferenceCallbacks : ConferenceSessionCallbacks{
    override fun onSlowLinkReceived(p0: Boolean, p1: Int) {
        Timber.i("onSlowLinkReceived")
    }

    override fun onPublisherLeft(p0: Int?) {
        Timber.i("onPublisherLeft")
    }

    override fun onPublishersReceived(p0: ArrayList<Int>?) {
        Timber.i("onPublishersReceived")
    }

    override fun onSessionClosed(p0: ConferenceSession?) {
        Timber.i("onSessionClosed")
    }

    override fun onMediaReceived(p0: String?, p1: Boolean) {
        Timber.i("onMediaReceived")
    }

    override fun onError(p0: WsException?) {
        Timber.i("onError")
    }
}