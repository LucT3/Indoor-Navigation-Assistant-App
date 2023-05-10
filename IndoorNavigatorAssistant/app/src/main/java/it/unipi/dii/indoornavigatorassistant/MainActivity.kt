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
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

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


//    private fun checkPermissions() {
//        val requiredPermissions = this.getRequestedPermissions()
//        if (isAnyOfPermissionsNotGranted(requiredPermissions)) {
//            Log.i("Sample", "OH NOOOOO 1")
//            ActivityCompat.requestPermissions(this, requiredPermissions, 100)
////            ComponentActivity.requestPermissions(this, requiredPermissions, 100)
//            Log.i("Sample", "OH NOOOOO 2")
//        } else {
//            Log.i("Sample", "AAAAA")
//        }
//
//    }
    private fun checkPermissions() {
        val x = getRequestedPermissions()
        Log.i("Sample", x.contentToString())
        val requiredPermissions = x

//        val requiredPermissions =
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) arrayOf(Manifest.permission.ACCESS_FINE_LOCATION) else arrayOf(
//                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT
//            ) // Note that there is no need to ask about ACCESS_FINE_LOCATION anymore for BT scanning purposes for VERSION_CODES.S and higher if you add android:usesPermissionFlags="neverForLocation" under BLUETOOTH_SCAN in your manifest file.
        if (isAnyOfPermissionsNotGranted(requiredPermissions)) {
            // you should also reqursively check if any of those permissions need rationale popup via ActivityCompat.shouldShowRequestPermissionRationale before doing the actual requestPermissions(...), but this part was cut out for brevity
            Log.i("Sample", requiredPermissions.contentToString())
            ActivityCompat.requestPermissions(this, requiredPermissions, 100)
        } else {
            Log.i("Sample", "check permission ok")
        }
    Log.i("Sample", "check permission okokok")
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i("Sample", "onRequestPermissionsResult ${permissions.contentToString()} \n ${grantResults.contentToString()}")
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (100 == requestCode) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                this,
                "Location permissions are mandatory to use BLE features on Android 6.0 or higher",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}
