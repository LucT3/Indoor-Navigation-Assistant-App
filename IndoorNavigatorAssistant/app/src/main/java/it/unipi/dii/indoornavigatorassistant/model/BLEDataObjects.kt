package it.unipi.dii.indoornavigatorassistant.model

data class BLERegionJson(val id: String, val pointOfInterests: List<String>)

data class BLECurveJson(val id: String)

data class BLEAreaBeforeCurveJson(val id: String, val curve: String, val direction: Boolean)

data class BLECurveInfo(val curve: String, val direction: Boolean)
