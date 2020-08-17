package com.example.chatappfinal.presentation.features.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatappfinal.domain.connectyCube.RegParams
import com.example.chatappfinal.domain.connectyCube.signUp
import com.example.chatappfinal.presentation.*
import java.lang.Exception

class RegisterViewModel(
    val registerStates: MutableLiveData<AuthState> = MutableLiveData()
) : ViewModel() {
    fun register(params: RegParams,passConfirmation:String) = params
        .apply { if (fullName.isEmpty()||email.isEmpty()||login.isEmpty()||password.isEmpty()||phone.isEmpty() ) registerStates.value = ErrorState("please fill all fields") }
        .apply { if (password!=passConfirmation)registerStates.value= ErrorState("Password is not matching")}
        .takeUnless { (it.fullName.isEmpty()||it.email.isEmpty()||it.login.isEmpty()||it.password.isEmpty()||it.phone.isEmpty()).or(registerStates.value is LoadingState)|| it.password!=passConfirmation  }
        ?.also { registerStates.value = LoadingState }
        ?.let { signUp(it) {onFinishedRegister(it)} }

    private fun onFinishedRegister(exception: Exception?) {
        registerStates.value = if (exception == null) SuccessState
        else ErrorState(exception.message ?: UNKNOWN_ERROR)
    }
}