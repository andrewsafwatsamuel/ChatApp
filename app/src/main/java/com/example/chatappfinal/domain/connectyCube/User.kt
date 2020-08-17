package com.example.chatappfinal.domain.connectyCube

import com.connectycube.core.EntityCallback
import com.connectycube.users.ConnectycubeUsers
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks

fun signUp(params: RegParams, onComplete: (Exception?) -> Unit = {}) = ConnectycubeUsers
    .signUp(registerUser(params))
    .performAsync(createEntityCallbacks({ removeUserFromPreference();onComplete(null) },onComplete))

private fun registerUser(params: RegParams) = ConnectycubeUser().apply {
        login = params.login
        phone = params.phone
        password = params.password
        email = params.email
        fullName = params.fullName
        avatar = params.avatar
    }

fun signIn(
    mail: String,
    pass: String,
    onComplete: (Exception?) -> Unit = {}
) = ConnectycubeUser()
    .apply { login = mail;password = pass;avatar = "" }
    .let { ConnectycubeUsers.signIn(it) }
    .performAsync(createEntityCallbacks({it?.let { saveUserWithPassword(it,pass)};onComplete(null)}, onComplete))

fun signOut(onComplete: (Exception?) -> Unit = {}) = ConnectycubeUsers
    .signOut()
    .performAsync(createEntityCallbacks({ removeUserFromPreference();onComplete(null) }, onComplete))

fun getUsers(
    ids: List<Int>,
    callBack: EntityCallback<ArrayList<ConnectycubeUser>>
) = ConnectycubeUsers.getUsersByIDs(ids, null).performAsync(callBack)