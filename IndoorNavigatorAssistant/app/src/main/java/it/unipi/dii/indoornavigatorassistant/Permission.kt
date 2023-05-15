package it.unipi.dii.indoornavigatorassistant

import android.Manifest
import android.Manifest.permission.*
import android.os.Build

sealed class Permission(vararg val permissions: String) {
    // Individual permissions
    object Camera : Permission(CAMERA)
    
    // Bundled permissions
    object MandatoryForFeatureOne : Permission(WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION)
    
    // Grouped permissions
    object Location : Permission(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    object Storage : Permission(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
    
//    object Ciao {
//        init {
//            val requiredPermissions =
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
//                    arrayOf(
//                        ACCESS_FINE_LOCATION
//                    )
//                } else {
//                    arrayOf(
//                        BLUETOOTH_SCAN,
//                        BLUETOOTH_CONNECT,
//                        ACCESS_FINE_LOCATION
//                    )
//                }
//            Permission(*requiredPermissions)
//        }
//    }
    
    companion object {
        fun from(permission: String) = when (permission) {
            ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION -> Location
            WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE -> Storage
            CAMERA -> Camera
            else -> throw IllegalArgumentException("Unknown permission: $permission")
        }
    }
}