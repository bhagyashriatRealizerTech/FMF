package realizer.com.makemepopular.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import realizer.com.makemepopular.DashboardActivity;
import realizer.com.makemepopular.R;
import realizer.com.makemepopular.backend.DatabaseQueries;
import realizer.com.makemepopular.chat.adapter.ChatThreadListModelAdapter;
import realizer.com.makemepopular.chat.asynctask.ThreadListAsyncTaskGet;
import realizer.com.makemepopular.chat.model.ChatThreadListModel;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.friendnear.FriendNearActivity;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.Singleton;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 30/11/2016.
 */
public class ChatThreadListActivity extends AppCompatActivity implements OnTaskCompleted
{
    DatabaseQueries qr;
    ProgressWheel loading;
    ListView threadList;
    private MenuItem done;
    TextView noData;
    String LoginId="";
    String UserFullName="";
    String IsFirstTimeLogin="";
    MessageResultReceiver resultReceiver;
    static boolean isRespondingAPI=false;
    ThreadListAsyncTaskGet getthread;

    /*DatabaseQueries qr;*/

    ArrayList<ChatThreadListModel> temp=new ArrayList<>();
    int num;

    private static final String TAG = "ThreadList";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,""));
        setContentView(R.layout.activity_chat_thread_list);
        qr=new DatabaseQueries(ChatThreadListActivity.this);
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ChatThreadListActivity.this);
        LoginId=sharedpreferences.getString("UserId","");
        UserFullName=sharedpreferences.getString("userFullName","");
        IsFirstTimeLogin=sharedpreferences.getString("FirstTimeLoginThread","");
        getSupportActionBar().setTitle("Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Singleton obj = Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);

        TextView userfullname= (TextView) findViewById(R.id.thread_user_name);
        /*userfullname.setText(UserFullName);
        userfullname.setTypeface(face);*/


        loading = (ProgressWheel) findViewById(R.id.loading);
        threadList= (ListView) findViewById(R.id.lsttname);
        noData = (TextView) findViewById(R.id.tvNoDataMsg);

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }


        threadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = threadList.getItemAtPosition(position);
                ChatThreadListModel chatThreadlist = (ChatThreadListModel) o;
                String threadId = chatThreadlist.getThreadId();
                String receiverID=chatThreadlist.getParticipentID();
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ChatThreadListActivity.this);
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("ActiveThread", threadId);
                edit.putString("RECEIVERID", receiverID);
                edit.putString("ChatThreadId", threadId);
                edit.commit();
                Bundle bundle = new Bundle();
                bundle.putString("THREADID",threadId );
                bundle.putString("ActionBarTitle",chatThreadlist.getCustomThreadName());
                bundle.putString("InitiatedID", chatThreadlist.getInitiateId());
                bundle.putInt("UnreadCountThread",chatThreadlist.getUnreadCount());
                Intent i = new Intent(ChatThreadListActivity.this, ChatMessageCenterActicity.class);
                i.putExtras(bundle);
                startActivity(i);
            }
        });
        UpdateThreadList();
    }
    public void newThread(View v)
    {
        //Config.FIreBaseCreateTopic("group");
        Intent i=new Intent(ChatThreadListActivity.this, InitateNewChatActivity.class);
        startActivity(i);
    }

    @Override
    public void onTaskCompleted(String s) {
        isRespondingAPI=true;
        loading.setVisibility(View.GONE);
        if (!s.equals("@@@ThreadList")) {

            String[] onTask = s.split("@@@");
            if (onTask[1].equals("ThreadList")) {
                JSONArray rootObj = null;
                Log.d("String", onTask[0]);
                try {

                    rootObj = new JSONArray(onTask[0]);
                    int i = rootObj.length();

                    for (int j = 0; j < i; j++) {
                        JSONObject obj = rootObj.getJSONObject(j);
                        boolean chk=qr.ChekThreadInThreadList(obj.getString("threadId"));
                        if (!chk)
                        {
                            qr.insertThread(obj.getString("threadId"),obj.getString("threadName"),obj.getString("thumbnailUrl"),obj.getString("lastSenderName"),obj.getString("badgeCount"),obj.getString("timeStamp"),obj.getString("lastSenderById"),obj.getString("lastMessageId"),obj.getString("lastMessageText"),obj.getString("initiateId"),obj.getString("initiateName"),obj.getString("participantList"),obj.getString("threadCustomName"),obj.getString("isLastMessageReadInOneToOne"),obj.getString("isLastMessageDeliverInOneToOne"));
                        }
                        else
                        {
                            qr.updateThreadDispMessage(obj.getString("threadId"),obj.getString("lastSenderName"),obj.getString("lastMessageText"),obj.getString("timeStamp"),obj.getString("badgeCount"),obj.getString("thumbnailUrl"),obj.getString("threadCustomName"),obj.getString("isLastMessageReadInOneToOne"),obj.getString("isLastMessageDeliverInOneToOne"),obj.getString("lastSenderById"));
                        }
                        /*ChatThreadListModel o = new ChatThreadListModel();
                        o.setThreadId(obj.getString("threadId"));
                        o.setThreadName(obj.getString("threadName"));
                        o.setProfileImg(obj.getString("threadName"));
                        o.setLastSenderName(obj.getString("threadName"));
                        o.setUnreadCount(obj.getInt("badgeCount"));
                        o.setDate(obj.getString("timeStamp"));
                        o.setLastSenderId(obj.getString("lastSenderById"));
                        o.setLastMessageId(obj.getString("lastMessageId"));
                        o.setLastMessage(obj.getString("lastMessageText"));
                        o.setInitiateId(obj.getString("initiateId"));
                        o.setInitiateName(obj.getString("initiateName"));
                        o.setParticipentID(obj.getString("participantList"));
                        o.setCustomThreadName(obj.getString("threadCustomName"));
                        temp.add(o);*/
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    num = 1;
                   /* Log.e("JSON", e.toString());
                    Log.e("Login.JLocalizedMessage", e.getLocalizedMessage());
                    Log.e("Login(JStackTrace)", e.getStackTrace().toString());
                    Log.e("Login(JCause)", e.getCause().toString());
                    Log.wtf("Login(JMsg)", e.getMessage());*/
                }

                UpdateThreadList();
            } else {

            }
        }
        else {
            UpdateThreadList();
        }
    }

    public void UpdateThreadList()
    {
                                                                     temp=new ArrayList<>();
        temp=qr.getThreadList();
        if(temp.size()!=0)
        {
            threadList.setAdapter(new ChatThreadListModelAdapter(ChatThreadListActivity.this, temp));
            noData.setVisibility(View.GONE);
            threadList.setVisibility(View.VISIBLE);
        }
        else
        {
            noData.setVisibility(View.VISIBLE);
            threadList.setVisibility(View.GONE);
        }
    }

    public void GetThreadLIST()
    {
        if (Config.isConnectingToInternet(ChatThreadListActivity.this))
        {
            String time=qr.getLastThreadTime();
            if (time.equals(""))
            {
                loading.setVisibility(View.VISIBLE);
            }
            getthread = new ThreadListAsyncTaskGet(ChatThreadListActivity.this, ChatThreadListActivity.this, LoginId, time);
            getthread.execute();
            new CountDownTimer( 15000, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    loading.setVisibility(View.GONE);
                    if (!isRespondingAPI)
                    {
                        if (getthread != null && getthread.getStatus() != AsyncTask.Status.FINISHED) {
                            getthread.cancel(true);
                            Config.alertDialog(ChatThreadListActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                        }
                    }
                }
            }.start();
        }
        else
        {
            UpdateThreadList();
            Config.alertDialog(ChatThreadListActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
        }

    }
    class UpdateUI implements Runnable {
        String update;

        public UpdateUI(String update) {

            this.update = update;
        }

        public void run() {
            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ChatThreadListActivity.this);
            if(update.equals("Emergency")) {
                String notType=sharedpreferences.getString("Type", "");
                if (notType.equalsIgnoreCase("FriendRequest"))
                {
                    String reqstName=sharedpreferences.getString("RequsetByName", "");
                    String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
                    Config.showacceptrejectFriendRequest(reqstName,thumbnail,ChatThreadListActivity.this);
                    //Config.showacceptrejectFriendRequest(reqstName,FriendListActivity.this);
                }
                else if (notType.equalsIgnoreCase("Emergency"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String trobler=sharedpreferences.getString("TroublerName", "");
                    String troblerid=sharedpreferences.getString("TroublerUserId", "");
                    Config.showEmergencyAcceptReject(msg,trobler,troblerid,ChatThreadListActivity.this);
                }
                else if (notType.equalsIgnoreCase("EmergencyRecipt"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("HelperUserName", "");
                    String isResch=sharedpreferences.getString("isReaching", "");
                    if (isResch.equalsIgnoreCase("true")){}
                    //showEmergencyAckAlert(newMsg, helpername);
                    Config.showEmergencyAckAlert(msg,helpername,ChatThreadListActivity.this);
                }
                else if (notType.equalsIgnoreCase("FriendRequestAccepted"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("AcceptByName", "");

                    Config.showAccptedRequestAlert(msg,helpername,ChatThreadListActivity.this);
                }
            }
            else if(update.equals("RecieveMessage"))
            {

            }

            else if(update.equals("RefreshThreadList")) {
                temp.clear();
                GetThreadLIST();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GetThreadLIST();
        Singleton obj = Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);
    }

    class MessageResultReceiver extends ResultReceiver
    {
        public MessageResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if(resultCode == 100){
                ChatThreadListActivity.this.runOnUiThread(new UpdateUI("RecieveMessage"));
            }
            if(resultCode == 200){
                ChatThreadListActivity.this.runOnUiThread(new UpdateUI("RefreshThreadList"));
            }
            if(resultCode == 300){
                ChatThreadListActivity.this.runOnUiThread(new UpdateUI("Emergency"));
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Intent reg=new Intent(ChatThreadListActivity.this,DashboardActivity.class);
                startActivity(reg);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent reg=new Intent(ChatThreadListActivity.this,DashboardActivity.class);
        startActivity(reg);
        finish();
    }
}
