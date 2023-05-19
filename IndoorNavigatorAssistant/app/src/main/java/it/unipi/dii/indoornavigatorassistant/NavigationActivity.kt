package it.unipi.dii.indoornavigatorassistant

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.LocationManager
import android.os.Bundle
import android.util.AndroidException
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

class NavigationActivity : AppCompatActivity() {

    private lateinit var beaconScanner: BeaconScanner
    private lateinit var qrCodeScanner: QRCodeScanner
    private lateinit var binding: ActivityNavigationBinding
    private var isCameraShowing = false

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        Log.i(Constants.LOG_TAG, "NavigationActivity::onCreate - Navigation Activity created")
        setContentView(binding.root)

        //call the method to set the layout (show/don't show camera)
        setCamera()
    }

    override fun onStart() {
        super.onStart()
        if (bluetoothAdapter.isEnabled && isLocationEnabled(this)) {
            startScanners()
        } else if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
        else {
            checkLocationEnabled()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconScanner.disconnect()
        qrCodeScanner.stop()
    }
    override fun onStop() {
        super.onStop()
        beaconScanner.stopScanning()
    }

    private fun startScanners() {
        beaconScanner = BeaconScanner(WeakReference(this),binding)
        qrCodeScanner = QRCodeScanner(WeakReference(this), binding)

        beaconScanner.startScanning()
        qrCodeScanner.start()
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
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
                    throw AndroidException(e)
                }
            }
        }
    }

    private fun promptEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        try {
            @Suppress("DEPRECATION")
            startActivityForResult(enableBtIntent, Constants.ENABLE_BLUETOOTH_REQUEST_CODE)
        } catch (ex: SecurityException) {
            throw RuntimeException(ex)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == Constants.ENABLE_BLUETOOTH_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                promptEnableBluetooth()
            } else {
                if (!isLocationEnabled(this)) {
                    checkLocationEnabled()
                } else {
                    startScanners()
                }
            }
        }
        if (requestCode == Constants.REQUEST_ENABLE_LOCATION) {
            if (resultCode != Activity.RESULT_OK) {
                checkLocationEnabled()
            } else {
                startScanners()
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


}
