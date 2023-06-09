package it.unipi.dii.indoornavigationassistant.permissions

import android.Manifest.permission.*
import android.os.Build

data class Permission(val permissions: Array<String>)

val BluetoothPermissions: Permission =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        Permission(arrayOf(ACCESS_FINE_LOCATION))
    } else {
        Permission(
            arrayOf(
                BLUETOOTH_SCAN,
                BLUETOOTH_CONNECT,
                ACCESS_FINE_LOCATION
            )
        )
    }

val CameraPermissions: Permission =
        Permission(
            arrayOf(
                CAMERA
            )
        )
