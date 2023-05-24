package it.unipi.dii.indoornavigatorassistant.scanners

import android.util.Log
import com.kontakt.sdk.android.ble.configuration.ScanMode
import com.kontakt.sdk.android.ble.configuration.ScanPeriod
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener
import com.kontakt.sdk.android.common.profile.IBeaconDevice
import com.kontakt.sdk.android.common.profile.IBeaconRegion
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.activities.NavigationActivity
import it.unipi.dii.indoornavigatorassistant.databinding.ActivityNavigationBinding
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.lang.ref.WeakReference

/**
 * Class for scanning beacons using KontaktSDK and show the corresponding data to user.
 *
 * @property navigationActivity the navigation activity
 * @property binding the ViewBinding object of navigation activity
 */
class BeaconScanner(
    private val navigationActivity: WeakReference<NavigationActivity>,
    private val binding: ActivityNavigationBinding
) {
    // Bluetooth Low Energy
    private val proximityManager = ProximityManagerFactory.create(navigationActivity.get()!!)
    
    // Navigation state
    private val beaconState = BeaconState(navigationActivity, binding)
    
    
    /**
     * Initializes the TextView for displaying beacon region information.
     */
    init {
        // Initialize text view
        binding.textViewCurrentRegion.text = navigationActivity.get()!!
            .resources.getString(
                R.string.navigation_activity_beacon_region_message,
                ""
            )
        
        // Configure proximity manager
        proximityManager.configuration()
            .scanMode(ScanMode.BALANCED)
            .scanPeriod(ScanPeriod.RANGING)
        proximityManager.setIBeaconListener(createIBeaconListener())
    }
    
    /**
     * Starts the scanning of iBeacons.
     */
    fun startScanning() {
        Log.d(Constants.LOG_TAG, "BeaconScanner::startScanning - scanning started")
        proximityManager.connect { proximityManager.startScanning() }
    }
    
    /**
     * Creates the IBeaconListener that listens for iBeacons and executes the subsequent logic
     * if the checks are satisfied.
     *
     * @return The created IBeaconListener.
     */
    private fun createIBeaconListener(): IBeaconListener {
        Log.d(Constants.LOG_TAG, "BeaconScanner::createIBeaconListener - beacon listener started")
        return object : SimpleIBeaconListener() {
            
            private val beaconsList = mutableListOf<IBeaconDevice>()
            
            override fun onIBeaconDiscovered(beacon: IBeaconDevice, region: IBeaconRegion) {
                Log.d(
                    Constants.LOG_TAG,
                    "BeaconScanner::onIBeaconDiscovered - beacon discovered: $beacon"
                )
                beaconsList.add(beacon)
                beaconState.updateBeaconRegion(beaconsList)
            }
            
            override fun onIBeaconsUpdated(
                beacons: MutableList<IBeaconDevice>,
                region: IBeaconRegion
            ) {
                Log.d(
                    Constants.LOG_TAG,
                    "BeaconScanner::onIBeaconsUpdated - beacons update: $beacons"
                )
                beaconsList.clear()
                beaconsList.addAll(beacons)
                beaconState.updateBeaconRegion(beaconsList)
                
            }
            
            override fun onIBeaconLost(beacon: IBeaconDevice?, region: IBeaconRegion?) {
                Log.d(
                    Constants.LOG_TAG,
                    "BeaconScanner::onIBeaconLost - beacon discovered: $beacon"
                )
                beaconsList.remove(beacon)
                beaconState.updateBeaconRegion(beaconsList)
            }
        }
    }
    
    
    /**
     * Stops scanning. Does not disconnect from backing service. Call [disconnect] to disconnect.
     */
    fun stopScanning() {
        proximityManager.stopScanning()
        Log.d(Constants.LOG_TAG, "BeaconScanner::stopScanning - scanning stopped")
    }
    
    /**
     * Disconnects the scanning service.
     */
    fun disconnect() {
        proximityManager.disconnect()
        Log.d(Constants.LOG_TAG, "BeaconScanner::disconnect - scanning service disconnected")
    }
    
}
