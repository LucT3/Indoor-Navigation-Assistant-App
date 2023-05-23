package it.unipi.dii.indoornavigatorassistant.model

data class QrCodeJson(val id: String, val type: QrCodeType, val pointOfInterest: String)

data class QrCodeInfo(val type: QrCodeType, val pointOfInterest: String)

enum class QrCodeType {
    FLOOR, DOOR
}
