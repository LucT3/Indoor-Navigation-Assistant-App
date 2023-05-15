package it.unipi.dii.indoornavigatorassistant

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.kontakt.sdk.android.common.KontaktSDK
import com.kontakt.sdk.android.common.log.LogLevel
import com.kontakt.sdk.android.common.log.Logger
import it.unipi.dii.indoornavigatorassistant.permissions.BluetoothPermissions
import it.unipi.dii.indoornavigatorassistant.permissions.PermissionManager
import it.unipi.dii.indoornavigatorassistant.util.Constants


class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(Constants.LOG_TAG, "MainActivity::onCreate - Activity created")
        
        // Initialize dependency
        initDependencies()
        Log.d(Constants.LOG_TAG, "MainActivity::onCreate - Dependencies initialized")
        
        // Set graphical user interface
        setContentView(R.layout.activity_main)
        Log.d(Constants.LOG_TAG, "MainActivity::onCreate - UI initialized")
        
        // Navigation button click listener
        // get reference to button
        val navigationButton: Button = findViewById(R.id.buttonNavigation)
        // set on-click listener
        navigationButton.setOnClickListener {
            // Check required "dangerous" permissions
            checkPermissions()
        }
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
        // Check which permissions are not granted and request them
        PermissionManager.from(this)
            .request(BluetoothPermissions)
            .rationale("We need location permissions to use BLE features")
            .checkPermission { granted: Boolean ->
                if (granted) {
                    Log.d(
                        Constants.LOG_TAG,
                        "MainActivity::checkPermissions - Permissions granted!"
                    )
                    startNavigationActivity()
                } else {
                    Log.d(
                        Constants.LOG_TAG,
                        "MainActivity::checkPermissions - Permissions refused!"
                    )
                    // Show pop-up to user and close the application
                    AlertDialog.Builder(this)
                        .setTitle(this.getString(R.string.dialog_permission_not_granted_title))
                        .setMessage(this.getString(R.string.dialog_permission_not_granted_message))
                        .setCancelable(false)
                        .setPositiveButton(this.getString(R.string.dialog_permissions_not_granted_button)) { _, _ ->
                            finishAndRemoveTask()
                        }
                        .show()
                }
            }
    }
    
    
    /**
     * Start activity NavigationActivity
     */
    private fun startNavigationActivity() {
        Log.d(
            Constants.LOG_TAG, "MainActivity::startNavigationActivity - " +
                    "All permissions are granted => start NavigationActivity"
        )
        val intent = Intent(this, NavigationActivity::class.java)
        startActivity(intent)
    }
    
}
