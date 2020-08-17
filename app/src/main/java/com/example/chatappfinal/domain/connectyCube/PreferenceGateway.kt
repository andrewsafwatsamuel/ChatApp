package com.example.chatappfinal.domain.connectyCube

import android.content.Context
import com.connectycube.users.model.ConnectycubeUser
import com.google.gson.Gson

private const val USER_PREFERENCE_KEY = "user_preference_key"
private const val USER_PREFERENCES = "USER_PREFERENCES"

private fun getUserPreference(appContext:Context= ConnectyCube.appContext)=
    appContext.getSharedPreferences(USER_PREFERENCES,Context.MODE_PRIVATE)

fun getUserFromPreference(key:String = USER_PREFERENCE_KEY):ConnectycubeUser? = getUserPreference()
    .getString(key,null)
    ?.takeUnless { it.isBlank() }
    ?.let {Gson().fromJson(it,ConnectycubeUser::class.java)}

fun saveUserWithPassword(user: ConnectycubeUser,password:String)= user
    .also { it.password = password }
    .let { saveUserToPreference(it) }

private fun saveUserToPreference(
    user: ConnectycubeUser,
    key:String = USER_PREFERENCE_KEY
) = getUserPreference()
    .edit()
    .putString(key,Gson().toJson(user))
    .commit()

fun removeUserFromPreference(key:String = USER_PREFERENCE_KEY) = getUserPreference()
    .edit()
    .remove(key)
    .commit()