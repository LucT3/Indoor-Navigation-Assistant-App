package it.unipi.dii.indoornavigatorassistant

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import androidx.core.content.ContextCompat
import it.unipi.dii.indoornavigatorassistant.databinding.ActivityNavigationBinding
import it.unipi.dii.indoornavigatorassistant.scanners.BeaconScanner
import it.unipi.dii.indoornavigatorassistant.scanners.QRCodeScanner
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.lang.ref.WeakReference
import java.util.Locale

class NavigationActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    
    private lateinit var binding: ActivityNavigationBinding
    
    // Scanners
    private lateinit var beaconScanner: BeaconScanner
    private lateinit var qrCodeScanner: QRCodeScanner
    
    // Text-to-speech
    private lateinit var textToSpeech: TextToSpeech
    
    // Variables for GUI
    private var isCameraShowing = false
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the activity layout
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCamera()
        
        // Configure Text-to-Speech functionality
        textToSpeech = TextToSpeech(this, this)
        
        Log.d(Constants.LOG_TAG, "NavigationActivity::onCreate - Navigation Activity created")
    }
    
    override fun onStart() {
        super.onStart()
        beaconScanner = BeaconScanner(WeakReference(this))
        qrCodeScanner = QRCodeScanner(WeakReference(this), binding)

        beaconScanner.startScanning()
        qrCodeScanner.start()
        
        // Check Bluetooth and GPS services
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (!bluetoothManager.adapter.isEnabled) {
            promptEnableBluetooth()
        }
        else {
            checkLocationEnabled()
        }
    }
    
    override fun onStop() {
        beaconScanner.stopScanning()
        textToSpeech.stop()
        
        super.onStop()
    }
    
    override fun onDestroy() {
        beaconScanner.disconnect()
        qrCodeScanner.stop()
        textToSpeech.shutdown()
    
        super.onDestroy()
    }
    // TODO gestire meglio create/start/stop/destroy dell'activity (by Riccardo)
    
    

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
            throw RuntimeException(ex)
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
                }
                else {
                    // If BT is enabled, next step is to enable GPS
                    checkLocationEnabled()
                }
            }
            
            // GPS activation
            Constants.REQUEST_ENABLE_LOCATION -> {
                if (resultCode != Activity.RESULT_OK) {
                    // GPS is mandatory, so ask again
                    checkLocationEnabled()
                }
            }
        }
    }
    
    
    
    //--------------------------------------
    //--------------MENU--------------------
    //--------------------------------------
    /**
     * To inflate the options menu (set the current icon)
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.layout_menu, menu)

        val layoutButton = menu?.findItem(R.id.action_switch_layout)
        setIcon(layoutButton)
        return true
    }

    /**
     * Called when a menu layout is selected, it changes the current layout and
     * update the menu choices
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_switch_layout -> {
                isCameraShowing = !isCameraShowing
                setCamera()
                setIcon(item)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * It change the layout of the application (show/don't show camera)
     */
    private fun setCamera() {
        if (isCameraShowing) {
            //turn camera on
            binding.viewFinder.visibility = View.VISIBLE
        }
        else {
            //turn camera off
            binding.viewFinder.visibility = View.INVISIBLE
        }
    }

    /**
     * It change the icon menu, based on which layout is chosen (camera visible/non-visible)
     */
    private fun setIcon(menuItem: MenuItem?) {
        if (menuItem == null)
            return

        menuItem.icon =
            if (isCameraShowing)
                ContextCompat.getDrawable(this, R.drawable.show_img)
            else ContextCompat.getDrawable(this, R.drawable.dont_show_img)
    }
    
    
    // --------------------------------------
    // ----------- TEXT-TO-SPEECH -----------
    // --------------------------------------
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.ITALIAN)
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(Constants.LOG_TAG,"The language is not supported!")
            }
        }
    }
    
    /**
     * Speaks the text using the specified queuing strategy.
     * This method is asynchronous, i.e. the method just adds the request
     * to the queue of TTS requests and then returns.
     *
     * @param text The string of text to be spoken.
     *             No longer than `TextToSpeech.getMaxSpeechInputLength()` characters.
     * @param queueMode The queuing strategy to use, `TextToSpeech.QUEUE_ADD` or `TextToSpeech.QUEUE_FLUSH`.
     */
    fun speak(text: String, queueMode: Int) {
        textToSpeech.speak(text, queueMode, null, "")
        
    }
    
}
