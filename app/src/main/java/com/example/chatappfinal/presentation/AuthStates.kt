package com.example.chatappfinal.presentation

sealed class AuthState
object LoadingState : AuthState()
class ErrorState(val message: String) : AuthState()
object SuccessState : AuthState()

interface AuthStates{
    fun loading()
    fun success()
    fun error(message: String)
}

fun AuthStates.drawStates(state: AuthState) = when(state){
    is LoadingState -> loading()
    is SuccessState -> success()
    is ErrorState -> error(state.message)
}