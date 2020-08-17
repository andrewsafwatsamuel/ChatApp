package com.example.chatappfinal.presentation.features.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatappfinal.domain.connectyCube.signIn
import com.example.chatappfinal.presentation.*
import java.lang.Exception

class LoginViewModel(
    val loginStates: MutableLiveData<AuthState> = MutableLiveData()
) : ViewModel() {

    fun login(login: String, password: String) = login
        .also { if (it.isEmpty().or(password.isEmpty())) loginStates.value = ErrorState("please fill all fields") }
        .takeUnless { it.isEmpty().or(password.isEmpty()).or(loginStates.value is LoadingState) }
        ?.also { loginStates.value = LoadingState }
        ?.let { signIn(login, password) {onFinishedLogin(it)} }

    private fun onFinishedLogin(exception: Exception?) {
        loginStates.value = if (exception == null) SuccessState
        else ErrorState(exception.message ?: UNKNOWN_ERROR)
    }
}