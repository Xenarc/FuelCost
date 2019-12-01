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
import android.R.attr.button
import android.widget.Button
import java.lang.Exception


class MainActivity : Activity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var CostofFuel = 0.0
    private var Economy = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewCostOfFuel = findViewById<View>(R.id.txtCostOfFuel) as EditText
        val viewEconomy = findViewById<View>(R.id.txtEconomy) as EditText
        val viewCostOutput = findViewById<View>(R.id.dispCost) as TextView
        val viewFuelOutput = findViewById<View>(R.id.dispFuel) as TextView
        val viewHistoryOutput = findViewById<View>(R.id.dispHistory) as TextView

        val btnNewTrip = findViewById<View>(R.id.btnNewTrip) as Button
        val btnUpdateFuel = findViewById<View>(R.id.btnUpdateFuel) as Button


        // Set up Location Stuff
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

        // Set OnClick Listeners for the buttons
        btnNewTrip.setOnClickListener {
            // New Trip Code

        }

        btnNewTrip.setOnClickListener {
            // Update Fuel Details

            CostofFuel = try {
                viewCostOfFuel.text.toString().toDouble()
            }catch (e: NumberFormatException){
                0.0
            }

            Economy = try{
                viewEconomy.text.toString().toDouble()
            }catch (e: NumberFormatException){
                0.0
            }
        }
    }
}
