package it.unipi.dii.indoornavigatorassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kontakt.sdk.android.common.KontaktSDK
import com.kontakt.sdk.android.common.log.LogLevel
import com.kontakt.sdk.android.common.log.Logger
import it.unipi.dii.indoornavigatorassistant.permissions.BluetoothPermissions
import it.unipi.dii.indoornavigatorassistant.permissions.PermissionManager
import it.unipi.dii.indoornavigatorassistant.util.Constants


class MainActivity : AppCompatActivity() {
    
    private lateinit var permissionManager: PermissionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(Constants.LOG_TAG, "Activity created")
        
        // Initialize dependency
        initDependencies()
        Log.i(Constants.LOG_TAG, "Dependencies initialized")
        
        // Set graphical user interface
        setContentView(R.layout.activity_main)
        Log.i(Constants.LOG_TAG, "UI initialized")
        
        // Check required "dangerous" permissions
        checkPermissions()
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
        permissionManager = PermissionManager.from(this)
        // Check which permissions are not granted and request them
        permissionManager
            .request(BluetoothPermissions)
            .rationale("We need location permissions to use BLE features")
            .checkPermission { granted: Boolean ->
                if (granted) {
                    println("Yes!")
                    startScanningActivity()
                } else {
                    println("No!")
                    checkPermissions()
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
