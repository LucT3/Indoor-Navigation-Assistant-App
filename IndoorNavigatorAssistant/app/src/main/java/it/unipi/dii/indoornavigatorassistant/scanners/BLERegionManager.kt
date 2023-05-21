package it.unipi.dii.indoornavigatorassistant.scanners

import it.unipi.dii.indoornavigatorassistant.R

/**
 * Class to manage the detection of new BLE regions based on a consecutive occurrence threshold.
 */
class BLERegionManager {

    private var currentRegion : String? = null
    private var lastRegionScanned : String? = null
    private var counter : Int = 0
    private val threshold : Int = 2

    /**
     * Checks if the provided region is a new region based on the consecutive occurrence threshold.
     *
     * @param regionScanned The last region that was detected.
     * @return True if the region is new, false otherwise.
     */
    fun isNewRegion(regionScanned: String): Boolean {
        if (currentRegion != regionScanned) {
            if (regionScanned != lastRegionScanned) {
                // Reset the counter to 1 (new consecutive sequence)
                counter = 1
            } else {
                // Increment counter (region scanned is the same)
                counter++
            }

            if (counter == threshold) {
                // The region has changed consecutively for the specified number of times.
                // Update the currentRegion, reset the counter, and update the lastRegionScanned.
                currentRegion = regionScanned
                counter = 0
                lastRegionScanned = regionScanned
                return true
            }
        } else {
            // If the regionScanned is the same as the currentRegion,
            // reset the counter as the consecutive sequence is broken.
            counter = 0
        }

        // Update the lastRegionScanned
        lastRegionScanned = regionScanned
        return false
    }

}