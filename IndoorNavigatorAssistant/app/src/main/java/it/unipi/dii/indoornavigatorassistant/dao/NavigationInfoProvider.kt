package it.unipi.dii.indoornavigatorassistant.dao

import android.content.Context
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import it.unipi.dii.indoornavigatorassistant.model.BLERegion
import java.io.IOException

class NavigationInfoProvider(context: Context) {

    private lateinit var bleRegionMap: MutableMap<String, List<String>>

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

        val bleRegions: List<BLERegion> = mapper.readValue(jsonString)
        for (region in bleRegions) {
            bleRegionMap.put(region.id, region.pointOfInterests)
        }


    }

    fun getBLERegionInfo(beacon1: String, beacon2: String): Array<String> {
        return null;
    }
}
