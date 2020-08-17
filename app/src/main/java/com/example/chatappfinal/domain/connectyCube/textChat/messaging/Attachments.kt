package com.example.chatappfinal.domain.connectyCube.textChat.messaging

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.connectycube.chat.model.ConnectycubeAttachment
import com.connectycube.core.ConnectycubeProgressCallback
import com.connectycube.core.EntityCallback
import com.connectycube.storage.ConnectycubeStorage
import com.connectycube.storage.model.ConnectycubeFile
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import timber.log.Timber
import java.io.File
import java.lang.Exception

fun attachPhoto(
    pathName: String,
    progressCallback: ConnectycubeProgressCallback,
    attachedFile: File = File(pathName),
    onAttachmentReady: (ConnectycubeAttachment) -> Unit
) = uploadFile(
    attachedFile,
    progressCallback,
    createEntityCallbacks({ it?.let { onAttachmentReady(createAttachment(it)) } }, {Timber.e("failed to attach due to ${it?.message}")})
)

//for uploading file
fun uploadFile(
    file: File,
    progressCallbacks: ConnectycubeProgressCallback,
    callBacks: EntityCallback<ConnectycubeFile> //= attachmentCallBacks
) = ConnectycubeStorage
    .uploadFileTask(file, false, progressCallbacks)
    .performAsync(callBacks)

//for getting file path from the intent data (content type URI)
fun Context.getRealPathFromURI(contentUri: Uri): String? {
    var cursor = contentResolver.query(contentUri, null, null, null, null)
    return try {
        cursor?.moveToFirst()
        var documentId = cursor?.getString(0)
        documentId = documentId?.substring(documentId.lastIndexOf(":") + 1)
        cursor?.close()
        cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            MediaStore.Images.Media._ID + " = ? ",
            arrayOf(documentId),
            null
        )
        cursor?.moveToFirst()
        cursor?.getString(cursor.getColumnIndex("_data")) ?: ""
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    } finally {
        cursor?.close()

    }
}

fun createAttachment(file: ConnectycubeFile) = ConnectycubeAttachment("image").apply {
    id = file.id.toString()
    url = file.privateUrl
}