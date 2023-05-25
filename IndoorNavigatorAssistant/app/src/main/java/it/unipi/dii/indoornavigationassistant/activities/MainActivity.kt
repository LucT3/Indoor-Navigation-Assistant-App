package it.unipi.dii.indoornavigationassistant.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kontakt.sdk.android.common.KontaktSDK
import it.unipi.dii.indoornavigationassistant.R
import it.unipi.dii.indoornavigationassistant.databinding.ActivityMainBinding
import it.unipi.dii.indoornavigationassistant.permissions.BluetoothPermissions
import it.unipi.dii.indoornavigationassistant.permissions.CameraPermissions
import it.unipi.dii.indoornavigationassistant.permissions.PermissionManager
import it.unipi.dii.indoornavigationassistant.util.Constants


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        Log.d(Constants.LOG_TAG, "MainActivity::onCreate - Activity created")
        
        // Initialize dependency
        initDependencies()
        Log.d(Constants.LOG_TAG, "MainActivity::onCreate - Dependencies initialized")
        
        // Set graphical user interface
        setContentView(binding.root)
        Log.d(Constants.LOG_TAG, "MainActivity::onCreate - UI initialized")
        
        // Check required "dangerous" permissions
        checkPermissions()
        
        // Navigation button on-click listener
        binding.buttonNavigation.setOnClickListener {
            startNavigationActivity()
        }
    }
    
    /**
     * Initialize KontaktSDK dependency
     */
    private fun initDependencies() {
        // Initialize Bluetooth beacons API
        KontaktSDK.initialize(this)
    }
    
    
    /**
     * Check if application owns necessary permissions for its features
     */
    private fun checkPermissions() {
        // Check which permissions are not granted and request them
        PermissionManager.from(this)
            .request(BluetoothPermissions, CameraPermissions)
            .rationale(this.getString(R.string.dialog_permission_request))
            .checkPermission { granted: Boolean ->
                if (granted) {
                    Log.d(
                        Constants.LOG_TAG,
                        "MainActivity::checkPermissions - Permissions granted!"
                    )
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
