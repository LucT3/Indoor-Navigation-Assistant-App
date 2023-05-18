package it.unipi.dii.indoornavigatorassistant.scanners

import android.util.Log
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import it.unipi.dii.indoornavigatorassistant.NavigationActivity
import it.unipi.dii.indoornavigatorassistant.dao.QrCodeInfoProvider
import it.unipi.dii.indoornavigatorassistant.databinding.ActivityNavigationBinding
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.lang.ref.WeakReference

class QRCodeScanner(private val navigationActivity : WeakReference<NavigationActivity>){
    private lateinit var barcodeScanner : BarcodeScanner
    private val qrCodeInfoProvider: QrCodeInfoProvider = QrCodeInfoProvider(navigationActivity.get()!!)
    
    fun startCamera(binding: ActivityNavigationBinding) {
        val cameraController = LifecycleCameraController(navigationActivity.get()!!)
        val previewView: PreviewView = binding.viewFinder

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)

        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(navigationActivity.get()!!),
            MlKitAnalyzer(
                listOf(barcodeScanner),
                CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(navigationActivity.get()!!)
            ) { result: MlKitAnalyzer.Result? ->
                val barcodeResults = result?.getValue(barcodeScanner)
                if (barcodeResults == null
                    || barcodeResults.size == 0
                    || barcodeResults.first() == null
                ) {
                    return@MlKitAnalyzer
                }
                val qrCodeId : String = barcodeResults[0].rawValue.toString()
                val pointsOfInterest = qrCodeInfoProvider.getQrCodeInfo(qrCodeId)
                Log.d(Constants.LOG_TAG, "QrCodeScanner::startCamera -  QR Code Id: ${qrCodeId}")
                Log.d(Constants.LOG_TAG, "QrCodeScanner::startCamera " +
                        "- Points of interest: $pointsOfInterest")

                Thread.sleep(1000)
            }
        )

        cameraController.bindToLifecycle(navigationActivity.get()!!)
        previewView.controller = cameraController
    }

    fun disconnect() {
        Log.i(Constants.LOG_TAG, "QrCodeScanner::disconnect - barCodeScanner instance closed")
        barcodeScanner.close()
    }

}
