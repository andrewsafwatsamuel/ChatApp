package com.example.chatappfinal.presentation.features.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.chat.model.ConnectycubeDialogType
import com.connectycube.core.ConnectycubeProgressCallback
import com.connectycube.core.helper.StringifyArrayList
import com.example.chatappfinal.CallingActivity
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.connectyCube.getUserFromPreference
import com.example.chatappfinal.domain.connectyCube.pushNotifications.MESSAGE
import com.example.chatappfinal.domain.connectyCube.pushNotifications.composeChatNotification
import com.example.chatappfinal.domain.connectyCube.pushNotifications.createEvent
import com.example.chatappfinal.domain.connectyCube.pushNotifications.currentDialog
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.ChatDialogHandler
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.dialogTypingListener
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.setNotTyping
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.editMessage
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.getRealPathFromURI
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.updateMessages
import com.example.chatappfinal.presentation.openPhotos
import com.example.chatappfinal.presentation.typingListener
import kotlinx.android.synthetic.main.content_conversation.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.toolbar.view.*
import timber.log.Timber

const val FILES_CODE = 101
const val VIDEO_CALL = "Video Call"
const val AUDIO_CALL = "Audio Call"

class ChatFragment : Fragment() {

    internal val dialog by lazy { ChatFragmentArgs.fromBundle(requireArguments()).dialog }

    private val adapter by lazy {
        ChatAdapterDefault(
            dialog.type,
            onLongClick = {
            onItemsSelected(it.map { message -> message.connectyCubeMessage })
        }, onPhotoClicked = { url ->
            ChatFragmentDirections.actionGlobalPhotoViewerFragment(url).let { findNavController().navigate(it)}
        })
    }
    private lateinit var menu: Menu
    internal fun deleteItem() = menu.findItem(R.id.item_delete_message)

    internal fun editItem() = menu.findItem(R.id.item_edit_message)


    private val viewModel by lazy {  ChatViewModelFactory(dialog).create(ChatViewModel::class.java) }
    private val onTextChanged by lazy { typingListener { viewModel.isTypingSubject.onNext(it > 0) } }

    internal val deleteDialog by lazy { DeleteDialog() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_chat, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        drawToolbar(dialog)

        ChatDialogHandler(dialog, viewLifecycleOwner, viewModel.messageListener)

        recyclerView.adapter = adapter

        updateMessages(viewModel.disposables) {
            adapter.submitList(it)
            recyclerView.scrollToPosition(adapter.messages.size - 1)
            hideOnSelected()
        }
        setOnClicks(dialog)
        et_message.addTextChangedListener(onTextChanged)
        currentDialog.setCurrentDialog(dialog.dialogId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        et_message.removeTextChangedListener(onTextChanged)
        currentDialog.removeCurrentDialogId()
        dialog.setNotTyping()
    }

    private fun setOnClicks(dialog: ConnectycubeChatDialog) {
        bt_attachment.setOnClickListener { openPhotos() }
        bt_send.setOnClickListener {
            if (adapter.getSelectedItemCount() == 1) edit(dialog) else makeMessage(dialog)
            progress_textView.text = ""
        }
    }

    private fun makeMessage(dialog: ConnectycubeChatDialog) =
        viewModel.sender.sendMessage(et_message.text.toString(), dialog)
            ?.also {
                sendNotification(it.id, et_message.text.toString(), dialog)
                et_message.setText("")
            }

    private fun sendNotification(
        messageId: String,
        messageText: String,
        dialog: ConnectycubeChatDialog
    ) {
        createEvent(
            StringifyArrayList<Int>().apply { addAll(dialog.occupants) }, composeChatNotification(
                MESSAGE, dialog.dialogId, dialog.name, messageId, messageText, getUserFromPreference()?.login?:""
            )
        )
    }

    private fun edit(dialog: ConnectycubeChatDialog) = editMessage(
        adapter.getMessageIds()[0].id,
        et_message.text.toString(),
        adapter.messages.size - 1 == adapter.getSelectedItems()[0],
        dialog
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) onSuccessfulActivityResult(requestCode, data)
    }

    @SuppressLint("SetTextI18n")
    private fun onSuccessfulActivityResult(requestCode: Int, data: Intent?) = when (requestCode) {
        FILES_CODE -> viewModel.sender.attachPhoto(
            requireContext().getRealPathFromURI(data?.data!!) ?: "",
            ConnectycubeProgressCallback {
                progress_textView.text = "loading your photo $it%"
            Timber.i("progress $it")
            }

        )
        else -> Unit
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun drawToolbar(dialog: ConnectycubeChatDialog) = with(toolbar.main_toolbar) {
        title = dialog.name
        dialogTypingListener.listenOnTypingChange(viewModel.disposables) {
            title = if (it.isNotEmpty()&&it!= getUserFromPreference()?.login) "$it is typing" else dialog.name
        }
        setupWithNavController(findNavController())
        navigationIcon = requireContext().getDrawable(R.drawable.ic_action_back)
        inflateMenu(R.menu.menu_chat)
        this@ChatFragment.menu = menu
        hideOnSelected()
        setOnMenuItemClickListener { onMenuItemClicked(it, dialog) }
        if (dialog.type != ConnectycubeDialogType.PRIVATE) {
            menu.findItem(R.id.item_voice_call).isVisible = false
            menu.findItem(R.id.item_video_call).isVisible = false
        }
        setOnClickListener {
            if (dialog.type in listOf(ConnectycubeDialogType.GROUP, ConnectycubeDialogType.PUBLIC))
                ChatFragmentDirections.actionChatFragmentToEditDialogFragment(dialog.dialogId)
                    .let { findNavController().navigate(it) }
        }
    }

    private fun hideOnSelected() {
        deleteItem().isVisible = false
        editItem().isVisible = false
    }

    private fun onMenuItemClicked(menuItem: MenuItem, dialog: ConnectycubeChatDialog) = menuItem
        .takeIf { dialog.type == ConnectycubeDialogType.PRIVATE }
        ?.let { setCallType(menuItem.itemId, dialog);true } ?: false

    private fun setCallType(id: Int, dialog: ConnectycubeChatDialog) = when (id) {
        R.id.item_video_call -> startCall(VIDEO_CALL, dialog)
        R.id.item_voice_call -> startCall(AUDIO_CALL, dialog)
        else -> Unit
    }

    private fun startCall(
        flag: String,
        dialog: ConnectycubeChatDialog
    ) = Intent(requireContext(),CallingActivity::class.java)
        .apply {
            putExtra("recepientId",dialog.recipientId)
            putExtra("flag",flag)
            putExtra("dialog",dialog)
        }.let { startActivity(it) }
}