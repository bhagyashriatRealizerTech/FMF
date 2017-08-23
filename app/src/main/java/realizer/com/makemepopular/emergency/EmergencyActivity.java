package realizer.com.makemepopular.emergency;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import realizer.com.makemepopular.DashboardActivity;
import realizer.com.makemepopular.R;
import realizer.com.makemepopular.asynctask.SendEmergencyAlertAsyncTask;
import realizer.com.makemepopular.backend.DatabaseQueries;
import realizer.com.makemepopular.chat.model.NewFriendListModel;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.UtilLocation;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by shree on 1/23/2017.
 */
public class EmergencyActivity extends AppCompatActivity implements OnTaskCompleted,com.google.android.gms.location.LocationListener ,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    Double currentLat,currentLog;
    String currentAddress;
    ///ArrayList<EmergencyFrndModel> emergencyfrndz=new ArrayList<>();
    String provider;
    static String premise,route,sublocality,locality,administrative_area_level_2,administrative_area_level_1,country,postal_code;
    LocationManager locationmanager;
    ProgressWheel loading,loadingemergeny;
    ArrayList<FriendListModel> friendlist=new ArrayList<>();
    EmergencyFrndModelAdapter friendadapter;
    ListView emergencyFrndz;
    TextView noresultFound;
    String address="";
    MessageResultReceiver resultReceiver;
    EditText message;
    //test location
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int MY_PERMISSION_REQUEST_READ_FINE_LOCATION= 100;
    static int locationCounter=0;
    DatabaseQueries db;
    SendEmergencyAlertAsyncTask sendalert;
    static boolean isRespondingAPI=false;
    String userName="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,""));
        setContentView(R.layout.emergency_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Emergency");
        Button emergency= (Button) findViewById(R.id.btn_emergency);
        message= (EditText) findViewById(R.id.edt_msg);
        emergency.setTypeface(FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME));

        final TextView ico_medical,ico_trouble,ico_accident,txt_medical,txt_trouble,txt_accident;
        final LinearLayout layout_medical,layout_accident,layout_trouble;

        ico_medical= (TextView) findViewById(R.id.txt_emergency_medicalico);
        ico_trouble= (TextView) findViewById(R.id.txt_emergency_troubleico);
        ico_accident= (TextView) findViewById(R.id.txt_emergency_accidentico);
        noresultFound= (TextView) findViewById(R.id.noresultFound);

        txt_medical= (TextView) findViewById(R.id.txt_emergency_Medicaltext);
        txt_accident= (TextView) findViewById(R.id.txt_emergency_accidenttext);
        txt_trouble= (TextView) findViewById(R.id.txt_emergency_troubletext);

        layout_medical= (LinearLayout) findViewById(R.id.linar_emergency_medical);
        layout_accident= (LinearLayout) findViewById(R.id.linar_emergency_accident);
        layout_trouble= (LinearLayout) findViewById(R.id.linar_emergency_trouble);

        ico_medical.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_trouble.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_accident.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        loadingemergeny= (ProgressWheel) findViewById(R.id.loadingemergeny);

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(EmergencyActivity.this);
        String fname = sharedpreferences.getString("Fname","");
        String lname = sharedpreferences.getString("Lname","");
        userName = fname + " " + lname;
       // loadingemergeny.setVisibility(View.VISIBLE);
        emergencyFrndz= (ListView) findViewById(R.id.list_emergencyfrndz);
        db=new DatabaseQueries(EmergencyActivity.this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

//        checkLocationService chklocation=new checkLocationService(EmergencyActivity.this);
//        chklocation.execute();

        realizer.com.makemepopular.utils.Singleton obj = realizer.com.makemepopular.utils.Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);

        emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (friendlist.size()>0)
                {


                    //Toast.makeText(DashboardActivity.this, "Emergency Message Send", Toast.LENGTH_SHORT).show();
                    String msg=message.getText().toString();

                    if (!msg.equalsIgnoreCase("") && currentLat != null)
                    {
                        if (Config.isConnectingToInternet(EmergencyActivity.this))
                        {
                            loadingemergeny.setVisibility(View.VISIBLE);
                            sendalert=new SendEmergencyAlertAsyncTask(msg,String.valueOf(currentLat),String.valueOf(currentLog),EmergencyActivity.this,EmergencyActivity.this);
                            sendalert.execute();
                            isRespondingAPI=false;
                            new CountDownTimer( 15000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                }
                                public void onFinish() {
                                    loadingemergeny.setVisibility(View.GONE);
                                    if (!isRespondingAPI)
                                    {
                                        if (sendalert != null && sendalert.getStatus() != AsyncTask.Status.FINISHED) {
                                            sendalert.cancel(true);
                                            Config.alertDialog(EmergencyActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                        }
                                    }
                                }
                            }.start();
                        }
                        else
                        {
                            Utility.CustomToast(EmergencyActivity.this, "No Internet Connection..!");
                        }
                    }
                    else
                    {
                       // Toast.makeText(getApplicationContext(),"Please enter message...",Toast.LENGTH_LONG).show();
                        Config.alertDialog(EmergencyActivity.this, "Suggestion", "Please enter message.");
                    }
                }
                else
                {
                    //Toast.makeText(EmergencyActivity.this,"You Have No Any Emergency Friend...",Toast.LENGTH_LONG).show();
                    Config.alertDialog(EmergencyActivity.this, "Emergency", "No Emergency friends are added..");
                }

            }
        });

        layout_medical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_medical.setBackgroundResource(R.drawable.border_shadow_red);
                txt_medical.setTextColor(Color.WHITE);
                ico_medical.setTextColor(Color.WHITE);

                layout_accident.setBackgroundResource(R.drawable.border_shadow_white);
                txt_accident.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                ico_accident.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                layout_trouble.setBackgroundResource(R.drawable.border_shadow_white);
                txt_trouble.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                ico_trouble.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                if (!address.equalsIgnoreCase(""))
                    message.setText("In Medical Emergency, "+userName+" need help at "+address + " and coordinates are" +String.valueOf(currentLat)+","+String.valueOf(currentLog));
                else
                    message.setText("In Medical Emergency, "+userName+" need help , coordinates are" +String.valueOf(currentLat)+","+String.valueOf(currentLog));
            }
        });

        layout_accident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_accident.setBackgroundResource(R.drawable.border_shadow_red);
                txt_accident.setTextColor(Color.WHITE);
                ico_accident.setTextColor(Color.WHITE);

                layout_medical.setBackgroundResource(R.drawable.border_shadow_white);
                txt_medical.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                ico_medical.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                layout_trouble.setBackgroundResource(R.drawable.border_shadow_white);
                txt_trouble.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                ico_trouble.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                if (!address.equalsIgnoreCase(""))
                    message.setText( userName+" has met with an accident at "+address+" and coordinates are "+String.valueOf(currentLat)+","+String.valueOf(currentLog));
                else
                    message.setText(userName+" has met with an accident, coordinates are "+String.valueOf(currentLat)+","+String.valueOf(currentLog));

            }
        });

        layout_trouble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_trouble.setBackgroundResource(R.drawable.border_shadow_red);
                txt_trouble.setTextColor(Color.WHITE);
                ico_trouble.setTextColor(Color.WHITE);

                layout_medical.setBackgroundResource(R.drawable.border_shadow_white);
                txt_medical.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                ico_medical.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                layout_accident.setBackgroundResource(R.drawable.border_shadow_white);
                txt_accident.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                ico_accident.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

                if (!address.equalsIgnoreCase(""))
                    message.setText(userName+" in trouble at "+address+" and coordinates are "+String.valueOf(currentLat)+","+String.valueOf(currentLog));
                else
                    message.setText( userName+" in trouble ,coordinates are "+String.valueOf(currentLat)+","+String.valueOf(currentLog));
            }
        });

        layout_medical.setBackgroundResource(R.drawable.border_shadow_red);
        txt_medical.setTextColor(Color.WHITE);
        ico_medical.setTextColor(Color.WHITE);

        message.setText("In Medical Emergency, "+userName+" need help , coordinates are" +String.valueOf(currentLat)+","+String.valueOf(currentLog));

        Bundle bundle=getIntent().getExtras();
        currentLat=bundle.getDouble("CURRENTLAT");
        currentLog=bundle.getDouble("CURRENTLOG");

        if(address != null) {
            if (!address.equalsIgnoreCase(""))
                message.setText("In Medical Emergency, "+userName+" need help at "+address + " and coordinates are" +String.valueOf(currentLat)+","+String.valueOf(currentLog));
            else
            {
                message.setText("In Medical Emergency, "+userName+" need help , coordinates are" +String.valueOf(currentLat)+","+String.valueOf(currentLog));
                CurrentAddressAsyntask address = new CurrentAddressAsyntask(currentLat, currentLog, EmergencyActivity.this);
                address.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        else
        {
            CurrentAddressAsyntask address = new CurrentAddressAsyntask(currentLat, currentLog, EmergencyActivity.this);
            address.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        getAllEmergencyContacts();

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_CONTACTS},
                        MY_PERMISSION_REQUEST_READ_FINE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_READ_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
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

    public static String getLocationCityName( double lat, double lon ){
        JSONObject result = getLocationFormGoogle(lat + "," + lon );
        return getCityAddress(result);
    }

    protected static JSONObject getLocationFormGoogle(String placesName) {

        String apiRequest = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + placesName; //+ "&ka&sensor=false"
        HttpGet httpGet = new HttpGet(apiRequest);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {

            e.printStackTrace();
        }

        return jsonObject;
    }

    protected static String getCityAddress( JSONObject result ){
        if( result.has("results") ){
            try {
                JSONArray array = result.getJSONArray("results");
                if( array.length() > 0 ){
                    JSONObject place = array.getJSONObject(0);
                    JSONArray components = place.getJSONArray("address_components");
                    for( int i = 0 ; i < components.length() ; i++ ){
                        JSONObject component = components.getJSONObject(i);
                        JSONArray types = component.getJSONArray("types");
                        for( int j = 0 ; j < types.length() ; j ++ ){
                            if( types.getString(j).equals("premise") ){
                                premise= component.getString("long_name");
                            }
                            if( types.getString(j).equals("route") ){
                                route= component.getString("long_name");
                            }
                            if( types.getString(j).equals("sublocality") ){
                                sublocality= component.getString("long_name");
                            }
                            if( types.getString(j).equals("locality") ){
                                locality= component.getString("long_name");
                            }
                            if( types.getString(j).equals("administrative_area_level_1") ){
                                administrative_area_level_1= component.getString("long_name");
                            }
                            if( types.getString(j).equals("country") ){
                                country= component.getString("long_name");
                            }
                            if( types.getString(j).equals("postal_code") ){
                                postal_code= component.getString("long_name");
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String address=null;
        if (premise.equals(null) && locality.equals(null))
        {
            address=null;
        }
        else
        {
            address=premise+","+route+","+sublocality+","+locality+","+administrative_area_level_1+","+
                    country+","+postal_code;
        }
        return address;
    }

    public String getLocationName(double latitude, double longitude) {

        String cityName = "";

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String country = addresses.get(0).getCountryName();
            cityName = address+","+addresses.get(0).getAddressLine(1)+","+city+","+country;
        } catch (IOException e) {
            if (Config.isConnectingToInternet(EmergencyActivity.this))
            {
                cityName=getLocationCityName(latitude,longitude);
            }
            else
            {
                //Utility.CustomToast(EmergencyActivity.this, "No Internet Connection..!");
            }

            e.printStackTrace();
        }


        return cityName;

    }

    @Override
    public void onTaskCompleted(String s) {
        isRespondingAPI=true;
        String arr[]=s.split("@@@");
        if (arr[1].equalsIgnoreCase("SendAlert"))
        {
            if (arr[0].equalsIgnoreCase("true"))
            {
                //Toast.makeText(EmergencyActivity.this, "Emergency Message Sent Successfully", Toast.LENGTH_SHORT).show();
                Config.alertDialog(EmergencyActivity.this, "Emergency", "Emergency Message Sent Successfully.");
            }
            else
            {
                Config.alertDialog(EmergencyActivity.this, "Emergency", "Emergency Message Not Send.");
                //Toast.makeText(EmergencyActivity.this, "Emergency Message Not Send", Toast.LENGTH_SHORT).show();
                //EmergencyDialog();
            }
            loadingemergeny.setVisibility(View.GONE);
        }
        else  if (arr[1].equalsIgnoreCase("Emergency"))
        {
            friendlist=new ArrayList<>();
            if (! arr[0].equalsIgnoreCase("[]"))
            {
                JSONArray jsonarray = null;
                try {
                    jsonarray = new JSONArray(arr[0]);

                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);

                        FriendListModel model=new FriendListModel();
                        model.setFriendName(jsonobject.getString("friendName"));
                        model.setIsEmergency(jsonobject.getBoolean("isEmergencyAlert"));
                        model.setIsmessaging(jsonobject.getBoolean("isMessagingAllowed"));
                        model.setIstracking(jsonobject.getBoolean("isTrackingAllowed"));
                        model.setThumbnailUrl(jsonobject.getString("friendThumbnailUrl"));
                        model.setFriendId(jsonobject.getString("friendsId"));
                        friendlist.add(model);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                friendadapter= new EmergencyFrndModelAdapter(this,friendlist);
                emergencyFrndz.setAdapter(friendadapter);
                emergencyFrndz.setVisibility(View.VISIBLE);
                loadingemergeny.setVisibility(View.GONE);
                noresultFound.setVisibility(View.GONE);
            }
            else
            {
                noresultFound.setVisibility(View.VISIBLE);
                emergencyFrndz.setVisibility(View.GONE);
                loadingemergeny.setVisibility(View.GONE);
                //Toast.makeText(EmergencyActivity.this,"You have no any Emergency Friend...",Toast.LENGTH_LONG).show();
                Config.alertDialog(EmergencyActivity.this, "Emergency", "No Emergency friends are added.");
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }
    }

    private void handleNewLocation(Location location) {
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(EmergencyActivity.this);
        if (locationCounter==0)
        {
            currentLat = location.getLatitude();
            currentLog = location.getLongitude();

            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("CurrentLatitudeEmergency",String.valueOf(currentLat));
            edit.putString("CurrentLongitudeEmergency",String.valueOf(currentLog));
            edit.commit();
            locationCounter++;
            if (currentLat != null )
            {
                CurrentAddressAsyntask address = new CurrentAddressAsyntask(currentLat, currentLog, EmergencyActivity.this);
                address.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        else
        {
            if (!sharedpreferences.getString("CurrentLatitudeEmergency","").equalsIgnoreCase(String.valueOf(location.getLatitude())))
            {
                currentLat = location.getLatitude();
                currentLog = location.getLongitude();
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("CurrentLatitudeEmergency",String.valueOf(currentLat));
                edit.putString("CurrentLongitudeEmergency",String.valueOf(currentLog));
                edit.commit();
                if (currentLat != null )
                {
                    CurrentAddressAsyntask address = new CurrentAddressAsyntask(currentLat, currentLog, EmergencyActivity.this);
                    address.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
        //Toast.makeText(this,""+currentLat+","+currentLog,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
        }
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
                Location locatin = UtilLocation.getLastKnownLoaction(true, EmergencyActivity.this);
                if (locatin != null) {
                    handleNewLocation(locatin);
                } else {
                    // Toast.makeText(this, "location not found", Toast.LENGTH_LONG).show();
                    // Config.alertDialog(this, "Error", "Location not found");
                    Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (location == null) {
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                        onGPSService();
                    }
                    else {
                        handleNewLocation(location);
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
                    handleNewLocation(location);
                }
            }

            //DO SOMETHING USEFUL HERE. ALL GPS PROVIDERS ARE CURRENTLY ENABLED
        } else {
            //SHOW OUR SETTINGS ALERT, AND LET THE USE TURN ON ALL THE GPS PROVIDERS
            showSettingsAlert();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }


    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,EmergencyActivity.this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Intent reg=new Intent(EmergencyActivity.this,DashboardActivity.class);
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

            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(EmergencyActivity.this);
            if(update.equals("Emergency")) {

                String notType=sharedpreferences.getString("Type", "");
                if (notType.equalsIgnoreCase("FriendRequest"))
                {
                    String reqstName=sharedpreferences.getString("RequsetByName", "");
                    String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
                    Config.showacceptrejectFriendRequest(reqstName, thumbnail, EmergencyActivity.this);
                    //Config.showacceptrejectFriendRequest(reqstName,EmergencyActivity.this);
                }
                else if (notType.equalsIgnoreCase("Emergency"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String trobler=sharedpreferences.getString("TroublerName", "");
                    String troblerid=sharedpreferences.getString("TroublerUserId", "");
                    Config.showEmergencyAcceptReject(msg, trobler, troblerid, EmergencyActivity.this);
                }
                else if (notType.equalsIgnoreCase("EmergencyRecipt"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("HelperUserName", "");
                    String isResch=sharedpreferences.getString("isReaching", "");
                    if (isResch.equalsIgnoreCase("true")){}
                        //showEmergencyAckAlert(newMsg, helpername);
                    Config.showEmergencyAckAlert(msg, helpername, EmergencyActivity.this);
                }
                else if (notType.equalsIgnoreCase("FriendRequestAccepted"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("AcceptByName", "");

                    Config.showAccptedRequestAlert(msg, helpername, EmergencyActivity.this);
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
                EmergencyActivity.this.runOnUiThread(new UpdateUI("Emergency"));
            }
            if(resultCode == 200){
                EmergencyActivity.this.runOnUiThread(new UpdateUI("RefreshThreadList"));
            }

        }
    }


    public class CurrentAddressAsyntask extends AsyncTask<Void,Void,String> {

        StringBuilder resultbuilder;
        Context mycontext;
        Double latitude;
        Double longitude;

        public CurrentAddressAsyntask(Double lat,Double log,Context mycontext) {
            this.mycontext=mycontext;
            this.latitude=lat;
            this.longitude=log;
        }

        @Override
        protected String doInBackground(Void... params) {

            if (currentLat != null )
            {
                address=getLocationName(currentLat,currentLog);
            }
           return address;
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);

            if (!address.equalsIgnoreCase(""))
                message.setText("In Medical Emergency, "+userName+" need help at "+address + " and coordinates are" +String.valueOf(currentLat)+","+String.valueOf(currentLog));
            else
                message.setText("In Medical Emergency, "+userName+" need help , coordinates are" +String.valueOf(currentLat)+","+String.valueOf(currentLog));

        }
    }

    public class checkLocationService extends AsyncTask<Void,Void,Boolean> {

        StringBuilder resultbuilder;
        Context mycontext;
        Boolean location;

        public checkLocationService(Context mycontext) {
            this.mycontext=mycontext;
            loadingemergeny.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

           location=canGetLocation();

            return location;
        }

        @Override
        protected void onPostExecute(Boolean string) {
            super.onPostExecute(string);
            loadingemergeny.setVisibility(View.GONE);
            if (string)
            {

            }
            else
            {
                showSettingsAlert();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent reg=new Intent(EmergencyActivity.this,DashboardActivity.class);
        startActivity(reg);
        finish();
    }

    public void getAllEmergencyContacts()
    {
        ArrayList<NewFriendListModel> emergency=new ArrayList<>();
        emergency= db.getAllEmergencyConatcts();
        friendlist=new ArrayList<>();
        if (emergency.size() > 0)
        {
            for (int i = 0; i < emergency.size(); i++) {

                FriendListModel model=new FriendListModel();
                model.setFriendName(emergency.get(i).getFriendName());
                model.setIsEmergency(Boolean.valueOf(emergency.get(i).getIsEmergencyAlert()));
                model.setIsmessaging(Boolean.valueOf(emergency.get(i).getIsMessagingAllowed()));
                model.setIstracking(Boolean.valueOf(emergency.get(i).getIsTrackingAllowed()));
                model.setThumbnailUrl(emergency.get(i).getFriendThumbnailUrl());
                model.setFriendId(emergency.get(i).getFriendsId());
                friendlist.add(model);
            }

            friendadapter= new EmergencyFrndModelAdapter(this,friendlist);
            emergencyFrndz.setAdapter(friendadapter);
            emergencyFrndz.setVisibility(View.VISIBLE);
            loadingemergeny.setVisibility(View.GONE);
            noresultFound.setVisibility(View.GONE);
        }
        else
        {
            noresultFound.setVisibility(View.VISIBLE);
            emergencyFrndz.setVisibility(View.GONE);
            loadingemergeny.setVisibility(View.GONE);
            //Toast.makeText(EmergencyActivity.this,"You have no any Emergency Friend...",Toast.LENGTH_LONG).show();
            Config.alertDialog(EmergencyActivity.this, "Emergency", "No Emergency friends are added.");
        }
    }

}

