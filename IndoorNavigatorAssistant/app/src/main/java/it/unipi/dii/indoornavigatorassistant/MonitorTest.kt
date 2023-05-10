package it.unipi.dii.indoornavigatorassistant

import android.R
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kontakt.sdk.android.ble.manager.ProximityManager
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener
import com.kontakt.sdk.android.common.KontaktSDK
import com.kontakt.sdk.android.common.profile.IBeaconDevice
import com.kontakt.sdk.android.common.profile.IBeaconRegion
import com.kontakt.sdk.android.common.profile.IEddystoneDevice
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace


class MonitorTest : AppCompatActivity() {
    private var proximityManager: ProximityManager? = null

    protected override fun onStart() {
        println("**START MONITOR**")
        proximityManager = ProximityManagerFactory.create(this)
        proximityManager?.setIBeaconListener(createIBeaconListener())
        super.onStart()
        startScanning()
    }

    protected override fun onStop() {
        println("**STOP MONITOR**")
        proximityManager!!.stopScanning()
        super.onStop()
    }

    protected override fun onDestroy() {
        proximityManager!!.disconnect()
        proximityManager = null
        super.onDestroy()
    }

    private fun startScanning() {
        println("**START SCANNING**")
        proximityManager!!.connect { proximityManager!!.startScanning() }
    }

    private fun createIBeaconListener(): IBeaconListener {
        println("**CREATE BEACON LISTENER")
        return object : SimpleIBeaconListener() {
            override fun onIBeaconDiscovered(ibeacon: IBeaconDevice, region: IBeaconRegion) {
                Log.i("Sample", "IBeacon discovered: $ibeacon")
                println("**BEACON DISCOVERED**: ${ibeacon}")
            }
        }
    }
}