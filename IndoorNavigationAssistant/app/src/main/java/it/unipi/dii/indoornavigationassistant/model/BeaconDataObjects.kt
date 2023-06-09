package it.unipi.dii.indoornavigationassistant.model

import java.io.Serializable

// Data class for JSON documents which describe regions of BLE beacons
data class BLERegionJson (val id: String, val name: String, val pointsOfInterest: List<String>): Serializable
// Data class for JSON documents which describe curves in a assisted path
data class BLECurveJson(val id: String, val preCurveRight: String, val preCurveLeft: String): Serializable
// Data class for JSON documents which describe areas before curves in a assisted path
data class BLEAreaBeforeCurveJson(val id: String): Serializable


// Data class which stores information related to a curve
data class BLERegionInfo(val name: String, val pointsOfInterest: List<String>): Serializable
data class BLECurveInfo(val preCurveRight: String, val preCurveLeft: String)
