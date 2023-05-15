package it.unipi.dii.indoornavigatorassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kontakt.sdk.android.common.KontaktSDK
import com.kontakt.sdk.android.common.log.LogLevel
import com.kontakt.sdk.android.common.log.Logger
import it.unipi.dii.indoornavigatorassistant.util.Constants


class MainActivity : AppCompatActivity() {
    
//    private val semaphore = Semaphore(0, 1)
    
    private lateinit var permissionManager: PermissionManager
    private var num = 1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(Constants.LOG_TAG,"Activity created")

        // Initialize dependency
        initDependencies()
        Log.i(Constants.LOG_TAG,"Dependencies initialized")

        // Set graphical user interface
        setContentView(R.layout.activity_main)
        Log.i(Constants.LOG_TAG,"UI initialized")

        permissionManager = PermissionManager.from(this)
        // Check required "dangerous" permissions
        checkPermissions()
        
//        semaphore.acquire()
//        startScanningActivity()
    }

    /**
     * Initialize KontaktSDK dependency
     */
    private fun initDependencies() {
        // Initialize Bluetooth beacons API
        KontaktSDK.initialize(this)
        // Initialize logger
        Logger.setDebugLoggingEnabled(true)
        Logger.setLogLevelEnabled(LogLevel.DEBUG, true)
    }


    /**
     * Check if application owns necessary permissions for its features
     */
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
            permissionManager
                .request(Permission.Location)
                .rationale("We need permission to use the camera")
                .checkPermission { granted: Boolean ->
                    if (granted) {
                        // Do something with the camera
                        println("Yes!")
                    } else {
                        // You can't access the camera
                        println("No!")
                        num = 2
                    }
                }
        }
        else {
//            semaphore.release()
        }
        else {
            startScanningActivity()
        }
    }


    /**
     * Check if any of the input permission is NOT granted
     */
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


    /**
     * Handle the result of a permission request
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (100 == requestCode) {
            Log.d(Constants.LOG_TAG, "onRequestPermissionsResult " +
                    "${permissions.contentToString()} => ${grantResults.contentToString()}")

            // Check if any permission have not been granted
            if (grantResults.contentEquals(
                    IntArray(grantResults.size){ PackageManager.PERMISSION_GRANTED })
            ) {
                Toast.makeText(
                    this,
                    "Permissions granted",
                    Toast.LENGTH_SHORT
                ).show()

                startScanningActivity()
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


    /**
     * Start activity MonitorTest TODO
     */
    private fun startScanningActivity() {
        Log.i(Constants.LOG_TAG, "All permissions are granted => start scan")
        val intent = Intent(this, NavigationActivity::class.java)
        startActivity(intent)
    }
    
}
