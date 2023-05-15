package it.unipi.dii.indoornavigatorassistant.permissions

import android.Manifest
import android.os.Build

data class Permission(val permissions: Array<String>)

val BluetoothPermission: Permission =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        Permission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    } else {
        Permission(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
