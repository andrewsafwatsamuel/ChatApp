package com.example.chatappfinal.domain.connectyCube.rtc

import com.connectycube.videochat.RTCSession
import com.connectycube.videochat.RTCTypes
import com.connectycube.videochat.callbacks.RTCClientAudioTracksCallback
import com.connectycube.videochat.callbacks.RTCClientSessionCallbacks
import com.connectycube.videochat.callbacks.RTCClientVideoTracksCallback
import com.connectycube.videochat.callbacks.RTCSessionStateCallback
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.connectyCube.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

sealed class SessionState
object CallNotAnswered : SessionState()
object StartToClose : SessionState()
class CallAccepted(val session: RTCSession?) : SessionState()
class NewCall(val session: RTCSession?) : SessionState()
object UserNoActions : SessionState()
object SessionClosed : SessionState()
object CallRejected : SessionState()
object OtherUserHangup : SessionState()

val sessionCallbacks by lazy { SessionCallbacks() }

class SessionCallbacks(
    val stateSubject: BehaviorSubject<SessionState> = BehaviorSubject.create(),
    var session: RTCSession? = null,
    private val video: VideoCallbacks = videoCallbacks,
    private val audio: AudioCallbacks = audioCallbacks,
   private val incomingCallRing: RingingManager = incomingRingingManager
) : RTCClientSessionCallbacks {

    override fun onUserNotAnswer(session: RTCSession?, p1: Int?) =
        stateSubject.onNext(CallNotAnswered)


    override fun onSessionStartClose(session: RTCSession?) {
        session?.close()
        this.session = null
        stateSubject.onNext(StartToClose)
        incomingCallRing.stopRinging()
    }

    override fun onReceiveHangUpFromUser(
        p0: RTCSession?,
        p1: Int?,
        p2: MutableMap<String, String>?
    ) = stateSubject.onNext(OtherUserHangup)

    override fun onCallAcceptByUser(
        session: RTCSession?,
        p1: Int?,
        p2: MutableMap<String, String>?
    ) {
        this.session = session
        stateSubject.onNext(CallAccepted(session))
    }

    override fun onReceiveNewSession(session: RTCSession?) {
        session?.initialize()
        this.session = session
        stateSubject.onNext(NewCall(session))
        incomingCallRing
        incomingCallRing.start(R.raw.dance_monkey_sax)
    }

    override fun onUserNoActions(p0: RTCSession?, p1: Int?) = stateSubject.onNext(UserNoActions)

    override fun onSessionClosed(session: RTCSession?) = stateSubject.onNext(SessionClosed)

    override fun onCallRejectByUser(p0: RTCSession?, p1: Int?, p2: MutableMap<String, String>?) =
        stateSubject.onNext(CallRejected)

    private fun RTCSession.close(
        videoTrackCallback: RTCClientVideoTracksCallback<RTCSession> = videoCallbacks,
        audioTracksCallback: RTCClientAudioTracksCallback<RTCSession> = audioCallbacks,
        sessionState: RTCSessionStateCallback<RTCSession> = sessionStateCallbacks
    ) {
        when (conferenceType) {
            RTCTypes.ConferenceType.CONFERENCE_TYPE_AUDIO -> closeAudio(audioTracksCallback)
            RTCTypes.ConferenceType.CONFERENCE_TYPE_VIDEO -> closeVideo(videoTrackCallback, audioTracksCallback)
            else -> Unit
        }
        removeSessionCallbacksListener(sessionState)
    }

    private fun RTCSession.closeVideo(
        videoTrackCallback: RTCClientVideoTracksCallback<RTCSession>,
        audioTracksCallback: RTCClientAudioTracksCallback<RTCSession>
    ) = run {
        removeVideoTrackCallbacksListener(videoTrackCallback)
        video.dispose()
        closeAudio(audioTracksCallback)
    }

    private fun RTCSession.closeAudio(
        audioTracksCallback: RTCClientAudioTracksCallback<RTCSession>
    ) = removeAudioTrackCallbacksListener(audioTracksCallback)
        .also { audio.dispose() }

    fun observeOnSession(success: (SessionState) -> Unit): Disposable = stateSubject
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(success, Throwable::printStackTrace)
}