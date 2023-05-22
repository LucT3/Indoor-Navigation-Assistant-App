package it.unipi.dii.indoornavigatorassistant.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.util.Locale

class TextToSpeechContainer(context: Context) : TextToSpeech.OnInitListener {
    
    private var textToSpeech: TextToSpeech = TextToSpeech(context, this)
    
    /**
     * Called to signal the completion of the TextToSpeech engine initialization.
     *
     * @param status [TextToSpeech.SUCCESS] or [TextToSpeech.ERROR].
     */
    override fun onInit(status: Int) {
        textToSpeech.setSpeechRate(0.7F)
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(Constants.LOG_TAG, "The language is not supported!")
            }
        }
    }
    
    fun stop() {
        textToSpeech.stop()
    }
    
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
