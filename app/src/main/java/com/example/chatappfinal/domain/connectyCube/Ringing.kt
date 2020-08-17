package com.example.chatappfinal.domain.connectyCube

import android.content.Context
import android.media.MediaPlayer

val incomingRingingManager by lazy { RingingManager() }

class RingingManager(private var mediaPlayer:MediaPlayer? = null){

    fun start(
        track: Int,
        context: Context = ConnectyCube.appContext
    ){
        mediaPlayer = MediaPlayer.create(context,track)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    fun stopRinging()=mediaPlayer?.run {
      //  stop()
        release()
    }

}