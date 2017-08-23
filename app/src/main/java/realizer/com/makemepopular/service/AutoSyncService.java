package realizer.com.makemepopular.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.UtilLocation;

/**
 * Created by shree on 1/23/2017.
 */
public class AutoSyncService extends Service implements OnTaskCompleted,com.google.android.gms.location.LocationListener ,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    SharedPreferences sharedpreferences;
    String userid;
    LocationManager locationmanager;
    static Double currentLat,currentLog;
    String provider;
    String CityName="";
    static String premise,route,sublocality,locality,administrative_area_level_2,administrative_area_level_1,country,postal_code;
    //test location
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    boolean isStartService=true;

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,""));
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userid = sharedpreferences.getString("UserId", "");

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();


        String firstTime = intent.getStringExtra("FirstTime");
        int sec=10000;
        if (firstTime.equals("1"))
        {
            sec=1000;
        }

        int total_min=(60000 * 30);//AUTOSYNC call after 30 mins
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new AutoSyncServerDataTrack(), sec, total_min);
        return START_NOT_STICKY;
    }

    @Override
     public void onTaskCompleted(String s) {
        if (!s.equalsIgnoreCase(""))
        {
            try {
                JSONObject json = new JSONObject(s);
                String success=json.getString("success");
                String todaysTrackingCount=json.getString("todaysTrackingCount");
                String lastWeekTrackingCount=json.getString("lastWeekTrackingCount");
                String lastMonthTrackingCount=json.getString("lastMonthTrackingCount");

                if (success.equalsIgnoreCase("true"))
                {
                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(AutoSyncService.this);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("Success", success);
                    edit.putString("TodaysTrackingCount", todaysTrackingCount);
                    edit.putString("LastWeekTrackingCount",lastWeekTrackingCount);
                    edit.putString("LastMonthTrackingCount",lastMonthTrackingCount);
                    edit.putString("PendingFriendRequestCount",json.getString("friendBadgeCount"));
                    edit.putString("UnreadChatCount",json.getString("chatBadgeCount"));
                    edit.commit();
                    Log.d("AutoService","End");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
       // mGoogleApiClient.connect();
        Log.d("Onconnected","Invoked");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }
        startLocationUpdate();
    }

    public void startLocationUpdate() {
        initLocationRequest();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void handleNewLocation(Location location) {
        currentLat = location.getLatitude();
        currentLog = location.getLongitude();
        Log.d("LAtLog=",""+currentLat+","+currentLog);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("onConnectionSuspended","Invoked");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged","Invoked");
        handleNewLocation(location);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("onConnectionFailed","Invoked");
        if (currentLat == null && currentLog == null)
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
                Location locatin = UtilLocation.getLastKnownLoaction(true, AutoSyncService.this);
                if (locatin != null) {
                    currentLat = locatin.getLatitude();
                    currentLog = locatin.getLongitude();
                    //getLocationName(currentLat,currentLog);
                } else {
                    // Toast.makeText(this, "location not found", Toast.LENGTH_LONG).show();
                    // Config.alertDialog(this, "Error", "Location not found");
                    Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (location == null) {
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                        onGPSService();
                    }
                    else {
                        currentLat = locatin.getLatitude();
                        currentLog = locatin.getLongitude();
                        //getLocationName(currentLat,currentLog);
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
                    currentLat = location.getLatitude();
                    currentLog = location.getLongitude();
                    //getLocationName(currentLat,currentLog);
                }
            }

            //DO SOMETHING USEFUL HERE. ALL GPS PROVIDERS ARE CURRENTLY ENABLED
        } else {
            //SHOW OUR SETTINGS ALERT, AND LET THE USE TURN ON ALL THE GPS PROVIDERS
            //showSettingsAlert();
        }
    }

    class AutoSyncServerDataTrack extends TimerTask
    {
        @Override
        public void run() {
            if(Config.isConnectingToInternet(AutoSyncService.this)) {
                Log.d("AutoService","Start");
                initializeTimerTask();
            }
        }
    }

    public void  initializeTimerTask() {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("hh:mm aa");
        String datetime = dateformat.format(c.getTime());
        if (datetime.contains("11:00 PM"))
        {
            //stopService(new Intent(this, AutoSyncService.class));
            isStartService=false;
            Log.d("AutoSyncTimer", "Stop");
        }
        else  if (datetime.contains("08:00 AM"))
        {
            //startService(new Intent(this, AutoSyncService.class));
            isStartService=true;
            Log.d("AutoSyncTimer", "Start");
        }

        if (isStartService)
        {
            Log.d("AutoSync", "Start");
            if (currentLat != null) {
                Log.d("latitude",String.valueOf(currentLat));
                Log.d("longitude",String.valueOf(currentLog));
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(AutoSyncService.this);
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("CurrentLatitudeEmergency",String.valueOf(currentLat));
                edit.putString("CurrentLongitudeEmergency",String.valueOf(currentLog));
                edit.putString("CurrentCity","");
                edit.commit();
                AutoSynckSetCordinatesTask auto=new AutoSynckSetCordinatesTask(currentLat,currentLog,AutoSyncService.this,AutoSyncService.this);
                auto.execute();
                //DO SOMETHING USEFUL HERE.ALL GPS PROVIDERS ARE CURRENTLY ENABLED
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
}
