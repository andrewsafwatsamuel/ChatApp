package com.example.chatappfinal.presentation.features.editDialogInfo

import android.app.Dialog
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.R
import kotlinx.android.synthetic.main.user_clicked.*

fun EditDialogFragment.onUserClicked(
    user: ConnectycubeUser,
    isAdmin: Boolean
) = Dialog(requireContext()).apply {
    setContentView(R.layout.user_clicked)
    send_message_textView.setOnClickListener { viewModel.startPrivateChat(user.id);dismiss() }
    make_admin_textView.setOnClickListener { (if (isAdmin)viewModel.removeAdmin(user.id) else viewModel.addToAdmins(id));dismiss() }
    remove_user_textView.setOnClickListener { viewModel.removeUser(user.id);dismiss() }
}.show()
