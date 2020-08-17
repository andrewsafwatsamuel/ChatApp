package com.example.chatappfinal.presentation.features.startChat

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.R
import com.example.chatappfinal.presentation.createSnackBar
import com.example.chatappfinal.presentation.features.contacts.ContactsAdapterDefault
import com.example.chatappfinal.presentation.hide
import com.example.chatappfinal.presentation.show
import kotlinx.android.synthetic.main.error_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_start_chat.*
import kotlinx.android.synthetic.main.toolbar.view.*

class StartChatFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(StartChatViewModel::class.java)
    }

    private val adapter by lazy { ContactsAdapterDefault ({ viewModel.startChat(it.id) },{}) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_start_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.stateLiveData.observe(viewLifecycleOwner, Observer { drawStates(it) })
        start_chat_recyclerView.adapter = adapter
        start_chat_bottomCheat.setOnClickListener { viewModel.getContacts() }
        group_chat_fab.setOnClickListener { findNavController().navigate(R.id.action_startChatFragment_to_stratGroupChatFragment) }
        drawToolbar()
    }

    private fun drawStates(state: StartChatState) = when (state) {
        is LoadingState -> loading()
        is EmptyContactState -> emptyContacts()
        is SuccessContactsState -> successContacts(state.Contacts)
        is ErrorContactsState -> errorContacts(state.message)
        is SuccessChatState -> successChat(state.dialog)
        is ErrorChatState -> errorChat(state.message)
    }

    private fun loading() {
        start_chat_recyclerView.show()
        group_chat_fab.hide()
        start_chat_bottomCheat.hide()
        start_chat_progressBar.show()
        create_chat_empty_textView.hide()
    }

    private fun emptyContacts() {
        start_chat_recyclerView.hide()
        group_chat_fab.show()
        start_chat_bottomCheat.hide()
        start_chat_progressBar.hide()
        create_chat_empty_textView.show()
    }

    private fun successContacts(contacts: ArrayList<ConnectycubeUser>) {
        start_chat_recyclerView.show()
        group_chat_fab.show()
        start_chat_bottomCheat.hide()
        start_chat_progressBar.hide()
        create_chat_empty_textView.hide()
        adapter.submitList(contacts)
    }

    private fun errorContacts(message: String) {
        start_chat_recyclerView.hide()
        group_chat_fab.hide()
        start_chat_bottomCheat.show()
        start_chat_progressBar.hide()
        create_chat_empty_textView.hide()
        start_chat_bottomCheat.error_textView.text = message
    }

    private fun successChat(dialog: ConnectycubeChatDialog?) = dialog
        ?.let { StartChatFragmentDirections.actionGlobalChatFragment(dialog) }
        ?.let { findNavController().navigate(it) }

    private fun errorChat(message: String) {
        start_chat_recyclerView.show()
        group_chat_fab.show()
        start_chat_bottomCheat.hide()
        start_chat_progressBar.hide()
        create_chat_empty_textView.hide()
        start_chat_layout.createSnackBar(message)
    }

    private fun drawToolbar() = with(create_chat_toolbar.main_toolbar) {
        setupWithNavController(findNavController())
        navigationIcon = requireContext().getDrawable(R.drawable.ic_action_back)
        setTitleTextColor(Color.WHITE)
    }

}
