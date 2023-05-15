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
     * @param regionScanned The last region that was detected.
     * @return True if the region is new, false otherwise.
     */
    fun isNewRegion(regionScanned: String): Boolean {
        if (currentRegion != regionScanned) {
            if (regionScanned != lastRegionScanned) {
                counter = 1
            } else {
                counter++
            }

            if (counter == threshold) {
                currentRegion = regionScanned
                counter = 0
                lastRegionScanned = regionScanned
                return true
            }
        } else {
            counter = 0
        }

        lastRegionScanned = regionScanned
        return false
    }
}