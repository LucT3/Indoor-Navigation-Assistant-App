package it.unipi.dii.indoornavigatorassistant

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener
import com.kontakt.sdk.android.common.profile.IBeaconDevice
import com.kontakt.sdk.android.common.profile.IBeaconRegion
import it.unipi.dii.indoornavigatorassistant.util.Constants


class NavigationActivity : AppCompatActivity() {

    private lateinit var beaconScanner : BeaconScanner

    override fun onStart() {
        super.onStart()
        beaconScanner = BeaconScanner(this)
        beaconScanner.startScanning()
    }

    override fun onStop() {
        super.onStop()
        beaconScanner.stopScanning()
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconScanner.disconnect()
    }


}
