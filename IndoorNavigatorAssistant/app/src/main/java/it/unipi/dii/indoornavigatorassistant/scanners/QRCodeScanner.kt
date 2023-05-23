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

class QRCodeScanner(private val navigationActivity: WeakReference<NavigationActivity>,
                    private val binding: ActivityNavigationBinding) {
    
    private val barcodeScanner: BarcodeScanner
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val qrCodeState = QRCodeState(navigationActivity, binding)
    
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
    
    fun create() {
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
                
                displayQrInfo(barcodeResults)
            }
        )
        
        cameraController.bindToLifecycle(navigationActivity.get()!!)
        previewView.controller = cameraController
    }
    
    /**
     * TODO
     * @param barcodeResults The list of Barcode results from scanning the QR code.
     */
    private fun displayQrInfo(barcodeResults: List<Barcode>) {
        // Get the QR code ID from the first barcode result, or use an empty string if not available
        val qrCodeId: String? = barcodeResults[0].rawValue
        qrCodeState.notifyQrCode(qrCodeId)
    }
    
    
    fun destroy() {
        Log.d(Constants.LOG_TAG, "QrCodeScanner::stop - barCodeScanner instance closed")
        cameraExecutor.shutdown()
        barcodeScanner.close()
    }
    
}
