package org.llschall.dmixtrip.model

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID

class ConnectionHandler {
    companion object {
        val handler: ConnectionHandler = ConnectionHandler()
    }

    var context: Context? = null

    private var wrapper: Adapter? = null

    fun status(): String {
        if (wrapper == null) {
            return "no started"
        }
        return wrapper!!.status()
    }

    fun setup(): String {
        if (context == null) return "context not found"
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return "permission not granted"
        }

        try {
            val manager = context!!.getSystemService(BluetoothManager::class.java)
                ?: return "manager not found"
            val adapter = manager.adapter ?: return "adapter not found"
            wrapper = Adapter(context!!, adapter)
            return "found adapter ${adapter.name} ${adapter.isEnabled}"
        } catch (e: Exception) {
            if (e.message == null) return "unknown error"
            return e.message!!
        }
    }

    fun connect(): String {
        return wrapper!!.connect()
    }

    fun listen() {
        return wrapper!!.listen()
    }

}

private class Adapter(
    val context: Context,
    val adapter: BluetoothAdapter
) {

    val uuid = UUID.randomUUID()

    var socket: BluetoothSocket? = null

    fun status(): String {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "permission not granted"
        }

        val devices = adapter.bondedDevices

        val writer = StringBuilder()

        for (device in devices) {
            writer.append("# ")
            writer.append(device.name + " ")
            writer.append(device.address + "\n")
        }
        writer.append("---\n")

        socket =
            devices.first().createRfcommSocketToServiceRecord(uuid)

        adapter.cancelDiscovery()

        writer.append("Remote ${socket!!.remoteDevice.name}\n")
        writer.append("Connected ${socket!!.isConnected}\n")

        writer.append(
            """
            adapter.name ${adapter.name}
            adapter.state ${adapter.state}
            adapter.isEnabled ${adapter.isEnabled}
            devices: ${devices.size}
            """
        )

        return writer.toString()
    }

    fun connect(): String {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "permission not granted"
        }

        val name = socket!!.remoteDevice.name

        try {
            socket!!.connect()
        } catch (e: IOException) {
            return "### Connection failed on $name: " + e.message
        }
        return "Connected to $name"

    }

    fun listen() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            println("permission not granted")
            return
        }
        println("listening...")
        adapter.listenUsingRfcommWithServiceRecord("Galaxy", uuid)
        println("listening is over")
    }

}