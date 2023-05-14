package it.unipi.dii.indoornavigatorassistant.dao

import android.content.Context
import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import it.unipi.dii.indoornavigatorassistant.model.BLERegion
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.io.IOException

class NavigationInfoProvider(context: Context) {

    private var bleRegionMap: MutableMap<String, List<String>> = mutableMapOf()

    init {
        val mapper = jacksonObjectMapper()

        lateinit var jsonString: String
        try {
            jsonString = context.assets.open("ble-regions.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }

        Log.d(Constants.LOG_TAG, "json: $jsonString")

        val bleRegions: List<BLERegion> = mapper.readValue(jsonString)
        Log.d(Constants.LOG_TAG, "ble regions: $bleRegions")

        for (region in bleRegions) {
            bleRegionMap[region.id] = region.pointOfInterests
        }


    }

    private fun getBLERegionId(beacon1: String, beacon2: String): String {
        return if (beacon1 <= beacon2) {
            beacon1 + beacon2
        } else {
            beacon2 + beacon1
        }
    }

    fun getBLERegionInfo(beacon1: String, beacon2: String): List<String>? {
        return bleRegionMap[getBLERegionId(beacon1, beacon2)]
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
