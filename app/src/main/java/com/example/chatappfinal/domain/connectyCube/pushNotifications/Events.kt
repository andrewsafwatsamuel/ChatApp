package com.example.chatappfinal.domain.connectyCube.pushNotifications

import com.connectycube.core.helper.StringifyArrayList
import com.connectycube.pushnotifications.ConnectycubePushNotifications
import com.connectycube.pushnotifications.model.ConnectycubeEnvironment
import com.connectycube.pushnotifications.model.ConnectycubeEvent
import com.connectycube.pushnotifications.model.ConnectycubeNotificationType
import com.example.chatappfinal.domain.connectyCube.PushObject
import com.example.chatappfinal.domain.connectyCube.getUserFromPreference
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import com.google.gson.Gson
import timber.log.Timber

//types
const val CALL = "call"
const val MESSAGE = "message"

fun createEvent(
    users: StringifyArrayList<Int>,
    notificationMessage: String
) = ConnectycubeEvent().apply {
    userIds = users
    environment = ConnectycubeEnvironment.DEVELOPMENT
    notificationType = ConnectycubeNotificationType.PUSH
    message = notificationMessage
}.let { ConnectycubePushNotifications.createEvent(it) }
    .performAsync(createEntityCallbacks({ Timber.i("sent successfully") }, Timber::e))

fun composeChatNotification(
    type: String,
    dialogId: String,
    dialogName: String,
    messageId: String = "",
    messageText: String = ""
): String = PushObject(
    messageId,
    dialogId,
    dialogName,
    messageText,
    type,
    getUserFromPreference()?.id ?: 0
).let { Gson().toJson(it) }