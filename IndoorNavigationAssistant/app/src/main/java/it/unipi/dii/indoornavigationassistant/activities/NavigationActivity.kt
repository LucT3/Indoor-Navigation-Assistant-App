package it.unipi.dii.indoornavigationassistant.activities

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.LocationManager
import android.os.Bundle
import android.util.AndroidRuntimeException
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import it.unipi.dii.indoornavigationassistant.databinding.ActivityNavigationBinding
import it.unipi.dii.indoornavigationassistant.scanners.BeaconScanner
import it.unipi.dii.indoornavigationassistant.scanners.QRCodeScanner
import it.unipi.dii.indoornavigationassistant.speech.TextToSpeechContainer
import it.unipi.dii.indoornavigationassistant.util.Constants
import it.unipi.dii.indoornavigationassistant.R
import java.lang.ref.WeakReference

class NavigationActivity : AppCompatActivity() {
    
    // Scanners
    private lateinit var beaconScanner: BeaconScanner
    private lateinit var qrCodeScanner: QRCodeScanner
    
    // Text-to-speech
    private lateinit var textToSpeech: TextToSpeechContainer
    
    // Variables for GUI
    private lateinit var binding: ActivityNavigationBinding
    private var isCameraShowing = false
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the activity layout
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateCameraVisibility()
        
        // Initialize scanners
        beaconScanner = BeaconScanner(WeakReference(this), binding)
        qrCodeScanner = QRCodeScanner(WeakReference(this), binding)
        
        // Start QR code scanner
        qrCodeScanner.startCamera()
        
        // Configure Text-to-Speech functionality
        textToSpeech = TextToSpeechContainer(this)
        
        Log.d(Constants.LOG_TAG, "NavigationActivity::onCreate - Navigation Activity created")
    }
    
    override fun onStart() {
        super.onStart()

        // Check if Bluetooth and GPS services are enabled
        val isBluetoothEnabled = this.isBluetoothEnabled()
        val isLocationEnabled = this.isLocationEnabled()
        
        if (!isBluetoothEnabled) {
            promptEnableBluetooth()
        }
        else if (!isLocationEnabled) {
            promptEnableLocation()
        }
        else {
            // Start beacon scanning
            beaconScanner.startScanning()
        }
    }
    
    override fun onStop() {
        super.onStop()
        
        // Stop scanners
        beaconScanner.stopScanning()
        // Stop text-to-speech
        textToSpeech.stop()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Disconnect scanner from BLE service
        beaconScanner.disconnect()

        // Stop QR code scanner
        qrCodeScanner.destroy()

        // Shutdown text-to-speech
        textToSpeech.shutdown()
    }
    
    
    
    /**
     * Check if Bluetooth is enabled
     *
     * @return true if Bluetooth is enabled, false otherwise
     */
    private fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter.isEnabled
    }
    
    /**
     * Check if localization services are enabled
     *
     * @return true if enabled, false otherwise
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    
    
    /**
     * Prompt the user with a request to enable GPS
     */
    private fun promptEnableLocation() {
        @Suppress("DEPRECATION")
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
                    throw AndroidRuntimeException(e)
                }
            }
        }
    }
    
    
    /**
     * Prompt the user with a request to enable Bluetooth
     */
    private fun promptEnableBluetooth() {
        // Send a request to the smartphone's bluetooth adapter to prompt the user
        // by starting an activity
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        try {
            @Suppress("DEPRECATION")
            startActivityForResult(enableBtIntent, Constants.ENABLE_BLUETOOTH_REQUEST_CODE)
        } catch (ex: SecurityException) {
            // Only thrown if Bluetooth permissions have not been granted.
            // This should never happen since permissions are handled in MainActivity
            throw AndroidRuntimeException(ex)
        }
    }
    
    
    /**
     * Handle the reception of the result from another activity.
     *
     * @param requestCode code of the request
     * @param resultCode code of the result
     * @param data data returned by activity
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        
        when(requestCode) {
            // Bluetooth activation
            Constants.ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    // Bluetooth is mandatory, so ask again
                    promptEnableBluetooth()
                } else {
                    // If BT is enabled, next step is to check if GPS is enabled
                    if (!isLocationEnabled()) {
                        promptEnableLocation()
                    } else {
                        beaconScanner.startScanning()
                    }
                }
            }
            
            // GPS activation
            Constants.REQUEST_ENABLE_LOCATION -> {
                if (resultCode != Activity.RESULT_OK) {
                    // GPS is mandatory, so ask again
                    promptEnableLocation()
                } else {
                    beaconScanner.startScanning()
                }
            }
        }
    }
    
    
    
    //--------------------------------------
    //--------------MENU--------------------
    //--------------------------------------
    
    /**
     * It change the layout of the application (show/don't show camera)
     */
    private fun updateCameraVisibility() {
        if (isCameraShowing) {
            // Turn camera on
            binding.previewView.visibility = View.VISIBLE
        }
        else {
            // Turn camera off
            binding.previewView.visibility = View.INVISIBLE
        }
    }
    
    /**
     * It change the icon menu, based on which layout is chosen (camera visible/non-visible)
     */
    private fun updateCameraButton() {
        if (isCameraShowing) {
            binding.switchCameraButton.setImageResource(R.drawable.show_img)
        }
        else {
            binding.switchCameraButton.setImageResource(R.drawable.dont_show_img)
        }
    }
    
    /**
     * Switch camera visibility state from invisible to visible and vice-versa.
     *
     * @param view unused reference to button. Must be kept otherwise code fails
     */
    fun onClickSwitchLayout(view: View) {
        isCameraShowing = !isCameraShowing
        updateCameraVisibility()
        updateCameraButton()
    }

}
