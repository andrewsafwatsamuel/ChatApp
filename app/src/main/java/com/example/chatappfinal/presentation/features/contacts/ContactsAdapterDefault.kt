package com.example.chatappfinal.presentation.features.contacts

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.R
import com.example.chatappfinal.presentation.BaseViewHolder
import com.example.chatappfinal.presentation.DefaultSelectableAdapter
import com.example.chatappfinal.presentation.inflateView
import com.example.chatappfinal.presentation.loadPhoto

class ContactsViewHolder(view: View) : BaseViewHolder<ConnectycubeUser>(view) {

    private val contactNameTextView by lazy { view.findViewById<TextView>(R.id.contact_name_textView) }
    private val contactPhotoImageView by lazy { view.findViewById<ImageView>(R.id.contact_photo_imageView) }
    private val contactSelectedImageView : ImageView? by lazy {
        view.findViewById<ImageView>(R.id.contact_selected_imageView)
    }

    override fun bind(item: ConnectycubeUser, isSelected: Boolean) = with(item) {
        contactPhotoImageView.loadPhoto(avatar)
        contactNameTextView.text = login
        contactSelectedImageView?.visibility = if (isSelected)View.VISIBLE else View.GONE
    }
}

class ContactsAdapterDefault(
    onClick: (ConnectycubeUser) -> Unit,
    onLongClick: (List<ConnectycubeUser>) -> Unit
) : DefaultSelectableAdapter<ConnectycubeUser, ContactsViewHolder>(onLongClick, onClick) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = parent
        .inflateView(R.layout.item_contact)
        .let { ContactsViewHolder(it) }
}