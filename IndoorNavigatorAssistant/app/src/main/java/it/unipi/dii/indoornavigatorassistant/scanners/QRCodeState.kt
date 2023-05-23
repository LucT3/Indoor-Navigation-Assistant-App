package it.unipi.dii.indoornavigatorassistant.scanners

import android.speech.tts.TextToSpeech
import android.util.Log
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.activities.NavigationActivity
import it.unipi.dii.indoornavigatorassistant.dao.QrCodeInfoProvider
import it.unipi.dii.indoornavigatorassistant.databinding.ActivityNavigationBinding
import it.unipi.dii.indoornavigatorassistant.speech.TextToSpeechContainer
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.lang.ref.WeakReference
import java.time.Duration
import java.time.LocalDateTime


/**
 * Class which holds the history of seen QR codes, in order to prevent the app to
 * continuously repeat the same point of interest over and over again.
 *
 * @property navigationActivity activity responsible for the assisted navigation
 * @property binding view binding of the NavigationActivity
 */
class QRCodeState(
    private val navigationActivity: WeakReference<NavigationActivity>,
    private val binding: ActivityNavigationBinding
) {
    
    companion object {
        // Refresh period (the app will wait at least [PERIOD_IN_MILLISECONDS]
        // to repeat the same QR code)
        private const val PERIOD_IN_MILLISECONDS = 10000
    }
    
    // State of QR code scanner
    private var lastQrCode: String = ""
    private var lastTimestamp: LocalDateTime = LocalDateTime.now()
    
    // Helper objects
    private val qrCodeInfoProvider = QrCodeInfoProvider.getInstance(navigationActivity.get()!!)
    private val textToSpeech = TextToSpeechContainer(navigationActivity.get()!!)
    
    /**
     * Notify the scanning of a QR code from the camera.
     *
     * @param qrCode id of the QR code
     */
    fun notifyQrCode(qrCode: String?) {
        // New QR code?
        if (qrCode != null && qrCode != lastQrCode) {
            lastQrCode = qrCode
            lastTimestamp = LocalDateTime.now()
            displayQrCode()
            return
        }
        
        // Did a whole period of time (10s) elapsed?
        if (isRefreshNeeded()) {
            lastTimestamp = LocalDateTime.now()
            if (qrCode != null) {
                displayQrCode()
            } else {
                // Show NOT FOUND message
                displayNotFound()
            }
        }
    }
    
    
    /**
     * Check if enough time has elapsed to refresh the current QR code.
     *
     * @return true if refresh is needed, false otherwise
     */
    private fun isRefreshNeeded(): Boolean {
        val duration = Duration.between(lastTimestamp, LocalDateTime.now())
        return duration.toMillis() >= PERIOD_IN_MILLISECONDS
    }
    
    
    /**
     * Display the data concerning the current QR code.
     */
    private fun displayQrCode() {
        // Get the point of interest using the QR code ID
        val pointOfInterest = qrCodeInfoProvider.getQrCodeInfo(lastQrCode)
        
        Log.d(
            Constants.LOG_TAG,
            "QRCodeState::displayQrCode - QR code id: $lastQrCode; " +
                    "point of interest: $pointOfInterest"
        )
        
        // Check if point of interest exists
        if (pointOfInterest == null) {
            displayNotFound()
            return
        }
        
        // Display the point of interest information in the TextView and speak it out loud
        // Set the TextView text with the formatted point of interest message
        binding.textViewQrCode.text = navigationActivity.get()?.resources?.getString(
            R.string.navigation_activity_qr_code_point,
            pointOfInterest
        )
        // Say aloud the point of interest message using TextToSpeech
        textToSpeech.speak(
            "There is the $pointOfInterest nearby you",
            TextToSpeech.QUEUE_FLUSH
        )
    }
    
    /**
     * Display NOT FOUND message on GUI.
     */
    private fun displayNotFound() {
        // Set the TextView text with the QR code not found message
        binding.textViewQrCode.text = navigationActivity.get()?.resources?.getString(
            R.string.navigation_activity_qr_code_not_found
        )
    }
}
