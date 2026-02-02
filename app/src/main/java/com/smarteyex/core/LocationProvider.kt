package com.smarteyex.core

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.content.Context

object LocationProvider {

    data class SimpleLocation(
        val lat: Double,
        val lon: Double
    )

    @SuppressLint("MissingPermission")
    fun getLastKnown(): SimpleLocation? {
        val context = AppContext.app
        val manager =
            context.getSystemService(
                Context.LOCATION_SERVICE
            ) as LocationManager

        val providers = manager.getProviders(true)
        for (p in providers) {
            val loc: Location? =
                manager.getLastKnownLocation(p)
            if (loc != null) {
                return SimpleLocation(
                    loc.latitude,
                    loc.longitude
                )
            }
        }
        return null
    }
}
