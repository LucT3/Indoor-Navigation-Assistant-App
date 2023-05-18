package it.unipi.dii.indoornavigatorassistant.dao

import android.content.Context
import com.fasterxml.jackson.core.type.TypeReference
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.model.BLEAreaBeforeCurveJson
import it.unipi.dii.indoornavigatorassistant.model.BLECurveInfo
import it.unipi.dii.indoornavigatorassistant.model.BLECurveJson
import it.unipi.dii.indoornavigatorassistant.model.BLERegionJson
import it.unipi.dii.indoornavigatorassistant.util.JsonParser

class NavigationInfoProvider(context: Context) {
    
    private var bleRegions: MutableMap<String, List<String>> = mutableMapOf()
    private var bleCurves: MutableSet<String> = mutableSetOf()
    private var bleAreasBeforeCurves: MutableMap<String, BLECurveInfo> = mutableMapOf()
    
    /**
     * Initializer block (primary constructor) of NavigationInfoProvider class.
     * Reads a json (to do for all JSONs) and put the json data into the BLE Region Map
     */
    init {
        // Load data of BLE regions
        val bleRegionJsonList = JsonParser.loadFromJsonAsset(
            context.assets,
            context.resources.getString(R.string.ble_regions_file),
            object: TypeReference<List<BLERegionJson>>(){}
        )
        bleRegionJsonList.forEach { x -> bleRegions[x.id] = x.pointOfInterests }
        
        // Load data of BLE curves
        val bleCurveJsonList = JsonParser.loadFromJsonAsset(
            context.assets,
            context.resources.getString(R.string.ble_curves_file),
            object: TypeReference<List<BLECurveJson>>(){}
        )
        bleCurveJsonList.forEach { x -> bleCurves.add(x.id) }

        // Load data of areas before curves
        val bleAreaBeforeCurveJsonList = JsonParser.loadFromJsonAsset(
            context.assets,
            context.resources.getString(R.string.ble_pre_curves_file),
            object: TypeReference<List<BLEAreaBeforeCurveJson>>(){}
        )
        bleAreaBeforeCurveJsonList.forEach { x ->
            bleAreasBeforeCurves[x.id] = BLECurveInfo(x.curve, x.isTurnRight)
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
