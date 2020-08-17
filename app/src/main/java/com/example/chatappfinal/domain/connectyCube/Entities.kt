package com.example.chatappfinal.domain.connectyCube

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.google.gson.annotations.SerializedName

data class RegParams(
    val login: String = "",
    val password: String = "",
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val avatar: String = "unknown"
)

@Entity
data class PushObject(
    @PrimaryKey
    @field:SerializedName("message_id") val messageId: String ,
    @field:SerializedName("dialog_id") val dialogId: String? ,
    @field:SerializedName("dialog_name") val dialogName: String? ,
    @field:SerializedName("message_text") val messageText: String? ,
    @field:SerializedName("type") val type: String? ,
    @field:SerializedName("user_id") val userId: Int?
)

data class InAppDialog(
    val dialog: ConnectycubeChatDialog,
    var unmuted: Boolean? = null
)