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
import it.unipi.dii.indoornavigatorassistant.dao.BeaconInfoProvider
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
    private val beaconInfoProvider = BeaconInfoProvider.getInstance(navigationActivity.get()!!)
    
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

            override fun onIBeaconDiscovered(ibeacon: IBeaconDevice, region: IBeaconRegion) {
                Log.d(
                    Constants.LOG_TAG,
                    "BeaconScanner::onIBeaconDiscovered - beacon discovered: $ibeacon"
                )
                beaconsList.add(ibeacon)
                updateBeaconRegion(beaconsList)
            }
            
            override fun onIBeaconsUpdated(
                ibeacons: MutableList<IBeaconDevice>,
                region: IBeaconRegion
            ) {
                beaconsList.clear()
                beaconsList.addAll(ibeacons)
                updateBeaconRegion(beaconsList)

            }

            override fun onIBeaconLost(ibeacon: IBeaconDevice?, region: IBeaconRegion?) {
                beaconsList.remove(ibeacon)
                updateBeaconRegion(beaconsList)
            }
        }
    }

    fun updateBeaconRegion(ibeacons: MutableList<IBeaconDevice>) {
        val regionId = getCurrentRegionId(ibeacons) ?: return

        if (beaconState.isNewRegion(regionId)) {
            beaconState.handleCurve(regionId)

            // Get points of interest
            val pointsOfInterest = beaconInfoProvider.getBLERegionInfo(regionId)
            beaconState.displayPointsOfInterestInfo(pointsOfInterest)
        }
    }
    
    /**
     * Get id of the current BLE region
     *
     * @param beacons list of detected BLE beacons
     * @return id of the region if the two beacons with highest RSSI are an actual region, null otherwise
     */
    private fun getCurrentRegionId(beacons: MutableList<IBeaconDevice>): String? {
        // Sort beacons by signal strength
        val sortedBeacons = beacons.sortedByDescending { it.rssi }
        
        // Check if there are at least 2 beacons
        if (sortedBeacons.size < 2) {
            return null
        }
        
        // Get the top 2 beacons
        val top2Beacons = sortedBeacons.take(2)
        // Take the top 2 beacon IDs
        val top2BeaconIds = top2Beacons.map { it.uniqueId }
        // Check if both IDs are present
        if (top2BeaconIds.all { it?.isNotEmpty() == true }) {
            Log.d(
                Constants.LOG_TAG, "BeaconScanner::getCurrentRegionId " +
                        "- 2 nearest beacons: $top2BeaconIds"
            )
            // Get regionId from the top 2 beacons for rssi
            val regionId = beaconInfoProvider.computeBLERegionId(
                top2BeaconIds[0],
                top2BeaconIds[1]
            )
            displayBeaconRegionInfo(regionId)
            return regionId
        } else {
            return null
        }
    }
    
    
    /**
     * Display on Logcat and Navigation activity page the current Beacon Region where the user is.
     *
     * @param regionId id of current region
     */
    private fun displayBeaconRegionInfo(regionId: String) {
        Log.d(
            Constants.LOG_TAG, "BeaconScanner::onIBeaconsUpdated " +
                    "- Region scanned: $regionId"
        )
        binding.textViewCurrentRegion.text = navigationActivity.get()!!
            .resources
            .getString(
                R.string.navigation_activity_beacon_region_message,
                regionId
            )
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
