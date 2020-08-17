package com.example.chatappfinal.presentation.features.dialogs

import com.example.chatappfinal.domain.connectyCube.InAppDialog
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.muteDialog
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.removeDialogUser
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.unMuteDialog
import com.example.chatappfinal.presentation.onClicked
import timber.log.Timber

fun DialogsFragment.onItemsSelected(dialogs: List<InAppDialog>) {
    deleteItem()?.isVisible = dialogs.isNotEmpty()
    deleteItem()?.onClicked { deleteDialogs(dialogs.map { it.dialog.dialogId });hideMultiSelectMenuItems() }
    muteItem()?.isVisible = dialogs.size == 1
    muteItem()?.onClicked { mute(dialogs[0]);hideMultiSelectMenuItems() }
}

private fun DialogsFragment.deleteDialogs(dialogs: List<String>) {
    for (i in dialogs.indices) {
        if (i < dialogs.size - 1) dialogs[i].removeDialogUser()
        else dialogs[i].removeDialogUser(createEntityCallbacks({ viewModel.retrieveDialogs() }, Timber::e))
    }
}

private fun DialogsFragment.mute(inAppDialog:InAppDialog) = with(inAppDialog){
    if (unmuted==true) muteDialog(dialog.dialogId, createEntityCallbacks ({ viewModel.retrieveDialogs()},Timber::e))
    else unMuteDialog(dialog.dialogId, createEntityCallbacks ({ viewModel.retrieveDialogs()},Timber::e))
}
