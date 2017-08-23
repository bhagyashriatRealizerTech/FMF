package realizer.com.makemepopular;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import realizer.com.makemepopular.asynctask.CheckUserAsyntask;
import realizer.com.makemepopular.asynctask.RegistrationAsyncTaskPost;
import realizer.com.makemepopular.backend.DatabaseQueries;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.models.RegisterUserRequestModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.ImageStorage;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.UtilLocation;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.ProgressWheel;
import realizer.com.makemepopular.view.RoundedImageView;

/**
 * Created by Win on 10/01/2017.
 */
public class RegistrationActivity extends AppCompatActivity implements OnTaskCompleted,com.google.android.gms.location.LocationListener ,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    TextView ico_user, ico_email, ico_mobile, ico_calendar, ico_gender, ico_list, edt_reg_dob;
    Button registration;
    SharedPreferences sharedpreferences;
    RegisterUserRequestModel registerModel;
    RoundedImageView roundedImageView;
    EditText edt_reg_fname, edt_reg_lname, edt_reg_email, edt_mob_no;
    RadioButton rB_male, rB_female;
    Spinner spn_reg_user_acc;
    private static String fireBasetoken=null;
    LocationManager locationmanager;
    static Double currentLat, currentLog;
    DatePickerDialog.OnDateSetListener date;
    Calendar myCalendar;
    ProgressWheel loading;
    final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    public static final int MEDIA_TYPE_IMAGE = 1;
    Bitmap bitmap;
    private static final String TAG = RegistrationActivity.class.getSimpleName();
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private Uri fileUri;
    String imagebase64 = "";
    ImageView userimg;
    String localPath = "", dob = "";
    final int CROP_PIC = 3;
    private Uri picUri;
    DatabaseQueries qr;
    private static Bitmap convertedImage;
    static boolean isRespondingAPI=false;
    RegistrationAsyncTaskPost register;

    //test location
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int MY_PERMISSION_REQUEST_READ_FINE_LOCATION= 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,"Register"));
        setContentView(R.layout.registratin_activity);

        getSupportActionBar().hide();

        initializeComponants();
        qr=new DatabaseQueries(RegistrationActivity.this);
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String[] items = new String[]{"Select Account Type", "Personal", "Official", "Both"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_reg_user_acc.setAdapter(adapter);

        ico_user.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_email.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_mobile.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_calendar.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_gender.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_list.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)        // 10 seconds, in milliseconds
                .setFastestInterval(2000); // 1 second, in milliseconds

        userimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pickImageFromGallery();
                selectImage();
                //getOption();
            }
        });

        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                registerModel = new RegisterUserRequestModel();
                if (currentLat == null && currentLog == null)
                {
                    onGPSService();
                }

                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("DeviceId", telephonyManager.getDeviceId());
                edit.commit();

                String gender;
                if (rB_male.isChecked()) {
                    gender = rB_male.getText().toString();
                } else if (rB_female.isChecked()) {
                    gender = rB_female.getText().toString();
                } else {
                    gender = "";
                }

                //account type
                final String accType = spn_reg_user_acc.getItemAtPosition(spn_reg_user_acc.getSelectedItemPosition()).toString();

                if (edt_reg_fname.getText().toString().trim().length() <=0 ) {
                    Config.alertDialog(RegistrationActivity.this, "Alert", "Please Enter First Name");
                } else if (edt_reg_lname.getText().toString().trim().length() <=0 ) {
                    Config.alertDialog(RegistrationActivity.this, "Alert", "Please Enter Last Name");
                    // Toast.makeText(Registration2Activity.this, "Enter Last Name", Toast.LENGTH_SHORT).show();
                } else if (edt_reg_email.getText().toString().trim().length() <=0 ) {
                    Config.alertDialog(RegistrationActivity.this, "Alert", "Please Enter Email ID");
                    //Toast.makeText(Registration2Activity.this, "Enter Email Id", Toast.LENGTH_SHORT).show();
                } else if (!edt_reg_email.getText().toString().matches(emailPattern)) {
                    Config.alertDialog(RegistrationActivity.this, "Error", "Email ID is not Valid");
                    //Toast.makeText(Registration2Activity.this, "Enter Valid Emailid", Toast.LENGTH_SHORT).show();
                } else if (edt_mob_no.getText().toString().trim().length() <=0 ) {
                    Config.alertDialog(RegistrationActivity.this, "Alert", "Please Enter Contact Number");
                    //Toast.makeText(Registration2Activity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                } else if (edt_reg_dob.getText().toString().length() <=0 ) {
                    Config.alertDialog(RegistrationActivity.this, "Alert", "Please Select Date of Birth");
                    //Toast.makeText(Registration2Activity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                } else if (gender.equals("")) {
                    Config.alertDialog(RegistrationActivity.this, "Alert", "Please Select Gender");
                    //Toast.makeText(Registration2Activity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                } else if (accType.equalsIgnoreCase("Select Account Type")) {
                    Config.alertDialog(RegistrationActivity.this, "Alert", "Please Select Account Type");
                    //Toast.makeText(Registration2Activity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                }
                else if (currentLat == null && currentLog ==null) {
                    Config.alertDialog(RegistrationActivity.this, "Error", "Cannot find your GPS location.Please check your GPS connection or update Google Play Services.");
                    //Toast.makeText(Registration2Activity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                }else {
                    registerModel.setLastCity(getLocationName(currentLat, currentLog));
                    registerModel.setfName(edt_reg_fname.getText().toString().trim());
                    registerModel.setlName(edt_reg_lname.getText().toString().trim());
                    registerModel.setEmailId(edt_reg_email.getText().toString().trim());
                    registerModel.setContactNo(edt_mob_no.getText().toString().trim());
                    registerModel.setDob(edt_reg_dob.getText().toString());
                    registerModel.setAccountType(accType);

                    registerModel.setDeviceId(telephonyManager.getDeviceId());
                    registerModel.setGender(gender);
                    registerModel.setThumbnailUrl(imagebase64);
                    if (Config.isConnectingToInternet(RegistrationActivity.this))
                    {
                        FireBaseInitialization();
                        if (fireBasetoken != null && !fireBasetoken.isEmpty() && !fireBasetoken.equals("null"))  {
                            registerModel.setFcmRegId(fireBasetoken);
                            loading.setVisibility(View.VISIBLE);
                            registration.setEnabled(false);
                            registration.setFocusableInTouchMode(false);
                            register = new RegistrationAsyncTaskPost(registerModel, RegistrationActivity.this, RegistrationActivity.this);
                            register.execute();

                            isRespondingAPI=false;
                            new CountDownTimer( 15000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                }
                                public void onFinish() {
                                    loading.setVisibility(View.GONE);
                                    if (!isRespondingAPI)
                                    {
                                        if (register != null && register.getStatus() != AsyncTask.Status.FINISHED) {
                                            register.cancel(true);
                                            Config.alertDialog(RegistrationActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                        }
                                    }
                                }
                            }.start();
                        }
                        else
                        {
                            Config.alertDialog(RegistrationActivity.this,"FCM Error","FCM ID Not Generated....");
                        }
                    }
                    else
                    {
                        Config.alertDialog(RegistrationActivity.this,"Network Error","Your device not connected to internet");
                    }
                }
            }
        });

        myCalendar = Calendar.getInstance();

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                upDateLable();
            }

        };

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{ android.Manifest.permission.READ_CONTACTS},
                        MY_PERMISSION_REQUEST_READ_FINE_LOCATION);

                // MY_PERMISSION_REQUEST_READ_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
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

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void initializeComponants() {
        ico_user = (TextView) findViewById(R.id.reg_ico_user);
        ico_email = (TextView) findViewById(R.id.reg_ico_email);
        ico_mobile = (TextView) findViewById(R.id.ico_mobile_no);
        ico_calendar = (TextView) findViewById(R.id.reg_ico_dob);
        ico_gender = (TextView) findViewById(R.id.reg_ico_gendar);
        ico_list = (TextView) findViewById(R.id.reg_ico_accinfo);
        spn_reg_user_acc = (Spinner) findViewById(R.id.spn_reg_user_acc);
        registration = (Button) findViewById(R.id.btn_registration);
        edt_reg_fname = (EditText) findViewById(R.id.edt_reg_fname);
        edt_reg_lname = (EditText) findViewById(R.id.edt_reg_lname);
        edt_reg_email = (EditText) findViewById(R.id.edt_reg_email);
        edt_mob_no = (EditText) findViewById(R.id.edt_mob_no);
        edt_reg_dob = (TextView) findViewById(R.id.edt_reg_dob);
        rB_male = (RadioButton) findViewById(R.id.rB_male);
        rB_female = (RadioButton) findViewById(R.id.rB_female);
        userimg = (RoundedImageView) findViewById(R.id.setting_user_icon);
        loading = (ProgressWheel) findViewById(R.id.loading);
    }


    private void selectImage() {

        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    try {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/picture.jpg";
                        File imageFile = new File(imageFilePath);
                        picUri = Uri.fromFile(imageFile); // convert path to Uri
                        takePictureIntent.putExtra( MediaStore.EXTRA_OUTPUT,  picUri );
                        startActivityForResult(takePictureIntent, 1);
                    } catch(ActivityNotFoundException anfe){
                        //display an error message
                        String errorMessage = "Whoops - your device doesn't support capturing images!";
                        // Toast.makeText(UserProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        Config.alertDialog(RegistrationActivity.this, "Error",errorMessage+".");
                    }
                }
                else if (options[item].equals("Choose from Gallery"))
                {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 2);//one can be replaced with any action code
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void FireBaseInitialization() {
        fireBasetoken = FirebaseInstanceId.getInstance().getToken();
        if (fireBasetoken.equalsIgnoreCase(null) || fireBasetoken.isEmpty())
        {
            try {
                fireBasetoken=FirebaseInstanceId.getInstance().getToken(getApplication().getResources().getString(R.string.gcm_defaultSenderId), FirebaseMessaging.INSTANCE_ID_SCOPE);
            } catch (IOException e) {
                e.printStackTrace();
                FireBaseInitialization();
            }
            if (fireBasetoken.equalsIgnoreCase(null) || fireBasetoken.isEmpty())
            {
                FireBaseInitialization();
            }
        }
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(RegistrationActivity.this);
        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString("TockenID", fireBasetoken);
        edit.commit();
    }

    @Override
    public void onTaskCompleted(String s) {
        isRespondingAPI=true;
        String contactn=edt_mob_no.getText().toString();
        if (!s.equalsIgnoreCase("Already registered user") && !s.equalsIgnoreCase("Server not Responding")) {
            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(RegistrationActivity.this);
            String getValueBack = sharedpreferences.getString("UserContactNo", "");
            if(getValueBack.equals(""))
            {
                try {
                    JSONObject json = new JSONObject(s);
                    String userId=json.getString("userId");
                    String ThumbnailURl=json.getString("thumbnailUrl");
                    String gender=json.getString("gender");
                    String contactno=json.getString("contactNo");
                    String dob=json.getString("dob");
                    String userName=json.getString("fName");
                    String userLname=json.getString("lName");
                    String email=json.getString("emailId");
                    Singleton.setUserId(userId);
                    //SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(RegistrationActivity.this);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("UserId", userId);
                    edit.putString("UserContactNo", contactno);
                    edit.putString("ThumbnailURL",ThumbnailURl);
                    edit.putString("Gender",gender);
                    edit.putString("Fname",userName);
                    edit.putString("Lname",userLname);
                    edit.putString("FirstTimeLogin1","true");
                    edit.putString("FirstTimeLoginThread","true");
                    edit.putString("userLoginId",userName+" "+userLname);
                    edit.putString("EmailId",email);
                    edit.putString("DOB",dob);
                    edit.putString("TodaysTrackingCount", "0");
                    edit.putString("LastWeekTrackingCount","0");
                    edit.putString("LastMonthTrackingCount","0");
                    edit.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Bundle b=new Bundle();
                Intent interest = new Intent(RegistrationActivity.this, InterestActivity.class);
                b.putString("FromWhere","Register");
                interest.putExtras(b);
                startActivity(interest);
                finish();
            }
            else {
                if (!contactn.equals(getValueBack)) {
                    showDeleteMessageAlert(s);
                } else {
                    try {
                        JSONObject json = new JSONObject(s);
                        String userId = json.getString("userId");
                        String ThumbnailURl = json.getString("thumbnailUrl");
                        String gender = json.getString("gender");
                        String contactno = json.getString("contactNo");
                        String dob = json.getString("dob");
                        String userName = json.getString("fName");
                        String userLname = json.getString("lName");
                        String email = json.getString("emailId");
                        Singleton.setUserId(userId);
                        //SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(RegistrationActivity.this);
                        SharedPreferences.Editor edit = sharedpreferences.edit();
                        edit.putString("UserId", userId);
                        edit.putString("UserContactNo", contactno);
                        edit.putString("ThumbnailURL", ThumbnailURl);
                        edit.putString("Gender", gender);
                        edit.putString("Fname", userName);
                        edit.putString("Lname", userLname);
                        edit.putString("FirstTimeLogin1", "true");
                        edit.putString("FirstTimeLoginThread", "true");
                        edit.putString("userLoginId", userName + " " + userLname);
                        edit.putString("EmailId", email);
                        edit.putString("DOB", dob);
                        edit.putString("TodaysTrackingCount", "0");
                        edit.putString("LastWeekTrackingCount", "0");
                        edit.putString("LastMonthTrackingCount", "0");
                        edit.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Bundle b = new Bundle();
                    Intent interest = new Intent(RegistrationActivity.this, InterestActivity.class);
                    b.putString("FromWhere", "Register");
                    interest.putExtras(b);
                    startActivity(interest);
                    finish();
                    //LoginAsyncTask();
                }
            }
        } else if (s.equalsIgnoreCase("Already registered user")) {
            Config.alertDialog(RegistrationActivity.this, "Error", "Already Registered User");
        } else {
            Config.alertDialog(RegistrationActivity.this,"Error","Server not responding.Please try again after sometime");
        }
        loading.setVisibility(View.GONE);
        registration.setEnabled(true);
        registration.setFocusableInTouchMode(true);
    }


    public String getLocationName(double latitude, double longitude) {
        String cityName = "Not Found";

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String country = addresses.get(0).getCountryName();

            cityName = address+","+addresses.get(0).getAddressLine(1)+","+city+","+country;
        } catch (IOException e) {
            if (Config.isConnectingToInternet(RegistrationActivity.this))
            {
                cityName=getLocationCityName(latitude,longitude);
            }
            else
            {
                Utility.CustomToast(RegistrationActivity.this, "No Internet Connection..!");
            }
            e.printStackTrace();
        }
        return cityName;

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
        String premise="",locality="";
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
                            if( types.getString(j).equals("locality") ){
                                locality= component.getString("long_name");
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return premise+","+locality;
    }

    public boolean canGetLocation() {
        boolean result = true;
        LocationManager lm = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        LocationListener locationListener = null;
        Location location = null;

        if (lm == null)

            lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

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

    public void showRegistationFailedAlert() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegistrationActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Error!");

        // Setting Dialog Message
        alertDialog.setMessage("Registration Unsuccesefull!!!Please Try After Sometime.");
        final AlertDialog builder = alertDialog.create();
        // On pressing Settings button
        alertDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        builder.dismiss();
                    }
                });

        alertDialog.show();
    }

    public void regdobclick(View view) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(RegistrationActivity.this, date, year, month, day);
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year-18);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    public void upDateLable() {
        String myFormat = "MM/dd/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        edt_reg_dob.setText(sdf.format(myCalendar.getTime()));
        edt_reg_dob.getText().toString();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            String path="";
            if (requestCode == 2) {
                picUri = data.getData();
                try
                {
                    convertedImage = MediaStore.Images.Media.getBitmap(getContentResolver() , picUri);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    convertedImage.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                }
                catch (Exception e)
                {
                    //handle exception
                }
                performCrop();

            }
            else if (requestCode == 1) {
                //get the Uri for the captured image
                try
                {
                    convertedImage = MediaStore.Images.Media.getBitmap(getContentResolver() , picUri);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    convertedImage.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                }
                catch (Exception e)
                {
                    //handle exception
                }
                //carry out the crop operation
                performCrop();
            }
            else if (requestCode == CROP_PIC) {
                Bundle extras = data.getExtras();
                if(extras != null ) {
                    Bitmap photo = extras.getParcelable("data");
                    setPhoto(photo);
                    userimg.setImageBitmap(photo);
                    path = encodephoto(photo);
                    imagebase64 = path;
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("ProfilePicPath", path);
                    editor.commit();
                }
                else
                {
                    setPhoto(convertedImage);
                    userimg.setImageBitmap(convertedImage);
                    path = encodephoto(convertedImage);
                    imagebase64 = path;
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("ProfilePicPath", path);
                    editor.commit();
                }
            }
        } else if (resultCode == RESULT_CANCELED) {

            // user cancelled Image capture
            //Toast.makeText(getApplicationContext(),"User cancelled image capture", Toast.LENGTH_SHORT).show();
            if (requestCode != CROP_PIC) {
                Config.alertDialog(this, "Image Cancelled", "User cancelled image capture.");
            }

        } else {
            // failed to capture image
            //Toast.makeText(getApplicationContext(),"Sorry! Failed to capture image", Toast.LENGTH_SHORT).show();
            Config.alertDialog(this, "Image Error","Sorry! Failed to capture image.");
        }

    }

    /**
     * this function does the crop operation.
     */
    private void performCrop(){

        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 0);
            cropIntent.putExtra("aspectY", 0);
            //indicate output X and Y
            cropIntent.putExtra("outputX",512);
            cropIntent.putExtra("outputY", 512);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, CROP_PIC);
        }
        catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!.Setting original image to profile.";
//            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
//            toast.show();
            Config.alertDialog(RegistrationActivity.this, "Error",errorMessage+".");
            String path="";
            setPhoto(convertedImage);
            userimg.setImageBitmap(convertedImage);
            path = encodephoto(convertedImage);
            imagebase64 = path;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("ProfilePicPath", path);
            editor.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            String path="";
            setPhoto(convertedImage);
            userimg.setImageBitmap(convertedImage);
            path = encodephoto(convertedImage);
            imagebase64 = path;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("ProfilePicPath", path);
            editor.commit();
        }
    }

    //Encode image to Base64 to send to server
    private void setPhoto(Bitmap bitmapm) {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Config.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + Config.IMAGE_DIRECTORY_NAME + " directory");

            }
        }
        else {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmapm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            //4
            File file = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpeg");
            try {
                file.createNewFile();
                FileOutputStream fo = new FileOutputStream(file);
                //5
                fo.write(bytes.toByteArray());
                fo.close();
                //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file)));

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }
    }

    //Encode image to Base64 to send to server
    private String encodephoto(Bitmap bitmapm) {
        String imagebase64string = "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmapm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            byte[] byteArrayImage = baos.toByteArray();
            imagebase64string = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return imagebase64string;
    }

    public void showDeleteMessageAlert(final String s)
    {
        LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View dialoglayout = inflater.inflate(R.layout.alert_messagedelete_dialog_layout, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialoglayout);

        Button cancel= (Button) dialoglayout.findViewById(R.id.btn_msgDelet_cancel);
        Button delete= (Button) dialoglayout.findViewById(R.id.btn_msgDelet_delete);
        TextView titleName=(TextView) dialoglayout.findViewById(R.id.titleName);
        final AlertDialog alertDialog = builder.create();
        titleName.setText("Is Delete Old Messages ");

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent interest = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(interest);
                finish();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long data = qr.deleteChatData();
                if (data >= 0)
                {
                    try {
                        JSONObject json = new JSONObject(s);
                        String userId=json.getString("userId");
                        String ThumbnailURl=json.getString("thumbnailUrl");
                        String gender=json.getString("gender");
                        String contactn=json.getString("contactNo");
                        String dob=json.getString("dob");
                        String userName=json.getString("fName");
                        String userLname=json.getString("lName");
                        String email=json.getString("emailId");
                        Singleton.setUserId(userId);
                        //SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(RegistrationActivity.this);
                        SharedPreferences.Editor edit = sharedpreferences.edit();
                        edit.putString("UserId", userId);
                        edit.putString("UserContactNo", contactn);
                        edit.putString("ThumbnailURL",ThumbnailURl);
                        edit.putString("Gender",gender);
                        edit.putString("Fname",userName);
                        edit.putString("Lname",userLname);
                        edit.putString("FirstTimeLogin1","true");
                        edit.putString("FirstTimeLoginThread","true");
                        edit.putString("userLoginId",userName+" "+userLname);
                        edit.putString("EmailId",email);
                        edit.putString("DOB",dob);
                        edit.putString("TodaysTrackingCount", "0");
                        edit.putString("LastWeekTrackingCount","0");
                        edit.putString("LastMonthTrackingCount","0");
                        edit.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Bundle b=new Bundle();
                    Intent interest = new Intent(RegistrationActivity.this, InterestActivity.class);
                    b.putString("FromWhere","Register");
                    interest.putExtras(b);
                    startActivity(interest);
                    finish();
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(RegistrationActivity.this, "Old Data Not Deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            onGPSService();
        }
        else {
            handleNewLocation(location);
        }
    }

    private void handleNewLocation(Location location) {
        currentLat = location.getLatitude();
        currentLog = location.getLongitude();
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

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }


    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,RegistrationActivity.this);
            mGoogleApiClient.disconnect();
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
                Location locatin = UtilLocation.getLastKnownLoaction(true, RegistrationActivity.this);
                if (locatin != null) {
                    currentLat = locatin.getLatitude();
                    currentLog = locatin.getLongitude();
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
    public void onBackPressed() {
        super.onBackPressed();
        Intent reg=new Intent(RegistrationActivity.this,LoginActivity.class);
        startActivity(reg);
        finish();
    }
}

