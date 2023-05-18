package it.unipi.dii.indoornavigatorassistant.dao

import android.content.Context
import com.fasterxml.jackson.core.type.TypeReference
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.model.QrCodeJson
import it.unipi.dii.indoornavigatorassistant.util.JsonParser

class QrCodeInfoProvider(context: Context) {

    private var qrInfoMap: MutableMap<String, String> = mutableMapOf()

    init {
        // Load data of QR Code Info
        val qrCodeInfoList = JsonParser.loadFromJsonAsset(
            context.assets,
            context.resources.getString(R.string.qr_codes_info_file),
            object: TypeReference<List<QrCodeJson>>(){}
        )
        qrCodeInfoList.forEach { x -> qrInfoMap[x.id] = x.pointOfInterest }
    }


    fun getQrCodeInfo(qrCodeId: String): String? {
        return qrInfoMap[qrCodeId]
    }

}