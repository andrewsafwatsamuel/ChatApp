package com.example.chatappfinal.presentation.features.chat

import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.util.contains
import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.connectycube.chat.model.ConnectycubeChatMessage
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.connectyCube.getUserFromPreference
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.DELIVERED
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.InAppMessage
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.READ
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.getAttachmentUrl
import com.example.chatappfinal.presentation.BaseViewHolder
import com.example.chatappfinal.presentation.DefaultSelectableAdapter
import com.example.chatappfinal.presentation.hide

private val messagesUtils by lazy {
    object : DiffUtil.ItemCallback<InAppMessage>() {
        override fun areItemsTheSame(oldItem: InAppMessage, newItem: InAppMessage): Boolean =
            oldItem.connectyCubeMessage.id == newItem.connectyCubeMessage.id


        override fun areContentsTheSame(oldItem: InAppMessage, newItem: InAppMessage): Boolean =
            oldItem.status == newItem.status
    }
}

class ChatViewHolder(
    itemView: View,
private val onPhotoClicked: (String) -> Unit
    ) : BaseViewHolder<InAppMessage>(itemView) {

    private val messageTextView by lazy { view.findViewById<TextView>(R.id.message_textView) }
    private val statusImageView by lazy { view.findViewById<ImageView>(R.id.status_imageView) }
    private val attachmentImageView by lazy { view.findViewById<ImageView>(R.id.attachment_imageView) }
    private val isSelectedView by lazy { view.findViewById<View>(R.id.selected_view) }

    override fun bind(item: InAppMessage, isSelected: Boolean) = with(item) {
        messageTextView.text = connectyCubeMessage.body

        connectyCubeMessage.getAttachmentUrl()
            .takeUnless { it.isBlank() }
            ?.let { loadPhoto(it); attachmentImageView.visibility = View.VISIBLE }
        if (connectyCubeMessage.senderId == getUserFromPreference()?.id) statusImageView.setImageResource(
            status.drawStatus()
        )
        if (messageTextView.text == "null") messageTextView.hide()
        isSelectedView.visibility = if (isSelected) View.VISIBLE else View.GONE
    }

    private fun loadPhoto(url: String) = Glide.with(view.context)
        .load(url)
        .into(attachmentImageView)
        .also { attachmentImageView.setOnClickListener{onPhotoClicked(url)} }


    private fun String.drawStatus() = when (this) {
        DELIVERED -> R.drawable.ic_delivered
        READ -> R.drawable.ic_read
        else -> R.drawable.ic_sent
    }
}

class ChatAdapterDefault(
    private val selectedItems: SparseBooleanArray = SparseBooleanArray(),
  private val onLongClick: (List<InAppMessage>) -> Unit,
    private val onPhotoClicked:(String)->Unit
) : ListAdapter<InAppMessage, ChatViewHolder>(messagesUtils) {

    private fun isSelected(position: Int) = selectedItems.contains(position)

    private fun toggleSelection(position: Int) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position)
        } else {
            selectedItems.put(position, true)
        }
        notifyItemChanged(position)
    }

    private fun clearSelection(selection: SparseBooleanArray = selectedItems) {
        selectedItems.clear()
        selection.forEach { position, _ -> notifyItemChanged(position) }
    }

    fun getSelectedItemCount() = selectedItems.size()

    fun getSelectedItems() =
        arrayListOf<Int>().apply { selectedItems.forEach { item, _ -> add(item) } }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position), isSelected(position))
        holder.view.setOnLongClickListener { setOnSelectionChanged(position) }
        holder.view.setOnClickListener {if (selectedItems.isNotEmpty()) setOnSelectionChanged(position) }

    }
    private fun setOnSelectionChanged(position: Int): Boolean {
        toggleSelection(position)
        onLongClick(getSelection())
        return true
    }
    private fun getSelection() = mutableListOf<InAppMessage>()
        .apply { getSelectedItems().forEach { add(currentList[it]) } }

    override fun getItemViewType(
        position: Int
    ): Int = if (getItem(position).connectyCubeMessage.senderId in listOf(
            getUserFromPreference()?.id,
            null
        )
    ) 1 else 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LayoutInflater
        .from(parent.context)
        .inflate(
            if (viewType == 1) R.layout.item_message_send else R.layout.item_message_receive,
            parent,
            false
        )
        .let { ChatViewHolder(it,onPhotoClicked) }

    fun getMessageIds() = mutableListOf<ConnectycubeChatMessage>()
        .apply { getSelectedItems().forEach { add(currentList[it].connectyCubeMessage) } }
        .toList()

}
