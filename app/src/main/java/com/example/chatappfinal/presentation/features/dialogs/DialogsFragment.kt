package com.example.chatappfinal.presentation.features.dialogs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.connectyCube.pushNotifications.ONLINE_NOTIFICATION
import com.example.chatappfinal.domain.connectyCube.removeUserFromPreference
import com.example.chatappfinal.domain.connectyCube.rtc.NewCall
import com.example.chatappfinal.domain.connectyCube.rtc.sessionCallbacks
import com.example.chatappfinal.domain.connectyCube.signOut
import com.example.chatappfinal.domain.connectyCube.textChat.chatLogout
import com.example.chatappfinal.domain.connectyCube.toInApp
import com.example.chatappfinal.presentation.hide
import com.example.chatappfinal.presentation.show
import kotlinx.android.synthetic.main.error_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_dialogs.*
import kotlinx.android.synthetic.main.toolbar.view.*

class DialogsFragment : Fragment() {

    private lateinit var menu: Menu
    internal fun deleteItem()= menu.findItem(R.id.item_delete_dialog)
   internal fun muteItem()= menu.findItem(R.id.item_mute_dialog)

    internal val viewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(DialogsViewModel::class.java)
    }

    private val adapter by lazy {
        DialogsAdapterDefault(
            onClick = { navigateToChat(it.dialog) },
            onLongClick = { onItemsSelected(it) }
        )
    }

    private val retryButton by lazy { dialogs_error_layout.retry_button }
    private val errorTextView by lazy { dialogs_error_layout.error_textView }

    private val onlineReceiver by lazy {
        object : BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) = viewModel.retrieveDialogs()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_dialogs, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogs_recyclerView.adapter = adapter
        viewModel.dialogStates.observe(viewLifecycleOwner, Observer { drawStates(it) })
        retryButton.setOnClickListener { viewModel.login() }
        sessionCallbacks.observeOnSession { if (it is NewCall) { findNavController().navigate(R.id.action_global_receiveCallFragment) } }
        drawToolbar()
        floatingActionButton.setOnClickListener { findNavController().navigate(R.id.action_dialogsFragment_to_createChatFragment) }
    }

    private fun navigateToChat(dialog: ConnectycubeChatDialog) = DialogsFragmentDirections
        .actionGlobalChatFragment(dialog)
        .let { findNavController().navigate(it) }

    private fun drawStates(state: DialogState) = when (state) {
        is DialogsLoading -> onLoading()
        is DialogsSuccess -> onSuccess(state.dialogs)
        is DialogsError -> onError(state.message)
    }

    private fun drawToolbar() = with(toolbar.main_toolbar) {
        inflateMenu(R.menu.menu_dialog)
        setOnMenuItemClickListener { menuOnClick(it.itemId) }
        this@DialogsFragment.menu = menu
        hideMultiSelectMenuItems()
        title = "dialogs"
    }

    internal fun hideMultiSelectMenuItems() {
        deleteItem().isVisible = false
        muteItem().isVisible = false
    }

    private fun menuOnClick(itemId: Int) = when (itemId) {
        R.id.item_logout -> logout()
        R.id.item_contacts -> { findNavController().navigate(R.id.action_dialogsFragment_to_contactsFragment);true }
        else -> false
    }

    private fun logout(): Boolean {
        signOut()
        removeUserFromPreference()
        findNavController().navigate(R.id.action_dialogsFragment_to_loginFragment)
        chatLogout {  }
        return true
    }

    private fun onLoading() {
        dialogs_recyclerView.hide()
        dialogs_progressBar.show()
        dialogs_error_layout.hide()
        floatingActionButton.hide()
    }

    private fun onSuccess(dialogs: ArrayList<ConnectycubeChatDialog>) {
        adapter.submitList(toInApp(dialogs))
        dialogs_recyclerView.show()
        dialogs_progressBar.hide()
        dialogs_error_layout.hide()
        floatingActionButton.show()
    }

    private fun onError(message: String) {
        dialogs_error_layout.show()
        dialogs_recyclerView.show()
        dialogs_progressBar.hide()
        floatingActionButton.hide()
        errorTextView.text = message
    }

    override fun onResume() {
        super.onResume()
        viewModel.retrieveDialogs()
    }
    override fun onStart() {
        super.onStart()
        requireContext().registerReceiver(onlineReceiver, IntentFilter(ONLINE_NOTIFICATION))
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(onlineReceiver)
    }

}
