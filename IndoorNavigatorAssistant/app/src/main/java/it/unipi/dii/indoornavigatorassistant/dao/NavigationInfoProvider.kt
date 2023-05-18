package it.unipi.dii.indoornavigatorassistant.dao

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.model.BLEAreaBeforeCurveJson
import it.unipi.dii.indoornavigatorassistant.model.BLECurveInfo
import it.unipi.dii.indoornavigatorassistant.model.BLECurveJson
import it.unipi.dii.indoornavigatorassistant.model.BLERegionJson
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.io.IOException

class NavigationInfoProvider(context: Context) {
    
    private var bleRegions: MutableMap<String, List<String>> = mutableMapOf()
    private var bleCurves: MutableSet<String> = mutableSetOf()
    private var bleAreasBeforeCurves: MutableMap<String, BLECurveInfo> = mutableMapOf()
    
    // ObjectMapper instance to read the json
    private val jsonObjectMapper = jacksonObjectMapper()
    
    /**
     * Initializer block (primary constructor) of NavigationInfoProvider class.
     * Reads a json (to do for all JSONs) and put the json data into the BLE Region Map
     */
    init {
        // Load data of BLE regions
        val bleRegionJsonList = loadListFromJsonFile<BLERegionJson>(
            context.assets,
            context.resources.getString(R.string.ble_regions_file)
        )
        bleRegionJsonList.forEach { x -> bleRegions[x.id] = x.pointOfInterests }
        
        // Load data of BLE curves
        val bleCurveJsonList = loadListFromJsonFile<BLECurveJson>(
            context.assets,
            context.resources.getString(R.string.ble_curves_file)
        )
        bleCurveJsonList.forEach { x -> bleCurves.add(x.id) }
        
        // Load data of areas before curves
        val bleAreaBeforeCurveJsonList = loadListFromJsonFile<BLEAreaBeforeCurveJson>(
            context.assets,
            context.resources.getString(R.string.ble_pre_curves_file)
        )
        bleAreaBeforeCurveJsonList.forEach { x ->
            bleAreasBeforeCurves[x.id] = BLECurveInfo(x.curve, x.direction)
        }
    }
    
    private fun <T> loadListFromJsonFile(assets: AssetManager, filename: String): List<T> {
        lateinit var jsonString: String
        try {
            jsonString = assets
                .open(filename)
                .bufferedReader()
                .use { it.readText() }
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
        Log.d(Constants.LOG_TAG, "NavigationInfoProvider::loadFromJson - json read: $jsonString")
        
        //read data from a JSON string
        val tList: List<T> = jsonObjectMapper.readValue(jsonString)
        Log.d(Constants.LOG_TAG, "NavigationInfoProvider::loadFromJson - ble regions: $tList")
        return tList
    }
    
    
    /**
     * Compute the `id` of a **BLE region** from the `id` of the two corresponding beacons.
     *
     * The `region id` is computed by concatenating the `id` of the beacons in lexicographic order.
     *
     * @param beacon1 id of the first beacon
     * @param beacon2 id of the second beacon
     * @return the id of the BLE region
     */
    fun computeBLERegionId(beacon1: String, beacon2: String): String {
        return if (beacon1 <= beacon2) {
            beacon1 + beacon2
        } else {
            beacon2 + beacon1
        }

    }
    
    /**
     * Get the list of points of interest within a BLE region,
     * given the corresponding BLE beacons.
     *
     * @param regionId id of the region
     * @return list of strings which describes the points of interest if the region is valid, null otherwise
     */
    fun getBLERegionInfo(regionId: String): List<String>? {
        return bleRegions[regionId]
    }
    
    // TODO get info about curves and area before curves
    
}
