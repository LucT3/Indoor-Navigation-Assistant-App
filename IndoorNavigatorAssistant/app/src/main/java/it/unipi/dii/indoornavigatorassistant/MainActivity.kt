package it.unipi.dii.indoornavigatorassistant

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.kontakt.sdk.android.common.KontaktSDK


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Bluetooth API
        KontaktSDK.initialize(this)
        setContentView(R.layout.activity_main)
        Log.i(this.localClassName, "ciao")
    }

}
