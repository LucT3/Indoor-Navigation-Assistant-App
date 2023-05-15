package it.unipi.dii.indoornavigatorassistant

class BLERegionManager {

    private var currentRegion : String? = null
    private var lastRegionScanned : String? = null
    private var counter : Int = 0
    private val threshold : Int = 3

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
            if (regionScanned == currentRegion) {
                counter++
                lastRegionScanned = regionScanned
                if (counter == threshold) {
                    currentRegion = regionScanned
                    counter = 0
                    return true
                }
            }
        }
        return false
    }
}