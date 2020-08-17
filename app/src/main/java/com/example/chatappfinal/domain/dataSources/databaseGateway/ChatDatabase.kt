package com.example.chatappfinal.domain.dataSources.databaseGateway

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.chatappfinal.domain.connectyCube.ConnectyCube
import com.example.chatappfinal.domain.connectyCube.PushObject

val chatDatabase by lazy {
    Room.databaseBuilder(ConnectyCube.appContext, ChatDatabase::class.java, "database-name").build()
}

@Database(entities = [PushObject::class], version = 1, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract val pushObjectDao: PushObjectDao
}
