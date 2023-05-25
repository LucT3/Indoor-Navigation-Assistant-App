package it.unipi.dii.indoornavigationassistant.scanners

import android.speech.tts.TextToSpeech
import android.util.Log
import it.unipi.dii.indoornavigationassistant.R
import it.unipi.dii.indoornavigationassistant.activities.NavigationActivity
import it.unipi.dii.indoornavigationassistant.dao.QrCodeInfoProvider
import it.unipi.dii.indoornavigationassistant.databinding.ActivityNavigationBinding
import it.unipi.dii.indoornavigationassistant.model.QrCodeInfo
import it.unipi.dii.indoornavigationassistant.model.QrCodeType
import it.unipi.dii.indoornavigationassistant.speech.TextToSpeechContainer
import it.unipi.dii.indoornavigationassistant.util.Constants
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
        private const val FLOOR_PERIOD_IN_MILLISECONDS = 10 * 10000
        private const val DOOR_PERIOD_IN_MILLISECONDS = 4 * 1000
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
     * @param qrCodeId id of the QR code
     */
    fun notifyQrCode(qrCodeId: String?) {
        // No QR code?
        if (qrCodeId == null) {
            // Check if a GUI refresh is required
            if (isRefreshNeeded(FLOOR_PERIOD_IN_MILLISECONDS)) {
                lastQrCode = ""
                lastTimestamp = LocalDateTime.now()
                displayNotFound()
            }
            return
        }
        
        // Get info about QR code
        val info = qrCodeInfoProvider.getQrCodeInfo(qrCodeId)
        if (info == null) {
            // Qr code invalid
            displayNotFound()
            return
        }
        
        // New QR code?
        if (qrCodeId != lastQrCode) {
            lastQrCode = qrCodeId
            lastTimestamp = LocalDateTime.now()
            displayQrCode(info)
            return
        }
        
        // Refresh required?
        val refreshPeriod = when (info.type) {
            QrCodeType.DOOR -> DOOR_PERIOD_IN_MILLISECONDS
            QrCodeType.FLOOR -> FLOOR_PERIOD_IN_MILLISECONDS
        }
        
        if (isRefreshNeeded(refreshPeriod)) {
            lastTimestamp = LocalDateTime.now()
            displayQrCode(info)
        }
    }
    
    
    /**
     * Check if enough time has elapsed to refresh the current QR code.
     *
     * @param period
     *
     * @return true if refresh is needed, false otherwise
     */
    private fun isRefreshNeeded(period: Int): Boolean {
        val duration = Duration.between(lastTimestamp, LocalDateTime.now())
        return duration.toMillis() >= period
    }
    
    
    /**
     * Display the data concerning the current QR code.
     */
    private fun displayQrCode(qrCodeInfo: QrCodeInfo) {
        Log.d(
            Constants.LOG_TAG,
            "QRCodeState::displayQrCode - QR code id: $lastQrCode; " +
                    "point of interest: ${qrCodeInfo.pointOfInterest}"
        )
        
        // Display the point of interest information in the TextView and speak it out loud
        // Set the TextView text with the formatted point of interest message
        binding.textViewQrCode.text = navigationActivity.get()?.resources?.getString(
            R.string.navigation_activity_qr_code_point,
            qrCodeInfo.pointOfInterest
        )
        
        val text = when (qrCodeInfo.type) {
            QrCodeType.FLOOR -> "${qrCodeInfo.pointOfInterest} nearby you"
            QrCodeType.DOOR -> "${qrCodeInfo.pointOfInterest} ahead"
        }
        // Say aloud the point of interest message using TextToSpeech
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH)
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
