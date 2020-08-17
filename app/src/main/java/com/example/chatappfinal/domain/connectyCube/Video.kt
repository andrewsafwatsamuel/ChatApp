package com.example.chatappfinal.domain.connectyCube

import android.content.Context
import com.connectycube.chat.ConnectycubeChatService
import com.connectycube.chat.WebRTCSignaling
import com.connectycube.videochat.RTCCameraVideoCapturer
import com.connectycube.videochat.RTCClient
import com.connectycube.videochat.RTCSession
import com.connectycube.videochat.RTCTypes
import com.connectycube.videochat.callbacks.RTCClientAudioTracksCallback
import com.connectycube.videochat.callbacks.RTCClientSessionCallbacks
import com.connectycube.videochat.callbacks.RTCClientVideoTracksCallback
import com.connectycube.videochat.callbacks.RTCSessionStateCallback
import com.example.chatappfinal.domain.connectyCube.rtc.audioCallbacks
import com.example.chatappfinal.domain.connectyCube.rtc.sessionCallbacks
import com.example.chatappfinal.domain.connectyCube.rtc.sessionStateCallbacks
import com.example.chatappfinal.domain.connectyCube.rtc.videoCallbacks
import org.webrtc.CameraVideoCapturer

fun initializeVideo(
    session: RTCSession,
    videoTracksCallback: RTCClientVideoTracksCallback<RTCSession>,
    audioTracksCallback: RTCClientAudioTracksCallback<RTCSession>,
    sessionState: RTCSessionStateCallback<RTCSession>
) {
    session.addAudioTrackCallbacksListener(audioTracksCallback)
    session.addVideoTrackCallbacksListener(videoTracksCallback)
    session.addSessionCallbacksListener(sessionState)
}

fun RTCClient.createSession(
    opponents: List<Int>,
    type: RTCTypes.ConferenceType
): RTCSession = createNewSessionWithOpponents(opponents, type)
    .apply { initialize();startCall(userInfo) }

fun RTCSession.initialize(
    videoTrackCallback: RTCClientVideoTracksCallback<RTCSession> = videoCallbacks,
    audioTracksCallback: RTCClientAudioTracksCallback<RTCSession> = audioCallbacks,
    sessionState: RTCSessionStateCallback<RTCSession> = sessionStateCallbacks
) {
    when (conferenceType) {
        RTCTypes.ConferenceType.CONFERENCE_TYPE_AUDIO -> initializeAudio(audioTracksCallback)
        RTCTypes.ConferenceType.CONFERENCE_TYPE_VIDEO -> initializeVideo(
            videoTrackCallback,
            audioTracksCallback
        )
        else -> Unit
    }
    addSessionCallbacksListener(sessionState)
}

private fun RTCSession.initializeVideo(
    videoTrackCallback: RTCClientVideoTracksCallback<RTCSession>,
    audioTracksCallback: RTCClientAudioTracksCallback<RTCSession>
) = run {
    addVideoTrackCallbacksListener(videoTrackCallback)
    initializeAudio(audioTracksCallback)
}

private fun RTCSession.initializeAudio(audioTracksCallback: RTCClientAudioTracksCallback<RTCSession>) =
    addAudioTrackCallbacksListener(audioTracksCallback)

fun endCall(
    session: RTCSession,
    // opponentView: RTCSurfaceView,
    videoTracksCallback: RTCClientVideoTracksCallback<RTCSession>,
    audioTracksCallback: RTCClientAudioTracksCallback<RTCSession>,
    sessionState: RTCSessionStateCallback<RTCSession>
) {
    //opponentView.release()
    session.removeAudioTrackCallbacksListener(audioTracksCallback)
    session.removeVideoTrackCallbacksListener(videoTracksCallback)
    session.removeSessionCallbacksListener(sessionState)
    session.hangUp(HashMap())
}

//RtcClient
fun Context.getRtcClient(): RTCClient = RTCClient.getInstance(this)

fun initializeRtc(
    client: RTCClient = rtcClient,
    callBacks: RTCClientSessionCallbacks = sessionCallbacks
) = with(client) {
    createSignalingManager()
    addSessionCallbacksListener(callBacks)
    prepareToProcessCalls()
}

fun RTCClient.createSignalingManager() = ConnectycubeChatService
    .getInstance()
    .videoChatWebRTCSignalingManager
    ?.addSignalingManagerListener { signaling, createdLocally ->
        if (!createdLocally) addSignaling(signaling as WebRTCSignaling)
    }


fun disposeRtc(
    client: RTCClient = rtcClient,
    callBacks: RTCClientSessionCallbacks = sessionCallbacks
) = with(client) {
    removeSessionsCallbacksListener(callBacks)
    destroy()
}

//camera switching
private var videoCapturer: RTCCameraVideoCapturer? = null

fun RTCSession.initCapturer() {
    videoCapturer = mediaStreamManager?.videoCapturer as RTCCameraVideoCapturer?
}

fun switchCamera(
    onSuccess: (Boolean) -> Unit = {},
    onError: (String?) -> Unit = {}
) = videoCapturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
    override fun onCameraSwitchDone(p0: Boolean) = onSuccess(p0)
    override fun onCameraSwitchError(p0: String?) = onError(p0)
})