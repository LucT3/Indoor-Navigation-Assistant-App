package it.unipi.dii.indoornavigatorassistant.dao

import android.content.Context
import com.fasterxml.jackson.core.type.TypeReference
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.model.QrCodeInfo
import it.unipi.dii.indoornavigatorassistant.model.QrCodeJson
import it.unipi.dii.indoornavigatorassistant.util.JsonParser

class QrCodeInfoProvider private constructor(context: Context) {

    private var qrInfoMap: MutableMap<String, QrCodeInfo> = mutableMapOf()
    
    companion object {
        private var instance: QrCodeInfoProvider? = null
        
        fun getInstance(context: Context): QrCodeInfoProvider {
            if (instance == null) {
                instance = QrCodeInfoProvider(context)
            }
            return instance as QrCodeInfoProvider
        }
    }

    init {
        // Load data of QR Code Info
        val qrCodeInfoList = JsonParser.loadFromJsonAsset(
            context.assets,
            context.resources.getString(R.string.qr_codes_info_file),
            object: TypeReference<List<QrCodeJson>>(){}
        )
        qrCodeInfoList.forEach { x -> qrInfoMap[x.id] = QrCodeInfo(x.type, x.pointOfInterest) }
    }
    
    /**
     * Get the point of interest related to a QR code.
     *
     * @param qrCodeId id of the QR code
     * @return name of the point of interest if the QR code is valid, null otherwise
     */
    fun getQrCodeInfo(qrCodeId: String): QrCodeInfo? {
        return qrInfoMap[qrCodeId]
    }

}
