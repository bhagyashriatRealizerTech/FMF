package realizer.com.makemepopular.notifications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import realizer.com.makemepopular.DashboardActivity;
import realizer.com.makemepopular.R;
import realizer.com.makemepopular.asynctask.AcceptRejectFriendRequestAsyntask;
import realizer.com.makemepopular.asynctask.EmergencyAcknowledgementAsynctask;
import realizer.com.makemepopular.asynctask.TrackFriendAsynctask;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.friendnear.FriendNearActivity;
import realizer.com.makemepopular.models.NearByFriends;
import realizer.com.makemepopular.service.TrackShowMap;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.GetImages;
import realizer.com.makemepopular.utils.ImageStorage;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by shree on 1/25/2017.
 */
public class NotificationDetailsActivity extends AppCompatActivity implements OnTaskCompleted {

    TextView txt_notidtls_byname,txt_notidtls_notidate,txt_notidtls_notitype,txt_notidtls_notitxt;
    Button btn_accept,btn_reject,btn_emergency_reject,btn_ackmap,btn_share;
    String fromUserID,toUserId,profilePic;
    ProgressWheel loading;
    Boolean isReceived=false;
    Boolean isRead=false;
    String notificationId="";
    ImageView userimg;
    LinearLayout emergencyLinearLayout,friendrequestLayout;
    MessageResultReceiver resultReceiver;
    static boolean isRespondingAPI=false;
    AcceptRejectFriendRequestAsyntask sendalert;
    EmergencyAcknowledgementAsynctask aer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,""));
        setContentView(R.layout.notification_detail_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notification Details");

        realizer.com.makemepopular.utils.Singleton obj = realizer.com.makemepopular.utils.Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);
        txt_notidtls_byname = (TextView)findViewById(R.id.txt_notidtls_byname);
        txt_notidtls_notidate = (TextView)findViewById(R.id.txt_notidtls_notidate);
        txt_notidtls_notitype = (TextView)findViewById(R.id.txt_notidtls_notitype);
        txt_notidtls_notitxt = (TextView)findViewById(R.id.txt_notidtls_notitxt);
        btn_accept = (Button)findViewById(R.id.btn_accept);
        btn_reject = (Button)findViewById(R.id.btn_reject);
        btn_share = (Button)findViewById(R.id.btn_emergency_share);
        userimg= (ImageView) findViewById(R.id.user_img);

        btn_emergency_reject = (Button)findViewById(R.id.btn_emergency_reject);
        btn_ackmap = (Button)findViewById(R.id.btn_ackmap);

        loading =(ProgressWheel) findViewById(R.id.loading);
        emergencyLinearLayout = (LinearLayout)findViewById(R.id.emergencyLinearLayout);
        friendrequestLayout = (LinearLayout)findViewById(R.id.friendrequestLayout);

        Bundle bundle=getIntent().getExtras();
        String frndName=bundle.getString("FriendName");
        String message=bundle.getString("Message");
        String time=bundle.getString("Time");
        String type=bundle.getString("Type");
        fromUserID=bundle.getString("FromUserID");
        toUserId=bundle.getString("ToUserId");
        isReceived=bundle.getBoolean("IsReceived");
        isRead=bundle.getBoolean("IsRead");
        notificationId=bundle.getString("NotificationID");
        profilePic=bundle.getString("ProfilePic");

        SetThumbnail();

        if (type.equalsIgnoreCase("FriendRequest") && isReceived && !isRead)
        {
            friendrequestLayout.setVisibility(View.VISIBLE);
            emergencyLinearLayout.setVisibility(View.GONE);
        }
        else if (type.equalsIgnoreCase("Emergency"))
        {
            friendrequestLayout.setVisibility(View.GONE);
            if (isReceived)
                emergencyLinearLayout.setVisibility(View.VISIBLE);
            else
                emergencyLinearLayout.setVisibility(View.GONE);
        }
        else
        {
            friendrequestLayout.setVisibility(View.GONE);
            emergencyLinearLayout.setVisibility(View.GONE);
        }

        txt_notidtls_byname.setText(frndName);
        txt_notidtls_notidate.setText(DateFormat(time));
        txt_notidtls_notitxt.setText(message);

        if (type.equalsIgnoreCase("FriendRequest")) {
            txt_notidtls_notitype.setText("Friend Request");
        }
        else  if (type.equalsIgnoreCase("FriendRequestAccepted")) {
            txt_notidtls_notitype.setText("Friend Request Accepted");
        }
        else  if (type.equalsIgnoreCase("FriendRequestRejected")) {
            txt_notidtls_notitype.setText("Friend Request Rejected");
        }
        else  if (type.equalsIgnoreCase("EmergencyRecipt")) {
            txt_notidtls_notitype.setText("Emergency Receipt");
        }
        else  if (type.equalsIgnoreCase("TrackingStarted")) {
            txt_notidtls_notitype.setText("Tracking");
        }
        else
        {
            txt_notidtls_notitype.setText(type);
        }

        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isConnectingToInternet(NotificationDetailsActivity.this))
                {
                    loading.setVisibility(View.VISIBLE);
                    sendalert=new AcceptRejectFriendRequestAsyntask(notificationId,"AcceptRequest",fromUserID,false,true,NotificationDetailsActivity.this,NotificationDetailsActivity.this);
                    sendalert.execute();
                    isRespondingAPI=false;
                    new CountDownTimer( 15000, 1000) {
                        public void onTick(long millisUntilFinished) {
                        }
                        public void onFinish() {
                            loading.setVisibility(View.GONE);
                            if (!isRespondingAPI)
                            {
                                if (sendalert != null && sendalert.getStatus() != AsyncTask.Status.FINISHED) {
                                    sendalert.cancel(true);
                                    Config.alertDialog(NotificationDetailsActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                }
                            }
                        }
                    }.start();
                }
                else
                {
                    Config.alertDialog(NotificationDetailsActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                }
            }
        });

        btn_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isConnectingToInternet(NotificationDetailsActivity.this))
                {
                    loading.setVisibility(View.VISIBLE);
                    sendalert=new AcceptRejectFriendRequestAsyntask(notificationId,"RejectRequest",fromUserID,false,false,NotificationDetailsActivity.this,NotificationDetailsActivity.this);
                    sendalert.execute();
                    isRespondingAPI=false;
                    new CountDownTimer( 15000, 1000) {
                        public void onTick(long millisUntilFinished) {
                        }
                        public void onFinish() {
                            loading.setVisibility(View.GONE);
                            if (!isRespondingAPI)
                            {
                                if (sendalert != null && sendalert.getStatus() != AsyncTask.Status.FINISHED) {
                                    sendalert.cancel(true);
                                    Config.alertDialog(NotificationDetailsActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                }
                            }
                        }
                    }.start();
                }
                else
                {
                    Config.alertDialog(NotificationDetailsActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                }
            }
        });

       /* btn_ack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isConnectingToInternet(NotificationDetailsActivity.this))
                {
                    loading.setVisibility(View.VISIBLE);
                    EmergencyAcknowledgementAsynctask aer=new EmergencyAcknowledgementAsynctask(notificationId,"Acknowledge",fromUserID,true,"Acknowledge Your Request.",NotificationDetailsActivity.this,NotificationDetailsActivity.this);
                    aer.execute();
                }
                else
                {
                    Config.alertDialog(NotificationDetailsActivity.this, "Network Error", "No Internet connection");
                }
            }
        });*/

        btn_emergency_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isConnectingToInternet(NotificationDetailsActivity.this))
                {
                    loading.setVisibility(View.VISIBLE);
                    aer = new EmergencyAcknowledgementAsynctask(notificationId,"Reject", fromUserID, false, "Not Acknowledge Your Request.", NotificationDetailsActivity.this, NotificationDetailsActivity.this);
                    aer.execute();
                    isRespondingAPI=false;
                    new CountDownTimer( 15000, 1000) {
                        public void onTick(long millisUntilFinished) {
                        }
                        public void onFinish() {
                            loading.setVisibility(View.GONE);
                            if (!isRespondingAPI)
                            {
                                if (aer != null && aer.getStatus() != AsyncTask.Status.FINISHED) {
                                    aer.cancel(true);
                                    Config.alertDialog(NotificationDetailsActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                }
                            }
                        }
                    }.start();
                }
                else
                {
                    Config.alertDialog(NotificationDetailsActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                }
            }
        });

        btn_ackmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isConnectingToInternet(NotificationDetailsActivity.this))
                {
                    loading.setVisibility(View.VISIBLE);
                    aer = new EmergencyAcknowledgementAsynctask(notificationId,"ACK & Map", fromUserID, true, "Acknowledge Your Request.", NotificationDetailsActivity.this, NotificationDetailsActivity.this);
                    aer.execute();
                    isRespondingAPI=false;
                    new CountDownTimer( 15000, 1000) {
                        public void onTick(long millisUntilFinished) {
                        }
                        public void onFinish() {
                            loading.setVisibility(View.GONE);
                            if (!isRespondingAPI)
                            {
                                if (aer != null && aer.getStatus() != AsyncTask.Status.FINISHED) {
                                    aer.cancel(true);
                                    Config.alertDialog(NotificationDetailsActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                }
                            }
                        }
                    }.start();
                }
                else
                {
                    Config.alertDialog(NotificationDetailsActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                }
            }
        });


        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareAddress(NotificationDetailsActivity.this,txt_notidtls_notitxt.getText().toString());
            }
        });
    }

    public static void shareAddress(Context context, String msg)
    {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }


    public void SetThumbnail() {
        if (profilePic.equals("") || profilePic.equals("null") || profilePic.equals(null)) {

        } else {
            String newURL = Utility.getURLImage(profilePic);
            if (!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                new GetImages(newURL, userimg, newURL.split("/")[newURL.split("/").length - 1]).execute(newURL);
            else {
                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length - 1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                userimg.setImageBitmap(bitmap);
            }
        }
    }

    public String DateFormat(String dateinput)
    {
        String[] setnttime=dateinput.split("T");
        String[] date=setnttime[0].split("-");
        String month= Config.getMonth(Integer.valueOf(date[1]));
        String newdate=date[2]+"-"+ month+"-"+date[0];
        return newdate;
    }

    @Override
    public void onTaskCompleted(String s) {
        isRespondingAPI=true;
        String arr[]=s.split("@@@");
        if (arr[1].equalsIgnoreCase("AcceptReject"))
        {
            if (arr[0].equalsIgnoreCase("true"))
            {
                if (arr[2].equalsIgnoreCase("AcceptRequest"))
                    Config.alertDialog(NotificationDetailsActivity.this, "Friend Request","Friend Request Accepted Successfully");
                   // Toast.makeText(NotificationDetailsActivity.this, "Friend Request Accepted Successfully", Toast.LENGTH_SHORT).show();
                else
                    Config.alertDialog(NotificationDetailsActivity.this, "Friend Request","Friend Request Rejected Successfully");
                   // Toast.makeText(NotificationDetailsActivity.this, "Friend Request Rejected Successfully", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if (arr[2].equalsIgnoreCase("AcceptRequest"))
                    Config.alertDialog(NotificationDetailsActivity.this, "Friend Request","Friend Request Not Accepted");
                    //Toast.makeText(NotificationDetailsActivity.this, "Friend Request Not Accepted", Toast.LENGTH_SHORT).show();
                else
                    Config.alertDialog(NotificationDetailsActivity.this, "Friend Request","Friend Request Not Rejected");
                   // Toast.makeText(NotificationDetailsActivity.this, "Friend Request Not Rejected", Toast.LENGTH_SHORT).show();
            }

            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NotificationDetailsActivity.this);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("Type", "");
            edit.putString("RequsetByName", "");
            edit.putString("ThumbnailUrl","");
            edit.putString("RequsetByUserId","");
            edit.commit();

            Intent intent = new Intent(NotificationDetailsActivity.this, Notification_activity.class);
            Bundle bundle = new Bundle();
            bundle.putString("FriendID",fromUserID);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();

            loading.setVisibility(View.GONE);
        }
        else if (arr[1].equalsIgnoreCase("EmergencyAck"))
        {
            if (arr[0].equalsIgnoreCase("true"))
            {
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NotificationDetailsActivity.this);
                if (arr[2].equalsIgnoreCase("ACK & Map")) {
                 /*   Intent intent = new Intent(NotificationDetailsActivity.this, TrackShowMap.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("FriendName", sharedpreferences.getString("TroublerName", ""));
                    bundle.putDouble("LATITUDE", Double.valueOf(sharedpreferences.getString("Latitude", "")));
                    bundle.putDouble("LONGITUDE", Double.valueOf(sharedpreferences.getString("Longitude", "")));
                    intent.putExtras(bundle);
                    startActivity(intent);*/
                    if (Config.isConnectingToInternet(NotificationDetailsActivity.this)) {
                        loading.setVisibility(View.VISIBLE);
                        TrackFriendAsynctask getListbyname = new TrackFriendAsynctask(fromUserID, NotificationDetailsActivity.this, NotificationDetailsActivity.this);
                        getListbyname.execute();
                    } else {
                        Config.alertDialog(NotificationDetailsActivity.this, " Internet Connection", "Make sure your device is connected to the internet.");
                    }
                }
                else
                {
                    Intent intent = new Intent(NotificationDetailsActivity.this, Notification_activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("FriendID",fromUserID);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                    loading.setVisibility(View.GONE);
                }

                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("Type", "");
                edit.putString("ContactNo", "");
                edit.putString("Message","");
                edit.putString("Latitude","");
                edit.putString("Longitude","");
                edit.putString("ThumbnailUrl","");
                edit.putString("TroublerName","");
                edit.commit();
            }
        }
        else if (arr[1].equalsIgnoreCase("TrackFriend"))
        {
            if(!arr[0].equalsIgnoreCase("302"))
            {
                JSONObject json = null;
                try {
                    json = new JSONObject(arr[0]);

                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NotificationDetailsActivity.this);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("TrackFriendName", json.getString("friendName"));
                    edit.putInt("TrackFriendLatitude", json.getInt("latitude"));
                    edit.putInt("TrackFriendLongitude", json.getInt("longitude"));
                    edit.commit();
                    if (!json.getString("friendName").equalsIgnoreCase("null"))
                    {
                       /* Intent intent = new Intent(NotificationDetailsActivity.this, TrackShowMap.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("FriendName",json.getString("friendName"));
                        bundle.putDouble("LATITUDE",  json.getDouble("latitude"));
                        bundle.putDouble("LONGITUDE", json.getDouble("longitude"));
                        bundle.putString("Age",json.getString("age"));
                        bundle.putString("gender",json.getString("gender"));
                        bundle.putString("thumbnailUrl",json.getString("thumbnailUrl"));
                        bundle.putString("lastupdated",json.getString("lastUpdatedOn"));
                        intent.putExtras(bundle);
                        startActivity(intent);*/
                        ArrayList<NearByFriends> nearFrndlist=new ArrayList<>();

                        NearByFriends near=new NearByFriends();
                        near.setGender(json.getString("gender"));
                        near.setLatitude(String.valueOf(json.getDouble("latitude")));
                        near.setLongitude(String.valueOf(json.getDouble("longitude")));
                        near.setThumbnailUrl(json.getString("thumbnailUrl"));
                        near.setFriendName(json.getString("friendName"));
                        near.setAge(json.getString("age"));
                        near.setLastupdate(json.getString("lastUpdatedOn"));
                        nearFrndlist.add(near);

                        realizer.com.makemepopular.utils.Singleton.setSingleNearFriendList(nearFrndlist);

                        Intent intent = new Intent(NotificationDetailsActivity.this, FriendNearActivity.class);
                        Bundle bu=new Bundle();
                        bu.putInt("Distance",0);
                        bu.putString("Interest","");
                        bu.putString("Flag","Single");
                        intent.putExtras(bu);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        //Toast.makeText(NotificationDetailsActivity.this,"No data available for this friend",Toast.LENGTH_LONG).show();
                        Config.alertDialog(NotificationDetailsActivity.this, "Near Friend","No data available for this friend");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                //Toast.makeText(NotificationDetailsActivity.this,"You have no permission to track this friend",Toast.LENGTH_LONG).show();
                Config.alertDialog(NotificationDetailsActivity.this, "Near Friend","You have no permission to track this friend");
            }
            loading.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Bundle bundle = new Bundle();
                Intent friendlist=new Intent(NotificationDetailsActivity.this, Notification_activity.class);
                bundle.putString("FriendID","");
                friendlist.putExtras(bundle);
                startActivity(friendlist);
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

            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NotificationDetailsActivity.this);
            if(update.equals("Emergency")) {

                String notType=sharedpreferences.getString("Type", "");
                if (notType.equalsIgnoreCase("FriendRequest"))
                {
                    String reqstName=sharedpreferences.getString("RequsetByName", "");
                    String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
                    Config.showacceptrejectFriendRequest(reqstName,thumbnail,NotificationDetailsActivity.this);
                    //Config.showacceptrejectFriendRequest(reqstName,NotificationDetailsActivity.this);
                }
                else if (notType.equalsIgnoreCase("Emergency"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String trobler=sharedpreferences.getString("TroublerName", "");
                    String troblerid=sharedpreferences.getString("TroublerUserId", "");
                    Config.showEmergencyAcceptReject(msg,trobler,troblerid,NotificationDetailsActivity.this);
                }
                else if (notType.equalsIgnoreCase("EmergencyRecipt"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("HelperUserName", "");
                    String isResch=sharedpreferences.getString("isReaching", "");
                    if (isResch.equalsIgnoreCase("true")){}
                    //showEmergencyAckAlert(newMsg, helpername);
                    Config.showEmergencyAckAlert(msg,helpername,NotificationDetailsActivity.this);
                }
                else if (notType.equalsIgnoreCase("FriendRequestAccepted"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("AcceptByName", "");

                    Config.showAccptedRequestAlert(msg,helpername,NotificationDetailsActivity.this);
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
                NotificationDetailsActivity.this.runOnUiThread(new UpdateUI("Emergency"));
            }
            if(resultCode == 200){
                NotificationDetailsActivity.this.runOnUiThread(new UpdateUI("RefreshThreadList"));
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent reg=new Intent(NotificationDetailsActivity.this,DashboardActivity.class);
        startActivity(reg);
        finish();
    }
}
