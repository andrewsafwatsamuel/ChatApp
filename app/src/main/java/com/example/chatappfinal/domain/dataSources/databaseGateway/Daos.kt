package com.example.chatappfinal.domain.dataSources.databaseGateway

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.chatappfinal.domain.connectyCube.PushObject
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface PushObjectDao {
    @Insert
    fun insertMessage(pushObject:PushObject):Completable

    @Query("DELETE FROM PushObject WHERE dialogId LIKE :id")
    fun removeOpenedMessages(id:String):Completable

    @Query("SELECT * FROM PushObject")
    fun retrieveAllMessages(): Observable<List<PushObject>>

}
