package it.unipi.dii.indoornavigatorassistant

/**
 * Class to manage the detection of new BLE regions based on a consecutive occurrence threshold.
 */
class BLERegionManager {

    private var currentRegion : String? = null
    private var lastRegionScanned : String? = null
    private var counter : Int = 0
    private val threshold : Int = 3

    /**
     * Checks if the provided region is a new region based on the consecutive occurrence threshold.
     *
     * @param regionScanned The region that was scanned.
     * @return True if the region is new, false otherwise.
     */
    fun isNewRegion (regionScanned : String) : Boolean {
        if (currentRegion == null) {
            if (regionScanned == lastRegionScanned || lastRegionScanned == null) {
                counter++
            } else {
                counter = 0
            }
            lastRegionScanned = regionScanned
            return if (counter == threshold) {
                currentRegion = regionScanned
                counter = 0
                true
            } else {
                false
            }
        } else {
            if (regionScanned == lastRegionScanned) {
                counter++
                if (counter == threshold && regionScanned != currentRegion) {
                    currentRegion = regionScanned
                    counter = 0
                    return true
                }
            }
        }
        return false
    }
}