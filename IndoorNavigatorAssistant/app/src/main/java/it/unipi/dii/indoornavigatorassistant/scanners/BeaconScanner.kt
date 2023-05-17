package it.unipi.dii.indoornavigatorassistant.scanners

import android.util.Log
import android.widget.Toast
import com.kontakt.sdk.android.ble.manager.ProximityManager
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener
import com.kontakt.sdk.android.common.profile.IBeaconDevice
import com.kontakt.sdk.android.common.profile.IBeaconRegion
import it.unipi.dii.indoornavigatorassistant.dao.NavigationInfoProvider
import it.unipi.dii.indoornavigatorassistant.NavigationActivity
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.lang.ref.WeakReference

class BeaconScanner(private val navigationActivity: WeakReference<NavigationActivity>) {
    private val proximityManager: ProximityManager =
        ProximityManagerFactory.create(navigationActivity.get()!!)
    private val navigationInfoProvider: NavigationInfoProvider =
        NavigationInfoProvider(navigationActivity.get()!!)
    private val regionManager : BLERegionManager = BLERegionManager()

    fun startScanning() {
        Log.d(Constants.LOG_TAG, "BeaconScanner::startScanning - scanning started")
        proximityManager.setIBeaconListener(createIBeaconListener())
        proximityManager.connect { proximityManager.startScanning() }
    }

    private fun createIBeaconListener(): IBeaconListener {
        Log.d(Constants.LOG_TAG, "BeaconScanner::createIBeaconListener - beacon listener started")
        return object : SimpleIBeaconListener() {
            override fun onIBeaconDiscovered(ibeacon: IBeaconDevice, region: IBeaconRegion) {
                Log.d(Constants.LOG_TAG, "BeaconScanner::onIBeaconDiscovered - beacon discovered: $ibeacon")
            }
            override fun onIBeaconsUpdated(ibeacons: MutableList<IBeaconDevice>, region: IBeaconRegion) {
                // Sort beacons by signal strength
                val sortedBeacons = ibeacons.sortedByDescending { it.rssi }
                // Get the top 2 beacons
                val top2Beacons = sortedBeacons.take(2)
                // Print the top 2 beacon IDs
                val top2BeaconIds = top2Beacons.map { it.uniqueId }
                Log.d(Constants.LOG_TAG, "BeaconScanner::onIBeaconsUpdated " +
                        "- 2 nearest beacons: $top2BeaconIds")
                // Get regionId from the top 2 beacons for rssi
                val regionId = navigationInfoProvider.computeBLERegionId(top2BeaconIds[0], top2BeaconIds[1])
                Log.d(Constants.LOG_TAG, "BeaconScanner::onIBeaconsUpdated " +
                        "- Region scanned: $regionId")
                if (regionManager.isNewRegion(regionId)) {
                    // Get points of interest
                    val pointsOfInterest = navigationInfoProvider.getBLERegionInfo(regionId)
                    Log.d(Constants.LOG_TAG, "BeaconScanner::onIBeaconsUpdated " +
                            "- Points of interest: $pointsOfInterest")
                    Toast.makeText(navigationActivity.get()!!, pointsOfInterest.toString(), Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    fun stopScanning() {
        proximityManager.stopScanning()
        Log.d(Constants.LOG_TAG, "BeaconScanner::stopScanning - scanning stopped")
    }

    fun disconnect() {
        proximityManager.disconnect()
        Log.d(Constants.LOG_TAG, "BeaconScanner::disconnect - scanning service disconnected")
    }

}
