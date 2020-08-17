package com.example.chatappfinal.presentation.features.editDialogInfo

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.R
import com.example.chatappfinal.databinding.ItemContactBinding
import com.example.chatappfinal.presentation.BaseSelectableAdapter
import com.example.chatappfinal.presentation.BaseViewHolder
import com.example.chatappfinal.presentation.inflateView
import com.example.chatappfinal.presentation.loadPhoto
import timber.log.Timber

class DialogParticipantsViewHolder(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: ConnectycubeUser, isAdmin: Boolean) = with(item) {
        binding.contactPhotoImageView.loadPhoto(avatar)
        binding.dialogAdminTextView.visibility = if (isAdmin) View.VISIBLE else View.GONE
        binding.contactNameTextView.text = login
    }
}

class DialogParticipantsAdapter(
    private val admins:MutableList<Int> = mutableListOf(),
    private val items:MutableList<ConnectycubeUser> = mutableListOf(),
    private val onClick: (ConnectycubeUser,Boolean) -> Unit
) : RecyclerView.Adapter<DialogParticipantsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogParticipantsViewHolder = parent
        .inflateView(R.layout.item_contact)
        .let { ItemContactBinding.bind(it) }
        .let { DialogParticipantsViewHolder(it) }

    override fun getItemCount(): Int =items.size

    override fun onBindViewHolder(holder: DialogParticipantsViewHolder, position: Int)= with(holder) {
        bind(items[position],items[position].id in admins)
        binding.root.setOnClickListener { onClick(items[position],items[position].id in admins) }
    }
    fun submitList(items: List<ConnectycubeUser>){
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun submitAdmins(newAdmins:MutableList<Int>){
        admins.clear()
        admins.addAll(newAdmins)
        notifyDataSetChanged()
    }
}

class DialogParticipantsLayoutManager(context: Context,attributeSet: AttributeSet,defStyleAttr:Int,defStyleRes: Int):LinearLayoutManager(context,attributeSet,defStyleAttr,defStyleRes){
    override fun canScrollVertically() = false
}