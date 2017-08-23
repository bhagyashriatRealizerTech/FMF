package realizer.com.makemepopular.friendnear;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import realizer.com.makemepopular.DashboardActivity;
import realizer.com.makemepopular.R;
import realizer.com.makemepopular.asynctask.NearByfriendAsyntask;
import realizer.com.makemepopular.asynctask.TrackFriendAsynctask;
import realizer.com.makemepopular.asynctask.TrackFriendBackgroundAsyncTask;
import realizer.com.makemepopular.backend.DatabaseQueries;
import realizer.com.makemepopular.chat.model.NewFriendListModel;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.models.NearByFriends;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.ImageStorage;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.UtilLocation;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 14/01/2017.
 */
public class FriendNearActivity extends AppCompatActivity implements LocationListener, OnTaskCompleted,OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    //private Button infoButton;
    private OnInfoWindowElemTouchListener infoButtonListener;
    ArrayList<NearByFriends> nearFrndlist=new ArrayList<>();
    ProgressWheel loading;
    static boolean isRespondingAPI=false;
    TrackFriendBackgroundAsyncTask trackBackGround;
    TrackFriendAsynctask getListbyname;
    MapView mMapView;
    private GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationManager locationmanager;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Double latitude = 0.0, longitude = 0.0;
    ArrayList<FriendNearModel> markersArray = new ArrayList<>();
    String interest="";
    String flag="";
    int distance=0;
    TextView marker_name,marker_userdtls,marker_lastupdate;
    Button marker_button;
    //LinearLayout marker_outerlayout;
    String provider;
    ImageView marker_imageview;
    LinearLayout marker_outerlayout;
    MessageResultReceiver resultReceiver;
    static boolean istrackingdialogShow=false;
    String fromwhere="";
    LinearLayout friendsouter,possibilitesouter,friendbtnouter,linear_nearfriends_nofriends,linear_nextPrevbtn_outer;
    TextView tvbackbtn,txt_nearfrnd_no,pageNext,pagePrev,pageNumber;
    ProgressWheel loadingtrack;
    Spinner spnfrndlist;
    int progressDistance=25;
    static String getInterest;
    static FriendListModel selectedView;
    String[] spnlist;
    AlertDialog alertDialog;
    boolean isfromDashboard=false,isInterestSearch=false;
    static String friendId="";
    int totalPages,totalCount,pageCounter=0,totalDisplayPageno;
    DatabaseQueries qr;
    boolean isActiveActivity=false;
//    private ProgressBar firstBar = null;
//    TextView progressCount;
    static int trackTaskCount=0;
    NearByfriendAsyntask nearbycheck;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,""));
        setContentView(R.layout.friendnear_activity);
        getSupportActionBar().setTitle("Near Friend");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loading = (ProgressWheel) findViewById(R.id.loading);
        marker_name= (TextView) findViewById(R.id.txt_marker_username);
        marker_userdtls= (TextView) findViewById(R.id.txt_marker_userdtls);
        marker_outerlayout= (LinearLayout) findViewById(R.id.marker_image_linear);
        marker_imageview= (ImageView) findViewById(R.id.marker_userimg);
        marker_lastupdate= (TextView) findViewById(R.id.txt_marker_lastupdate);
        linear_nextPrevbtn_outer= (LinearLayout) findViewById(R.id.linear_pagenumber_outer);
        pageNext= (TextView) findViewById(R.id.txt_btn_next);
        pagePrev= (TextView) findViewById(R.id.txt_btn_prev);
        pageNumber= (TextView) findViewById(R.id.txt_pageno);
        qr=new DatabaseQueries(this);
        //firstBar = (ProgressBar) findViewById(R.id.progressBar);
        pageNext.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        pagePrev.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        //progressCount= (TextView) findViewById(R.id.progressCount);

        realizer.com.makemepopular.utils.Singleton obj = realizer.com.makemepopular.utils.Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);
        istrackingdialogShow=true;
        Bundle bundle=getIntent().getExtras();
        interest=bundle.getString("Interest");
        distance=bundle.getInt("Distance");
        flag=bundle.getString("Flag");
        isfromDashboard=bundle.getBoolean("isFromDashboard");
        isInterestSearch=bundle.getBoolean("isInterestSearch");
        friendId=bundle.getString("FriendId");
        fromwhere=bundle.getString("FROMWHERE");
        isActiveActivity=true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        Criteria cri = new Criteria();
        locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationmanager.getBestProvider(cri, false);
//        Location oldLocation = locationmanager.getLastKnownLocation(provider);
//        latitude=oldLocation.getLatitude();
//        longitude=oldLocation.getLongitude();
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFrag.getMapAsync(this);

        }
        if (isInterestSearch)
        {
            linear_nextPrevbtn_outer.setVisibility(View.VISIBLE);
        }
        else
        {
            linear_nextPrevbtn_outer.setVisibility(View.GONE);
        }

        pageNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pageCounter==totalPages-1)
                {
                    pageNext.setEnabled(false);
                }
                else {
                    pageNext.setEnabled(true);
                    pagePrev.setEnabled(true);
                    mMap.clear();
                    pageCounter++;
                    pageNumber.setText("Page "+(pageCounter+1)+" of "+String.valueOf(totalPages));
                    if (Config.isConnectingToInternet(FriendNearActivity.this)) {
                        loading.setVisibility(View.VISIBLE);
                        NearByfriendAsyntask check = new NearByfriendAsyntask(latitude, longitude, interest, distance, pageCounter, FriendNearActivity.this, FriendNearActivity.this);
                        check.execute();
                    } else {
                        Config.alertDialog(FriendNearActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                    }
                }
            }
        });
        pagePrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pageCounter==0)
                {
                    pagePrev.setEnabled(false);
                }
                else {
                    pagePrev.setEnabled(true);
                    pageNext.setEnabled(true);
                    mMap.clear();
                    pageCounter--;
                    pageNumber.setText("Page "+(pageCounter+1)+" of "+String.valueOf(totalPages));
                    if (Config.isConnectingToInternet(FriendNearActivity.this)) {
                        loading.setVisibility(View.VISIBLE);
                        NearByfriendAsyntask check = new NearByfriendAsyntask(latitude, longitude, interest, distance, pageCounter, FriendNearActivity.this, FriendNearActivity.this);
                        check.execute();
                    } else {
                        Config.alertDialog(FriendNearActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                    }
                }
            }
        });
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
                    //Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                    Config.alertDialog(this, "Error", "Permission denied.");
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }


    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    @Override
    public void onLocationChanged(Location location) {
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
        //Toast.makeText(FriendNearActivity.this,"Your Current Location", Toast.LENGTH_LONG).show();

        Log.d("onLocationChanged", String.format("latitude:%.3f longitude:%.3f",latitude,longitude));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }

        Log.d("onLocationChanged", "Exit");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onTaskCompleted(String s)
    {
        isRespondingAPI=true;
        String arr[]=s.split("@@@");
        if (arr[1].equalsIgnoreCase("NearByFriends"))
        {
            if (!arr[0].equalsIgnoreCase("ServerNotResponding"))
            {
                if (!arr[0].equalsIgnoreCase("") && !arr[0].equalsIgnoreCase("[]"))
                {
                    JSONArray jsonarray = null;
                    JSONObject jsonObject=null;
                    try {
                        jsonObject=new JSONObject(arr[0]);
                        totalCount=jsonObject.getInt("totalCount");
                        totalPages=jsonObject.getInt("totalPages");
                        pageNumber.setText("Page "+(pageCounter+1)+" of "+String.valueOf(totalPages));
                        jsonarray = jsonObject.getJSONArray("results");
                        nearFrndlist=new ArrayList<>();
                        for (int i = 0; i < jsonarray.length(); i++) {
                            JSONObject jsonobject = jsonarray.getJSONObject(i);
                            NearByFriends afm=new NearByFriends();
                            afm.setFriendName(jsonobject.getString("friendName"));
                            afm.setDistance(jsonobject.getInt("distance"));
                            afm.setDistanceInKm(jsonobject.getString("distanceInKm"));
                            afm.setDuration(jsonobject.getString("duration"));
                            afm.setFriendUserId(jsonobject.getString("friendUserId"));
                            //String latlog[]=jsonobject.getString("FriendsCordinates").split(",");
                            afm.setLatitude(String.valueOf(jsonobject.getString("latitude")));
                            afm.setLongitude(String.valueOf(jsonobject.getString("longitude")));
                            afm.setThumbnailUrl(jsonobject.getString("thumbnailUrl"));
                            afm.setGender(jsonobject.getString("gender"));
                            afm.setAge(jsonobject.getString("age"));
                            afm.setLastupdate(jsonobject.getString("lastUpdatedOn"));
                            nearFrndlist.add(afm);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //Place current location marker
                    for(int i = 0 ; i < nearFrndlist.size() ; i++ ) {

                        marker_name.setText(nearFrndlist.get(i).getFriendName().toString());
                        marker_userdtls.setText("" + nearFrndlist.get(i).getGender() + ", " + nearFrndlist.get(i).getAge() + " yrs");
                        String date = Utility.convertUTCdate(nearFrndlist.get(i).getLastupdate().toString().replace("T", " "));
                        String tp;
                        String datet[] = date.split(" ");
                        String time[] = datet[1].split(":");
                        int t1 = Integer.valueOf(time[0]);
                        String sentTm = "";
                        if (t1 == 12) {
                            tp = "PM";
                            sentTm = "" + t1 + ":" + time[1] + " " + tp;
                        } else if (t1 > 12) {
                            int t2 = t1 - 12;
                            tp = "PM";
                            sentTm = "" + t2 + ":" + time[1] + " " + tp;
                        } else {
                            tp = "AM";
                            sentTm = time[0] + ":" + time[1] + " " + tp;
                        }
                        marker_lastupdate.setText("Last Update: "+datet[0]+"\n"+sentTm);

                        if (nearFrndlist.get(i).getThumbnailUrl().equals("")||nearFrndlist.get(i).getThumbnailUrl().equals("null")||nearFrndlist.get(i).getThumbnailUrl().equals(null))
                        {
                            marker_imageview.setImageDrawable(getResources().getDrawable(R.drawable.user_icon));
                        }
                        else {
                       /* Bitmap bitmap=getBitmapFromURL(nearFrndlist.get(i).getThumbnailUrl().toString(), FriendNearActivity.this);
                        if (bitmap !=null)
                            marker_imageview.setImageBitmap(bitmap);
                        else
                            marker_imageview.setImageDrawable(getResources().getDrawable(R.drawable.user_icon));    */

                            String newURL= Utility.getURLImage(nearFrndlist.get(i).getThumbnailUrl().toString());
                            if(!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                                new GetImages(newURL,marker_imageview,newURL.split("/")[newURL.split("/").length-1]).execute(newURL);
                            else
                            {
                                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length - 1]);
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                                marker_imageview.setImageBitmap(bitmap);
                            }
                        }

                    /*marker_outerlayout.setDrawingCacheEnabled(true);
                    marker_outerlayout.measure(180,180);
                    marker_outerlayout.layout(0, 0, marker_outerlayout.getMeasuredWidth(), marker_outerlayout.getMeasuredHeight());
                    marker_outerlayout.buildDrawingCache(true);
                    Bitmap bm = Bitmap.createBitmap(marker_outerlayout.getDrawingCache());
                    marker_outerlayout.setDrawingCacheEnabled(false); // clear drawing cache*/

                        marker_outerlayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        marker_outerlayout.layout(0, 0, marker_outerlayout.getMeasuredWidth(), marker_outerlayout.getMeasuredHeight());
                        marker_outerlayout.buildDrawingCache();
                        Bitmap returnedBitmap = Bitmap.createBitmap(marker_outerlayout.getMeasuredWidth(), marker_outerlayout.getMeasuredHeight(),
                                Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(returnedBitmap);
                        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
                        Drawable drawable = marker_outerlayout.getBackground();
                        if (drawable != null)
                            drawable.draw(canvas);
                        marker_outerlayout.draw(canvas);

                        createMarker(Double.valueOf(nearFrndlist.get(i).getLatitude()), Double.valueOf(nearFrndlist.get(i).getLongitude()), nearFrndlist.get(i).getFriendName(),returnedBitmap);
                        LatLng origin=new LatLng(latitude,longitude);
                        LatLng dest=new LatLng(Double.valueOf(nearFrndlist.get(i).getLatitude()),Double.valueOf(nearFrndlist.get(i).getLongitude()));

                        //String url=getDirectionsUrl(origin,dest);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(dest));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
//                    DownloadTask downloadTask = new DownloadTask();
//
//                    downloadTask.execute(url);
                    }

//                //Place current location marker
//                LatLng latLng=new LatLng(latitude,longitude);
//                MarkerOptions markerOptions = new MarkerOptions();
//                markerOptions.position(latLng);
//                markerOptions.title("Current Position");
//                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
//                mCurrLocationMarker = mMap.addMarker(markerOptions);
//                //move map camera
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                }
                else
                {
                    Config.alertDialog(FriendNearActivity.this, "Error", "No people matching your interests found in the provided range. Please change the criteria or increase the range.");
                }
                loading.setVisibility(View.GONE);
                if (!isfromDashboard)
                {
                    if (alertDialog.isShowing())
                    {
                        loadingtrack.setVisibility(View.GONE);
                        alertDialog.dismiss();
                    }
                }
            }
            else  if(arr[0].contains("ServerNotResponding"))
            {
                Config.alertDialog(FriendNearActivity.this, "Error", "Server not responding.Please try again after sometime");
            }
            else
            {
                Config.alertDialog(FriendNearActivity.this, "Error", "No people matching your interests found in the provided range. Please change the criteria or increase the range.");
            }

        }
        else if (arr[1].equalsIgnoreCase("TrackFriend")) {
            if (!arr[0].contains("302")) {
                JSONObject json = null;
                try {
                    json = new JSONObject(arr[0]);

                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendNearActivity.this);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("TrackFriendName", json.getString("friendName"));
                    edit.putInt("TrackFriendLatitude", json.getInt("latitude"));
                    edit.putInt("TrackFriendLongitude", json.getInt("longitude"));
                    edit.commit();
                    if (!json.getString("friendName").equalsIgnoreCase("null")) {
                        nearFrndlist = new ArrayList<>();
                        String updatedDate = "";

                        NearByFriends near = new NearByFriends();
                        near.setGender(json.getString("gender"));
                        near.setLatitude(String.valueOf(json.getDouble("latitude")));
                        near.setLongitude(String.valueOf(json.getDouble("longitude")));
                        near.setThumbnailUrl(json.getString("thumbnailUrl"));
                        near.setFriendName(json.getString("friendName"));
                        near.setAge(json.getString("age"));
                        near.setFriendUserId(json.getString("friendId"));
                        near.setLastupdate(json.getString("lastUpdatedOn"));
                        nearFrndlist.add(near);
                        mMap.clear();
                        for (int i = 0; i < nearFrndlist.size(); i++) {

                            marker_name.setText(nearFrndlist.get(i).getFriendName().toString());
                            marker_userdtls.setText("" + nearFrndlist.get(i).getGender() + ", " + nearFrndlist.get(i).getAge() + " yrs");
                            String date = Utility.convertUTCdate(nearFrndlist.get(i).getLastupdate().toString().replace("T", " "));
                            String tp;
                            String datet[] = date.split(" ");
                            String time[] = datet[1].split(":");
                            int t1 = Integer.valueOf(time[0]);
                            String sentTm = "";
                            if (t1 == 12) {
                                tp = "PM";
                                sentTm = "" + t1 + ":" + time[1] + " " + tp;
                            } else if (t1 > 12) {
                                int t2 = t1 - 12;
                                tp = "PM";
                                sentTm = "" + t2 + ":" + time[1] + " " + tp;
                            } else {
                                tp = "AM";
                                sentTm = time[0] + ":" + time[1] + " " + tp;
                            }
                            updatedDate = datet[0] + " " + sentTm;
                            marker_lastupdate.setText("Last Update: " + datet[0] + "\n" + sentTm);
                            if (nearFrndlist.get(i).getThumbnailUrl().equals("") || nearFrndlist.get(i).getThumbnailUrl().equals("null") || nearFrndlist.get(i).getThumbnailUrl().equals(null)) {
                                marker_imageview.setImageDrawable(getResources().getDrawable(R.drawable.user_icon));
                            } else {

                                String newURL = Utility.getURLImage(nearFrndlist.get(i).getThumbnailUrl().toString());
                                if (!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                                    new GetImages(newURL, marker_imageview, newURL.split("/")[newURL.split("/").length - 1]).execute(newURL);
                                else {
                                    File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length - 1]);
                                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                                    marker_imageview.setImageBitmap(bitmap);
                                }
                            }
                            marker_outerlayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                            marker_outerlayout.layout(0, 0, marker_outerlayout.getMeasuredWidth(), marker_outerlayout.getMeasuredHeight());
                            marker_outerlayout.buildDrawingCache();
                            Bitmap returnedBitmap = Bitmap.createBitmap(marker_outerlayout.getMeasuredWidth(), marker_outerlayout.getMeasuredHeight(),
                                    Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(returnedBitmap);
                            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
                            Drawable drawable = marker_outerlayout.getBackground();
                            if (drawable != null)
                                drawable.draw(canvas);
                            marker_outerlayout.draw(canvas);

                            createMarker(Double.valueOf(nearFrndlist.get(i).getLatitude()), Double.valueOf(nearFrndlist.get(i).getLongitude()), nearFrndlist.get(i).getFriendName(), returnedBitmap);

                            LatLng dest = new LatLng(Double.valueOf(nearFrndlist.get(i).getLatitude()), Double.valueOf(nearFrndlist.get(i).getLongitude()));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(dest));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                            // mMap.setMaxZoomPreference(15);
                            LatLng origin = new LatLng(latitude, longitude);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(origin);
                            markerOptions.title("Current Position");
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                            mCurrLocationMarker = mMap.addMarker(markerOptions);
                            String url = getDirectionsUrl(origin, dest);
                            DownloadTask downloadTask = new DownloadTask();
                            downloadTask.execute(url);
                      /*  if (fromwhere.equalsIgnoreCase("Emergency"))
                        {
                        }
*/
                            String lastUpdatedOn = Utility.convertUTCdate(nearFrndlist.get(i).getLastupdate().toString().replace("T", " "));
                            String lastUpdatedOndate[] = lastUpdatedOn.split(" ");
                            String lastUpdatedOntime[] = lastUpdatedOndate[1].split(":");

                            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm");
                            String currentUpdatedOn = sdf.format(Calendar.getInstance().getTime());
                            String currentUpdatedOndate[] = currentUpdatedOn.split(" ");
                            String currentUpdatedOntime[] = currentUpdatedOndate[1].split(":");

                            if (lastUpdatedOndate[0].equalsIgnoreCase(currentUpdatedOndate[0]) &&
                                    lastUpdatedOntime[0].equalsIgnoreCase(currentUpdatedOntime[0])) {
                                int lastUpdatedmin = Integer.valueOf(lastUpdatedOntime[1]);
                                int currentUpdatedmin = Integer.valueOf(currentUpdatedOntime[1]);
                                if (lastUpdatedmin - currentUpdatedmin > 1) {
                                    //Config.alertDialog(FriendListActivity.this,"Tracking Alert","You will see location before one minute.You want to see current location???");
                                    String msg = "Your friend was located at " + updatedDate + ".Do you want to show that location???";
                                    if (istrackingdialogShow) {
                                        istrackingdialogShow = false;
                                        alertDialogNearFriend(FriendNearActivity.this, "Near Me", msg, nearFrndlist.get(i).getFriendUserId());
                                    }
                                }
                            } else {
                                String msg = "Your friend was located at " + updatedDate + ".Do you want to show that location???\n";
                                if (istrackingdialogShow) {
                                    istrackingdialogShow = false;
                                    //showTrackAgainAlert(msg,nearFrndlist.get(i).getFriendUserId(),nearFrndlist);
                                    alertDialogNearFriend(FriendNearActivity.this, "Near Me", msg, nearFrndlist.get(i).getFriendUserId());
                                }
                            }

                        }

                    } else {
                        // Toast.makeText(DashboardActivity.this,"No data available for this friend",Toast.LENGTH_LONG).show();
                        Config.alertDialog(FriendNearActivity.this, "Near Friend", "No data available for this friend.");
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (isActiveActivity && trackTaskCount == 0) {
                    trackTaskCount++;
                    Utility.customtoastnew(FriendNearActivity.this,"Click friend icon,you will see google map with more option.");
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new CustomTimerTask(), 1000, 60000);
                }

            }
        }
        else if (arr[1].equalsIgnoreCase("TrackFriendBackGround")) {
            if (!arr[0].contains("302")) {
                JSONObject json = null;
                try {
                    json = new JSONObject(arr[0]);

                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendNearActivity.this);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("TrackFriendName", json.getString("friendName"));
                    edit.putInt("TrackFriendLatitude", json.getInt("latitude"));
                    edit.putInt("TrackFriendLongitude", json.getInt("longitude"));
                    edit.commit();
                    if (!json.getString("friendName").equalsIgnoreCase("null")) {
                        nearFrndlist = new ArrayList<>();
                        String updatedDate = "";

                        NearByFriends near = new NearByFriends();
                        near.setGender(json.getString("gender"));
                        near.setLatitude(String.valueOf(json.getDouble("latitude")));
                        near.setLongitude(String.valueOf(json.getDouble("longitude")));
                        near.setThumbnailUrl(json.getString("thumbnailUrl"));
                        near.setFriendName(json.getString("friendName"));
                        near.setAge(json.getString("age"));
                        near.setFriendUserId(json.getString("friendId"));
                        near.setLastupdate(json.getString("lastUpdatedOn"));
                        nearFrndlist.add(near);
                        mMap.clear();
                        for (int i = 0; i < nearFrndlist.size(); i++) {

                            marker_name.setText(nearFrndlist.get(i).getFriendName().toString());
                            marker_userdtls.setText("" + nearFrndlist.get(i).getGender() + ", " + nearFrndlist.get(i).getAge() + " yrs");
                            String date = Utility.convertUTCdate(nearFrndlist.get(i).getLastupdate().toString().replace("T", " "));
                            String tp;
                            String datet[] = date.split(" ");
                            String time[] = datet[1].split(":");
                            int t1 = Integer.valueOf(time[0]);
                            String sentTm = "";
                            if (t1 == 12) {
                                tp = "PM";
                                sentTm = "" + t1 + ":" + time[1] + " " + tp;
                            } else if (t1 > 12) {
                                int t2 = t1 - 12;
                                tp = "PM";
                                sentTm = "" + t2 + ":" + time[1] + " " + tp;
                            } else {
                                tp = "AM";
                                sentTm = time[0] + ":" + time[1] + " " + tp;
                            }
                            updatedDate = datet[0] + " " + sentTm;
                            marker_lastupdate.setText("Last Update: " + datet[0] + "\n" + sentTm);
                            if (nearFrndlist.get(i).getThumbnailUrl().equals("") || nearFrndlist.get(i).getThumbnailUrl().equals("null") || nearFrndlist.get(i).getThumbnailUrl().equals(null)) {
                                marker_imageview.setImageDrawable(getResources().getDrawable(R.drawable.user_icon));
                            } else {

                                String newURL = Utility.getURLImage(nearFrndlist.get(i).getThumbnailUrl().toString());
                                if (!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                                    new GetImages(newURL, marker_imageview, newURL.split("/")[newURL.split("/").length - 1]).execute(newURL);
                                else {
                                    File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length - 1]);
                                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                                    marker_imageview.setImageBitmap(bitmap);
                                }
                            }
                            marker_outerlayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                            marker_outerlayout.layout(0, 0, marker_outerlayout.getMeasuredWidth(), marker_outerlayout.getMeasuredHeight());
                            marker_outerlayout.buildDrawingCache();
                            Bitmap returnedBitmap = Bitmap.createBitmap(marker_outerlayout.getMeasuredWidth(), marker_outerlayout.getMeasuredHeight(),
                                    Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(returnedBitmap);
                            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
                            Drawable drawable = marker_outerlayout.getBackground();
                            if (drawable != null)
                                drawable.draw(canvas);
                            marker_outerlayout.draw(canvas);

                            createMarker(Double.valueOf(nearFrndlist.get(i).getLatitude()), Double.valueOf(nearFrndlist.get(i).getLongitude()), nearFrndlist.get(i).getFriendName(), returnedBitmap);

                            LatLng dest = new LatLng(Double.valueOf(nearFrndlist.get(i).getLatitude()), Double.valueOf(nearFrndlist.get(i).getLongitude()));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(dest));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                            LatLng origin = new LatLng(latitude, longitude);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(origin);
                            markerOptions.title("Current Position");
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                            mCurrLocationMarker = mMap.addMarker(markerOptions);
                            String url = getDirectionsUrl(origin, dest);
                            DownloadTask downloadTask = new DownloadTask();
                            downloadTask.execute(url);
                        }
                    } else {
                        // Toast.makeText(DashboardActivity.this,"No data available for this friend",Toast.LENGTH_LONG).show();
                        Config.alertDialog(FriendNearActivity.this, "Near Friend", "No data available for this friend.");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public class CustomTimerTask extends TimerTask {

        private Handler mHandler = new Handler();

        @Override
        public void run() {
             new Thread(new Runnable() {
                @Override
                public void run() {

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                           // Toast.makeText(FriendNearActivity.this,"called after 1 min.",Toast.LENGTH_LONG).show();
                            if (Config.isConnectingToInternet(FriendNearActivity.this))
                            {
                                isRespondingAPI=false;
                                trackBackGround = new TrackFriendBackgroundAsyncTask(friendId, FriendNearActivity.this, FriendNearActivity.this);
                                trackBackGround.execute();
                                new CountDownTimer(15000, 1000) {
                                    public void onTick(long millisUntilFinished) {
                                    }

                                    public void onFinish() {
                                        if (!isRespondingAPI)
                                        {
                                            if (trackBackGround != null && trackBackGround.getStatus() != AsyncTask.Status.FINISHED) {
                                                trackBackGround.cancel(true);
                                                Config.alertDialog(FriendNearActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                                LatLng origin=new LatLng(latitude,longitude);
                                                MarkerOptions markerOptions = new MarkerOptions();
                                                markerOptions.position(origin);
                                                markerOptions.title("Current Position");
                                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                                mCurrLocationMarker = mMap.addMarker(markerOptions);
                                                mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                                                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                                            }
                                        }
                                    }

                                }.start();
                            }
                        }
                    });
                }
            }).start();
        }
    }


    public static Bitmap getBitmapFromURL(String src,Context context) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
    protected void createMarker(double latitude, double longitude, String title,Bitmap bm) {

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(title)
                .snippet("")
                .icon(BitmapDescriptorFactory.fromBitmap(bm)).visible(true));

        /*.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)).visible(true));*/

        /*.icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.drawable.map, near)))*/
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }
    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            //Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMapToolbarEnabled(true);
       // MapsInitializer.initialize(this);
        if (mMap != null) {

            if (canGetLocation() == true) {
                if (provider != null & !provider.equals(""))

                {
                    Location locatin= UtilLocation.getLastKnownLoaction(true, FriendNearActivity.this);
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

            if(flag.equalsIgnoreCase("Single"))
            {
                if (Config.isConnectingToInternet(FriendNearActivity.this))
                {
                    isRespondingAPI=false;
                    getListbyname = new TrackFriendAsynctask(friendId, FriendNearActivity.this, FriendNearActivity.this);
                    getListbyname.execute();
                    new CountDownTimer(15000, 1000) {
                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            if (!isRespondingAPI)
                            {
                                if (getListbyname != null && getListbyname.getStatus() != AsyncTask.Status.FINISHED) {
                                    getListbyname.cancel(true);
                                    Config.alertDialog(FriendNearActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                    LatLng origin=new LatLng(latitude,longitude);
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(origin);
                                    markerOptions.title("Current Position");
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                    mCurrLocationMarker = mMap.addMarker(markerOptions);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                                }
                            }
                        }

                    }.start();
                }
                else
                {
                    Config.alertDialog(FriendNearActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                }
            }
            else
            {
                if (Config.isConnectingToInternet(FriendNearActivity.this))
                {
                    loading.setVisibility(View.VISIBLE);
                    NearByfriendAsyntask check=new NearByfriendAsyntask(latitude,longitude,interest,distance,0,FriendNearActivity.this,FriendNearActivity.this);
                    check.execute();
                }
                else
                {
                    Config.alertDialog(FriendNearActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                }
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

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(FriendNearActivity.this)
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

    @Override
    public void onConnectionSuspended(int i) {

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
                Location locatin = UtilLocation.getLastKnownLoaction(true, FriendNearActivity.this);
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
            //showSettingsAlert();
        }
    }

    private void handleNewLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(18);
                lineOptions.color(Color.rgb(2, 179, 253));

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null)
            {
                mMap.addPolyline(lineOptions);
            }

        }
    }

    class UpdateUI implements Runnable {
        String update;

        public UpdateUI(String update) {

            this.update = update;
        }

        public void run() {

            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendNearActivity.this);
            if(update.equals("Emergency")) {

                String notType=sharedpreferences.getString("Type", "");
                if (notType.equalsIgnoreCase("FriendRequest"))
                {
                    String reqstName=sharedpreferences.getString("RequsetByName", "");
                    String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
                    Config.showacceptrejectFriendRequest(reqstName, thumbnail, FriendNearActivity.this);
                    //Config.showacceptrejectFriendRequest(reqstName,FriendNearActivity.this);
                }
                else if (notType.equalsIgnoreCase("Emergency"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String trobler=sharedpreferences.getString("TroublerName", "");
                    String troblerid=sharedpreferences.getString("TroublerUserId", "");
                    Config.showEmergencyAcceptReject(msg, trobler, troblerid, FriendNearActivity.this);
                }
                else if (notType.equalsIgnoreCase("EmergencyRecipt"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("HelperUserName", "");
                    String isResch=sharedpreferences.getString("isReaching", "");
                    if (isResch.equalsIgnoreCase("true")){}
                    //showEmergencyAckAlert(newMsg, helpername);
                    Config.showEmergencyAckAlert(msg, helpername, FriendNearActivity.this);
                }
                else if (notType.equalsIgnoreCase("FriendRequestAccepted"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("AcceptByName", "");

                    Config.showAccptedRequestAlert(msg, helpername, FriendNearActivity.this);
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
                FriendNearActivity.this.runOnUiThread(new UpdateUI("Emergency"));
            }
            if(resultCode == 200){
                FriendNearActivity.this.runOnUiThread(new UpdateUI("RefreshThreadList"));
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Intent i=new Intent(FriendNearActivity.this,DashboardActivity.class);
                startActivity(i);
                finish();
                isActiveActivity=false;
                break;

            case R.id.action_filter:
                FriendNearClick();
                return true;

            case R.id.map_types:
                showMapTpes();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMapTpes()
    {
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.notification_type_list_items, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(FriendNearActivity.this);
        builder.setView(dialoglayout);

        final ListView alertList=(ListView) dialoglayout.findViewById(R.id.list_notTypelist);
        final Button btn_search=(Button) dialoglayout.findViewById(R.id.btn_search);
        btn_search.setVisibility(View.GONE);

        final TextView title=(TextView) dialoglayout.findViewById(R.id.alert_dialog_title);
        title.setText("Select Map Type");

        final AlertDialog alertDialog = builder.create();

        String[] list={"Road Map", "Hybrid", "Satellite", "Terrain"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice,list);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        alertList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        alertList.setAdapter(adapter);

        int type=mMap.getMapType();
        if (type==GoogleMap.MAP_TYPE_HYBRID)
        {
            alertList.setItemChecked(1,true);
        }
        else  if (type==GoogleMap.MAP_TYPE_NORMAL)
        {
            alertList.setItemChecked(0,true);
        }
        else  if (type==GoogleMap.MAP_TYPE_SATELLITE)
        {
            alertList.setItemChecked(2,true);
        }
        else  if (type==GoogleMap.MAP_TYPE_TERRAIN)
        {
            alertList.setItemChecked(3,true);
        }


        alertList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String type = (String)alertList.getItemAtPosition(position);
                if (type.equalsIgnoreCase("Road Map"))
                {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    alertList.setItemChecked(0,true);
                }
                else  if (type.equalsIgnoreCase("Hybrid"))
                {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    alertList.setItemChecked(1,true);
                }
                else  if (type.equalsIgnoreCase("Satellite"))
                {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    alertList.setItemChecked(2,true);
                }
                else  if (type.equalsIgnoreCase("Terrain"))
                {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    alertList.setItemChecked(3,true);
                }
                alertDialog.dismiss();
            }
        });

        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i=new Intent(FriendNearActivity.this,DashboardActivity.class);
        startActivity(i);
        finish();
        isActiveActivity=false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notification_filter, menu);
        MenuItem item = menu.findItem(R.id.map_types);
        item.setVisible(true);
        return true;
    }


    public void FriendNearClick()
    {
        isfromDashboard=false;
        LayoutInflater inflater= this.getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.nearfriend_alertdialog, null);
        final Button btnFriends= (Button) dialoglayout.findViewById(R.id.btn_nearfrnds_friend);
        final Button btnPossibilition= (Button) dialoglayout.findViewById(R.id.btn_nearfrnds_possibilities);
        friendsouter= (LinearLayout) dialoglayout.findViewById(R.id.linear_nearfriends_friends);
        possibilitesouter= (LinearLayout) dialoglayout.findViewById(R.id.linear_nearfriends_posibilities);
        friendbtnouter= (LinearLayout) dialoglayout.findViewById(R.id.linear_btnOuter);
        tvbackbtn= (TextView) dialoglayout.findViewById(R.id.txt_nearby_backbtn);
        loadingtrack =(ProgressWheel) dialoglayout.findViewById(R.id.loadingtrack);
        linear_nearfriends_nofriends= (LinearLayout) dialoglayout.findViewById(R.id.linear_nearfriends_nofriends);
        txt_nearfrnd_no= (TextView) dialoglayout.findViewById(R.id.txt_nearfrnd_no);
        final TextView track= (TextView) dialoglayout.findViewById(R.id.btn_nearfrnd_all);
        track.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        TextView tvClosebtn= (TextView) dialoglayout.findViewById(R.id.txt_invitfrnd_closebtn);
        tvClosebtn.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        TextView btn_interst= (TextView) dialoglayout.findViewById(R.id.btn_nearfrnd_interest);
        btn_interst.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        tvbackbtn.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        final Spinner spninterestlst= (Spinner) dialoglayout.findViewById(R.id.spn_nearfrnd_interestlist);
        SeekBar spinrangelst= (SeekBar) dialoglayout.findViewById(R.id.seekbar);
        final TextView SeekCount= (TextView) dialoglayout.findViewById(R.id.seek_count);

        spnfrndlist= (Spinner) dialoglayout.findViewById(R.id.spn_nearfrnd_frndlist);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialoglayout);
        alertDialog = builder.create();
        spnlist= realizer.com.makemepopular.utils.Singleton.getSpnlist();

        ArrayList<FriendListModel> newfrdlist=new ArrayList<>();
        ArrayList<NewFriendListModel> newfrdlist1=new ArrayList<>();
        newfrdlist1=qr.getAllFriendsData();


        if (newfrdlist1!=null) {
            for (int i = 0; i < newfrdlist1.size(); i++) {
                FriendListModel model = new FriendListModel();
                if (newfrdlist1.get(i).getStatus().equalsIgnoreCase("Accepted")) {
                    model.setFriendName(newfrdlist1.get(i).getFriendName());
                    model.setStatus(newfrdlist1.get(i).getStatus());
                    model.setFriendId(newfrdlist1.get(i).getFriendsId());
                    newfrdlist.add(model);
                }
            }
            if (newfrdlist.size()>0)
            {
                FriendListModel model=new FriendListModel();
                model.setFriendName("All");
                newfrdlist.add(model);
                NearFriendSpinnerAdapter near=new NearFriendSpinnerAdapter(FriendNearActivity.this,newfrdlist);
                spnfrndlist.setAdapter(near);
                spnfrndlist.setSelection(0);

                linear_nearfriends_nofriends.setVisibility(View.GONE);
                friendsouter.setVisibility(View.VISIBLE);
                possibilitesouter.setVisibility(View.GONE);

                tvbackbtn.setVisibility(View.VISIBLE);
                friendbtnouter.setVisibility(View.VISIBLE);
            }
            else
            {
                //Toast.makeText(DashboardActivity.this,"Add Friend First",Toast.LENGTH_LONG).show();
                // Config.alertDialog(DashboardActivity.this,"Near Friend","You have added no friends. Please add friends first.");
                friendsouter.setVisibility(View.GONE);
                possibilitesouter.setVisibility(View.GONE);
                tvbackbtn.setVisibility(View.GONE);
                friendbtnouter.setVisibility(View.VISIBLE);
                linear_nearfriends_nofriends.setVisibility(View.VISIBLE);
                txt_nearfrnd_no.setText("You have added no friends. Please add friends first.");
            }
        }
        else
        {
            //Toast.makeText(DashboardActivity.this,"Add Friend First",Toast.LENGTH_LONG).show();
            //Config.alertDialog(DashboardActivity.this,"Near Friend","You have added no friends. Please add friends first.");
            friendsouter.setVisibility(View.GONE);
            linear_nearfriends_nofriends.setVisibility(View.VISIBLE);
            txt_nearfrnd_no.setText("You have added no friends. Please add friends first.");
            possibilitesouter.setVisibility(View.GONE);
            tvbackbtn.setVisibility(View.GONE);
            friendbtnouter.setVisibility(View.VISIBLE);
        }

        spinrangelst.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressDistance=progress;
                seekBar.setProgress(progress);
                SeekCount.setText(String.valueOf(progress)+ " KM");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        spninterestlst.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getInterest = spninterestlst.getSelectedItem().toString();
                interest=spninterestlst.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_interst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isConnectingToInternet(FriendNearActivity.this))
                {
                    linear_nextPrevbtn_outer.setVisibility(View.VISIBLE);
                    pageNext.setEnabled(true);
                    pagePrev.setEnabled(false);
                    totalCount=0;
                    totalPages=0;
                    pageCounter=0;
                    istrackingdialogShow=true;
                    loadingtrack.setVisibility(View.VISIBLE);
                    loadingtrack.setActivated(true);
                    nearbycheck=new NearByfriendAsyntask(latitude,longitude,getInterest,progressDistance,pageCounter,FriendNearActivity.this,FriendNearActivity.this);
                    nearbycheck.execute();
                    isRespondingAPI=false;
                    new CountDownTimer( 15000, 1000) {
                        public void onTick(long millisUntilFinished) {
                        }
                        public void onFinish() {
                            loadingtrack.setVisibility(View.GONE);
                            if (!isRespondingAPI)
                            {
                                if (nearbycheck != null && nearbycheck.getStatus() != AsyncTask.Status.FINISHED) {
                                    nearbycheck.cancel(true);
                                    Config.alertDialog(FriendNearActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                }
                            }
                        }
                    }.start();
                }
                else
                {
                    Config.alertDialog(FriendNearActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                }
            }
        });

        spnfrndlist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //getInterest = spnfrndlist.getSelectedItem().toString();
                Object o = parent.getItemAtPosition(position);
                FriendListModel obj = (FriendListModel) o;
                selectedView=obj;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linear_nextPrevbtn_outer.setVisibility(View.GONE);
                istrackingdialogShow=true;
                if (!selectedView.getFriendName().equalsIgnoreCase("All")) {
                    if (Config.isConnectingToInternet(FriendNearActivity.this))
                    {
                        isRespondingAPI=false;
                        friendId=selectedView.getFriendId();
                        getListbyname = new TrackFriendAsynctask(friendId, FriendNearActivity.this, FriendNearActivity.this);
                        getListbyname.execute();
                        new CountDownTimer(15000, 1000) {
                            public void onTick(long millisUntilFinished) {
                            }

                            public void onFinish() {
                                if (!isRespondingAPI)
                                {
                                    if (getListbyname != null && getListbyname.getStatus() != AsyncTask.Status.FINISHED) {
                                        getListbyname.cancel(true);
                                        Config.alertDialog(FriendNearActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                        LatLng origin=new LatLng(latitude,longitude);
                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(origin);
                                        markerOptions.title("Current Position");
                                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                        mCurrLocationMarker = mMap.addMarker(markerOptions);
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                                    }
                                }
                            }

                        }.start();
                    }
                    else
                    {
                        Config.alertDialog(FriendNearActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                    }

                } else {
                    if (Config.isConnectingToInternet(FriendNearActivity.this))
                    {
                        loadingtrack.setVisibility(View.VISIBLE);
                        loadingtrack.setActivated(true);
                        nearbycheck=new NearByfriendAsyntask(latitude,longitude,"",0,0,FriendNearActivity.this,FriendNearActivity.this);
                        nearbycheck.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        isRespondingAPI=false;
                        new CountDownTimer( 15000, 1000) {
                            public void onTick(long millisUntilFinished) {
                            }
                            public void onFinish() {
                                loadingtrack.setVisibility(View.GONE);
                                if (!isRespondingAPI)
                                {
                                    if (nearbycheck != null && nearbycheck.getStatus() != AsyncTask.Status.FINISHED) {
                                        nearbycheck.cancel(true);
                                        Config.alertDialog(FriendNearActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                    }
                                }
                            }
                        }.start();
                    }
                    else
                    {
                        Config.alertDialog(FriendNearActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                    }
                }
                alertDialog.dismiss();
            }
        });

        btnFriends.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFriends.setBackgroundResource(R.drawable.friends_icon);
                btnPossibilition.setBackgroundResource(R.drawable.unselect_possibilities);

                ArrayList<FriendListModel> newfrdlist=new ArrayList<>();
                ArrayList<NewFriendListModel> newfrdlist1=new ArrayList<>();
                newfrdlist1=qr.getAllFriendsData();


                if (newfrdlist1!=null) {
                    for (int i = 0; i < newfrdlist1.size(); i++) {
                        FriendListModel model = new FriendListModel();
                        if (newfrdlist1.get(i).getStatus().equalsIgnoreCase("Accepted")) {
                            model.setFriendName(newfrdlist1.get(i).getFriendName());
                            model.setStatus(newfrdlist1.get(i).getStatus());
                            model.setFriendId(newfrdlist1.get(i).getFriendsId());
                            newfrdlist.add(model);
                        }
                    }
                    if (newfrdlist.size()>0)
                    {
                        FriendListModel model=new FriendListModel();
                        model.setFriendName("All");
                        newfrdlist.add(model);
                        NearFriendSpinnerAdapter near=new NearFriendSpinnerAdapter(FriendNearActivity.this,newfrdlist);
                        spnfrndlist.setAdapter(near);
                        spnfrndlist.setSelection(0);

                        linear_nearfriends_nofriends.setVisibility(View.GONE);
                        friendsouter.setVisibility(View.VISIBLE);
                        possibilitesouter.setVisibility(View.GONE);

                        tvbackbtn.setVisibility(View.VISIBLE);
                        friendbtnouter.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        //Toast.makeText(DashboardActivity.this,"Add Friend First",Toast.LENGTH_LONG).show();
                        // Config.alertDialog(DashboardActivity.this,"Near Friend","You have added no friends. Please add friends first.");
                        friendsouter.setVisibility(View.GONE);
                        possibilitesouter.setVisibility(View.GONE);
                        tvbackbtn.setVisibility(View.GONE);
                        friendbtnouter.setVisibility(View.VISIBLE);
                        linear_nearfriends_nofriends.setVisibility(View.VISIBLE);
                        txt_nearfrnd_no.setText("You have added no friends. Please add friends first.");
                    }
                }
                else
                {
                    //Toast.makeText(DashboardActivity.this,"Add Friend First",Toast.LENGTH_LONG).show();
                    //Config.alertDialog(DashboardActivity.this,"Near Friend","You have added no friends. Please add friends first.");
                    friendsouter.setVisibility(View.GONE);
                    linear_nearfriends_nofriends.setVisibility(View.VISIBLE);
                    txt_nearfrnd_no.setText("You have added no friends. Please add friends first.");
                    possibilitesouter.setVisibility(View.GONE);
                    tvbackbtn.setVisibility(View.GONE);
                    friendbtnouter.setVisibility(View.VISIBLE);
                }
            }
        });

        btnPossibilition.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        btnPossibilition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPossibilition.setBackgroundResource(R.drawable.possibilities_icon);
                btnFriends.setBackgroundResource(R.drawable.unselect_friends);
                if (spnlist !=null && spnlist.length >0)
                {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(FriendNearActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, spnlist);
                    //adapter.setDropDownViewResource(R.layout.spinner_list_item);
                    spninterestlst.setAdapter(adapter);
                    friendsouter.setVisibility(View.GONE);
                    linear_nearfriends_nofriends.setVisibility(View.GONE);
                    possibilitesouter.setVisibility(View.VISIBLE);

                    tvbackbtn.setVisibility(View.VISIBLE);
                    friendbtnouter.setVisibility(View.VISIBLE);
                }
                else
                {
                    friendsouter.setVisibility(View.GONE);
                    linear_nearfriends_nofriends.setVisibility(View.VISIBLE);
                    possibilitesouter.setVisibility(View.GONE);
                    txt_nearfrnd_no.setText("You have selected no interests. Please select interest first.");
                    tvbackbtn.setVisibility(View.GONE);
                    friendbtnouter.setVisibility(View.VISIBLE);
                    // Toast.makeText(DashboardActivity.this,"No Interest Added.Please Add Interest!!!",Toast.LENGTH_LONG).show();
                    //Config.alertDialog(DashboardActivity.this,"Near Friend","You have selected no interests. Please select interest first.");
                }
            }
        });

        tvbackbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tvbackbtn.setVisibility(View.GONE);
                friendbtnouter.setVisibility(View.VISIBLE);
                friendsouter.setVisibility(View.VISIBLE);
                linear_nearfriends_nofriends.setVisibility(View.GONE);
                possibilitesouter.setVisibility(View.GONE);

            }
        });

        tvClosebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void alertDialogNearFriend(final Context context, String title, String message,final String friendid) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View dialoglayout = inflater.inflate(R.layout.alert_custom_near_friend, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialoglayout);

        Button alert_btn_cancel= (Button) dialoglayout.findViewById(R.id.alert_btn_cancel);
        Button buttonok= (Button) dialoglayout.findViewById(R.id.alert_btn_ok);
        TextView titleName=(TextView) dialoglayout.findViewById(R.id.alert_dialog_title);
        TextView alertMsg=(TextView) dialoglayout.findViewById(R.id.alert_dialog_message);


        final AlertDialog alertDialog = builder.create();

        buttonok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Config.isConnectingToInternet(FriendNearActivity.this))
                {
                    istrackingdialogShow=false;
                    getListbyname = new TrackFriendAsynctask(friendId, FriendNearActivity.this, FriendNearActivity.this);
                    getListbyname.execute();
                    new CountDownTimer(15000, 1000) {
                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            if (!isRespondingAPI)
                            {
                                if (getListbyname != null && getListbyname.getStatus() != AsyncTask.Status.FINISHED) {
                                    getListbyname.cancel(true);
                                    Config.alertDialog(FriendNearActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                    LatLng origin=new LatLng(latitude,longitude);
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(origin);
                                    markerOptions.title("Current Position");
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                    mCurrLocationMarker = mMap.addMarker(markerOptions);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                                }
                            }
                        }

                    }.start();
                }
                else
                {
                    Config.alertDialog(FriendNearActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                }

                alertDialog.dismiss();
            }
        });

        alert_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                istrackingdialogShow=false;
                alertDialog.dismiss();
            }
        });

        titleName.setText(title);
        alertMsg.setText(message);

        alertDialog.show();

    }

    public void showTrackAgainAlert(final String msg,final String friendid,final ArrayList<NearByFriends> nearFrndlist) {

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(FriendNearActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Tracking Alert");

        // Setting Dialog Message
        alertDialog.setMessage(msg);

        // On pressing Settings button
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (Config.isConnectingToInternet(FriendNearActivity.this))
                        {
                            istrackingdialogShow=false;
                            getListbyname = new TrackFriendAsynctask(friendId, FriendNearActivity.this, FriendNearActivity.this);
                            getListbyname.execute();
                            new CountDownTimer(15000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                }

                                public void onFinish() {
                                    if (!isRespondingAPI)
                                    {
                                        if (getListbyname != null && getListbyname.getStatus() != AsyncTask.Status.FINISHED) {
                                            getListbyname.cancel(true);
                                            Config.alertDialog(FriendNearActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                            LatLng origin=new LatLng(latitude,longitude);
                                            MarkerOptions markerOptions = new MarkerOptions();
                                            markerOptions.position(origin);
                                            markerOptions.title("Current Position");
                                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                            mCurrLocationMarker = mMap.addMarker(markerOptions);
                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                                            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                                        }
                                    }
                                }

                            }.start();
                        }
                        else
                        {
                            Config.alertDialog(FriendNearActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                        }

                        dialog.dismiss();
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        istrackingdialogShow=false;
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }


    public class GetImageForMap extends AsyncTask<Object, Object, Object> {
        private String requestUrl, imagename_;
        private ImageView view;
        private Bitmap bitmap ;
        private FileOutputStream fos;
        private FriendListModel friendmodel;

        public GetImageForMap(FriendListModel model,String requestUrl, ImageView view, String _imagename_) {
            this.requestUrl = requestUrl;
            this.view = view;
            this.imagename_ = _imagename_ ;
            this.friendmodel=model;
        }

        @Override
        protected Object doInBackground(Object... objects) {
            try {
                URL url = new URL(requestUrl);
                URLConnection conn = url.openConnection();
                bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            } catch (Exception ex) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {

            if(bitmap != null) {
                if (!ImageStorage.checkifImageExists(imagename_)) {
                    view.setImageBitmap(bitmap);
                    ImageStorage.saveToSdCard(bitmap, imagename_);
                }
            }
            else
            {
                view.setImageResource(R.drawable.user_icon);
            }
        }
    }

    public class GetImages extends AsyncTask<Object, Object, Object> {
        private String requestUrl, imagename_;
        private ImageView view;
        private Bitmap bitmap ;
        private FileOutputStream fos;
        public GetImages(String requestUrl, ImageView view, String _imagename_) {
            this.requestUrl = requestUrl;
            this.view = view;
            this.imagename_ = _imagename_ ;
        }

        @Override
        protected Object doInBackground(Object... objects) {
            try {
                URL url = new URL(requestUrl);
                URLConnection conn = url.openConnection();
                bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            } catch (Exception ex) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {

            if(bitmap != null) {
                if (!ImageStorage.checkifImageExists(imagename_)) {
                    //view.setImageBitmap(bitmap);
                    ImageStorage.saveToSdCard(bitmap, imagename_);
                }
            }
            else
            {
                view.setImageResource(R.drawable.user_icon);
            }
        }
    }

    public void setApiRespond()
    {
       isRespondingAPI=false;
        if (!isRespondingAPI)
        {
            Config.alertDialog(FriendNearActivity.this, "Error", "Server is not responding.Please try again after sometime.");
        }
    }
}
