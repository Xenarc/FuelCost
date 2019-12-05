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
import android.content.pm.PackageManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.location.Location
import android.location.LocationListener
import android.location.LocationProvider
import android.os.Handler
import android.os.Looper
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DecimalFormat


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
    private lateinit var btnPausePlay: Button

    // Location variables
    private var locationPermitted = false
    private lateinit var locationManager: LocationManager
    private var CurrentSpeed = 0.0
    private var currentFuelUsage = 0.0
    private lateinit var fuelUpdateHandler: Handler
    private lateinit var fuelUpdateRunnable: Runnable
    private lateinit var provider: LocationProvider
    private var currentLocation: Location? = null
    private lateinit var oldLocation: Location
    private var pausePlay = 0 // pause = 2; play = 1; none = 0
    private val Granularity = 10.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Sets the view variables from the UI
        initialiseViews()
        // Init the Fuel Update Handler and the runnable so that they are initialised to run every 1s
        fuelUpdateHandler = Handler(Looper.getMainLooper())
        fuelUpdateRunnable = Runnable {
            updateFuelClock()
            fuelUpdateHandler.postDelayed(fuelUpdateRunnable, (1000/Granularity).toLong())
        }

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
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), POSITION_CODE)
                    }
                    .create()
                    .show()

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), POSITION_CODE)
            }

        }
        else{
            initLocationObjects()

            //Request location updates:
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                500,
                5.0f, locationListenerGPS)
        }
    }

    private fun updateFuel() {
        fuelUpdateHandler.post(fuelUpdateRunnable)
    }

    @SuppressLint("SetTextI18n")
    private fun updateFuelClock() {
        // Update FuelClock
        CurrentSpeed = 16.67
        currentFuelUsage += (CurrentSpeed/3600)*Economy/100/Granularity
        dispFuel.text = DecimalFormat("#.0000L").format(currentFuelUsage)
        dispCost.text = DecimalFormat("$#.0000").format(currentFuelUsage*CostofFuel/100)
    }

    private fun btnUpdateFuelClicked() {
        // New Trip Code
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

    private fun btnPausePlayClicked(){
        dispHistory.text = pausePlay.toString()
        // pause = 2; play = 1; none = 0
        if(pausePlay == 0) {
            btnPausePlay.text = ""

        } else if(pausePlay == 1) {
            btnPausePlay.text = "Play"
            pausePlay = 2

            fuelUpdateHandler.removeCallbacks(fuelUpdateRunnable)

        } else if(pausePlay == 2) {
            btnPausePlay.text = "Pause"
            pausePlay = 1

            updateFuel()

        } else {
            btnPausePlay.text = ""
            pausePlay = 0
        }
    }

    private fun btnNewTripClicked() {
        // Pause Play
        dispHistory.text = pausePlay.toString()
        pausePlay = 2
        btnPausePlayClicked()

        // Update Fuel Details
        currentFuelUsage = 0.0
        fuelUpdateHandler.removeCallbacks(fuelUpdateRunnable)   // Removed old
        updateFuel()    // Start New
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
                500,
                5.0f, locationListenerGPS)

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
        btnPausePlay = findViewById<View>(R.id.btnPausePlay) as Button

        // Set OnClick Listeners for the buttons
        btnUpdateFuel.setOnClickListener {btnUpdateFuelClicked()}
        btnNewTrip.setOnClickListener {btnNewTripClicked()}
        btnPausePlay.setOnClickListener {btnPausePlayClicked()}
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
        if (ContextCompat.checkSelfPermission( applicationContext, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission( applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // No permissions, die
            invokeFailedLocationDialogue()
        }else {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            provider = locationManager.getProvider(LocationManager.GPS_PROVIDER)
        }
    }

    private fun getLastKnownLocation(): Location? {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    // Found best last known location: %s", l);
                    bestLocation = l
                }
            }
            bestLocation
        }else {
            null
        }
    }

    private var locationListenerGPS: LocationListener = object : LocationListener {
        @SuppressLint("SetTextI18n")
        override fun onLocationChanged(location: Location) {
            dispHistory.text = dispHistory.text.toString() + "~"

            currentLocation = getLastKnownLocation()
            CurrentSpeed = currentLocation?.speed?.times(3.6)!!
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
