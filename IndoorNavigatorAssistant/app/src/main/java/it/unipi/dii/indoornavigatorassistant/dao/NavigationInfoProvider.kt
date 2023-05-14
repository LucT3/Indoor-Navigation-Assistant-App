package it.unipi.dii.indoornavigatorassistant.dao

import android.content.Context
import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.model.BLERegion
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.io.IOException

class NavigationInfoProvider(context: Context) {
    
    private var bleRegionMap: MutableMap<String, List<String>> = mutableMapOf()

    /**
     * Initializer block (primary constructor) of NavigationInfoProvider class.
     * Reads a json (to do for all JSONs) and put the json data into the BLE Region Map
     */
    init {
        //ObjectMapper instance to read the json
        val mapper = jacksonObjectMapper()

        //load json file TODO customize for all JSONs or for a specific json
        lateinit var jsonString: String
        try {
            jsonString = context.assets
                .open(context.resources.getString(R.string.ble_regions_file))
                .bufferedReader()
                .use { it.readText() }
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
        Log.d(Constants.LOG_TAG, "NavigationInfoProvider::init - json read: $jsonString")

        //read data from a JSON string
        val bleRegions: List<BLERegion> = mapper.readValue(jsonString)
        Log.d(Constants.LOG_TAG, "NavigationInfoProvider::init - ble regions: $bleRegions")

        //append json data into the BLE region Map
        for (region in bleRegions) {
            bleRegionMap[region.id] = region.pointOfInterests
        }

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
