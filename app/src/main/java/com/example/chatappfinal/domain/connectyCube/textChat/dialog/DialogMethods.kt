package com.example.chatappfinal.domain.connectyCube.textChat.dialog

import com.connectycube.chat.ConnectycubeChatService
import com.connectycube.chat.ConnectycubeRestChatService
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.chat.model.ConnectycubeDialogType
import com.connectycube.chat.utils.DialogUtils
import com.connectycube.core.EntityCallback
import com.connectycube.core.request.RequestGetBuilder
import com.example.chatappfinal.domain.connectyCube.chatInstance
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import com.example.chatappfinal.domain.connectyCube.textChat.doInDialog
import com.example.chatappfinal.domain.connectyCube.textChat.updateDialog
import timber.log.Timber

//create Dialog
fun createPrivateDialog(id: Int, callBacks: EntityCallback<ConnectycubeChatDialog>) =
    createDialog(callBacks) { DialogUtils.buildPrivateDialog(id) }

fun createPublicDialog(
    chatName: String,
    occupants: List<Int> = listOf(),
    callBacks: EntityCallback<ConnectycubeChatDialog>
) = createNonPrivateDialog(chatName, occupants, callBacks, ConnectycubeDialogType.PUBLIC)

fun createGroupDialog(
    chatName: String,
    occupants: List<Int>,
    callBacks: EntityCallback<ConnectycubeChatDialog>
) = createNonPrivateDialog(chatName, occupants, callBacks, ConnectycubeDialogType.GROUP)


private fun createNonPrivateDialog(
    chatName: String,
    occupants: List<Int>,
    callBacks: EntityCallback<ConnectycubeChatDialog>,
    type: ConnectycubeDialogType
) = createDialog(callBacks) { DialogUtils.buildDialog(chatName, type, occupants) }

private fun createDialog(
    callBacks: EntityCallback<ConnectycubeChatDialog>,
    create: () -> ConnectycubeChatDialog
) = create()
    .let { ConnectycubeRestChatService.createChatDialog(it) }
    .performAsync(callBacks)

//get dialogs
fun getDialogs(
    callBacks: EntityCallback<ArrayList<ConnectycubeChatDialog>>,
    type: ConnectycubeDialogType? = null,
    builder: RequestGetBuilder? = null
) = ConnectycubeRestChatService
    .getChatDialogs(type, builder)
    .performAsync(callBacks)

//subscribe/unsubscribe to public/group dialog
fun subscribeToNonPrivateDialog(
    dialogId: String,
    callBacks: EntityCallback<ConnectycubeChatDialog>
) = ConnectycubeRestChatService.subscribePublicDialog(dialogId).performAsync(callBacks)

fun unSubNonPrivateDialog(
    dialogId: String,
    callBacks: EntityCallback<ConnectycubeChatDialog>
) = ConnectycubeRestChatService.unsubscribePublicDialog(dialogId).performAsync(callBacks)

//update chat info
data class DialogUpdateParams(
    val photoUrl: String? = "",
    val description: String? = "",
    val name: String? = ""
)

fun updateChatInfo(
    params: DialogUpdateParams,
    callBacks: EntityCallback<ConnectycubeChatDialog>,
    dialog: ConnectycubeChatDialog
) = params.composeDialog(dialog)
    .let { ConnectycubeRestChatService.updateChatDialog(dialog, null) }
    .performAsync(callBacks)

private fun DialogUpdateParams.composeDialog(dialog: ConnectycubeChatDialog) = dialog
    .also {
        if (!photoUrl.isNullOrBlank()) it.photo = photoUrl
        if (!name.isNullOrBlank()) it.name = name
        if (!description.isNullOrBlank()) it.description = description
    }

//add/remove users from dialog (pu) by admins or chat owner only
fun addDialogUsers(
    dialog: ConnectycubeChatDialog,
    callBacks: EntityCallback<ConnectycubeChatDialog>,
    vararg users: Int
) = updateDialog(dialog, callBacks) { addUsers(*users) }

fun removeDialogUsers(
    dialog: ConnectycubeChatDialog,
    callBacks: EntityCallback<ConnectycubeChatDialog>,
    vararg users: Int
) = updateDialog(dialog, callBacks) { removeUsers(*users) }

//add/remove admins
fun addDialogAdmins(
    dialog: ConnectycubeChatDialog,
    callBacks: EntityCallback<ConnectycubeChatDialog>,
    vararg admins: Int
) = updateDialog(dialog, callBacks) { addAdminsIds(*admins) }

fun removeDialogAdmins(
    dialog: ConnectycubeChatDialog,
    callBacks: EntityCallback<ConnectycubeChatDialog>,
    vararg admins: Int
) = updateDialog(dialog, callBacks) { removeAdminsIds(*admins) }

//deleteDialog from user dialogs
fun String.removeDialogUser(callBacks: EntityCallback<Void> = createEntityCallbacks { Timber.e(it) }) =
    removeDialog(callBacks, false)

//permanently delete dialog
fun String.deleteDialog(callBacks: EntityCallback<Void>) = removeDialog(callBacks, true)

private fun String.removeDialog(
    callBacks: EntityCallback<Void>,
    forceDelete: Boolean
) = takeUnless { it.isBlank() }
    ?.let { ConnectycubeRestChatService.deleteDialog(it, forceDelete) }
    ?.performAsync(callBacks)

//send isTyping/nonTyping
//have a listener to get typing state
fun ConnectycubeChatDialog.setIsTyping() = doInDialog { sendIsTypingNotification() }

fun ConnectycubeChatDialog.setNotTyping() = doInDialog { sendStopTypingNotification() }

//mute/un mute notifications from chats
fun muteDialog(
    dialogId: String,
    callBacks: EntityCallback<Boolean>
) = updateNotificationSettings(dialogId, callBacks, false)

fun unMuteDialog(
    dialogId: String,
    callBacks: EntityCallback<Boolean>
) = updateNotificationSettings(dialogId, callBacks, true)

private fun updateNotificationSettings(
    dialogId: String,
    callBacks: EntityCallback<Boolean>,
    enabled: Boolean
) = ConnectycubeRestChatService
    .updateDialogNotificationSending(dialogId, enabled)
    .performAsync(callBacks)


fun getDialogNotificationSettings(
    string: String,
    callBacks: EntityCallback<Boolean>
) = ConnectycubeRestChatService
    .checkIsDialogNotificationEnabled(string)
    .performAsync(callBacks)

//TODO check with push notifications
fun markAsActive(chatService: ConnectycubeChatService = chatInstance)= chatService.enterActiveState()
fun markAsInActive(chatService: ConnectycubeChatService = chatInstance) = chatService.enterInactiveState()

//getLastActive
fun getLastActivity(
    uId:Int,
    chatService: ConnectycubeChatService = chatInstance
) =chatService.getLastUserActivity(uId)