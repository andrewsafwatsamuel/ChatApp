package com.example.chatappfinal.presentation.features.dialogs

import android.view.View
import android.view.ViewGroup
import com.example.chatappfinal.R
import com.example.chatappfinal.databinding.ItemDialogBinding
import com.example.chatappfinal.domain.connectyCube.InAppDialog
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.getDialogNotificationSettings
import com.example.chatappfinal.presentation.*
import timber.log.Timber

class DialogItemHolder(val binding: ItemDialogBinding) : BaseViewHolder<InAppDialog>(binding.root) {

    override fun bind(item: InAppDialog, isSelected: Boolean) = with(item.dialog) {

        binding.dialogPhotoImageView.loadPhoto(photo?:"")
        binding.dialogNameTextView.text = name ?: "name"
        binding.lastMessageTextView.text = lastMessage ?: ""
        binding.lastMessageDateTextView.text = formatDate(lastMessageDateSent)
        binding.unreadMessagesTextView.text = unreadMessageCount.toString()
        binding.isSelectedImageView.visibility = if (isSelected) View.VISIBLE else View.GONE
        handleMute(item)
    }

    private fun handleMute(dialog: InAppDialog) = getDialogNotificationSettings(
        dialog.dialog.dialogId,
        createEntityCallbacks({
            dialog.unmuted = it
            binding.muteImageView.visibility = if (it == false) View.VISIBLE else View.GONE
        }, Timber::e)
    )
}

class DialogsAdapterDefault(
    onLongClick: (List<InAppDialog>) -> Unit,
    onClick: (InAppDialog) -> Unit
) : DefaultSelectableAdapter<InAppDialog,DialogItemHolder>(onLongClick,onClick) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = parent
        .inflateView(R.layout.item_dialog)
        .let { DialogItemHolder(ItemDialogBinding.bind(it)) }

}

