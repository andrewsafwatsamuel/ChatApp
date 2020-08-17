package com.example.chatappfinal.presentation.features.startCall

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.connectycube.users.model.ConnectycubeUser
import com.connectycube.videochat.RTCClient
import com.connectycube.videochat.RTCSession
import com.connectycube.videochat.RTCTypes
import com.example.chatappfinal.domain.connectyCube.createSession
import com.example.chatappfinal.domain.connectyCube.getUser
import com.example.chatappfinal.domain.connectyCube.rtc.*
import com.example.chatappfinal.domain.connectyCube.rtcClient
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import com.example.chatappfinal.presentation.features.chat.AUDIO_CALL
import com.example.chatappfinal.presentation.features.chat.VIDEO_CALL
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.IllegalStateException

sealed class StartCallState
object LoadingState : StartCallState()
object ErrorState : StartCallState()
class SuccessUserState(val user:ConnectycubeUser?) : StartCallState()
class SuccessCall(val session:RTCSession?): StartCallState()

class StartCallViewModel(
    recipientId: Int,
    flag: String,
    callbacks: SessionCallbacks = sessionCallbacks,
    private var disposable: Disposable? = null,
    private var client: RTCClient? = rtcClient,
    var session: RTCSession?=null,
    val stateLiveData: MutableLiveData<StartCallState> = LoadingState.toMutableLiveData()
) : ViewModel() {

    init {
        retrieveUser(recipientId,flag)
        disposable = callbacks.observeOnSession { stateLiveData.value  = onCallbackStateChanged(it)}
    }

    private fun retrieveUser(userId: Int, flag: String) = getUser(userId, createEntityCallbacks(
        {
           session =  client?.createSession(listOf(userId), getType(flag))
            stateLiveData.value = SuccessUserState(it)
        },
        { stateLiveData.value = ErrorState  }
    ))

    private fun getType(flag: String) = when (flag) {
        AUDIO_CALL -> RTCTypes.ConferenceType.CONFERENCE_TYPE_AUDIO
        VIDEO_CALL -> RTCTypes.ConferenceType.CONFERENCE_TYPE_VIDEO
        else -> throw IllegalStateException("Bad Flag")
    }

    private fun onCallbackStateChanged(state: SessionState)=when(state){
        is CallAccepted -> SuccessCall(state.session)
        is CallRejected -> ErrorState
        else -> null
    }
    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }
}

@Suppress("UNCHECKED_CAST")
class StartCallViewModelFactory(
private val   recipientId: Int,
private val    flag: String
):ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(
        modelClass: Class<T>
    ): T = if (modelClass.isAssignableFrom(StartCallViewModel::class.java)) {
        StartCallViewModel(recipientId,flag)as T
    } else{
        throw IllegalStateException("Bad model class")
    }
}

fun<T> T.toMutableLiveData()=MutableLiveData<T>()
    .also { it.value = this }