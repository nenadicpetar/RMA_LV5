package com.example.whereami.data

import android.app.IntentService
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.os.ResultReceiver
import java.io.IOException
import java.util.*

class AddressIntentService: IntentService("AddressService")
{
    private var mReceiver: ResultReceiver? = null

    override fun onHandleIntent(intent: Intent?) {
        mReceiver = intent?.getParcelableExtra("CURRENT_LOCATION_RECEIVER")
        val geoCoder = Geocoder(this, Locale.getDefault())
        val location = intent?.getParcelableExtra("LOCATION_DATA") ?: LocationModel(0.0, 0.0)

        try {
            val address = geoCoder.getFromLocation(location.latitude, location.longitude, 1) [0]
            deliverResult(1, address.countryName + "|" + address.locality + "|" + address.thoroughfare)
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun deliverResult(resultCode: Int, message: String) {
        val bundle = Bundle()
        bundle.putString("ADDRESS_DATA", message)
        mReceiver?.send(resultCode, bundle)
    }
}