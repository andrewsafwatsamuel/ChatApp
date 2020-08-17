package com.example.chatappfinal.domain.connectyCube.textChat

import com.connectycube.chat.ConnectycubeRoster
import com.connectycube.chat.listeners.SubscriptionListener
import com.connectycube.chat.model.ConnectycubePresence
import com.connectycube.core.EntityCallback
import com.example.chatappfinal.domain.connectyCube.chatInstance
import timber.log.Timber

val chatRoster: ConnectycubeRoster by lazy {
    chatInstance.getRoster(ConnectycubeRoster.SubscriptionMode.mutual, subscriptionListener)
}

val rosterListener by lazy { RosterListener() }

class RosterListener : com.connectycube.chat.listeners.RosterListener {

    override fun entriesDeleted(entries: MutableCollection<Int>?) {
        Timber.i("contactsDeleted")
        printEntry(entries)
        chatRoster.reload()
    }

    override fun presenceChanged(p0: ConnectycubePresence?) {
        Timber.i("contactsPresenceChanged")

    }

    override fun entriesUpdated(entries: MutableCollection<Int>?) {
        Timber.i("contactsEntriesUpdated")
        printEntry(entries)
        chatRoster.reload()
    }

    override fun entriesAdded(entries: MutableCollection<Int>?) {
        Timber.i("contactsEntriesAdded")
        printEntry(entries)
        chatRoster.reload()
    }

    private fun printEntry(ids: MutableCollection<Int>?) = ids
        .also { Timber.i("entries: ${chatRoster.entries}") }
        .takeUnless { it.isNullOrEmpty() }
        ?.run { chatRoster.getEntry(toList()[0]) }
        ?.run {
            Timber.i("entry_name: $name")
            Timber.i("is_subscription_bending: $isSubscriptionPending")
            Timber.i("is subscription bending roster: ${rosterEntry.isSubscriptionPending}")
            Timber.i("can see his presence: ${rosterEntry.canSeeHisPresence()}")
            Timber.i("can see my presence: ${rosterEntry.canSeeMyPresence()}")
            Timber.i("is approved: ${rosterEntry.isApproved}")
        }

}

val subscriptionListener: SubscriptionListener = SubscriptionListener {
//    Timber.i("subscription $it")
//    acceptRequest(it, createEntityCallbacks {r-> Timber.e(r) })
}

//add User To contact list
fun addToContactList(id: Int, roster: ConnectycubeRoster = chatRoster) =
    if (roster.contains(id)) roster.subscribe(id)
    else roster.createEntry(id, null)

fun getPresence(id: Int) = chatRoster.doOnRoster {
    getPresence(id)?.let {
        if (it.type == ConnectycubePresence.Type.online) Timber.i("User online")
        else Timber.i("User offline")
    }
}

fun acceptRequest(id: Int, callback: EntityCallback<Void>) =
    chatRoster.doOnRoster { confirmSubscription(id, callback) }

fun rejectRequest(id: Int, callback: EntityCallback<Void>) =
    chatRoster.doOnRoster { reject(id, callback) }

fun removeContact(entry: Int, callback: EntityCallback<Void>) =
    chatRoster.doOnRoster {
        unsubscribe(entry, createEntityCallbacks({ reload() }, Timber::e))
    }

fun ConnectycubeRoster.doOnRoster(method: ConnectycubeRoster.() -> Unit) =
    try {
        method()
    } catch (exception: Exception) {
        Timber.e(exception)
    }

fun addRosterListener(
    roster: ConnectycubeRoster = chatRoster,
    listener: RosterListener = rosterListener
) = roster.addRosterListener(listener)

fun removeRosterListener(
    roster: ConnectycubeRoster = chatRoster,
    listener: RosterListener = rosterListener
) = roster.removeRosterListener(listener)