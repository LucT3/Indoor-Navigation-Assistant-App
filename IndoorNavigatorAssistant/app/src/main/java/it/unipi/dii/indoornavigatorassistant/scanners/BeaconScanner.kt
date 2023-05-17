package it.unipi.dii.indoornavigatorassistant.scanners

import android.util.Log
import com.kontakt.sdk.android.ble.manager.ProximityManager
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener
import com.kontakt.sdk.android.common.profile.IBeaconDevice
import com.kontakt.sdk.android.common.profile.IBeaconRegion
import it.unipi.dii.indoornavigatorassistant.NavigationActivity
import it.unipi.dii.indoornavigatorassistant.util.Constants

class BeaconScanner(navigationActivity: NavigationActivity) {
    private val proximityManager: ProximityManager

    init {
        proximityManager = ProximityManagerFactory.create(navigationActivity)
    }

    fun startScanning() {
        Log.i(Constants.LOG_TAG, "**START SCANNING**")
        proximityManager.setIBeaconListener(createIBeaconListener())
        proximityManager.connect { proximityManager.startScanning() }
    }

    private fun createIBeaconListener(): IBeaconListener {
        Log.i(Constants.LOG_TAG, "**CREATE BEACON LISTENER**")
        return object : SimpleIBeaconListener() {
            override fun onIBeaconDiscovered(ibeacon: IBeaconDevice, region: IBeaconRegion) {
                Log.i(Constants.LOG_TAG, "**BEACON DISCOVERED**: $ibeacon")
            }
            override fun onIBeaconsUpdated(ibeacons: MutableList<IBeaconDevice>, region: IBeaconRegion) {
                // Sort beacons by signal strength
                val sortedBeacons = ibeacons.sortedByDescending { it.rssi }
                // Get the top 2 beacons
                val top2Beacons = sortedBeacons.take(2)
                // Print the top 2 beacon IDs
                val top2BeaconIds = top2Beacons.map { it.uniqueId }
                Log.i(Constants.LOG_TAG, "**2 NEAREST BEACONS**: $top2BeaconIds")
            }
        }
    }

    fun stopScanning() {
        Log.i(Constants.LOG_TAG, "**STOP SCANNING**")
        proximityManager.stopScanning()
    }

    fun disconnect() {
        Log.i(Constants.LOG_TAG, "**DISCONNECT SCANNING SERVICE**")
        proximityManager.disconnect()
    }

}
