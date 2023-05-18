package it.unipi.dii.indoornavigatorassistant.dao

import android.content.Context
import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import it.unipi.dii.indoornavigatorassistant.R
import it.unipi.dii.indoornavigatorassistant.model.QrCodeJson
import it.unipi.dii.indoornavigatorassistant.util.Constants
import java.io.IOException

class QrCodeInfoProvider(context: Context) {

    private var qrInfoMap: MutableMap<String, String> = mutableMapOf()

    init {
        //ObjectMapper instance to read the json
        val mapper = jacksonObjectMapper()

        //load qr codes info file
        lateinit var jsonString: String
        try {
            jsonString = context.assets
                .open(context.resources.getString(R.string.qr_codes_info_file))
                .bufferedReader()
                .use { it.readText() }
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
        Log.d(Constants.LOG_TAG, "QrInfoProvider::init - json read: $jsonString")

        //read data from a JSON string
        val qrCodeInfo: List<QrCodeJson> = mapper.readValue(jsonString)
        Log.d(Constants.LOG_TAG, "QrInfoProvider::init - qr code info: $qrCodeInfo")

        //append json data into the Qr Code Info Map
        for (info in qrCodeInfo) {
            qrInfoMap[info.id] = info.pointOfInterests
        }
    }


    fun getQrCodeInfo(qrCodeId: String): String? {
        return qrInfoMap[qrCodeId]
    }

}