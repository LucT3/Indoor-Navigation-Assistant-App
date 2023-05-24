package it.unipi.dii.indoornavigatorassistant.scanners

import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.ArrayAdapter
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.activities.NavigationActivity
import it.unipi.dii.indoornavigatorassistant.dao.BeaconInfoProvider
import it.unipi.dii.indoornavigatorassistant.databinding.ActivityNavigationBinding
import it.unipi.dii.indoornavigatorassistant.model.BLERegionInfo
import it.unipi.dii.indoornavigatorassistant.speech.TextToSpeechContainer
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.lang.ref.WeakReference

/**
 * Class to manage the detection of new BLE regions based on a consecutive occurrence threshold.
 */
class BeaconState(
    private val navigationActivity: WeakReference<NavigationActivity>,
    private val binding: ActivityNavigationBinding
) {
    // Provider of information about beacons
    private val beaconInfoProvider = BeaconInfoProvider.getInstance(navigationActivity.get()!!)
    
    // Text-to-speech
    private val textToSpeechInstance = TextToSpeechContainer(navigationActivity.get()!!)
    
    // Region currently show to user
    private var currentRegion: String? = null
    
    // Last region scanned
    private var lastRegionScanned: String? = null
    
    // How many times the lastRegionScanned has been scanned in consecutive times
    private var counter: Int = 0
    
    companion object {
        // How many times a region must be scanned before consider it as the "current region"
        private const val THRESHOLD: Int = 2
    }
    
    // Data about areas before a curve
    private var preCurveId: String? = null
    private var preRegionName: String? = null
    
    
    /**
     * Checks if the provided region is a new region based on the consecutive occurrence threshold.
     *
     * @param regionScanned The last region that was detected.
     * @return True if the region is new, false otherwise.
     */
    fun isNewRegion(regionScanned: String): Boolean {
        if (currentRegion != regionScanned) {
            if (regionScanned != lastRegionScanned) {
                // Reset the counter to 1 (new consecutive sequence)
                counter = 1
            } else {
                // Increment counter (region scanned is the same)
                counter++
            }
            
            if (counter == THRESHOLD) {
                // The region has changed consecutively for the specified number of times.
                // Update the currentRegion, reset the counter, and update the lastRegionScanned.
                currentRegion = regionScanned
                counter = 0
                lastRegionScanned = regionScanned
                return true
            }
        } else {
            // If the regionScanned is the same as the currentRegion,
            // reset the counter as the consecutive sequence is broken.
            counter = 0
        }
        
        // Update the lastRegionScanned
        lastRegionScanned = regionScanned
        return false
    }
    
    /**
     * Check if a region is a pre-curve or a curve, and warn the user if is inside the curve region.
     * Display and send an audio message telling the direction of the curve
     *
     * @param regionId id of the current region
     */
    fun handleCurve(regionId: String) {
        if (beaconInfoProvider.isPreCurve(regionId)) {
            preCurveId = regionId
        }
        if (beaconInfoProvider.isCurve(regionId) && preCurveId != null) {
            val curveInfo = beaconInfoProvider.getCurveInfo(regionId)
            val direction = when (preCurveId) {
                curveInfo?.preCurveLeft -> navigationActivity.get()
                    ?.getString(R.string.curve_direction_left)
                
                else -> navigationActivity.get()?.getString(R.string.curve_direction_right)
            }
            Log.d(
                Constants.LOG_TAG,
                "BeaconScanner::onIBeaconsUpdated - Curve Detected $direction"
            )
            textToSpeechInstance.speak(
                "${navigationActivity.get()?.getString(R.string.curve_indication)} $direction",
                TextToSpeech.QUEUE_FLUSH
            )
            preCurveId = null
        }
    }
    
    
    /**
     * Display on Logcat and Navigation activity page the Points of interest of the current region.
     *
     * @param bleRegionInfo information about BLE region
     */
    fun displayPointsOfInterestInfo(bleRegionInfo: BLERegionInfo?) {
        Log.d(
            Constants.LOG_TAG, "BeaconScanner::displayPointsOfInterestInfo " +
                    "- Points of interest: ${bleRegionInfo?.pointsOfInterest}"
        )
        
        // Display region points of interest
        if (bleRegionInfo != null) {
            if (preRegionName == null) {
                preRegionName = bleRegionInfo.name
                // Notify user about region name if it's the first encountered
                textToSpeechInstance.speak(
                    "${
                        navigationActivity.get()?.getString(R.string.region_name_info)
                    } ${bleRegionInfo.name}",
                    TextToSpeech.QUEUE_ADD
                )
            } else if (preRegionName != bleRegionInfo.name) {
                // Notify user about region name if it's different from the previous one
                textToSpeechInstance.speak(
                    "${
                        navigationActivity.get()?.getString(R.string.region_name_info)
                    } ${bleRegionInfo.name}",
                    TextToSpeech.QUEUE_ADD
                )
            }
            // Notify user about points of interest
            textToSpeechInstance.speak(
                "${
                    navigationActivity.get()?.getString(R.string.region_points_of_interest)
                } ${bleRegionInfo.pointsOfInterest}",
                TextToSpeech.QUEUE_ADD
            )
            
            // Show points of interest on GUI
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
    
}
