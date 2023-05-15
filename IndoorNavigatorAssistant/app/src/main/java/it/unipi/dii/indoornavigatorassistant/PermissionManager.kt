package it.unipi.dii.indoornavigatorassistant

import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference

class PermissionManager private constructor(private val fragment: WeakReference<AppCompatActivity>) {
    
    private val requiredPermissions = mutableListOf<Permission>()
    private var rationale: String? = null
    private var callback: (Boolean) -> Unit = {}
    private var detailedCallback: (Map<Permission,Boolean>) -> Unit = {}
    
    private val permissionCheck =
        fragment.get()?.registerForActivityResult(RequestMultiplePermissions()) { grantResults ->
            sendResultAndCleanUp(grantResults)
        }
    
    companion object {
        fun from(fragment: AppCompatActivity) = PermissionManager(WeakReference(fragment))
    }
    
    fun rationale(description: String): PermissionManager {
        rationale = description
        return this
    }
    
    fun request(vararg permission: Permission): PermissionManager {
        requiredPermissions.addAll(permission)
        return this
    }
    
    fun checkPermission(callback: (Boolean) -> Unit) {
        this.callback = callback
        handlePermissionRequest()
    }
    
    fun checkDetailedPermission(callback: (Map<Permission,Boolean>) -> Unit) {
        this.detailedCallback = callback
        handlePermissionRequest()
    }
    
    private fun handlePermissionRequest() {
        fragment.get()?.let { fragment ->
            when {
                areAllPermissionsGranted(fragment) -> sendPositiveResult()
                shouldShowPermissionRationale(fragment) -> displayRationale(fragment)
                else -> requestPermissions()
            }
        }
    }
    
    private fun displayRationale(fragment: AppCompatActivity) {
        AlertDialog.Builder(fragment)
            .setTitle("Titolo di prova")
            .setMessage(rationale ?: "Why not")
            .setCancelable(false)
            .setPositiveButton("Positive!") { _, _ ->
                requestPermissions()
            }
            .show()
    }
    
    private fun sendPositiveResult() {
        sendResultAndCleanUp(getPermissionList().associateWith { true })
    }
    
    private fun sendResultAndCleanUp(grantResults: Map<String, Boolean>) {
        callback(grantResults.all { it.value })
        detailedCallback(grantResults.mapKeys { Permission.from(it.key) })
        cleanUp()
    }
    
    private fun cleanUp() {
        requiredPermissions.clear()
        rationale = null
        callback = {}
        detailedCallback = {}
    }
    
    private fun requestPermissions() {
        permissionCheck?.launch(getPermissionList())
    }
    
    private fun areAllPermissionsGranted(fragment: AppCompatActivity) =
        requiredPermissions.all { it.isGranted(fragment) }
    
    private fun shouldShowPermissionRationale(fragment: AppCompatActivity) =
        requiredPermissions.any { it.requiresRationale(fragment) }
    
    private fun getPermissionList() =
        requiredPermissions.flatMap { it.permissions.toList() }.toTypedArray()
    
    private fun Permission.isGranted(fragment: AppCompatActivity) =
        permissions.all { hasPermission(fragment, it) }
    
    private fun Permission.requiresRationale(fragment: AppCompatActivity) =
        permissions.any { fragment.shouldShowRequestPermissionRationale(it) }
    
    private fun hasPermission(fragment: AppCompatActivity, permission: String) =
        ContextCompat.checkSelfPermission(
            fragment,
            permission
        ) == PackageManager.PERMISSION_GRANTED
}
