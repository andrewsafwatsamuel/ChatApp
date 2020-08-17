package com.example.chatappfinal.domain.dataSources

import com.example.chatappfinal.domain.connectyCube.textChat.chatRoster
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import org.jivesoftware.smack.roster.RosterEntry
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = ""

val retrofitInstance by lazy {
Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
    .addConverterFactory(GsonConverterFactory.create())
    .build()
}

val api by lazy {
    retrofitInstance.create(Api::class.java)
}

interface Api

fun hello(){
}