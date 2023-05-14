package it.unipi.dii.indoornavigatorassistant

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import it.unipi.dii.indoornavigatorassistant.util.Constants


class NavigationActivity : AppCompatActivity() {

    private lateinit var beaconScanner : BeaconScanner

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    
    override fun onStart() {
        super.onStart()
        beaconScanner = BeaconScanner(this)
        beaconScanner.startScanning()
    }
    
    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }
    
    override fun onStop() {
        super.onStop()
        beaconScanner.stopScanning()
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconScanner.disconnect()
    }
    
    
    private fun promptEnableBluetooth() {
        // Check if bluetooth is not enabled
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            try {
                @Suppress("DEPRECATION")
                startActivityForResult(enableBtIntent, Constants.ENABLE_BLUETOOTH_REQUEST_CODE)
            } catch(ex: SecurityException) {
                throw RuntimeException(ex)
            }
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }

}
