package com.example.chatappfinal.presentation.features

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.contactsRepository
import com.example.chatappfinal.domain.dataSources.runtimeCache
import com.example.chatappfinal.presentation.features.editDialogInfo.DialogParticipantsAdapter
import kotlinx.android.synthetic.main.activity_add_to_chat.*

@Suppress("DEPRECATION")
class AddToChatActivity : AppCompatActivity() {

    private val adapter by lazy { DialogParticipantsAdapter { user, _ -> sendUserId(user.id) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_to_chat)
        setSize()
        add_user_recycler.adapter = adapter
        adapter.submitList(runtimeCache.getContactsFromCache())
        if (runtimeCache.getContactsFromCache().isEmpty()) contactsRepository.updateData {
            if (it == null) adapter.submitList(runtimeCache.getContactsFromCache())
        }
    }

    private fun setSize() = DisplayMetrics()
        .also { windowManager.defaultDisplay.getMetrics(it) }
        .apply { window.setLayout((widthPixels * 0.95).toInt(), (heightPixels * 0.8).toInt()) }

    private fun sendUserId(id: Int) = Intent("receiver")
        .apply { putExtra("extraId", id) }
        .let { i -> sendBroadcast(i) }
        .also { finish() }

}