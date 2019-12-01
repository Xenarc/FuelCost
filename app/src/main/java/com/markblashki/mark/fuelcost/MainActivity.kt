package com.markblashki.mark.fuelcost

import android.content.Context
import android.location.LocationManager
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import android.content.Intent
import android.provider.Settings


class MainActivity : Activity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewCostOfFuel = findViewById<View>(R.id.txtCostOfFuel) as EditText
        val viewEconomy = findViewById<View>(R.id.txtEconomy) as EditText
        val viewCostOutput = findViewById<View>(R.id.dispCost) as TextView

        val CostofFuel = viewCostOfFuel.text.toString()
        val Economy = viewEconomy.text.toString()
        val service = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val enabled = service
            .isProviderEnabled(LocationManager.GPS_PROVIDER)

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }
}
