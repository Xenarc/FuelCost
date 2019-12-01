package com.markblashki.mark.fuelcost

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.content.Intent
import android.provider.Settings
import android.content.pm.PackageManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.location.Criteria
import android.location.Criteria.*
import android.location.Location
import android.location.LocationListener
import android.widget.Toast
import java.lang.Exception
import android.content.DialogInterface
import android.R.string.ok




class MainActivity : Activity() {
    private var CostofFuel = 0.0
    private var Economy = 0.0
    private val POSITION_CODE = 1

    private lateinit var viewCostOfFuel: EditText
    private lateinit var viewEconomy: EditText
    private lateinit var viewCostOutput:TextView
    private lateinit var viewFuelOutput: TextView
    private lateinit var viewHistoryOutput: TextView
    private lateinit var btnNewTrip: Button
    private lateinit var btnUpdateFuel: Button

    private lateinit var locationManager: LocationManager
    private lateinit var provider: String
    private lateinit var currentLocation: Location
    private lateinit var oldLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initialiseViews()


        // Set up Location Stuff
        if(checkLocationPermission()) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            provider = locationManager.getProvider(LocationManager.GPS_PROVIDER).toString()
        }

        // Set OnClick Listeners for the buttons
        btnNewTrip.setOnClickListener {
            // New Trip Code

            // Test GPS
            doGPS()
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

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Location Request")
                    .setMessage("Location Features Must Be Enabled For This App To Operate")
                    .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            POSITION_CODE
                        )
                    }
                    .create()
                    .show()


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    POSITION_CODE
                )
            }
            return false
        } else {
            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            POSITION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {

                        //Request location updates:
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            750,
                            10.0f, locationListenerGPS)
                    }

                } else {
                    // permission denied, die
                    invokeFailedLocationDialogue()
                }

                return
            }
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun doGPS() {

        // Test Location
        checkLocationPermission()

        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            POSITION_CODE
        )

        currentLocation = locationManager.getLastKnownLocation(provider)

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
