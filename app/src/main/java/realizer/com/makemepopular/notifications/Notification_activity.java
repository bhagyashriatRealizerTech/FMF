package realizer.com.makemepopular.notifications;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import realizer.com.makemepopular.DashboardActivity;
import realizer.com.makemepopular.R;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.Singleton;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 25/01/2017.
 */
public class Notification_activity extends AppCompatActivity implements OnTaskCompleted,CompoundButton.OnCheckedChangeListener
{
    ListView notListView;
    ProgressWheel loading;
    ArrayList<NotificationModel> notificationList;
    ArrayList<String> selectedType;
    TextView noData;
    String frndId;
    ArrayList<NotificationModel> sendNotificationList;
    MessageResultReceiver resultReceiver;
    ToggleButton sendBtn,receiveBtn;
    NotificationAsyncTask notification;
    static boolean isRespondingAPI=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,""));
        setContentView(R.layout.activity_notificationlist);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notification List");
        notListView=(ListView)findViewById(R.id.list_notlist);
        loading=(ProgressWheel)findViewById(R.id.loading);
        noData=(TextView)findViewById(R.id.notext);
        sendBtn=(ToggleButton)findViewById(R.id.sendBtn);
        receiveBtn=(ToggleButton)findViewById(R.id.receiveBtn);

        sendBtn.setTextOn("Sent");
        sendBtn.setTextOff("Sent");
        sendBtn.setChecked(true);
        sendBtn.setGravity(Gravity.CENTER);
        sendBtn.setOnCheckedChangeListener(this);
        receiveBtn.setTextOn("Received");
        receiveBtn.setTextOff("Received");
        receiveBtn.setChecked(false);
        receiveBtn.setGravity(Gravity.CENTER);
        receiveBtn.setOnCheckedChangeListener(this);

        Bundle bundle=getIntent().getExtras();
        frndId=bundle.getString("FriendID");
        Singleton obj = Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);

        if (Config.isConnectingToInternet(Notification_activity.this))
        {
            loading.setVisibility(View.VISIBLE);
            ArrayList<String> list=new ArrayList<>();
            list.add("FriendRequest");
            notification=new NotificationAsyncTask(list,Notification_activity.this,Notification_activity.this);
            notification.execute();
            isRespondingAPI=false;
            new CountDownTimer( 15000, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    loading.setVisibility(View.GONE);
                    if (!isRespondingAPI)
                    {
                        if (notification != null && notification.getStatus() != AsyncTask.Status.FINISHED) {
                            notification.cancel(true);
                            Config.alertDialog(Notification_activity.this, "Error", "Server is not responding.Please try again after sometime.");
                        }
                    }
                }
            }.start();
        }
        else
        {
            Config.alertDialog(Notification_activity.this, "Internet Connection", "Make sure your device is connected to the internet.");
        }

        notListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o=parent.getItemAtPosition(position);
                NotificationModel obj=(NotificationModel)o;

                Intent intent = new Intent(Notification_activity.this, NotificationDetailsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("FriendName",obj.getNotiFromUserName());
                bundle.putString("Time", obj.getNotiTime());
                bundle.putString("Type", obj.getNotiType());
                bundle.putString("Message", obj.getNotiText());
                bundle.putString("FromUserID", obj.getNotiFromUserId());
                bundle.putString("ToUserId", obj.getNotiToUserId());
                bundle.putBoolean("IsReceived", obj.isReceived());
                bundle.putBoolean("IsRead", obj.isRead());
                bundle.putString("NotificationID", obj.getNotificationId());
                bundle.putString("ProfilePic", obj.getNotiFromThumbnailUrl());
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onTaskCompleted(String s) {
        isRespondingAPI=true;
        String arr[]=s.split("@@@");
        if (arr[1].equalsIgnoreCase("NotificationList"))
        {
            if (arr[0].equalsIgnoreCase("[]") || arr[0].equalsIgnoreCase("") || arr[0] == null)
            {
                noData.setVisibility(View.VISIBLE);
                notListView.setVisibility(View.GONE);
            }
            else
            {
                JSONArray jsonarray = null;
                try {
                    jsonarray = new JSONArray(arr[0]);
                    notificationList=new ArrayList<>();
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);

                        NotificationModel model = new NotificationModel();
                        model.setNotificationId(jsonobject.getString("notificationId"));
                        model.setNotiFromUserId(jsonobject.getString("notiFromUserId"));
                        model.setNotiFromUserName(jsonobject.getString("notiFromUserName"));
                        model.setNotiFromThumbnailUrl(jsonobject.getString("notiFromThumbnailUrl"));
                        model.setNotiToUserId(jsonobject.getString("notiToUserId"));
                        model.setNotiText(jsonobject.getString("notiText"));
                        model.setNotiTime(jsonobject.getString("notiTime"));
                        model.setNotiType(jsonobject.getString("notiType"));
                        model.setReceived(jsonobject.getBoolean("isReceived"));
                        model.setRead(jsonobject.getBoolean("isRead"));
                        notificationList.add(model);
                    }
                    Singleton.setNotificationList(notificationList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sendNotificationList=new ArrayList<>();
                //notificationList= Singleton.getNotificationList();

              if (sendBtn.isChecked())
              {
                  if (notificationList.size()>0 || notificationList != null)
                  {
                      for (int i=0;i<notificationList.size();i++)
                      {
                          if (!notificationList.get(i).isReceived)
                          {
                              sendNotificationList.add(notificationList.get(i));
                          }
                      }
                      if (sendNotificationList.size()>0)
                      {
                          notListView.setAdapter(new NotificationListAdapter(Notification_activity.this,sendNotificationList));
                          noData.setVisibility(View.GONE);
                          notListView.setVisibility(View.VISIBLE);
                      }
                      else
                      {
                          noData.setVisibility(View.VISIBLE);
                          notListView.setVisibility(View.GONE);
                      }
                  }
                  else
                  {
                      noData.setVisibility(View.VISIBLE);
                      notListView.setVisibility(View.GONE);
                  }
              }
              else
              {
                  if (notificationList.size()>0 || notificationList != null)
                  {
                      for (int i=0;i<notificationList.size();i++)
                      {
                          if (notificationList.get(i).isReceived)
                          {
                              sendNotificationList.add(notificationList.get(i));
                          }
                      }
                      if (sendNotificationList.size()>0)
                      {
                          notListView.setAdapter(new NotificationListAdapter(Notification_activity.this,sendNotificationList));
                          noData.setVisibility(View.GONE);
                          notListView.setVisibility(View.VISIBLE);
                      }
                      else
                      {
                          noData.setVisibility(View.VISIBLE);
                          notListView.setVisibility(View.GONE);
                      }
                  }
                  else
                  {
                      noData.setVisibility(View.VISIBLE);
                      notListView.setVisibility(View.GONE);
                  }
              }
            }

            loading.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Intent reg=new Intent(Notification_activity.this,DashboardActivity.class);
                startActivity(reg);
                finish();
                break;

            case R.id.action_filter:
                getNotificationtypealert();
                return true;

          /*  case R.id.action_add:

                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notification_filter, menu);

        return true;
    }

    public void getNotificationtypealert()
    {
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.notification_type_list_items, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(Notification_activity.this);
        builder.setView(dialoglayout);

        final ListView alertList=(ListView) dialoglayout.findViewById(R.id.list_notTypelist);
        final Button btn_search=(Button) dialoglayout.findViewById(R.id.btn_search);
        btn_search.setVisibility(View.VISIBLE);
        final AlertDialog alertDialog = builder.create();

        String[] list={"Friend Request","Emergency","Friend Request Accepted","Friend Request Rejected","Emergency Receipt","Tracking"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice,list);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_multiple_choice);
        alertList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        alertList.setAdapter(adapter);

        alertList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType.add(alertList.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> list=new ArrayList<String>();
                list.add("FriendRequest");
                list.add("Emergency");
                list.add("FriendRequestAccepted");
                list.add("FriendRequestRejected");
                list.add("EmergencyRecipt");
                list.add("TrackingStarted");
                selectedType=new ArrayList<String>();
                int len = alertList.getCount();
                SparseBooleanArray checked = alertList.getCheckedItemPositions();
                for (int i = 0; i < len; i++)
                    if (checked.get(i)) {
                        String item = list.get(i);
                        selectedType.add(item);
                    }
                if (Config.isConnectingToInternet(Notification_activity.this))
                {
                    loading.setVisibility(View.VISIBLE);
                    notification=new NotificationAsyncTask(selectedType,Notification_activity.this,Notification_activity.this);
                    notification.execute();
                    isRespondingAPI=false;
                    new CountDownTimer( 15000, 1000) {
                        public void onTick(long millisUntilFinished) {
                        }
                        public void onFinish() {
                            loading.setVisibility(View.GONE);
                            if (!isRespondingAPI)
                            {
                                if (notification != null && notification.getStatus() != AsyncTask.Status.FINISHED) {
                                    notification.cancel(true);
                                    Config.alertDialog(Notification_activity.this, "Error", "Server is not responding.Please try again after sometime.");
                                }
                            }
                        }
                    }.start();
                }
                else
                {
                    Config.alertDialog(Notification_activity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                }
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sendBtn:
                if (isChecked) {
                    receiveBtn.setChecked(false);
                    //sendBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_green));
                    sendBtn.setBackgroundResource(R.drawable.button_bg_green);
                    sendBtn.setTextColor(getResources().getColor(R.color.lightGrey));

                    receiveBtn.setBackgroundResource(Color.TRANSPARENT);
                    receiveBtn.setTextColor(getResources().getColor(R.color.abc_primary_text_disable_only_material_dark));
                    invalidateOptionsMenu();

                    notificationList=new ArrayList<>();
                    sendNotificationList=new ArrayList<>();
                    notificationList=Singleton.getNotificationList();

                    if (notificationList.size()>0 || notificationList != null)
                    {
                        for (int i=0;i<notificationList.size();i++)
                        {
                            if (!notificationList.get(i).isReceived)
                            {
                                sendNotificationList.add(notificationList.get(i));
                            }
                        }
                        if (sendNotificationList.size()>0)
                        {
                            notListView.setAdapter(new NotificationListAdapter(Notification_activity.this,sendNotificationList));
                            noData.setVisibility(View.GONE);
                            notListView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            noData.setVisibility(View.VISIBLE);
                            notListView.setVisibility(View.GONE);
                        }
                    }
                    else
                    {
                        noData.setVisibility(View.VISIBLE);
                        notListView.setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.receiveBtn:
                if (isChecked) {
                    sendBtn.setChecked(false);
                    //sendBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_green));
                    receiveBtn.setBackgroundResource(R.drawable.button_bg_green);
                    receiveBtn.setTextColor(getResources().getColor(R.color.lightGrey));

                    sendBtn.setBackgroundResource(Color.TRANSPARENT);
                    sendBtn.setTextColor(getResources().getColor(R.color.abc_primary_text_disable_only_material_dark));
                    invalidateOptionsMenu();

                    notificationList=new ArrayList<>();
                    sendNotificationList=new ArrayList<>();
                    notificationList=Singleton.getNotificationList();

                    if (notificationList.size()>0 || notificationList != null)
                    {
                        for (int i=0;i<notificationList.size();i++)
                        {
                            if (notificationList.get(i).isReceived)
                            {
                                sendNotificationList.add(notificationList.get(i));
                            }
                        }
                        if (sendNotificationList.size()>0)
                        {
                            notListView.setAdapter(new NotificationListAdapter(Notification_activity.this,sendNotificationList));
                            noData.setVisibility(View.GONE);
                            notListView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            noData.setVisibility(View.VISIBLE);
                            notListView.setVisibility(View.GONE);
                        }
                    }
                    else
                    {
                        noData.setVisibility(View.VISIBLE);
                        notListView.setVisibility(View.GONE);
                    }
                }
                break;

        }
    }

    class UpdateUI implements Runnable {
        String update;

        public UpdateUI(String update) {

            this.update = update;
        }

        public void run() {

            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(Notification_activity.this);
            if(update.equals("Emergency")) {

                String notType=sharedpreferences.getString("Type", "");
                if (notType.equalsIgnoreCase("FriendRequest"))
                {
                    String reqstName=sharedpreferences.getString("RequsetByName", "");
                    String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
                    Config.showacceptrejectFriendRequest(reqstName,thumbnail,Notification_activity.this);
                   // Config.showacceptrejectFriendRequest(reqstName,Notification_activity.this);
                }
                else if (notType.equalsIgnoreCase("Emergency"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    //String newMsg="Hey Buddy,"+""+msg;
                    String trobler=sharedpreferences.getString("TroublerName", "");
                    String troblerid=sharedpreferences.getString("TroublerUserId", "");
                    Config.showEmergencyAcceptReject(msg,trobler,troblerid,Notification_activity.this);
                }
                else if (notType.equalsIgnoreCase("EmergencyRecipt"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("HelperUserName", "");
                    String isResch=sharedpreferences.getString("isReaching", "");
                    if (isResch.equalsIgnoreCase("true")){}
                    //showEmergencyAckAlert(newMsg, helpername);
                    Config.showEmergencyAckAlert(msg,helpername,Notification_activity.this);
                }
                else if (notType.equalsIgnoreCase("FriendRequestAccepted"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("AcceptByName", "");

                    Config.showAccptedRequestAlert(msg,helpername,Notification_activity.this);
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
                Notification_activity.this.runOnUiThread(new UpdateUI("Emergency"));
            }
            if(resultCode == 200){
                Notification_activity.this.runOnUiThread(new UpdateUI("RefreshThreadList"));
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent reg=new Intent(Notification_activity.this,DashboardActivity.class);
        startActivity(reg);
        finish();
    }
}
