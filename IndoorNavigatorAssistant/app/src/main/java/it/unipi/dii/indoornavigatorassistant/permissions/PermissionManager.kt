package it.unipi.dii.indoornavigatorassistant.permissions

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.lang.ref.WeakReference

class PermissionManager private constructor(private val activity: WeakReference<AppCompatActivity>) {
    
    private val requiredPermissions = mutableListOf<Permission>()
    private var rationale: String? = null
    private var callback: (Boolean) -> Unit = {}
    
    // Launcher for permission request
    private val permissionCheck =
        activity.get()?.registerForActivityResult(RequestMultiplePermissions()) { grantResults ->
            sendResultAndCleanUp(grantResults)
        }
    
    
    
    companion object {
        /**
         * Generate a PermissionManager from an AppCompatActivity object.
         *
         * @param activity current activity
         */
        fun from(activity: AppCompatActivity) = PermissionManager(WeakReference(activity))
    }
    
    
    
    
    /**
     * Set rationale for permission request.
     *
     * @param description description of the rationale pop-up
     * @return this PermissionManager
     */
    fun rationale(description: String): PermissionManager {
        rationale = description
        return this
    }
    
    /**
     * Add new permissions to the list of permissions to be requested by the user.
     *
     * @param permission permissions to add
     * @return this PermissionManager
     */
    fun request(vararg permission: Permission): PermissionManager {
        requiredPermissions.addAll(permission)
        return this
    }
    
    /**
     * Set the callback and request permissions to the user.
     *
     * @param callback callback function
     */
    fun checkPermission(callback: (Boolean) -> Unit) {
        this.callback = callback
        handlePermissionRequest()
    }
    
    
    
    
    /**
     * Handle a permission request:
     * 1. If all permission are granted, send a positive result and cleanup manager
     * 2. Otherwise, show a rationale to user if required and request permissions to user
     * 3. Otherwise, just request permissions to user
     */
    private fun handlePermissionRequest() {
        activity.get()?.let { a ->
            when {
                areAllPermissionsGranted(a) -> sendPositiveResult()
                shouldShowPermissionRationale(a) -> displayRationale(a)
                else -> requestPermissions()
            }
        }
    }
    
    /**
     * Launch permission request.
     */
    private fun requestPermissions() {
        permissionCheck?.launch(getPermissionList())
    }
    
    
    /**
     * Display a pop-up to user which explains why we need the requested permission.
     *
     * @param activity current activity
     */
    private fun displayRationale(activity: AppCompatActivity) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.dialog_request_permission_title))
            .setMessage(rationale ?: activity.getString(R.string.dialog_request_permission_default_message))
            .setCancelable(false)
            .setPositiveButton(activity.getString(R.string.dialog_request_permission_button_positive)) { _, _ ->
                requestPermissions()
            }
            .show()
    }
    
    /**
     * Send a positive result (in case all permissions are already granted.
     */
    private fun sendPositiveResult() {
        sendResultAndCleanUp(getPermissionList().associateWith { true })
    }
    
    /**
     * Send result by calling the callback function and cleanup the PermissionManager.
     *
     * @param grantResults map which shows for each permission if it has been granted
     */
    private fun sendResultAndCleanUp(grantResults: Map<String, Boolean>) {
        Log.d(Constants.LOG_TAG, "PermissionManager::sendResultAndCleanUp - grantResults=$grantResults")
        callback(grantResults.all { it.value })
        cleanUp()
    }
    
    /**
     * Restore the state of the PermissionManager object.
     */
    private fun cleanUp() {
        requiredPermissions.clear()
        rationale = null
        callback = {}
    }
    
    
    
    
    private fun areAllPermissionsGranted(activity: AppCompatActivity) =
        requiredPermissions.all { it.isGranted(activity) }
    
    private fun shouldShowPermissionRationale(activity: AppCompatActivity) =
        requiredPermissions.any { it.requiresRationale(activity) }
    
    private fun getPermissionList() =
        requiredPermissions.flatMap { it.permissions.toList() }.toTypedArray()
    
    private fun Permission.isGranted(activity: AppCompatActivity) =
        permissions.all { hasPermission(activity, it) }
    
    private fun Permission.requiresRationale(activity: AppCompatActivity) =
        permissions.any { activity.shouldShowRequestPermissionRationale(it) }
    
    private fun hasPermission(activity: AppCompatActivity, permission: String) =
        ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
}
