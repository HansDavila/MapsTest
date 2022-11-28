package com.example.mapstest


import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import org.json.JSONException
import com.google.android.gms.maps.model.CircleOptions




class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap:GoogleMap
    private val DEFAULT_ZOOM = 15f
    val TAG:String = "main"

    private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    private lateinit var mFusedLocationProviderClient:FusedLocationProviderClient
    private var mLocationPermissionsGranted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getLocationPermission()


    }

    private fun initMap() {
        val mapFragment:SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)





    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)


        //createMarker(25.7299374,-100.2096866)

        if (mLocationPermissionsGranted) {
            getDeviceLocation()

            //Esta linea solo se pone para que nops deje poner el icono azuliño

            //Esta linea solo se pone para que nops deje poner el icono azuliño
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            //Añade icono azul de la current location del dispositivo
            //Añade icono azul de la current location del dispositivo
            mMap.isMyLocationEnabled = true

            cargaTabla()
        }


    }

     fun createMarker(lati:Double, longi:Double, name: String) {
        //val coordinates = LatLng(25.7299374,-100.2096866)
        val coordinates = LatLng(lati,longi)
        val marker = MarkerOptions().position(coordinates).title(name)


        val pointer = mMap.addMarker(marker)
         pointer?.tag = 0

    }

    fun getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){
                var location: Task<*> = mFusedLocationProviderClient.getLastLocation()
                location.addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        Log.d(TAG, "getUserLocation: Found Location")
                        var currentLocation: Location = task.getResult() as Location

                        moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude), DEFAULT_ZOOM, "My location")
                        drawCircle(LatLng(currentLocation.latitude, currentLocation.longitude))

                    }else{
                        Log.d(TAG, "onComplete: current location is null")
                        Toast.makeText(this@MainActivity, "Unable to get current location", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }catch (e: SecurityException){
            Log.d(TAG, "getUserLocation: Security Exception ${e.message}")
        }
    }

    fun cargaTabla(){

        var queue = Volley.newRequestQueue(this)
        var lati:Double
        var longi:Double

        var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?fields=price_level&location=25.7299374%2C-100.2096866&radius=2500&type=restaurant&key=AIzaSyDV6aFItX960hrbAaI229-8iDa3xTZ-RXU"
        var myJsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,url,null,
            {
                    response ->  try{
                var myJsonArray = response.getJSONArray("results")
                for(i in 0 until myJsonArray.length()){
                    var myJSONObject = myJsonArray.getJSONObject(i)
                    /*
                    val registro = LayoutInflater.from(this).inflate(R.layout.table_row_np,null,false)
                    val colName = registro.findViewById<View>(R.id.columnaNombre) as TextView
                    val colPrice = registro.findViewById<View>(R.id.columnaEmail) as TextView
                    val colLatitude = registro.findViewById<View>(R.id.colEditar)
                    val colBorrar = registro.findViewById<View>(R.id.colBorrar)
                    */


                    //colName.text= myJSONObject.getString("name")
                    Log.d(TAG, "Nombre:  ${ myJSONObject.getString("name")}" )

                    try{
                        Log.d(TAG, "Rating:  ${ myJSONObject.getString("price_level")}" )
                    }catch (e: Exception){
                        Log.d(TAG, "ERROR -> :  ${ e.message}" )
                    }

                    //colPrice.text=myJSONObject.getString("price_level")

                    var name = myJSONObject.getString("name")
                    var geometry = myJSONObject.getJSONObject("geometry")
                    var location = geometry.getJSONObject("location")

                    lati = location.getString("lat").toDouble()
                    longi = location.getString("lng").toDouble()
                    Log.d(TAG, "Latitude: ${location.getString("lat")}")
                    Log.d(TAG, "Latitude: ${location.getString("lng")}")

                    createMarker(lati, longi, name)




                    //colEditar.id=myJSONObject.getString("id").toInt()
                    //colBorrar.id=myJSONObject.getString("id").toInt()



                }

            }catch (e: JSONException){
                e.printStackTrace()
            }
            }, {
                    error ->  Toast.makeText(this,"Error $error", Toast.LENGTH_LONG).show()
            })
        queue.add(myJsonObjectRequest)
    }

    fun moveCamera(lati:LatLng, zoom:Float, title:String ){
        Log.d(TAG, "moveCamera: moving camero to -> lat: " + lati.latitude + ", longitude: " + lati.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lati, zoom))

    }



    //PERMISOS
    fun getLocationPermission(){
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (ContextCompat.checkSelfPermission(this.applicationContext, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.applicationContext, COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true
                initMap()
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE)
        }

    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.d(TAG, "onMarkerClick: Clicking")
        // Retrieve the data from the marker.
        val clickCount = marker.tag as? Int

        // Check if a click count was set, then display the click count.
        clickCount?.let {
            val newClickCount = it + 1
            marker.tag = newClickCount
            Toast.makeText(this, "${marker.title} has been clicked $newClickCount times.", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "onMarkerClick: ${marker.title} has been clicked $newClickCount times.")
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false
    }

    private fun drawCircle(point: LatLng) {

        // Instantiating CircleOptions to draw a circle around the marker
        val circleOptions = CircleOptions()

        // Specifying the center of the circle
        circleOptions.center(point)

        // Radius of the circle
        circleOptions.radius(2000.0)

        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK)

        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000)

        // Border width of the circle
        circleOptions.strokeWidth(2f)

        // Adding the circle to the GoogleMap
        mMap.addCircle(circleOptions)
    }


}