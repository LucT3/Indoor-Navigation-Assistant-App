package it.unipi.dii.indoornavigatorassistant

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import it.unipi.dii.indoornavigatorassistant.databinding.ActivityNavigationBinding
import it.unipi.dii.indoornavigatorassistant.scanners.BeaconScanner
import it.unipi.dii.indoornavigatorassistant.scanners.QRCodeScanner
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.lang.ref.WeakReference


class NavigationActivity : AppCompatActivity() {

    private lateinit var beaconScanner : BeaconScanner
    private lateinit var qrCodeScanner: QRCodeScanner
    private lateinit var binding : ActivityNavigationBinding

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        Log.i(Constants.LOG_TAG,"NavigationActivity::onCreate - Navigation Activity created")
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        beaconScanner = BeaconScanner(WeakReference(this))
        qrCodeScanner = QRCodeScanner(WeakReference(this))

        beaconScanner.startScanning()
        qrCodeScanner.startCamera(binding)
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
        qrCodeScanner.disconnect()
    }
    

    //BLUETOOTH METHODS
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
