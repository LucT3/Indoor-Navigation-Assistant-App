package it.unipi.dii.indoornavigatorassistant.util

import android.content.res.AssetManager
import android.util.Log
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.IOException

object JsonParser {
    
    private val jsonObjectMapper = jacksonObjectMapper()
    
    /**
     * TODO
     *
     * @param T
     * @param assets
     * @param filename
     * @param typeReference
     * @return
     */
    fun <T> loadFromJsonAsset(assets: AssetManager,
                              filename: String,
                              typeReference: TypeReference<T>): T
    {
        // Read from JSON file
        lateinit var jsonString: String
        try {
            jsonString = assets
                .open(filename)
                .bufferedReader()
                .use { it.readText() }
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
        Log.d(Constants.LOG_TAG, "JsonParser::loadFromJsonAsset - json read: $jsonString")
        
        // Convert JSON string to object
        val tList = jsonObjectMapper.readValue(jsonString, typeReference)
        Log.d(Constants.LOG_TAG, "JsonParser::loadFromJsonAsset - converted object: $tList")
        return tList
    }
    
}
