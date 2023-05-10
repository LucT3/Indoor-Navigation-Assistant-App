package it.unipi.dii.indoornavigatorassistant

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kontakt.sdk.android.common.KontaktSDK
import com.kontakt.sdk.android.common.log.LogLevel
import com.kontakt.sdk.android.common.log.Logger
import java.util.Arrays


class MainActivity : AppCompatActivity() {

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                isGranted ->
            // Do something if permission granted
            for (x in isGranted) {
                if (x.value) {
                    Log.i("Sample", "permission ${x.key} granted")
                } else {
                    Log.i("Sample", "permission ${x.key} denied")
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("Sample","Buongiorno")
        this.initializeDependencies()
        setContentView(R.layout.activity_main)
        Log.i("Sample", "ciao")
    }

    private fun initializeDependencies() {
        // Initialize Bluetooth API
        KontaktSDK.initialize(this)
        Logger.setDebugLoggingEnabled(true)
        Logger.setLogLevelEnabled(LogLevel.DEBUG, true)
        // Check permissions
        checkPermissions()
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
        val requiredPermissions = this.getRequestedPermissions()
        if (isAnyOfPermissionsNotGranted(requiredPermissions)) {
            Log.i("Sample", "OH NOOOOO 1")
            this.requestPermission.launch(requiredPermissions)
//            ComponentActivity.requestPermissions(this, requiredPermissions, 100)
            Log.i("Sample", "OH NOOOOO 2")
        } else {
            Log.i("Sample", "AAAAA")
        }

    }

    private fun isAnyOfPermissionsNotGranted(requiredPermissions: Array<String>): Boolean {
        for (permission in requiredPermissions) {
            Log.i("Sample", permission)
            val checkSelfPermissionResult = ContextCompat.checkSelfPermission(this, permission)
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermissionResult) {
                Log.i("Sample", "Permission not granted!")
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

}
