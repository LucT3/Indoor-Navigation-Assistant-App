package it.unipi.dii.indoornavigatorassistant.scanners

import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.ArrayAdapter
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener
import com.kontakt.sdk.android.common.profile.IBeaconDevice
import com.kontakt.sdk.android.common.profile.IBeaconRegion
import it.unipi.dii.indoornavigatorassistant.BLERegionManager
import it.unipi.dii.indoornavigatorassistant.NavigationActivity
import it.unipi.dii.indoornavigatorassistant.dao.BeaconInfoProvider
import it.unipi.dii.indoornavigatorassistant.databinding.ActivityNavigationBinding
import it.unipi.dii.indoornavigatorassistant.model.BLERegionInfo
import it.unipi.dii.indoornavigatorassistant.speech.TextToSpeechContainer
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.lang.ref.WeakReference


class BeaconScanner(
    private val navigationActivity: WeakReference<NavigationActivity>,
    private val binding: ActivityNavigationBinding
) {
    
    private val proximityManager = ProximityManagerFactory.create(navigationActivity.get()!!)
    private val beaconInfoProvider = BeaconInfoProvider.getInstance(navigationActivity.get()!!)
    private val regionManager = BLERegionManager()
    private val textToSpeechInstance: TextToSpeechContainer
    
    /**
     * Initializes the TextView for displaying beacon region information.
     */
    init {
        //textview initialization
        binding.textViewCurrentRegion.text = Constants.BEACON_INFO_MESSAGE
        
        //text to speech initialization
        textToSpeechInstance = TextToSpeechContainer(navigationActivity.get()!!)
    }
    
    /**
     * Starts the scanning of iBeacons.
     */
    fun startScanning() {
        Log.d(Constants.LOG_TAG, "BeaconScanner::startScanning - scanning started")
        proximityManager.setIBeaconListener(createIBeaconListener())
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
            override fun onIBeaconDiscovered(ibeacon: IBeaconDevice, region: IBeaconRegion) {
                Log.d(
                    Constants.LOG_TAG,
                    "BeaconScanner::onIBeaconDiscovered - beacon discovered: $ibeacon"
                )
            }
            
            override fun onIBeaconsUpdated(ibeacons: MutableList<IBeaconDevice>,
                                           region: IBeaconRegion)
            {
                val regionId = getCurrentRegionId(ibeacons) ?: return
                
                if (regionManager.isNewRegion(regionId)) {
                    // TODO rifare algoritmo
                    
                    // 1. controlla se area prima di curva => se sì,
                    
                    if (beaconInfoProvider.isCurve(regionId)) {
                        val curveDirection =
                            beaconInfoProvider.getAreaBeforeCurveInfo(regionId)
                        textToSpeechInstance.speak(
                            "You are in a curve to the $curveDirection",
                            TextToSpeech.QUEUE_FLUSH
                        )
                        Log.d(
                            Constants.LOG_TAG,
                            "BeaconScanner::onIBeaconsUpdated - Curve Detected $curveDirection"
                        )
                    }

                    // Get points of interest
                    val pointsOfInterest = beaconInfoProvider.getBLERegionInfo(regionId)
                    displayPointsOfInterestInfo(pointsOfInterest)
                }
            }
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
        
        // TODO filtrare beacons il cui RSSI è sotto una certa soglia?
        
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
        }
        else {
            return null
        }
    }
    
    
    
    /**
     * display on Logcat and Navigation activity page the current Beacon Region where the user is
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
                it.unipi.dii.indoornavigatorassistant.R.string.navigation_activity_beacon_region_message,
                regionId
            )
    }
    
    /**
     * display on Logcat and Navigation activity page the Points of interest of the current region
     *
     * @param bleRegionInfo
     */
    private fun displayPointsOfInterestInfo(bleRegionInfo: BLERegionInfo?) {
        Log.d(
            Constants.LOG_TAG, "BeaconScanner::displayPointsOfInterestInfo " +
                    "- Points of interest: ${bleRegionInfo?.pointsOfInterest}"
        )
        
        // Display region points of interest
        if (bleRegionInfo != null) {
            
            textToSpeechInstance.speak(
                "You Are in the ${bleRegionInfo.name}",
                TextToSpeech.QUEUE_ADD
            )
            textToSpeechInstance.speak(
                "In this region there is : ${bleRegionInfo.pointsOfInterest}",
                TextToSpeech.QUEUE_ADD
            )
            
            val pointsOfInterest: List<String> = bleRegionInfo.pointsOfInterest
            val arrayAdapter = ArrayAdapter(
                navigationActivity.get()!!,
                android.R.layout.simple_list_item_1,
                pointsOfInterest
            )
            binding.POIBeacons.adapter = arrayAdapter
        } else {
            binding.POIBeacons.adapter = null
        }
    }
    
    
    /**
     * Stops the scanning of iBeacons.
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
