package com.example.chatappfinal.presentation.features.editDialogInfo

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.core.ConnectycubeProgressCallback
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.DialogUpdateParams
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.attachPhoto
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.getRealPathFromURI
import com.example.chatappfinal.domain.dataSources.runtimeCache
import com.example.chatappfinal.domain.dialogsRepository
import com.example.chatappfinal.presentation.*
import com.example.chatappfinal.presentation.features.AddToChatActivity
import com.example.chatappfinal.presentation.features.chat.FILES_CODE
import kotlinx.android.synthetic.main.fragment_edit_dialog.*
import kotlinx.android.synthetic.main.toolbar.view.*

class EditDialogFragment : Fragment(){

    private val dialogId by lazy { EditDialogFragmentArgs.fromBundle(requireArguments()).dialogId }
    private lateinit var dialog: ConnectycubeChatDialog

    private var url: String = ""

    private val adapter by lazy {
        DialogParticipantsAdapter { user, admin -> onUserClicked(user, admin) }
    }

    internal val viewModel by lazy { EditDialogFactory(dialogId).create(EditDialogViewModel::class.java) }

    val receiver by lazy {
        object :BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                viewModel.addUser(p1?.getIntExtra("extraId",0)?:0)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requireContext().registerReceiver(receiver, IntentFilter("receiver"))
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(receiver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_edit_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = dialogsRepository.getDialogById(dialogId)  ?: ConnectycubeChatDialog()
        drawToolbar()
        chat_users_recyclerView.adapter = adapter
        viewModel.usersLiveData.observe(viewLifecycleOwner, Observer { adapter.submitList(it) })
        viewModel.adminsLiveData.observe(viewLifecycleOwner, Observer { adapter.submitAdmins(it) })
        viewModel.stateLiveData.observe(viewLifecycleOwner, Observer { drawStates(it) })
        drawDialog()
    }

    private fun drawToolbar() = with(edit_chat_toolbar.main_toolbar) {
        setupWithNavController(findNavController())
        navigationIcon = requireContext().getDrawable(R.drawable.ic_action_back)
        inflateMenu(R.menu.menu_edit_chat)
        title = "${dialog.name} Settings"
        setTitleTextColor(Color.WHITE)
        setOnMenuItemClickListener {
        requireActivity().startActivityForResult(Intent(requireContext(),AddToChatActivity::class.java),120)
            true
        }
    }

    private fun drawDialog() {
        group_name_editTextText.setText(dialog.name)
        description_editTextText.setText(dialog.description)
        group_photo_imageView.setOnClickListener { openPhotos() }
        update_chat_button.setOnClickListener { viewModel.updateDialogInfo(createParams()) }
        exit_chat_button.setOnClickListener { viewModel.unsubscribe() }
        delete_dialog_button.setOnClickListener { viewModel.clearDialog() }
        group_photo_imageView.loadPhoto(dialog.photo)
    }

    private fun createParams() = DialogUpdateParams(
        url,
        description_editTextText.text.toString(),
        group_name_editTextText.text.toString()
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == FILES_CODE) attachPhoto(
            requireContext().getRealPathFromURI(data?.data!!) ?: "",
            ConnectycubeProgressCallback { }
        ) { group_photo_imageView.loadPhoto(it.url);url = it.url }
    }

    private fun drawStates(state: EditDialogState) = when (state) {
        is Loading -> loading()
        is ErrorState -> error(state.message)
        is SuccessInScreenState -> successInScreen()
        is SuccessOutScreen -> successOutScreen()
        is PrivateChatState -> successChat(state.dialog)
    }

    private fun loading() {
        group_photo_imageView.disable()
        delete_dialog_button.disable()
        update_chat_button.disable()
        exit_chat_button.disable()
        chat_users_recyclerView.disable()
        edit_dialog_progressBar.show()
    }

    private fun error(message: String) {
        onNotLoading()
        requireView().createSnackBar(message)
    }

    private fun successInScreen() = onNotLoading()

    private fun onNotLoading() {
        group_photo_imageView.enable()
        delete_dialog_button.enable()
        exit_chat_button.enable()
        chat_users_recyclerView.enable()
        update_chat_button.enable()
        edit_dialog_progressBar.hide()
    }

    private fun successOutScreen() {
        findNavController().navigate(R.id.action_global_dialogsFragment)
    }

    private fun successChat(dialog: ConnectycubeChatDialog?) = dialog
        ?.let { EditDialogFragmentDirections.actionGlobalChatFragment(it) }
        ?.let { findNavController().navigate(it) }
}