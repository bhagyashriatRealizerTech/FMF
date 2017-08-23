package realizer.com.makemepopular;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import realizer.com.makemepopular.asynctask.CheckUserAsyntask;
import realizer.com.makemepopular.backend.DatabaseQueries;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.service.AutoSyncService;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 10/01/2017.
 */
public class LoginActivity extends AppCompatActivity implements OnTaskCompleted
{
    ProgressWheel loading;
    private static String fireBasetoken="";
    EditText editphoneno;
    DatabaseQueries qr;
    boolean chkServices=true;
    static boolean isRespondingAPI=false;
    CheckUserAsyntask check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,"Login"));
        setContentView(R.layout.login_activity);
        qr=new DatabaseQueries(LoginActivity.this);
        editphoneno= (EditText) findViewById(R.id.edt_mob_no);
        TextView phone_ico= (TextView) findViewById(R.id.ico_mobileno);
        TextView signupbtn= (TextView) findViewById(R.id.login_signup_btn);
        TextView loginmapimg= (TextView) findViewById(R.id.login_mapimg);
        Button btn_Submit= (Button) findViewById(R.id.btn_Submit);
        loading =(ProgressWheel) findViewById(R.id.loading);

        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission())
            {
                requestForSpecificPermission();
                //sleeptime=4000;

            }
        }

        chkServices=CheckGooglePlayServices();


        //loginmapimg.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME2));

        btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (chkServices)
               {
                   Config.hideSoftKeyboardWithoutReq(LoginActivity.this, editphoneno);
                   if(!editphoneno.getText().toString().equals("") && editphoneno.getText().length()==10)
                   {
                       SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                       String getValueBack = sharedpreferences.getString("UserContactNo", "");
                       if (getValueBack.equalsIgnoreCase(""))
                       {
                           LoginAsyncTask();
                       }
                       else
                       {
                           if (!editphoneno.getText().toString().equals(getValueBack))
                           {
                               showDeleteMessageAlert();
                           }
                           else
                           {
                               LoginAsyncTask();
                           }
                       }
                   }
                   else
                   {
                       Config.alertDialog(LoginActivity.this, "Suggestion", "Please Enter Valid Mobile Number.");
                   }
               }
               else
               {
                   GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
                   int result = googleAPI.isGooglePlayServicesAvailable(LoginActivity.this);
                   if(result != ConnectionResult.SUCCESS) {
                       if(googleAPI.isUserResolvableError(result)) {
                           googleAPI.getErrorDialog(LoginActivity.this, result,
                                   0).show();
                       }
                   }
               }
            }
        });

        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkServices)
                {
                    Intent reg=new Intent(LoginActivity.this,RegistrationActivity.class);
                    startActivity(reg);
                    finish();
                }
                else
                {
                    GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
                    int result = googleAPI.isGooglePlayServicesAvailable(LoginActivity.this);
                    if(result != ConnectionResult.SUCCESS) {
                        if(googleAPI.isUserResolvableError(result)) {
                            googleAPI.getErrorDialog(LoginActivity.this, result,
                                    0).show();
                        }
                    }
                }
            }
        });

        getSupportActionBar().hide();
        phone_ico.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));


        if (canGetLocation() == true) {
        } else {
            showSettingsAlert();
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

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]
                {
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.READ_PHONE_STATE,
                        android.Manifest.permission.GET_ACCOUNTS,
                        android.Manifest.permission.READ_SMS,
                        android.Manifest.permission.READ_CONTACTS,
                        android.Manifest.permission.WRITE_CONTACTS,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_NETWORK_STATE,
                        android.Manifest.permission.WAKE_LOCK,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA

                }, 101);
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

    public void FireBaseInitialization() {
        fireBasetoken = FirebaseInstanceId.getInstance().getToken();
        if (fireBasetoken!=null) {
            if (fireBasetoken.equalsIgnoreCase(null) || fireBasetoken.isEmpty()) {
                try {
                    fireBasetoken = FirebaseInstanceId.getInstance().getToken(getApplication().getResources().getString(R.string.gcm_defaultSenderId), FirebaseMessaging.INSTANCE_ID_SCOPE);
                } catch (IOException e) {
                    e.printStackTrace();
                    FireBaseInitialization();
                }
                if(fireBasetoken!=null) {
                    if (fireBasetoken.equalsIgnoreCase(null) || fireBasetoken.isEmpty()) {
                        //   FireBaseInitialization();
                        Config.alertDialog(this, "Error", "Please Try Again.");
                    }
                    else
                    {
                        storeToken();
                    }
                }
                else
                {
                    Config.alertDialog(this, "Error", "Please Try Again.");
                }
            }
            else
            {
                storeToken();
            }
        }
        else if (fireBasetoken.equalsIgnoreCase(null) || fireBasetoken.isEmpty()|| fireBasetoken.equals("null")) {
            try {
                fireBasetoken = FirebaseInstanceId.getInstance().getToken(getApplication().getResources().getString(R.string.gcm_defaultSenderId), FirebaseMessaging.INSTANCE_ID_SCOPE);
            } catch (IOException e) {
                e.printStackTrace();
                FireBaseInitialization();
            }
            if(fireBasetoken!=null) {
                if (fireBasetoken.equalsIgnoreCase(null) || fireBasetoken.isEmpty()) {
                    //   FireBaseInitialization();
                    Config.alertDialog(this, "Error", "Please Try Again.");
                }
                else
                {
                    storeToken();
                }
            }
            else
            {
                Config.alertDialog(this, "Error", "Please Try Again.");
            }
        }
        else
        {
            storeToken();
        }
        /*{
            FireBaseInitialization();
        }*/
    }

    public void storeToken()
    {
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString("TockenID", fireBasetoken);
        edit.commit();
    }

    @Override
    public void onTaskCompleted(String s) {
        isRespondingAPI=true;
        if (!s.equalsIgnoreCase(""))
        {
            if (!s.equalsIgnoreCase("ServerNotResponding"))
            {
                try {
                    JSONObject json = new JSONObject(s);
                    Log.d("json",json.toString());
                    String userId=json.getString("userId");
                    String ThumbnailURl=json.getString("thumbnailUrl");
                    String gender=json.getString("gender");
                    String contactn=json.getString("contactNo");
                    String dob=json.getString("dob");
                    String userName=json.getString("fName");
                    String userLname=json.getString("lName");
                    String email=json.getString("emailId");

                    if (!userId.equalsIgnoreCase("00000000-0000-0000-0000-000000000000"))
                    {
                        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
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
                        edit.putString("Login", "true");
                        edit.putString("TodaysTrackingCount", json.getString("todayTrackedCount"));
                        edit.putString("LastWeekTrackingCount",json.getString("lastWeekTrackedCount"));
                        edit.putString("LastMonthTrackingCount",json.getString("lastMonthTrackedCount"));
                        edit.putString("PendingFriendRequestCount",json.getString("friendBadgeCount"));
                        edit.putString("UnreadChatCount",json.getString("chatBadgeCount"));
                        edit.commit();

                        Intent interest=new Intent(LoginActivity.this,DashboardActivity.class);
                        startActivity(interest);

                        Intent ser = new Intent(LoginActivity.this, AutoSyncService.class);
                        ser.putExtra("FirstTime", "");
                        startService(ser);

                        finish();

                    }
                    else
                    {
                        showLoginFailedAlert();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Config.alertDialog(LoginActivity.this, "Error", "Server not responding.Please try again after sometime");
            }

            loading.setVisibility(View.GONE);
        }
        else
        {
            showLoginFailedAlert();
            loading.setVisibility(View.GONE);
        }
    }

    public void showLoginFailedAlert() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Error!");

        // Setting Dialog Message
        alertDialog.setMessage("Login Unsuccesefull");
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

    public void showDeleteMessageAlert()
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
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long data = qr.deleteChatData();
                if (data >= 0) {
                    LoginAsyncTask();
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(LoginActivity.this, "Old Data Not Deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.show();
    }

    public void LoginAsyncTask()
    {
        if (Config.isConnectingToInternet(LoginActivity.this))
        {
            FireBaseInitialization();
            if (fireBasetoken != null) {
                if (!fireBasetoken.equals(""))
                {
                    loading.setVisibility(View.VISIBLE);
                    check = new CheckUserAsyntask(editphoneno.getText().toString(), fireBasetoken, LoginActivity.this, LoginActivity.this);
                    check.execute();
                    new CountDownTimer( 15000, 1000) {
                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            loading.setVisibility(View.GONE);
                            if (!isRespondingAPI)
                            {
                                if (check != null && check.getStatus() != AsyncTask.Status.FINISHED) {
                                    check.cancel(true);
                                    Config.alertDialog(LoginActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                }
                            }
                        }

                    }.start();

                }
                else
                {
                    Config.alertDialog(LoginActivity.this, "FCM Error", "FCM ID Not Generated....");
                }
            }
            else
            {
                Config.alertDialog(LoginActivity.this, "FCM Error", "FCM ID Not Generated....");
            }
        }
        else
        {
            Config.alertDialog(LoginActivity.this, "Network Error", "Your device not connected to internet");
           // Utility.CustomToast(LoginActivity.this, "No Internet Cosnnection..!");
        }
    }
}
