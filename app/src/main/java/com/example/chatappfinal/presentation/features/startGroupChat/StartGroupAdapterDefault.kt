package com.example.chatappfinal.presentation.features.startGroupChat

import android.view.ViewGroup
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.presentation.BaseSelectableAdapter
import com.example.chatappfinal.presentation.features.contacts.ContactsViewHolder
import com.example.chatappfinal.presentation.inflateView

class StartGroupAdapterDefault(
    private val layout: Int,
    onClick: (List<ConnectycubeUser>) -> Unit
) : BaseSelectableAdapter<ConnectycubeUser, ContactsViewHolder>(onClick, {}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = parent
        .inflateView(layout)
        .let { ContactsViewHolder(it) }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.view.setOnClickListener { setOnSelectionChanged(position) }
    }
}