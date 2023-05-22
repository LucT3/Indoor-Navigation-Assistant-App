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

class QRCodeState (private val navigationActivity: WeakReference<NavigationActivity>,
                   private val binding: ActivityNavigationBinding) {
    
    companion object {
        private const val PERIOD_IN_MILLISECONDS = 10000
    }
    
    // State of QR code scanner
    private var lastQrCode: String = ""
    private var lastTimestamp: LocalDateTime? = null
    
    // Helper objects
    private val qrCodeInfoProvider = QrCodeInfoProvider.getInstance(navigationActivity.get()!!)
    private val textToSpeech = TextToSpeechContainer(navigationActivity.get()!!)
    
    /**
     * TODO
     *
     * @param qrCode
     */
    fun notifyQrCode(qrCode: String?) {
        // New QR code?
        if (qrCode != null && qrCode != lastQrCode) {
            lastQrCode = qrCode
            lastTimestamp = LocalDateTime.now()
            displayCurrentQrCode()
            return
        }
        
        // Did a whole period of time (10s) elapsed?
        if (isRefreshNeeded()) {
            lastTimestamp = LocalDateTime.now()
            if (qrCode != null) {
                displayCurrentQrCode()
            }
            else {
                // Clear interface
                clear()
            }
        }
    }
    
    private fun isRefreshNeeded(): Boolean {
        val duration = Duration.between(lastTimestamp, LocalDateTime.now())
        return duration.toMillis() >= PERIOD_IN_MILLISECONDS
    }
    
    /**
     * TODO
     */
    private fun displayCurrentQrCode() {
        // Get the point of interest using the QR code ID
        val pointOfInterest = qrCodeInfoProvider.getQrCodeInfo(lastQrCode)
    
        // Log the QR code ID and the corresponding point of interest
        Log.d(Constants.LOG_TAG, "QRCodeState::displayCurrentQrCode - QR Code Id: $lastQrCode")
        Log.d(Constants.LOG_TAG, "QRCodeState::displayCurrentQrCode - Point of interest: $pointOfInterest")
    
        // Display the point of interest information in the TextView and speak it out loud
        if (pointOfInterest != null) {
            // Set the TextView text with the formatted point of interest message
            binding.textViewQrCode.text = navigationActivity.get()?.resources?.getString(
                R.string.navigation_activity_qr_code_point,
                pointOfInterest
            )
            // Speak the point of interest message using TextToSpeech
            textToSpeech.speak(
                "There is the $pointOfInterest nearby you",
                TextToSpeech.QUEUE_FLUSH
            )
        } else {
            clear()
        }
    }
    
    /**
     * TODO
     */
    private fun clear() {
        // Set the TextView text with the QR code not found message
        binding.textViewQrCode.text = navigationActivity.get()?.resources?.getString(
            R.string.navigation_activity_qr_code_not_found
        )
    }
}