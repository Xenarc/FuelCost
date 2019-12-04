package com.markblashki.mark.fuelcost

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.content.pm.PackageManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.location.Location
import android.location.LocationListener
import android.widget.Toast




class MainActivity : Activity() {
    // Internal Calculation Variables
    private var CostofFuel = 0.0
    private var Economy = 0.0
    private val POSITION_CODE = 1

    // Views
    private lateinit var viewCostOfFuel: EditText
    private lateinit var viewEconomy: EditText
    private lateinit var viewCostOutput:TextView
    private lateinit var viewFuelOutput: TextView
    private lateinit var viewHistoryOutput: TextView
    private lateinit var btnNewTrip: Button
    private lateinit var btnUpdateFuel: Button

    // Location variables
    private var locationPermitted = false
    private lateinit var locationManager: LocationManager
    private lateinit var provider: String
    private lateinit var currentLocation: Location
    private lateinit var oldLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Sets the view variables from the UI
        initialiseViews()

        // Check if permissions are enabled
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check if we Should show a rationale for Location Permissions? (Only called once... ever)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Location Request")
                    .setMessage("Location Features Must Be Enabled For This App To Operate")
                    .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), POSITION_CODE)
                    }
                    .create()
                    .show()

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), POSITION_CODE)
            }
        } // Permissions allowed v

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            initLocationObjects()

            //Request location updates:
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                750,
                10.0f, locationListenerGPS)
        }

        // Set OnClick Listeners for the buttons
        btnUpdateFuel.setOnClickListener {btnUpdateFuelClicked()}
        btnNewTrip.setOnClickListener {btnNewTripClicked()}
    }

    private fun btnUpdateFuelClicked() {
        // New Trip Code
        doGPS()
    }

    private fun btnNewTripClicked() {
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

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                750,
                10.0f, locationListenerGPS)

        }
    }

    override fun onPause() {
        super.onPause()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            locationManager.removeUpdates(locationListenerGPS)
        }
    }

    private fun initialiseViews() {
        viewCostOfFuel = findViewById<View>(R.id.txtCostOfFuel) as EditText
        viewEconomy = findViewById<View>(R.id.txtEconomy) as EditText
        viewCostOutput = findViewById<View>(R.id.dispCost) as TextView
        viewFuelOutput = findViewById<View>(R.id.dispFuel) as TextView
        viewHistoryOutput = findViewById<View>(R.id.dispHistory) as TextView

        btnNewTrip = findViewById<View>(R.id.btnNewTrip) as Button
        btnUpdateFuel = findViewById<View>(R.id.btnUpdateFuel) as Button
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            POSITION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initLocationObjects()
                }else{
                    invokeFailedLocationDialogue()
                }
            }
        }
    }

    private fun initLocationObjects() {
        if (ContextCompat.checkSelfPermission( applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission( applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // No permissions, die
            invokeFailedLocationDialogue()
        }else {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            provider = locationManager.getProvider(LocationManager.GPS_PROVIDER).toString()
        }
    }

    private fun doGPS() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            currentLocation = locationManager.getLastKnownLocation(provider)
        }
        val toast = Toast.makeText(
            applicationContext,
            currentLocation.latitude.toString(),
            Toast.LENGTH_SHORT
        )

        toast.show()
    }

    private var locationListenerGPS: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: android.location.Location) {
            val latitude = location.latitude
            val longitude = location.longitude
            val msg = "New Latitude: " + latitude + "New Longitude: " + longitude
            Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

        }

        override fun onProviderEnabled(provider: String) {

        }

        override fun onProviderDisabled(provider: String) {

        }
    }

    private fun invokeFailedLocationDialogue() {
        print("Kind Kill --------------------------------------------------------------------------------------")
        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle("Location Not Enabled")
        alertDialogBuilder.setMessage("Sorry, this app requires location services")
        alertDialogBuilder.setPositiveButton(android.R.string.ok) { _,_ ->
            finishAffinity()

        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
