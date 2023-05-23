package it.unipi.dii.indoornavigatorassistant.scanners

import android.util.Log
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.activities.NavigationActivity
import it.unipi.dii.indoornavigatorassistant.databinding.ActivityNavigationBinding
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

/**
 * Class for scanning QR codes using MLKit library and show the corresponding data to user.
 *
 * @property navigationActivity the navigation activity
 * @property binding the ViewBinding object of navigation activity
 */
class QRCodeScanner(
    private val navigationActivity: WeakReference<NavigationActivity>,
    private val binding: ActivityNavigationBinding
) {
    // Barcode scanner
    private val barcodeScanner: BarcodeScanner
    // Object which holds the state of the seen QR codes
    private val qrCodeState = QRCodeState(navigationActivity, binding)
    // Thread for analyzing the frames from the camera
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    
    
    init {
        // Initialize barcode scanner
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)
        
        // Initialize text view
        binding.textViewQrCode.text = navigationActivity.get()!!
            .resources.getString(
                R.string.navigation_activity_qr_code_point,
                ""
            )
    }
    
    
    /**
     * Create and configure a controller to analyze QR codes from camera
     * and display the corresponding data on screen.
     *
     * The camera controller is bound to the NavigationActivity lifecycle and it
     * must be called inside the `onCreate` method of the activity.
     */
    fun startCamera() {
        val cameraController = LifecycleCameraController(navigationActivity.get()!!)
        val previewView: PreviewView = binding.previewView
        
        cameraController.setImageAnalysisAnalyzer(
            cameraExecutor,
            MlKitAnalyzer(
                listOf(barcodeScanner),
                CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
                cameraExecutor
            ) { result: MlKitAnalyzer.Result? ->
                val barcodeResults = result?.getValue(barcodeScanner)
                if ((barcodeResults == null)
                    || (barcodeResults.size == 0)
                    || (barcodeResults.first() == null)
                ) {
                    return@MlKitAnalyzer
                }
                
                // Get the QR code ID from the first barcode result and notify new reading
                val qrCodeId: String? = barcodeResults[0].rawValue
                qrCodeState.notifyQrCode(qrCodeId)
            }
        )
        
        cameraController.bindToLifecycle(navigationActivity.get()!!)
        previewView.controller = cameraController
    }
    
    
    /**
     * Close the QR code scanner.
     * It must be called inside the `onDestroy` method of the activity.
     */
    fun destroy() {
        Log.d(Constants.LOG_TAG, "QrCodeScanner::stop - barCodeScanner instance closed")
        barcodeScanner.close()
        cameraExecutor.shutdown()
    }
    
}
