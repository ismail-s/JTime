package com.ismail_s.jtime.android

import android.support.v4.app.FragmentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.*
import java.io.FileDescriptor
import java.io.PrintWriter
import java.util.concurrent.TimeUnit

class MockStatusPendingResult : PendingResult<Status>() {
    override fun await() = Status(0)
    override fun await(p0: Long, p1: TimeUnit) = await()
    override fun setResultCallback(p: ResultCallback<in Status>) {}
    override fun setResultCallback(p0: ResultCallback<in Status>, p1: Long, p2: TimeUnit) {}
    override fun isCanceled() = false
    override fun cancel() {}
}

class MockGoogleApiClient: GoogleApiClient() {
    override fun isConnecting() = false
    override fun isConnected() = false
    override fun isConnectionCallbacksRegistered(p: ConnectionCallbacks) = false
    override fun hasConnectedApi(p: Api<*>) = false
    override fun getConnectionResult(p: Api<*>): ConnectionResult = ConnectionResult(0)
    override fun registerConnectionFailedListener(p: OnConnectionFailedListener) {}
    override fun isConnectionFailedListenerRegistered(p: OnConnectionFailedListener) = false
    override fun unregisterConnectionCallbacks(p: ConnectionCallbacks) {}
    override fun clearDefaultAccountAndReconnect() = MockStatusPendingResult()
    override fun dump(p0: String, p1: FileDescriptor, p2: PrintWriter, p3: Array<String>) {}
    override fun reconnect() {}
    override fun registerConnectionCallbacks(c: ConnectionCallbacks) {}
    override fun unregisterConnectionFailedListener(p: OnConnectionFailedListener) {}
    override fun stopAutoManage(a: FragmentActivity) {}
    override fun blockingConnect() = ConnectionResult(0)
    override fun blockingConnect(p0: Long, p1: TimeUnit) = ConnectionResult(0)
    override fun disconnect() {}
    override fun connect() {}
}