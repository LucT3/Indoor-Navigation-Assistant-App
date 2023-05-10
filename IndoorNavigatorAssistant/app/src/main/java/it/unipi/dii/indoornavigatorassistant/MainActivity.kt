package it.unipi.dii.indoornavigatorassistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kontakt.sdk.android.common.KontaktSDK
import com.kontakt.sdk.android.common.log.LogLevel
import com.kontakt.sdk.android.common.log.Logger
import java.util.Arrays


class MainActivity : AppCompatActivity() {

    val LOG_TAG = "MSSS"


    override fun onCreate(savedInstanceState: Bundle?) {
        // Check the default mode and set theme accordingly
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState)
        Log.i(LOG_TAG,"Activity created")
        
        this.initializeDependencies()
        Log.i(LOG_TAG,"Dependencies initialized")
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

    private fun getRequestedPermissions(): Array<String> {
        val packageInfo  =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(
                        PackageManager.GET_PERMISSIONS.toLong()
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            }

        val requestedPermissions = packageInfo.requestedPermissions
        Log.i("Sample", "Requested permissions list " + Arrays.toString(requestedPermissions))
        return requestedPermissions
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
            Log.i(LOG_TAG, "All permissions are granted => start scan")
            // TODO start scan
        }
    }
    
    
    private fun isAnyOfPermissionsNotGranted(requiredPermissions: Array<String>): Boolean {
        Log.d(LOG_TAG, "Check permissions ${requiredPermissions.contentToString()}")
        for (permission in requiredPermissions) {
            val checkSelfPermissionResult = ContextCompat.checkSelfPermission(this, permission)
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermissionResult) {
                Log.d(LOG_TAG, "Permission $permission not granted!")
                return true
            }
        }
        return false
    }

//    override fun onRequestPermissionsResult(requestCode: Int,
//                                   permissions: Array<String>,
//                                   grantResults: IntArray)
//    {
//        if (100 == requestCode) { // same request code as was in request permission
//            if (allPermissionsGranted(grantResults)) {
//                Log.i("Sample", "onRequestPermissionsResult: ok")
//            } else {
//                //not granted permission
//                //show some explanation dialog that some features will not work
//                Log.e("Sample", "onRequestPermissionsResult: not granted permission")
//            }
//        }
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (100 == requestCode) {
            Log.d(LOG_TAG, "onRequestPermissionsResult ${permissions.contentToString()} => ${grantResults.contentToString()}")
            
            if (allPermissionsGranted(grantResults)) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(
                    this,
                    "Location permissions are mandatory to use BLE features on Android 6.0 or higher",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun allPermissionsGranted(grantResults: Array<String>) {
        
    }

}
