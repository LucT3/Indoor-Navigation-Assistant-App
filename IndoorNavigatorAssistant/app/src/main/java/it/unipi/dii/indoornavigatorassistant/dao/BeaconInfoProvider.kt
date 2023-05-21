package it.unipi.dii.indoornavigatorassistant.dao

import android.content.Context
import com.fasterxml.jackson.core.type.TypeReference
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.model.BLEAreaBeforeCurveJson
import it.unipi.dii.indoornavigatorassistant.model.BLECurveInfo
import it.unipi.dii.indoornavigatorassistant.model.BLECurveJson
import it.unipi.dii.indoornavigatorassistant.model.BLERegionInfo
import it.unipi.dii.indoornavigatorassistant.model.BLERegionJson
import it.unipi.dii.indoornavigatorassistant.util.JsonParser

class BeaconInfoProvider private constructor(context: Context) {
    
    private var bleRegions: MutableMap<String, BLERegionInfo> = mutableMapOf()
    private var bleCurves: MutableSet<String> = mutableSetOf()
    private var bleAreasBeforeCurves: MutableMap<String, BLECurveInfo> = mutableMapOf()
    
    companion object {
        private var instance: BeaconInfoProvider? = null
        
        fun getInstance(context: Context): BeaconInfoProvider {
            if (instance == null) {
                instance = BeaconInfoProvider(context)
            }
            return instance as BeaconInfoProvider
        }
    }
    
    /**
     * Initializer block (primary constructor) of NavigationInfoProvider class.
     * Reads a json (to do for all JSONs) and put the json data into the BLE Region Map
     */
    init {
        // Load data of BLE regions
        val bleRegionJsonList = JsonParser.loadFromJsonAsset(
            context.assets,
            context.resources.getString(R.string.ble_regions_file),
            object : TypeReference<List<BLERegionJson>>() {}
        )
        bleRegionJsonList.forEach { x ->
            bleRegions[x.id] = BLERegionInfo(x.name, x.pointsOfInterest)
        }
        
        // Load data of BLE curves
        val bleCurveJsonList = JsonParser.loadFromJsonAsset(
            context.assets,
            context.resources.getString(R.string.ble_curves_file),
            object : TypeReference<List<BLECurveJson>>() {}
        )
        bleCurveJsonList.forEach { x -> bleCurves.add(x.id) }
        
        // Load data of areas before curves
        val bleAreaBeforeCurveJsonList = JsonParser.loadFromJsonAsset(
            context.assets,
            context.resources.getString(R.string.ble_pre_curves_file),
            object : TypeReference<List<BLEAreaBeforeCurveJson>>() {}
        )
        bleAreaBeforeCurveJsonList.forEach { x ->
            bleAreasBeforeCurves[x.id] = BLECurveInfo(x.curve, x.direction)
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
    fun getBLERegionInfo(regionId: String): BLERegionInfo? {
        return bleRegions[regionId]
    }
    
    
    /**
     * Check if a pair of BLE beacons delimits a curve.
     *
     * @param regionId id of the region
     * @return true if the BLE region is a curve, false otherwise
     */
    fun isCurve(regionId: String): Boolean {
        return bleCurves.contains(regionId)
    }
    
    /**
     * Given the id of a BLE beacons pair which delimits an area next to a curve,
     * get information about the curve (id + direction).
     *
     * @param regionId id of the region
     * @return information about the curve if the selected region is just before a curve,
     *         null otherwise
     */
    fun getAreaBeforeCurveInfo(regionId: String): String? {
        bleAreasBeforeCurves.forEach { item ->
            val curveInfo = item.value
            if (curveInfo.curve == regionId) {
                return curveInfo.direction.toString()
            }
        }
        return null
    }
    
}
