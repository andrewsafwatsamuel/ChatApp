package com.example.chatappfinal

import android.app.Application
import android.app.NotificationManager
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.connectycube.chat.ConnectycubeRestChatService
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.core.EntityCallback
import com.connectycube.core.exception.ResponseException
import com.example.chatappfinal.domain.connectyCube.ConnectyCube
import com.example.chatappfinal.domain.connectyCube.PushObject
import com.example.chatappfinal.domain.connectyCube.pushNotifications.*
import com.example.chatappfinal.domain.connectyCube.textChat.chatLogin
import com.example.chatappfinal.domain.dataSources.databaseGateway.chatDatabase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.lang.StringBuilder
import java.util.*

const val TEXT_CHANNEL_ID = "text_channel"
const val CALL_CHANNEL_ID = "call_channel"

class ChatAppFinalApp : Application() {

    private val callReceiver by lazy { CallBroadCastReceiver() }
    private lateinit var disposable: Disposable

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        ConnectyCube.initialize(this)
        createChannels()
        registerReceiver(callReceiver, IntentFilter(getString(R.string.action_call)))
        disposable = chatDatabase.pushObjectDao.retrieveAllMessages()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ m -> m.mapMessages() }, Throwable::printStackTrace)

    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createNotificationChannel(
                TEXT_CHANNEL_ID,
                "Text chat",
                NotificationManager.IMPORTANCE_MAX,
                "channel for text messages"
            )
            createNotificationChannel(
                CALL_CHANNEL_ID,
                "Calling chat",
                NotificationManager.IMPORTANCE_MAX,
                "channel for video calls"
            )
        }
    }

    private fun List<PushObject>.mapMessages() = toMutableList()
        .groupBy { it.dialogName }
        .also { notificationManager.cancelAll() }
        .forEach { it.createDialogNotification(StringBuilder()) }

    private fun Map.Entry<String?, List<PushObject>>.createDialogNotification(
        messageBuilder: StringBuilder
    ) = takeUnless { value.isNullOrEmpty() }
        ?.run {
            value.forEach { messageBuilder.append("${it.messageText}\n") }
            val push = PushObject(
                value[0].messageId,
                key,
                value.setTitle(),
                value[0].messageText,
                value[0].type,
                value[0].userId,
                value[0].senderLogin
            )
            onNotLoggedIn(value[0].dialogId?:""){
                sendChatNotification(push, messageBuilder.toString(),it)
            }

        }

    private fun onNotLoggedIn(id: String, dialog: (ConnectycubeChatDialog)->Unit)=
        chatLogin { if (it!=null) getDialog(id,dialog) }


    private fun getDialog(id:String, dialog: (ConnectycubeChatDialog)->Unit)=ConnectycubeRestChatService
        .getChatDialogById(id)
        .performAsync(object :EntityCallback<ConnectycubeChatDialog> {
            override fun onSuccess(p0: ConnectycubeChatDialog?, p1: Bundle?) {
                p0?.let { dialog(it) }
            }

            override fun onError(p0: ResponseException?)=Unit
        })

    private fun List<PushObject>.setTitle()=(if (size==1) "1 message " else "$size messages ")
        .plus("from ${get(0).senderLogin}")

    private fun sendChatNotification(pushObject: PushObject, bigText: String,dialog: ConnectycubeChatDialog) {
        createNotification(pushObject, bigText,dialog)
        sendBroadcast(createIntentBroadCast())
    }

    private fun createNotification(
        pushObject: PushObject,
        bigText: String,
        dialog: ConnectycubeChatDialog
    ) = createNotificationBuilder(TEXT_CHANNEL_ID, this)
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setContentText(pushObject.messageText)
        .setContentTitle(pushObject.dialogName)
        .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
        .apply { setContentIntent(createPendingIntent(R.id.chatFragment, Bundle().apply { putSerializable("dialog",dialog) })) }
        .let { notificationManager.notify(Random().nextInt(), it.build()) }

    private fun createIntentBroadCast() = Intent(ONLINE_NOTIFICATION)

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(callReceiver)
        disposable.dispose()
    }
}