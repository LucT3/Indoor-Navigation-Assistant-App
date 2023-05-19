package it.unipi.dii.indoornavigatorassistant.scanners

import android.graphics.Point
import android.util.Log
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import it.unipi.dii.indoornavigatorassistant.NavigationActivity
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.dao.QrCodeInfoProvider
import it.unipi.dii.indoornavigatorassistant.databinding.ActivityNavigationBinding
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

class QRCodeScanner (private val navigationActivity : WeakReference<NavigationActivity>,
                     private val binding: ActivityNavigationBinding) {
    
    private val barcodeScanner : BarcodeScanner
    private val qrCodeInfoProvider = QrCodeInfoProvider.getInstance(navigationActivity.get()!!)
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    
    init {
        // Initialize barcode scanner
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)
    }
    
    fun start() {
        val cameraController = LifecycleCameraController(navigationActivity.get()!!)
        val previewView: PreviewView = binding.viewFinder

        cameraController.setImageAnalysisAnalyzer(
            cameraExecutor,
            MlKitAnalyzer(
                listOf(barcodeScanner),
                CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
                cameraExecutor
            ) { result: MlKitAnalyzer.Result? ->
                val barcodeResults = result?.getValue(barcodeScanner)
                if (barcodeResults == null
                    || barcodeResults.size == 0
                    || barcodeResults.first() == null
                ) {
                    return@MlKitAnalyzer
                }

                displayQrInfo(barcodeResults)
                Thread.sleep(1000)
            }
        )
        
        cameraController.bindToLifecycle(navigationActivity.get()!!)
        previewView.controller = cameraController
    }

    private fun displayQrInfo(barcodeResults : List<Barcode>){
        val qrCodeId : String = barcodeResults[0].rawValue.toString()
        val pointOfInterest = qrCodeInfoProvider.getQrCodeInfo(qrCodeId)
        Log.d(Constants.LOG_TAG, "QrCodeScanner::start -  QR Code Id: $qrCodeId")
        Log.d(Constants.LOG_TAG, "QrCodeScanner::start " +
                "- Point of interest: $pointOfInterest")
        binding.textViewQrCode.text = "Qr Code Point of Interest: ${pointOfInterest}"
    }
    
    fun stop() {
        Log.i(Constants.LOG_TAG, "QrCodeScanner::stop - barCodeScanner instance closed")
        cameraExecutor.shutdown()
        barcodeScanner.close()
    }

}
