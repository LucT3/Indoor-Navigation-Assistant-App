package it.unipi.dii.indoornavigatorassistant.util

import android.content.res.AssetManager
import android.util.Log
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.IOException

object JsonParser {
    
    private val jsonObjectMapper = jacksonObjectMapper()
    
    /**
     * Load an serialized object from a JSON file.
     *
     * @param T class of the object
     * @param assets application asset manager
     * @param filename name of the JSON file
     * @param typeReference specify type information of the class generics. It's required by
     *                      the parsing process.
     * @return an instance of the class T
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
