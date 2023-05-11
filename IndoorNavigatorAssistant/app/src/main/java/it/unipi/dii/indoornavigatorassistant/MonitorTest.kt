package it.unipi.dii.indoornavigatorassistant

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kontakt.sdk.android.ble.manager.ProximityManager
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener
import com.kontakt.sdk.android.common.profile.IBeaconDevice
import com.kontakt.sdk.android.common.profile.IBeaconRegion
import it.unipi.dii.indoornavigatorassistant.util.Constants


class MonitorTest : AppCompatActivity() {
    private lateinit var proximityManager: ProximityManager

    override fun onStart() {
        super.onStart()
        Log.i(Constants.LOG_TAG, "**START MONITOR**")
        proximityManager = ProximityManagerFactory.create(this)
        proximityManager.setIBeaconListener(createIBeaconListener())
        startScanning()
    }

    override fun onStop() {
        super.onStop()
        Log.i(Constants.LOG_TAG, "**STOP MONITOR**")
        proximityManager.stopScanning()
    }

    override fun onDestroy() {
        super.onDestroy()
        proximityManager.disconnect()
    }

    private fun startScanning() {
        Log.i(Constants.LOG_TAG, "**START SCANNING**")
        proximityManager.connect { proximityManager.startScanning() }
    }

    private fun createIBeaconListener(): IBeaconListener {
        Log.i(Constants.LOG_TAG, "**CREATE BEACON LISTENER")
        return object : SimpleIBeaconListener() {
            override fun onIBeaconDiscovered(ibeacon: IBeaconDevice, region: IBeaconRegion) {
                Log.i(Constants.LOG_TAG, "**BEACON DISCOVERED**: $ibeacon")
            }
        }
    }
}
