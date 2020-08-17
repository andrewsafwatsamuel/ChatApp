package com.example.chatappfinal.presentation.features.contacts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.connectycube.chat.ConnectycubeRoster
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.domain.RetrieveCacheRepository
import com.example.chatappfinal.domain.connectyCube.textChat.chatRoster
import com.example.chatappfinal.domain.contactsRepository

sealed class ContactsState
object LoadingState : ContactsState()
object EmptyContactState : ContactsState()
class SuccessState(val Contacts: ArrayList<ConnectycubeUser>) : ContactsState()
class ErrorState(val message: String) : ContactsState()

class ContactsViewModel(
    private val repository: RetrieveCacheRepository<ConnectycubeUser> = contactsRepository,
    private val roster: ConnectycubeRoster = chatRoster,
    val stateLiveData: MutableLiveData<ContactsState> = MutableLiveData()
) : ViewModel() {

    init {
        getContacts()
    }

    fun getContacts() = roster.entries
        .also { if (it.isEmpty()) stateLiveData.value = EmptyContactState }
        .takeUnless { it.isEmpty() || stateLiveData.value is LoadingState }
        ?.also { stateLiveData.value = LoadingState }
        ?.let { updateContacts() }

    private fun updateContacts() = repository.updateData {
        stateLiveData.value = if (it==null)SuccessState(repository.retrieveData()) else ErrorState(it)
    }
}