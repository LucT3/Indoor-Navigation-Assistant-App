package it.unipi.dii.indoornavigatorassistant.scanners

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.kontakt.sdk.android.common.util.HashCodeBuilder.init
import it.unipi.dii.indoornavigatorassistant.NavigationActivity
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.io.IOException

class QRCodeScanner(navigationActivity: NavigationActivity) : ImageAnalysis.Analyzer {
    private val navigationActivity : NavigationActivity = navigationActivity
    private val qrScanner : BarcodeScanner

    /**
     * Primary constructor of QrCodeScanner class. cofigure BarcodeScanner options and
     * instantiate a BarcodeScanner for QR codes
     */
    init{
        //configure BarcodeScanner options
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC)
            .enableAllPotentialBarcodes()
            .build()

        //get an instance of BarcodeScanner
        qrScanner = BarcodeScanning.getClient(options) //to specify the format
        Log.d(Constants.LOG_TAG, "QrCodeScanner::init - created qrScanner instance")


    }

    /**
     * Analyzes an image to produce a result. This method is called once for each image from the
     * camera, and called at the frame rate of the camera. Each analyze call is executed sequentially.
     *
     * @param imageProxy : image to analyze
     */
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            //transmit the image
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // Pass image to an ML Kit Vision API to elaborate the image
            val result = qrScanner.process(image)
                .addOnSuccessListener { qrCodes ->
                    for (code in qrCodes) {
                        val bounds = code.boundingBox
                        val corners = code.cornerPoints
                        val rawValue = code.rawValue
                        val valueType = code.valueType
                        // See API reference for complete list of supported types
                        when (valueType) {
                            Barcode.TYPE_WIFI -> {
                                val ssid = code.wifi!!.ssid
                                val password = code.wifi!!.password
                                val type = code.wifi!!.encryptionType
                            }
                            Barcode.TYPE_URL -> {
                                val title = code.url!!.title
                                val url = code.url!!.url
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    // ...
                }
            Log.d(Constants.LOG_TAG, "QrCodeScanner::analyze - image processed result: $result")
        }
    }
}
