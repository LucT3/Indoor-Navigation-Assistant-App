package it.unipi.dii.indoornavigatorassistant.scanners

/**
 * Class to manage the detection of new BLE regions based on a consecutive occurrence threshold.
 */
class BeaconState {
    // Region currently show to user
    private var currentRegion: String? = null
    
    // Last region scanned
    private var lastRegionScanned: String? = null
    
    // How many times the lastRegionScanned has been scanned in consecutive times
    private var counter: Int = 0
    
    companion object {
        // How many times a region must be scanned before consider it as the "current region"
        private const val THRESHOLD: Int = 2
    }
    
    
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
            
            if (counter == THRESHOLD) {
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
