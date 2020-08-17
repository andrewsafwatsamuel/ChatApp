@file:Suppress("DEPRECATION")

package com.example.chatappfinal.domain.connectyCube.textChat

import android.os.Bundle
import com.connectycube.chat.ConnectycubeRestChatService
import com.connectycube.chat.model.*
import com.connectycube.chat.request.DialogRequestBuilder
import com.connectycube.chat.request.SearchRequestBuilder
import com.connectycube.core.EntityCallback
import com.connectycube.core.exception.ResponseException
import com.connectycube.core.request.RequestUpdateBuilder

internal fun ConnectycubeChatDialog.doInDialog(
    method: ConnectycubeChatDialog.() -> Unit
) = try {
    method()
} catch (e: Exception) {
    e.printStackTrace()
}

fun<T> createEntityCallbacks(
    onSuccess: (T?) -> Unit = {},
    onError: (ResponseException?) -> Unit = {}
)=object :EntityCallback<T>{
    override fun onSuccess(p0: T, p1: Bundle?) = onSuccess(p0)
    override fun onError(p0: ResponseException?) =onError(p0)
}

//global search
fun search(
    text: String,
    builder: SearchRequestBuilder?,
    callBacks: EntityCallback<SearchChatEntity>
) = ConnectycubeRestChatService
    .searchByText(text, builder)
    .performAsync(callBacks)

internal fun updateDialog(
    dialog: ConnectycubeChatDialog,
    callBacks: EntityCallback<ConnectycubeChatDialog>,
    requestBuilder: DialogRequestBuilder = DialogRequestBuilder(),
    method: DialogRequestBuilder.() -> RequestUpdateBuilder
) = ConnectycubeRestChatService.updateChatDialog(dialog, requestBuilder.method())
    .performAsync(callBacks)