package com.example.chatappfinal.domain.connectyCube.pushNotifications

import timber.log.Timber

val currentDialog by lazy { CurrentDialog() }

class CurrentDialog(private var dialogId: String? = null) {
    fun setCurrentDialog(dialogId: String) {
        this.dialogId = dialogId
        Timber.i("current dialog $dialogId")
    }

    fun isCurrentDialog(dialogId: String?) =
        (this.dialogId != null && dialogId != null && dialogId == this.dialogId)
            .also {Timber.i("is currentDialog $it")}

    fun removeCurrentDialogId() {
        dialogId = null
        Timber.i("current dialog $dialogId")
    }
}