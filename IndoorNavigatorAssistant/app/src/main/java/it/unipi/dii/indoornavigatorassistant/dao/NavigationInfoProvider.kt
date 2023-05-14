package it.unipi.dii.indoornavigatorassistant.dao

object NavigationInfoProvider {
    
    private lateinit var bleRegionMap: MutableMap<String, Array<String>>
    
    fun init() {
        // parse JSON object
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
