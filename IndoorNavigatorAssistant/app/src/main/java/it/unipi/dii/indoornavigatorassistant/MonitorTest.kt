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
    private var proximityManager: ProximityManager? = null

    override fun onStart() {
        Log.i(Constants.LOG_TAG, "**START MONITOR**")
        proximityManager = ProximityManagerFactory.create(this)
        proximityManager?.setIBeaconListener(createIBeaconListener())
        super.onStart()
        startScanning()
    }

    override fun onStop() {
        Log.i(Constants.LOG_TAG, "**STOP MONITOR**")
        proximityManager!!.stopScanning()
        super.onStop()
    }

    override fun onDestroy() {
        proximityManager!!.disconnect()
        proximityManager = null
        super.onDestroy()
    }

    private fun startScanning() {
        Log.i(Constants.LOG_TAG, "**START SCANNING**")
        proximityManager!!.connect { proximityManager!!.startScanning() }
    }

    private fun createIBeaconListener(): IBeaconListener {
        println("**CREATE BEACON LISTENER")
        return object : SimpleIBeaconListener() {
            override fun onIBeaconDiscovered(ibeacon: IBeaconDevice, region: IBeaconRegion) {
                Log.i(Constants.LOG_TAG, "**BEACON DISCOVERED**: $ibeacon")
            }
        }
    }
}