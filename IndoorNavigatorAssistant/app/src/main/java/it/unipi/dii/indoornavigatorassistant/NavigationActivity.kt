package it.unipi.dii.indoornavigatorassistant

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import it.unipi.dii.indoornavigatorassistant.databinding.ActivityNavigationBinding
import it.unipi.dii.indoornavigatorassistant.scanners.BeaconScanner
import it.unipi.dii.indoornavigatorassistant.scanners.QRCodeScanner
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.lang.ref.WeakReference

class NavigationActivity : AppCompatActivity() {

    private lateinit var beaconScanner: BeaconScanner
    private lateinit var qrCodeScanner: QRCodeScanner
    private lateinit var binding: ActivityNavigationBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        Log.i(Constants.LOG_TAG, "NavigationActivity::onCreate - Navigation Activity created")
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStart() {
        super.onStart()
        beaconScanner = BeaconScanner(WeakReference(this))
        qrCodeScanner = QRCodeScanner(WeakReference(this))

        beaconScanner.startScanning()
        qrCodeScanner.startCamera(binding)

        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
        checkLocationEnabled()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun checkLocationEnabled() {
        val locationRequest = LocationRequest.create()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            // Location settings are not satisfied, show dialog to enable location services
            val statusCode = (exception as ApiException).statusCode
            if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult()
                    val resolvable = exception as ResolvableApiException
                    resolvable.startResolutionForResult(this, Constants.REQUEST_ENABLE_LOCATION)
                } catch (e: IntentSender.SendIntentException) {
                    // Error occurred while trying to show the dialog
                }
            }
        }
    }

    private fun promptEnableBluetooth() {
        // Check if bluetooth is not enabled
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            try {
                @Suppress("DEPRECATION")
                startActivityForResult(enableBtIntent, Constants.ENABLE_BLUETOOTH_REQUEST_CODE)
            } catch (ex: SecurityException) {
                throw RuntimeException(ex)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == Constants.ENABLE_BLUETOOTH_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                promptEnableBluetooth()
            }
        }
        if (requestCode == Constants.REQUEST_ENABLE_LOCATION) {
            if (resultCode != Activity.RESULT_OK) {
                checkLocationEnabled()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        beaconScanner.stopScanning()
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconScanner.disconnect()
        qrCodeScanner.disconnect()
    }

}
