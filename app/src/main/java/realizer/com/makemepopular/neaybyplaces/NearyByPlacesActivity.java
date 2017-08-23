package realizer.com.makemepopular.neaybyplaces;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import realizer.com.makemepopular.DashboardActivity;
import realizer.com.makemepopular.R;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.UtilLocation;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 14/01/2017.
 */
public class NearyByPlacesActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    Double latitude=0.0;
    Double longitude=0.0;
    private int PROXIMITY_RADIUS = 10000;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    ProgressWheel loading;
    MessageResultReceiver resultReceiver;
    LocationManager locationmanager;
    String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,""));
        setContentView(R.layout.activity_maps);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Near By Places");
        loading=(ProgressWheel)findViewById(R.id.loading);

        realizer.com.makemepopular.utils.Singleton obj = realizer.com.makemepopular.utils.Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        checkLocationService chklocation=new checkLocationService(NearyByPlacesActivity.this);
        chklocation.execute();


        Criteria cri = new Criteria();
        locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationmanager.getBestProvider(cri, false);

        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFrag.getMapAsync(this);

        }
    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (canGetLocation() == true) {
            if (provider != null & !provider.equals(""))

            {
                Location locatin= UtilLocation.getLastKnownLoaction(true, NearyByPlacesActivity.this);
                if (locatin!=null)
                {
                    latitude=locatin.getLatitude();
                    longitude=locatin.getLongitude();
                    LatLng latLng = new LatLng(locatin.getLatitude(), locatin.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Current Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    mCurrLocationMarker = mMap.addMarker(markerOptions);

                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                }
                else{
                    // Toast.makeText(AutoSyncService.this, "location not found", Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                //Toast.makeText(AutoSyncService.this,"Provider is null",Toast.LENGTH_LONG).show();
            }

            //DO SOMETHING USEFUL HERE. ALL GPS PROVIDERS ARE CURRENTLY ENABLED
        } else {
            //SHOW OUR SETTINGS ALERT, AND LET THE USE TURN ON ALL THE GPS PROVIDERS
            //showSettingsAlert();
        }

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }

        Button btnRestaurant = (Button) findViewById(R.id.btnRestaurant);
        btnRestaurant.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        btnRestaurant.setOnClickListener(new View.OnClickListener() {
            String Restaurant = "restaurant";
            @Override
            public void onClick(View v) {
                Log.d("onClick", "Button is Clicked");
                if (Config.isConnectingToInternet(NearyByPlacesActivity.this))
                {
                    mMap.clear();
                    String url = getUrl(latitude, longitude, Restaurant);
                    Object[] DataTransfer = new Object[2];
                    DataTransfer[0] = mMap;
                    DataTransfer[1] = url;
                    Log.d("onClick", url);
                    GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                    getNearbyPlacesData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,DataTransfer);
                }
                else
                {
                    Utility.CustomToast(NearyByPlacesActivity.this, "No Internet Connection..!");
                }
               // Toast.makeText(NearyByPlacesActivity.this,"Nearby Restaurants", Toast.LENGTH_LONG).show();
            }
        });

        Button btnHospital = (Button) findViewById(R.id.btnHospital);
        btnHospital.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
        btnHospital.setOnClickListener(new View.OnClickListener() {
            String Hospital = "hospital";
            @Override
            public void onClick(View v) {
                Log.d("onClick", "Button is Clicked");
                if (Config.isConnectingToInternet(NearyByPlacesActivity.this)) {
                    mMap.clear();
                    String url = getUrl(latitude, longitude, Hospital);
                    Object[] DataTransfer = new Object[2];
                    DataTransfer[0] = mMap;
                    DataTransfer[1] = url;
                    Log.d("onClick", url);
                    GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                    getNearbyPlacesData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, DataTransfer);
                }
                else
                {
                    Utility.CustomToast(NearyByPlacesActivity.this, "No Internet Connection..!");
                }
                //Toast.makeText(NearyByPlacesActivity.this,"Nearby Hospitals", Toast.LENGTH_LONG).show();
            }
        });

        Button btnSchool = (Button) findViewById(R.id.btnSchool);
        btnSchool.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
        btnSchool.setOnClickListener(new View.OnClickListener() {
            String School = "school";
            @Override
            public void onClick(View v) {
                Log.d("onClick", "Button is Clicked");
                if (Config.isConnectingToInternet(NearyByPlacesActivity.this)) {
                    mMap.clear();
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }
                    String url = getUrl(latitude, longitude, School);
                    Object[] DataTransfer = new Object[2];
                    DataTransfer[0] = mMap;
                    DataTransfer[1] = url;
                    Log.d("onClick", url);
                    GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                    getNearbyPlacesData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, DataTransfer);
                }
                else
                {
                    Utility.CustomToast(NearyByPlacesActivity.this, "No Internet Connection..!");
                }
                //Toast.makeText(NearyByPlacesActivity.this,"Nearby Schools", Toast.LENGTH_LONG).show();
            }
        });

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyATuUiZUkEc_UgHuqsBJa1oqaODI-3mLs0");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "entered");

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        //Toast.makeText(NearyByPlacesActivity.this,"Your Current Location", Toast.LENGTH_LONG).show();

        Log.d("onLocationChanged", String.format("latitude:%.3f longitude:%.3f",latitude,longitude));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }
        Log.d("onLocationChanged", "Exit");
        //loading.setVisibility(View.GONE);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (latitude == null && longitude == null)
        {
            onGPSService();
        }
    }

    private void onGPSService()
    {
        Criteria cri = new Criteria();
        locationmanager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        String provider = locationmanager.getBestProvider(cri, false);

        if (canGetLocation() == true) {
            if (provider != null & !provider.equals(""))

            {
                Location locatin = UtilLocation.getLastKnownLoaction(true, NearyByPlacesActivity.this);
                if (locatin != null) {
                    latitude = locatin.getLatitude();
                    latitude = locatin.getLongitude();
                } else {
                    // Toast.makeText(this, "location not found", Toast.LENGTH_LONG).show();
                    // Config.alertDialog(this, "Error", "Location not found");
                    Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (location == null) {
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                        onGPSService();
                    }
                    else {
                        latitude = locatin.getLatitude();
                        latitude = locatin.getLongitude();
                    }
                }
            } else {
                //Toast.makeText(this, "Provider is null", Toast.LENGTH_LONG).show();
                //Config.alertDialog(this, "Error","Provider is null");
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (location == null) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                    onGPSService();
                }
                else {
                    latitude = location.getLatitude();
                    latitude = location.getLongitude();
                }
            }

            //DO SOMETHING USEFUL HERE. ALL GPS PROVIDERS ARE CURRENTLY ENABLED
        } else {
            //SHOW OUR SETTINGS ALERT, AND LET THE USE TURN ON ALL THE GPS PROVIDERS
            //showSettingsAlert();
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                   // Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                    Config.alertDialog(this, "Error","Permission denied.");
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Intent reg=new Intent(NearyByPlacesActivity.this,DashboardActivity.class);
                startActivity(reg);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class UpdateUI implements Runnable {
        String update;

        public UpdateUI(String update) {

            this.update = update;
        }

        public void run() {

            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NearyByPlacesActivity.this);
            if(update.equals("Emergency")) {

                String notType=sharedpreferences.getString("Type", "");
                if (notType.equalsIgnoreCase("FriendRequest"))
                {
                    String reqstName=sharedpreferences.getString("RequsetByName", "");
                    String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
                    Config.showacceptrejectFriendRequest(reqstName,thumbnail,NearyByPlacesActivity.this);
                    //Config.showacceptrejectFriendRequest(reqstName, NearyByPlacesActivity.this);
                }
                else if (notType.equalsIgnoreCase("Emergency"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String trobler=sharedpreferences.getString("TroublerName", "");
                    String troblerid=sharedpreferences.getString("TroublerUserId", "");
                    Config.showEmergencyAcceptReject(msg,trobler,troblerid,NearyByPlacesActivity.this);
                }
                else if (notType.equalsIgnoreCase("EmergencyRecipt"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("HelperUserName", "");
                    String isResch=sharedpreferences.getString("isReaching", "");
                    if (isResch.equalsIgnoreCase("true")){}
                    //showEmergencyAckAlert(newMsg, helpername);
                    Config.showEmergencyAckAlert(msg,helpername,NearyByPlacesActivity.this);
                }
                else if (notType.equalsIgnoreCase("FriendRequestAccepted"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("AcceptByName", "");

                    Config.showAccptedRequestAlert(msg,helpername,NearyByPlacesActivity.this);
                }

            }

            else if(update.equals("RefreshThreadList")) {

            }
        }
    }

    class MessageResultReceiver extends ResultReceiver
    {
        public MessageResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if(resultCode == 300){
                NearyByPlacesActivity.this.runOnUiThread(new UpdateUI("Emergency"));
            }
            if(resultCode == 200){
                NearyByPlacesActivity.this.runOnUiThread(new UpdateUI("RefreshThreadList"));
            }

        }
    }

    public boolean canGetLocation() {
        boolean result = true;
        LocationManager lm=null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (lm == null)

            lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }
        try {
            network_enabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (gps_enabled == false || network_enabled == false) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

        String googlePlacesData;
        GoogleMap mMap;
        String url;

        @Override
        protected void onPreExecute() {

            loading.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d("GetNearbyPlacesData", "doInBackground entered");
                mMap = (GoogleMap) params[0];
                url = (String) params[1];
                DownloadUrl downloadUrl = new DownloadUrl();
                googlePlacesData = downloadUrl.readUrl(url);
                Log.d("GooglePlacesReadTask", "doInBackground Exit");
            } catch (Exception e) {
                Log.d("GooglePlacesReadTask", e.toString());
            }
            return googlePlacesData;
        }

        @Override
        protected void onPostExecute(String result) {
            loading.setVisibility(View.GONE);
            Log.d("GooglePlacesReadTask", "onPostExecute Entered");

            if(result.contains("error_message"))
            {
                Config.alertDialog(NearyByPlacesActivity.this,"Error","Error to find your location.\n Please try after sometime.");
            }
            else
            {
                List<HashMap<String, String>> nearbyPlacesList = null;
                DataParser dataParser = new DataParser();
                nearbyPlacesList =  dataParser.parse(result);
                ShowNearbyPlaces(nearbyPlacesList);
                Log.d("GooglePlacesReadTask", "onPostExecute Exit");
            }
        }

        private void ShowNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList) {
            for (int i = 0; i < nearbyPlacesList.size(); i++) {
                Log.d("onPostExecute","Entered into showing locations");
                MarkerOptions markerOptions = new MarkerOptions();
                HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
                double lat = Double.parseDouble(googlePlace.get("lat"));
                double lng = Double.parseDouble(googlePlace.get("lng"));
                String placeName = googlePlace.get("place_name");
                String vicinity = googlePlace.get("vicinity");
                LatLng latLng = new LatLng(lat, lng);
                markerOptions.position(latLng);
                markerOptions.title(placeName + " : " + vicinity);
                mMap.addMarker(markerOptions);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            }

        }
    }

    public class checkLocationService extends AsyncTask<Void,Void,Boolean> {

        StringBuilder resultbuilder;
        Context mycontext;
        Boolean location;

        public checkLocationService(Context mycontext) {
            this.mycontext=mycontext;
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            location=canGetLocation();

            return location;
        }

        @Override
        protected void onPostExecute(Boolean string) {
            super.onPostExecute(string);
            loading.setVisibility(View.GONE);
            if (string)
            {
            }
            else
            {
                showSettingsAlert();
            }
        }
    }

    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Error!");

        // Setting Dialog Message
        alertDialog.setMessage("Please Activate GPS Service");

        // On pressing Settings button
        alertDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        finish();
                    }
                });

        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent reg=new Intent(NearyByPlacesActivity.this,DashboardActivity.class);
        startActivity(reg);
        finish();
    }

}
