package com.example.chatappfinal.domain.connectyCube.pushNotifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.chatappfinal.CALL_CHANNEL_ID
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.connectyCube.PushObject
import com.example.chatappfinal.domain.connectyCube.rtc.*
import com.example.chatappfinal.domain.connectyCube.textChat.chatLogin
import com.example.chatappfinal.domain.dataSources.databaseGateway.PushObjectDao
import com.example.chatappfinal.domain.dataSources.databaseGateway.chatDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

const val ONLINE_NOTIFICATION = "onlineNotification"
const val CALL_ACTION = "call_action"

class FCMService(
    private val pushObjectDao: PushObjectDao = chatDatabase.pushObjectDao,
    private val disposables: CompositeDisposable = CompositeDisposable()
) : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        chatLogin { }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        chatLogin { if (it == null) onLoggedIn(message) }
    }


    private fun onLoggedIn(message: RemoteMessage) = when (message.data["type"]) {
        CALL -> observeSession(message.data["dialog_name"] ?: "")
        MESSAGE -> insertNewMessage(message.data)
        else -> Unit
    }

    private fun observeSession(name: String) {
        sessionCallbacks.observeOnSession { observeOnState(it, name) }
    }

    private fun observeOnState(state: SessionState, name: String) = when (state) {
        is NewCall -> onNewCall(name)
        is SessionClosed -> onSessionClosed()
        else -> Unit
    }

    private fun onNewCall(name: String) {
        notificationManager.notify(102, createVideoNotification(name))
    }

    private fun onSessionClosed() {
        notificationManager.cancel(102)
    }

    private fun createVideoNotification(name: String) =
        createNotificationBuilder(CALL_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_video_call)
            setContentIntent(createPendingIntent(R.id.receiveCallFragment))
            setContentTitle("Incoming Call from $name")
            addAction(R.drawable.ic_phone, "Accept", createCallIntent(true, 103))
            addAction(R.drawable.ic_close, "Reject", createCallIntent(false, 104))
            setOngoing(true)
            priority = NotificationCompat.PRIORITY_MAX
        }.build()

    private fun createCallIntent(accept: Boolean, requestCode: Int) = Intent(getString(R.string.action_call))
            .apply { putExtra(CALL_ACTION, accept) }
            .let { PendingIntent.getBroadcast(this, requestCode, it, PendingIntent.FLAG_UPDATE_CURRENT) }

    private fun insertNewMessage(data: MutableMap<String?, String?>): Unit = PushObject(
        data["message_id"] ?: "",
        data["dialog_id"],
        data["dialog_name"],
        data["message_text"],
        data["type"],
        data["user_id"].takeUnless { it.isNullOrBlank() }?.toInt(),
        data["sender_login"]
    ).takeUnless { currentDialog.isCurrentDialog(it.dialogId)  }
        ?.let { pushObjectDao.insertMessage(it) }
        ?.subscribeOn(Schedulers.io())
        ?.subscribe({}, Throwable::printStackTrace)
        ?.let { disposables.add(it) ;Unit} ?:Unit

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.i("token is $token")
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }
}

//onReceive message trigger login
//on successful login pass a function that
//function
//if title is call observe on session
//if callback is new call show notification of new call with answer and reject
//call notification onClick redirects to receive call fragment
//call notification on answer redirects to in call fragment
//call notification on reject rejects call
//if callback is closed or closing cancel notification
//if title is not call show notification by the old configurations

class CallBroadCastReceiver : BroadcastReceiver() {

    private var callAction = false
    private lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        callAction = intent.getBooleanExtra(CALL_ACTION, false)
        if (callAction) acceptCall(context) else rejectCall()
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(102)
    }

    private fun acceptCall(context: Context) = getCurrentSession()
        ?.acceptCall(getUserInfo())
        ?.let { context.createTaskBuilder(R.id.callingFragment) }
        ?.intents
        ?.takeUnless { it.isEmpty() }
        ?.let { context.startActivity(it[0]) }

    private fun rejectCall() = getCurrentSession()?.rejectCall(getUserInfo())

    private fun getUserInfo() = getCurrentSession()?.userInfo

    private fun getCurrentSession() = sessionCallbacks.session

}