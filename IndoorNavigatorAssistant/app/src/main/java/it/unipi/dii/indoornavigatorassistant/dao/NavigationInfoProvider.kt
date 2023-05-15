package it.unipi.dii.indoornavigatorassistant.dao

import android.content.Context
import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.model.BLECurveInfo
import it.unipi.dii.indoornavigatorassistant.model.BLECurveJson
import it.unipi.dii.indoornavigatorassistant.model.BLERegionJson
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.io.IOException

class NavigationInfoProvider(context: Context) {
    
    private var bleRegionMap: MutableMap<String, List<String>> = mutableMapOf()
    private var bleCurves: MutableSet<String> = mutableSetOf()
    private var bleBeforeCurves: MutableMap<String, BLECurveInfo> = mutableMapOf()
    
    // ObjectMapper instance to read the json
    private val jsonObjectMapper = jacksonObjectMapper()

    /**
     * Initializer block (primary constructor) of NavigationInfoProvider class.
     * Reads a json (to do for all JSONs) and put the json data into the BLE Region Map
     */
    init {
        // Load BLE regions
        val bleRegionJsonList = loadListFromJsonFile<BLERegionJson>(
            context,
            context.resources.getString(R.string.ble_regions_file)
        )
        bleRegionJsonList.forEach { r -> bleRegionMap[r.id] = r.pointOfInterests }
        
        // Load BLE curves
        val bleCurveJsonList = loadListFromJsonFile<BLECurveJson>(
            context,
            "TODO"
        )
        bleCurveJsonList.forEach { x -> bleCurves.add(x.id) }

    }
    
    private fun <T> loadListFromJsonFile(context: Context, filename: String): List<T> {
        lateinit var jsonString: String
        try {
            jsonString = context.assets
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
    private fun computeBLERegionId(beacon1: String, beacon2: String): String {
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
     * @param beacon1 id of the first beacon
     * @param beacon2 id of the second beacon
     * @return list of strings which describes the points of interest if the region is valid, null otherwise
     */
    fun getBLERegionInfo(beacon1: String, beacon2: String): List<String>? {
        return bleRegionMap[computeBLERegionId(beacon1, beacon2)]
    }
    
}

/*
    val provider = NavigationInfoProvider(this)
    val poi1 = provider.getBLERegionInfo("SJG9", "U9LQ")
    val poi2 = provider.getBLERegionInfo("U9LQ", "SJG9")
    assert(poi1 == poi2)
    poi1?.forEach {
        Log.d(Constants.LOG_TAG, it)
    }
 */
