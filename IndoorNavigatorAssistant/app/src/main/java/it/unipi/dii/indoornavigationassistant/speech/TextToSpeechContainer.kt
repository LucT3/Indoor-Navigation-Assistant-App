package it.unipi.dii.indoornavigationassistant.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.AndroidRuntimeException
import android.util.Log
import it.unipi.dii.indoornavigationassistant.util.Constants
import java.util.Locale

/**
 * Wrapper for [TextToSpeech] object.
 *
 * @param context application context
 */
class TextToSpeechContainer(context: Context) : TextToSpeech.OnInitListener {
    
    private var textToSpeech: TextToSpeech = TextToSpeech(context, this)
    
    /**
     * Called to signal the completion of the TextToSpeech engine initialization.
     *
     * @param status [TextToSpeech.SUCCESS] or [TextToSpeech.ERROR].
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set speech rate
            var result = textToSpeech.setSpeechRate(0.8F)
            if (result == TextToSpeech.ERROR) {
                Log.e(Constants.LOG_TAG, "TextToSpeechContainer::onInit - Failed to set the speech rate!")
                throw AndroidRuntimeException("Failed to set the speech rate!")
            }
            
            // Set language
            result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(Constants.LOG_TAG, "TextToSpeechContainer::onInit - Language ${Locale.US} is not supported!")
                throw AndroidRuntimeException("Language ${Locale.US} is not supported!")
            }
        }
    }
    
    
    /**
     * Interrupts the current utterance (whether played or rendered to file)
     * and discards other utterances in the queue.
     *
     * @return [TextToSpeech.ERROR] or [TextToSpeech.SUCCESS]
     */
    fun stop(): Int {
        return textToSpeech.stop()
    }
    
    
    /**
     * Releases the resources used by the TextToSpeech engine.
     * It is good practice for instance to call this method in the
     * onDestroy() method of an Activity so the TextToSpeech engine
     * can be cleanly stopped.
     */
    fun shutdown() {
        textToSpeech.shutdown()
    }
    
    
    /**
     * Speaks the text using the specified queuing strategy.
     * This method is asynchronous, i.e. the method just adds the request
     * to the queue of TTS requests and then returns.
     *
     * @param text The string of text to be spoken. No longer than [TextToSpeech.getMaxSpeechInputLength] characters.
     * @param queueMode The queuing strategy to use, [TextToSpeech.QUEUE_ADD] or [TextToSpeech.QUEUE_FLUSH].
     */
    fun speak(text: String, queueMode: Int) {
        textToSpeech.speak(text, queueMode, null, "")
    }
    
}
