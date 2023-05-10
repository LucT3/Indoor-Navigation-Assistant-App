package it.unipi.dii.indoornavigatorassistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kontakt.sdk.android.common.KontaktSDK
import com.kontakt.sdk.android.common.log.LogLevel
import com.kontakt.sdk.android.common.log.Logger
import it.unipi.dii.indoornavigatorassistant.util.Constants


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Check the default mode and set theme accordingly
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState)
        Log.i(Constants.LOG_TAG,"Activity created")
        
        this.initializeDependencies()
        Log.i(Constants.LOG_TAG,"Dependencies initialized")

        // Set interface
        setContentView(R.layout.activity_main)

        // Check permissions
        checkPermissions()
    }

    private fun initializeDependencies() {
        // Initialize Bluetooth beacons API
        KontaktSDK.initialize(this)
        // Initialize logger
        Logger.setDebugLoggingEnabled(true)
        Logger.setLogLevelEnabled(LogLevel.DEBUG, true)
    }
    
    
    private fun checkPermissions() {
        // Select required permissions
        val requiredPermissions =
           if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
               arrayOf(
                   Manifest.permission.ACCESS_FINE_LOCATION
               )
           } else {
               arrayOf(
                   Manifest.permission.BLUETOOTH_SCAN,
                   Manifest.permission.BLUETOOTH_CONNECT,
                   Manifest.permission.ACCESS_FINE_LOCATION
               )
           }
        // Check which permissions are not granted and request them
        if (isAnyOfPermissionsNotGranted(requiredPermissions)) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 100)
        } else {
            Log.i(Constants.LOG_TAG, "All permissions are granted => start scan")
            // TODO start scan
        }
    }
    
    
    private fun isAnyOfPermissionsNotGranted(requiredPermissions: Array<String>): Boolean {
        Log.d(Constants.LOG_TAG, "Check permissions ${requiredPermissions.contentToString()}")
        for (permission in requiredPermissions) {
            val checkSelfPermissionResult = ContextCompat.checkSelfPermission(this, permission)
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermissionResult) {
                Log.d(Constants.LOG_TAG, "Permission $permission not granted!")
                return true
            }
        }

        return false
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (100 == requestCode) {
            Log.d(Constants.LOG_TAG, "onRequestPermissionsResult ${permissions.contentToString()} => ${grantResults.contentToString()}")

            // Check if any permission have not been granted
            if (grantResults.contentEquals(
                    IntArray(grantResults.size){ PackageManager.PERMISSION_GRANTED })
            ) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(
                    this,
                    "Location permissions are mandatory to use BLE features",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}
