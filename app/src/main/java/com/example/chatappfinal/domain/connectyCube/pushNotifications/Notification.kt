package com.example.chatappfinal.domain.connectyCube.pushNotifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.navigation.NavDeepLinkBuilder
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.connectyCube.ConnectyCube
import com.example.chatappfinal.presentation.MainActivity

val notificationManager by lazy {
    ConnectyCube.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}

fun createNotificationBuilder(
    channelId: String,
    context: Context = ConnectyCube.appContext
) = NotificationCompat.Builder(context, channelId).apply {
    priority = NotificationCompat.PRIORITY_HIGH
    setAutoCancel(true)
}

fun createNotificationChannel(channelId: String,name: String, importance: Int, descriptionText: String) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
            enableLights(true)
            enableVibration(true)

        }
        // Register the channel with the system
        notificationManager.createNotificationChannel(channel)
    } else Unit

fun Context.createPendingIntent(destination: Int,bundle: Bundle?=null): PendingIntent? = NavDeepLinkBuilder(this)
    .setComponentName(MainActivity::class.java)
    .setGraph(R.navigation.main_navigation)
    .setDestination(destination)
    .setArguments(bundle)
    .createPendingIntent()

fun Context.createTaskBuilder(destination: Int): TaskStackBuilder = NavDeepLinkBuilder(this)
    .setComponentName(MainActivity::class.java)
    .setGraph(R.navigation.main_navigation)
    .setDestination(destination)
    .createTaskStackBuilder()