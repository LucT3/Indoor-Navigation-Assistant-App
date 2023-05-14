package it.unipi.dii.indoornavigatorassistant

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kontakt.sdk.android.common.KontaktSDK
import com.kontakt.sdk.android.common.log.LogLevel
import com.kontakt.sdk.android.common.log.Logger
import it.unipi.dii.indoornavigatorassistant.util.Constants


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(Constants.LOG_TAG,"Activity created")

        // Initialize dependency
        initDependencies()
        Log.i(Constants.LOG_TAG,"Dependencies initialized")

        // Set graphical user interface
        setContentView(R.layout.activity_main)
        Log.i(Constants.LOG_TAG,"UI initialized")

        // Check required "dangerous" permissions
        checkPermissions()

        enableBluetooth()

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

    private fun enableBluetooth() {
        // Get Bluetooth adapter
        val bluetoothAdapter = getSystemService(BluetoothManager::class.java).adapter
            ?: throw RuntimeException()

        // Check if bluetooth is not enabled
        if (!bluetoothAdapter.isEnabled) {
            // Enable Bluetooth
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
                    // do something
                    Log.d(Constants.LOG_TAG, "Bluetooth funziona! $data")
                    throw RuntimeException("Ciao!")
                }
                else {
                    // TODO handle refusal
                    throw RuntimeException()
                }
            }.launch(enableBtIntent)
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
