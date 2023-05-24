package it.unipi.dii.indoornavigatorassistant.dao

import android.content.Context
import com.fasterxml.jackson.core.type.TypeReference
import com.kontakt.sdk.android.common.profile.IBeaconDevice
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.model.BLEAreaBeforeCurveJson
import it.unipi.dii.indoornavigatorassistant.model.BLECurveInfo
import it.unipi.dii.indoornavigatorassistant.model.BLECurveJson
import it.unipi.dii.indoornavigatorassistant.model.BLERegionInfo
import it.unipi.dii.indoornavigatorassistant.model.BLERegionJson
import it.unipi.dii.indoornavigatorassistant.util.JsonParser

/**
 * Class which retrieve the data about the state of the beacons inside the
 * building environment and provides them through public methods.
 *
 * It must be instantiated throw the [getInstance] method.
 */
class BeaconInfoProvider private constructor(context: Context) {
    // Structures which hold data about beacon environment configuration
    private var bleRegions: MutableMap<String, BLERegionInfo> = mutableMapOf()
    private var bleCurves: MutableMap<String, BLECurveInfo> = mutableMapOf()
    private var bleAreasBeforeCurves: MutableSet<String> = mutableSetOf()
    
    companion object {
        // Singleton instance of the class
        private var instance: BeaconInfoProvider? = null
    
        /**
         * Get a singleton instance of the BeaconInfoProvider
         *
         * @param context context of the application environment
         * @return an instance of BeaconInfoProvider
         */
        fun getInstance(context: Context): BeaconInfoProvider {
            if (instance == null) {
                instance = BeaconInfoProvider(context)
            }
            return instance as BeaconInfoProvider
        }
    }
    
    /**
     * Primary constructor of the class.
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
        bleCurveJsonList.forEach { x -> bleCurves[x.id] = BLECurveInfo(x.preCurveRight, x.preCurveLeft) }
        
        // Load data of areas before curves
        val bleAreaBeforeCurveJsonList = JsonParser.loadFromJsonAsset(
            context.assets,
            context.resources.getString(R.string.ble_pre_curves_file),
            object : TypeReference<List<BLEAreaBeforeCurveJson>>() {}
        )
        bleAreaBeforeCurveJsonList.forEach { x ->
            bleAreasBeforeCurves.add(x.id)
        }
    }
    
    
    /**
     * Check if the obtained region is present in the bleRegions Map
     *
     * @param beaconsList list of the beacons discovered ordered by descending rssi
     * @return the id of the BLE region
     */
    fun checkBLERegionId(beaconsList: List<IBeaconDevice>) : String? {
        // Compute the regionId using the uniqueIds of the first and second beacons
        var regionId = computeBLERegionId(beaconsList[0].uniqueId, beaconsList[1].uniqueId)
        if (bleRegions.containsKey(regionId)) {
            return regionId
        }
        // Compute the regionId using the uniqueIds of the first and third beacons
        regionId = computeBLERegionId(beaconsList[0].uniqueId, beaconsList[2].uniqueId)
        if (bleRegions.containsKey(regionId)) {
            return regionId
        }
        // Compute the regionId using the uniqueIds of the second and third beacons
        regionId = computeBLERegionId(beaconsList[1].uniqueId, beaconsList[2].uniqueId)
        if (bleRegions.containsKey(regionId)) {
            return regionId
        }
        return null
    }

    /**
     * Compute the `id` of a **BLE region** from the `id` of the two corresponding beacons.
     *
     * The `region id` is computed by concatenating the `id` of the beacons in lexicographic order.
     *
     * @param beacon1 id of the first beacon
     * @param beacon2 id of the first beacon
     * @return the id of the BLE region
     */
    private fun computeBLERegionId (beacon1 : String?, beacon2: String?) : String {
        if (beacon1 == null || beacon2 == null) {
            return ""
        }
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

    fun isPreCurve (regionId: String): Boolean {
        return bleAreasBeforeCurves.contains(regionId)
    }
    
    /**
     * Given the id of a BLE beacons pair which delimits an area next to a curve,
     * get information about the curve (id + direction).
     *
     * @param regionId id of the region
     * @return information about the curve if the selected region is just before a curve,
     *         null otherwise
     */
    fun getCurveInfo(regionId: String?): BLECurveInfo? {
        return bleCurves[regionId]
    }
    
}
