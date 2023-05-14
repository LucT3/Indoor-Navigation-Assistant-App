package it.unipi.dii.indoornavigatorassistant

import androidx.appcompat.app.AppCompatActivity


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
