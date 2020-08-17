package com.example.chatappfinal.domain.connectyCube

import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.XMPPConnection
import java.lang.Exception

sealed class ConnectyCubeConnectionState
class Connected(val connection: XMPPConnection?) : ConnectyCubeConnectionState()
object ConnectionClosed : ConnectyCubeConnectionState()
class ClosedWithError(val exception: Exception?) : ConnectyCubeConnectionState()
object SuccessfullyReconnected : ConnectyCubeConnectionState()
class Authenticated(val connection: XMPPConnection?,val p1: Boolean): ConnectyCubeConnectionState()
class ReconnectionFailed(val exception:Exception?): ConnectyCubeConnectionState()
class ReconnectingIn(val p0:Int): ConnectyCubeConnectionState()

class ConnectivityListener(
    private inline val state: (ConnectyCubeConnectionState) -> Unit
) : ConnectionListener {

    override fun connected(p0: XMPPConnection?) = state(Connected(p0))

    override fun connectionClosed() = state(ConnectionClosed)

    override fun connectionClosedOnError(p0: Exception?) = state(ClosedWithError(p0))

    override fun reconnectionSuccessful() = state(SuccessfullyReconnected)

    override fun authenticated(p0: XMPPConnection?, p1: Boolean) = state(Authenticated(p0,p1))

    override fun reconnectionFailed(p0: Exception?) = state(ReconnectionFailed(p0))

    override fun reconnectingIn(p0: Int) = state(ReconnectingIn(p0))
}