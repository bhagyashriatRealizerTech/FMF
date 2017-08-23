package realizer.com.makemepopular.friendlist;

import android.content.Context;
import android.content.DialogInterface;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import realizer.com.makemepopular.DashboardActivity;
import realizer.com.makemepopular.R;
import realizer.com.makemepopular.asynctask.AcceptRejectFriendRequestAsyntask;
import realizer.com.makemepopular.asynctask.BlockFriendAsyntask;
import realizer.com.makemepopular.asynctask.GetFriendListAsynTask;
import realizer.com.makemepopular.asynctask.TrackFriendAsynctask;
import realizer.com.makemepopular.asynctask.UnFriendAsyncTask;
import realizer.com.makemepopular.backend.DatabaseQueries;
import realizer.com.makemepopular.chat.model.AddedContactModel;
import realizer.com.makemepopular.chat.model.NewFriendListModel;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.exceptionhandler.NetworkException;
import realizer.com.makemepopular.expandableview.ActionSlideExpandableListView;
import realizer.com.makemepopular.friendlist.adapter.FavoriteListAdapter;
import realizer.com.makemepopular.friendlist.adapter.FriendListModelAdapter;
import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.friendnear.FriendNearActivity;
import realizer.com.makemepopular.models.NearByFriends;
import realizer.com.makemepopular.service.TrackShowMap;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 12/01/2017.
 */
public class FriendListActivity extends AppCompatActivity implements OnTaskCompleted,ActionSlideExpandableListView.OnActionClickListener,View.OnTouchListener
{
    DatabaseQueries qr;
    ListView listviewFrnd;
    ArrayList<FriendListModel> friendlist=new ArrayList<>();
    FriendListModelAdapter friendadapter;
    ProgressWheel loading;
    FavoriteListAdapter listAdapter;
    ActionSlideExpandableListView favoriteListView;
    public static boolean isProfileVisitFromFav = false;
    private ArrayList<FavoriteListAdapter.Friend> employeeQuickViewList;
    private ArrayList<FavoriteListAdapter.Friend> updatedEmployeeQuickViewList;
    private FriendListModel acceptMadel=new FriendListModel();
    private FriendListModel rejectMadel=new FriendListModel();
    TextView noData;
    MessageResultReceiver resultReceiver;
    static int friendPostion=0;
    static boolean isRespondingAPI=false;
    GetFriendListAsynTask getfrnd;
    UnFriendAsyncTask getListbyname;
    BlockFriendAsyntask getblocklist;
    AcceptRejectFriendRequestAsyntask sendalert;
    SetFrinedAsEmergencyContactAsyncTask geemergencyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, ""));
        qr=new DatabaseQueries(FriendListActivity.this);
        setContentView(R.layout.activity_friend_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Friend List");
        noData= (TextView) findViewById(R.id.noDataFound);
        employeeQuickViewList = new ArrayList<>();
        listAdapter = new FavoriteListAdapter(FriendListActivity.this);
        // get the listview
        favoriteListView = (ActionSlideExpandableListView) findViewById(R.id.list_view);
        favoriteListView.setItemActionListener(FriendListActivity.this, R.id.id_track, R.id.btn_accept, R.id.btn_reject, R.id.id_unfriend, R.id.id_block, R.id.id_emergency);
        favoriteListView.setOnTouchListener(this);

        realizer.com.makemepopular.utils.Singleton obj = realizer.com.makemepopular.utils.Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);
        loading =(ProgressWheel) findViewById(R.id.loading);

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendListActivity.this);

        if (sharedpreferences.getString("Type","").equalsIgnoreCase("FriendRequestAccepted"))
        {
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("Type", "");
            edit.putString("AcceptByName", "");
            edit.putString("AcceptByUserId","");
            edit.putString("ThumbnailUrl","");
            edit.commit();
        }
        getFriendList();

        if (Config.isConnectingToInternet(FriendListActivity.this))
        {
            String time="";
            int count=qr.chekFriendTable();
            if(count>0)
            {
                time=qr.getCreateTsFriend();
            }
            else{
                time="";
                loading.setVisibility(View.VISIBLE);
            }

            getfrnd=new GetFriendListAsynTask("Other",FriendListActivity.this,FriendListActivity.this,time);
            getfrnd.execute();
            isRespondingAPI=false;
            new CountDownTimer( 15000, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    loading.setVisibility(View.GONE);
                    if (!isRespondingAPI)
                    {
                        if (getfrnd != null && getfrnd.getStatus() != AsyncTask.Status.FINISHED) {
                            getfrnd.cancel(true);
                            Config.alertDialog(FriendListActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                        }
                    }
                }
            }.start();

        }
        else
        {
            Utility.CustomToast(FriendListActivity.this, "No Internet Connection..!");
            getFriendList();
        }
    }


    @Override
    protected void onResume() {
     /*   if (!isProfileVisitFromFav) {
            favoriteListView.collapse();
            //isProfileVisitFromFav = false;
        }*/
        super.onResume();
    }

    @Override
    public void onTaskCompleted(String s) {
        isRespondingAPI=true;
        String arr[]=s.split("@@@");
        if (arr[1].equalsIgnoreCase("FriendList"))
        {
            if (! arr[0].equalsIgnoreCase("[]"))
            {
                JSONArray jsonarray = null;
                try {
                    jsonarray = new JSONArray(arr[0]);

                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        String ismessaging=jsonobject.getString("isMessagingAllowed");
                        String status=jsonobject.getString("status");

                        boolean frndChk = qr.ChekFriendinFrndList(jsonobject.getString("friendsUserId"));
                        if (!frndChk) {
                          //  qr.insertFriendList(jsonobject.getString("friendsUserId"), jsonobject.getString("friendName"), String.valueOf(jsonobject.getBoolean("isEmergencyAlert")), String.valueOf(jsonobject.getBoolean("isMessagingAllowed")), String.valueOf(jsonobject.getBoolean("isTrackingAllowed")), jsonobject.getString("friendThumbnailUrl"), jsonobject.getString("status"), String.valueOf(jsonobject.getBoolean("isRequestSent")), jsonobject.getString("createTS"), jsonobject.getString("allowTrackingTillDate"), jsonobject.getString("trackingStatusChangeDate"), jsonobject.getString("isDeleted"));
                        } else {
                           // qr.updateFriendLIst(jsonobject.getString("friendsUserId"), jsonobject.getString("friendName"), String.valueOf(jsonobject.getBoolean("isEmergencyAlert")), String.valueOf(jsonobject.getBoolean("isMessagingAllowed")), String.valueOf(jsonobject.getBoolean("isTrackingAllowed")), jsonobject.getString("friendThumbnailUrl"), jsonobject.getString("status"), String.valueOf(jsonobject.getBoolean("isRequestSent")), jsonobject.getString("createTS"), jsonobject.getString("allowTrackingTillDate"), jsonobject.getString("trackingStatusChangeDate"), jsonobject.getString("isDeleted"));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                getFriendList();
            }
            else if(arr[0].equalsIgnoreCase("ServerNotResponding"))
            {
                Config.alertDialog(FriendListActivity.this,"Error","Server not responding.Please try again after sometime");
            }
            else
            {
                //Config.alertDialog(FriendListActivity.this,"Error","Server not responding.Please try again after sometime");
            }
            loading.setVisibility(View.GONE);
        }
        else if (arr[1].equalsIgnoreCase("TrackFriend"))
        {
            if(!arr[0].equalsIgnoreCase("302"))
            {

                JSONObject json = null;
                try {
                    json = new JSONObject(arr[0]);

                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendListActivity.this);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("TrackFriendName", json.getString("friendName"));
                    edit.putInt("TrackFriendLatitude", json.getInt("latitude"));
                    edit.putInt("TrackFriendLongitude", json.getInt("longitude"));
                    edit.commit();

                    if (!json.getString("friendName").equalsIgnoreCase("null"))
                    {
                        ArrayList<NearByFriends> nearFrndlist=new ArrayList<>();

                        NearByFriends near=new NearByFriends();
                        near.setGender(json.getString("gender"));
                        near.setLatitude(String.valueOf(json.getDouble("latitude")));
                        near.setLongitude(String.valueOf(json.getDouble("longitude")));
                        near.setThumbnailUrl(json.getString("thumbnailUrl"));
                        near.setFriendName(json.getString("friendName"));
                        near.setAge(json.getString("age"));
                        near.setFriendUserId(json.getString("friendId"));
                        near.setLastupdate(json.getString("lastUpdatedOn"));
                        nearFrndlist.add(near);

                        realizer.com.makemepopular.utils.Singleton.setSingleNearFriendList(nearFrndlist);
                        Intent intent = new Intent(FriendListActivity.this, FriendNearActivity.class);
                        Bundle bu=new Bundle();
                        bu.putInt("Distance",0);
                        bu.putString("Interest","");
                        bu.putString("Flag","Single");
                        bu.putString("FROMWHERE","");
                        intent.putExtras(bu);
                        startActivity(intent);
                    }
                    else
                    {
                        // Toast.makeText(FriendListActivity.this,"No data available for this friend",Toast.LENGTH_LONG).show();
                        Config.alertDialog(this, "Friend List", "No data available for this friend.");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else  if(arr[0].equalsIgnoreCase("ServerNotResponding"))
            {
                Config.alertDialog(this,"Error","Server not responding.Please try again after sometime");
            }
            else
            {
                //Toast.makeText(FriendListActivity.this,"You have no permission to track this friend",Toast.LENGTH_LONG).show();
                Config.alertDialog(this, "Friend List", "You have no permission to track this friend.");
            }
            loading.setVisibility(View.GONE);

        }

        else if (arr[1].equalsIgnoreCase("UnFriend"))
        {
            String friendid="";
            boolean isSuccess=false;
            try {
                JSONObject json = new JSONObject(s);
                friendid=json.getString("friendId");
                isSuccess=json.getBoolean("isSuccess");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (isSuccess)
            {

                if (employeeQuickViewList != null) {
                    if (employeeQuickViewList.size() > 0) {

                        for (int i = 0; i < employeeQuickViewList.size(); i++) {
                            if (friendid.equalsIgnoreCase(employeeQuickViewList.get(i).employeeQuickView.getFriendId()) || friendid.equalsIgnoreCase(employeeQuickViewList.get(i).employeeQuickView.getUserId())) {
                                employeeQuickViewList.remove(i);
                            }
                        }
                        // Adding child data
                        listAdapter.setChildList(employeeQuickViewList);
                        //listAdapter= new InterestAdapter(this,afm);
                        //favoriteListView.setAdapter(listAdapter);
                        listAdapter.notifyDataSetChanged();
                    }
                }
             /*   if (Config.isConnectingToInternet(FriendListActivity.this))
                {
                    loading.setVisibility(View.VISIBLE);
                    GetFriendListAsynTask getfrnd=new GetFriendListAsynTask("Other",FriendListActivity.this,FriendListActivity.this);
                    getfrnd.execute();
                }
                else
                {
                    Config.alertDialog(FriendListActivity.this, "Network Error", "No Internet connection");
                }*/
            }
            else
            {
                //Toast.makeText(FriendListActivity.this, "Friend Is Not Unfriend", Toast.LENGTH_SHORT).show();
                Config.alertDialog(this, "Friend List", "Friend is not unfriend.");
                qr.deleteUserFromTable(friendid);

                if (employeeQuickViewList != null) {
                    if (employeeQuickViewList.size() > 0) {

                        for (int i = 0; i < employeeQuickViewList.size(); i++) {
                            if (friendid.equalsIgnoreCase(employeeQuickViewList.get(i).employeeQuickView.getFriendId()) || friendid.equalsIgnoreCase(employeeQuickViewList.get(i).employeeQuickView.getUserId())) {
                                employeeQuickViewList.remove(i);
                            }
                        }
                        // Adding child data
                        listAdapter.setChildList(employeeQuickViewList);
                        listAdapter.notifyDataSetChanged();
                    }
                }

            }
            loading.setVisibility(View.GONE);
        }
        else if (arr[1].equalsIgnoreCase("BlockFriend"))
        {
            String friendid="";
            boolean isSuccess=false;
            try {
                JSONObject json = new JSONObject(s);
                friendid=json.getString("friendId");
                isSuccess=json.getBoolean("isSuccess");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (isSuccess)
            {
                if (employeeQuickViewList!= null) {

                    if (employeeQuickViewList.size() >0)
                    {
                        for (int i=0;i<employeeQuickViewList.size();i++)
                        {
                            if (friendid.equalsIgnoreCase(employeeQuickViewList.get(i).employeeQuickView.getFriendId()) || friendid.equalsIgnoreCase(employeeQuickViewList.get(i).employeeQuickView.getUserId()))
                            {
                                employeeQuickViewList.remove(i);
                            }
                        }
                        // Adding child data
                        listAdapter.setChildList(employeeQuickViewList);
                        //listAdapter= new InterestAdapter(this,afm);
                        //favoriteListView.setAdapter(listAdapter);
                        listAdapter.notifyDataSetChanged();
                    }
                }
                /*if (Config.isConnectingToInternet(FriendListActivity.this))
                {
                    loading.setVisibility(View.VISIBLE);
                    GetFriendListAsynTask getfrnd=new GetFriendListAsynTask("Other",FriendListActivity.this,FriendListActivity.this);
                    getfrnd.execute();
                }
                else
                {
                    Config.alertDialog(FriendListActivity.this, "Network Error", "No Internet connection");
                }*/
            }
            else
            {
                //Toast.makeText(FriendListActivity.this, "Friend Is Not Block", Toast.LENGTH_SHORT).show();
                Config.alertDialog(this, "Friend List", "Friend is not block.");
            }
            loading.setVisibility(View.GONE);
        }
        else if (arr[1].equalsIgnoreCase("AcceptReject"))
        {
            if (arr[0].equalsIgnoreCase("true"))
            {
                if (arr[2].equalsIgnoreCase("AcceptRequest")) {
                    Config.alertDialog(this, "Friend List", "Friend Request Accepted Successfully.");

                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendListActivity.this);
                    String userID=sharedpreferences.getString("UserId","");
                    String friendId="";
                    if (userID.equalsIgnoreCase(acceptMadel.getUserId()))
                    {
                        friendId=acceptMadel.getFriendId();
                    }
                    else
                    {
                        friendId=acceptMadel.getUserId();
                    }

                    FriendListModel model=new FriendListModel();
                    model.setUserId(userID);
                    model.setFriendName(acceptMadel.getFriendName());
                    model.setIsEmergency(acceptMadel.isEmergency());
                    model.setIsmessaging(true);
                    model.setIstracking(true);
                    model.setThumbnailUrl(acceptMadel.getThumbnailUrl());
                    model.setFriendId(friendId);
                    model.setStatus("Accepted");
                    model.setSentRequest(true);

                    employeeQuickViewList.set(friendPostion,new FavoriteListAdapter.Friend(model));
                    listAdapter.setChildList(employeeQuickViewList);
                    listAdapter.notifyDataSetChanged();
                    // SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendListActivity.this);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("Type", "");
                    edit.putString("RequsetByName", "");
                    edit.putString("ThumbnailUrl","");
                    edit.putString("RequsetByUserId","");
                    edit.commit();
                    loading.setVisibility(View.GONE);
                }
                    //Toast.makeText(FriendListActivity.this, "Friend Request Accepted Successfully", Toast.LENGTH_SHORT).show();
                else
                    Config.alertDialog(this, "Friend List", "Friend Request Rejected Successfully.");
                   // Toast.makeText(FriendListActivity.this, "Friend Request Rejected Successfully", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if (arr[2].equalsIgnoreCase("AcceptRequest"))
                    Config.alertDialog(this, "Friend List", "Friend Request Not Accepted.");
                   // Toast.makeText(FriendListActivity.this, "Friend Request Not Accepted", Toast.LENGTH_SHORT).show();
                else
                    Config.alertDialog(this, "Friend List", "Friend Request Not Rejected.");
                    //Toast.makeText(FriendListActivity.this, "Friend Request Not Rejected", Toast.LENGTH_SHORT).show();
            }


        }
    }

    @Override
    public void onClick(View itemView, View clickedView, int position) {
        if (favoriteListView.getItemAtPosition(position) instanceof FavoriteListAdapter.Friend) {
            FriendListModel friendListModel = ((FavoriteListAdapter.Friend) favoriteListView.getItemAtPosition(position)).employeeQuickView;
            switch (clickedView.getId()) {
                case R.id.id_track:
                    if (friendListModel.getStatus().equalsIgnoreCase("Accepted"))
                    {
                        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendListActivity.this);
                        String userID=sharedpreferences.getString("UserId","");
                        String friendId="";
                        if (userID.equalsIgnoreCase(friendListModel.getUserId()))
                        {
                            friendId=friendListModel.getFriendId();
                        }
                        else
                        {
                            friendId=friendListModel.getUserId();
                        }

                        Intent intent = new Intent(FriendListActivity.this, FriendNearActivity.class);
                        Bundle bu=new Bundle();
                        bu.putInt("Distance",0);
                        bu.putString("Interest","");
                        bu.putString("Flag","Single");
                        bu.putString("FROMWHERE","");
                        bu.putString("FriendId",friendId);
                        intent.putExtras(bu);
                        startActivity(intent);
                        finish();
                    }

                    break;

                case R.id.id_unfriend:
                    if (friendListModel.getStatus().equalsIgnoreCase("Accepted"))
                    {
                        if (Config.isConnectingToInternet(FriendListActivity.this))
                        {
                            loading.setVisibility(View.VISIBLE);
                            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendListActivity.this);
                            String userID=sharedpreferences.getString("UserId","");
                            String friendId="";
                            if (userID.equalsIgnoreCase(friendListModel.getUserId()))
                            {
                                friendId=friendListModel.getFriendId();
                            }
                            else
                            {
                                friendId=friendListModel.getUserId();
                            }
                            getListbyname=new UnFriendAsyncTask(friendId,FriendListActivity.this,FriendListActivity.this);
                            getListbyname.execute();
                            isRespondingAPI=false;
                            new CountDownTimer( 15000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                }
                                public void onFinish() {
                                    loading.setVisibility(View.GONE);
                                    if (!isRespondingAPI)
                                    {
                                        if (getListbyname != null && getListbyname.getStatus() != AsyncTask.Status.FINISHED) {
                                            getListbyname.cancel(true);
                                            Config.alertDialog(FriendListActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                        }
                                    }
                                }
                            }.start();
                        }
                        else
                        {
                            //Config.alertDialog(FriendListActivity.this, "Network Error", "No Internet connection");
                            Utility.CustomToast(FriendListActivity.this, "No Internet Connection..!");
                        }
                    }
                    break;

                case R.id.id_block:
                    if (friendListModel.getStatus().equalsIgnoreCase("Accepted"))
                    {
                        if (Config.isConnectingToInternet(FriendListActivity.this))
                        {
                            loading.setVisibility(View.VISIBLE);
                            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendListActivity.this);
                            String userID=sharedpreferences.getString("UserId","");
                            String friendId="";
                            if (userID.equalsIgnoreCase(friendListModel.getUserId()))
                            {
                                friendId=friendListModel.getFriendId();
                            }
                            else
                            {
                                friendId=friendListModel.getUserId();
                            }
                            getblocklist=new BlockFriendAsyntask(friendId,FriendListActivity.this,FriendListActivity.this);
                            getblocklist.execute();
                            isRespondingAPI=false;
                            new CountDownTimer( 15000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                }
                                public void onFinish() {
                                    loading.setVisibility(View.GONE);
                                    if (!isRespondingAPI)
                                    {
                                        if (getblocklist != null && getblocklist.getStatus() != AsyncTask.Status.FINISHED) {
                                            getblocklist.cancel(true);
                                            Config.alertDialog(FriendListActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                        }
                                    }
                                }
                            }.start();
                        }
                        else
                        {
                            //Config.alertDialog(FriendListActivity.this, "Network Error", "No Internet connection");
                            Utility.CustomToast(FriendListActivity.this, "No Internet Connection..!");
                        }
                    }
                    break;

                case R.id.btn_accept:

                    if (Config.isConnectingToInternet(FriendListActivity.this))
                    {
                        friendPostion=position;
                        acceptMadel=friendListModel;
                        loading.setVisibility(View.VISIBLE);
                        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendListActivity.this);
                        String userID=sharedpreferences.getString("UserId","");
                        String friendId="";
                        if (userID.equalsIgnoreCase(friendListModel.getUserId()))
                        {
                            friendId=friendListModel.getFriendId();
                        }
                        else
                        {
                            friendId=friendListModel.getUserId();
                        }
                        sendalert=new AcceptRejectFriendRequestAsyntask("","AcceptRequest",friendId,false,true,FriendListActivity.this,FriendListActivity.this);
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
                                        Config.alertDialog(FriendListActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                    }
                                }
                            }
                        }.start();
                    }
                    else
                    {
                        //Config.alertDialog(FriendListActivity.this, "Network Error", "No Internet connection");
                        Utility.CustomToast(FriendListActivity.this, "No Internet Connection..!");
                    }
                    break;

                case R.id.btn_reject:
                    if (Config.isConnectingToInternet(FriendListActivity.this))
                    {
                        acceptMadel=friendListModel;
                        loading.setVisibility(View.VISIBLE);
                        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendListActivity.this);
                        String userID=sharedpreferences.getString("UserId","");
                        String friendId="";
                        if (userID.equalsIgnoreCase(friendListModel.getUserId()))
                        {
                            friendId=friendListModel.getFriendId();
                        }
                        else
                        {
                            friendId=friendListModel.getUserId();
                        }
                        sendalert=new AcceptRejectFriendRequestAsyntask("","RejectRequest",friendId,false,false,FriendListActivity.this,FriendListActivity.this);
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
                                        Config.alertDialog(FriendListActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                    }
                                }
                            }
                        }.start();
                    }
                    else
                    {
                        //Config.alertDialog(FriendListActivity.this, "Network Error", "No Internet connection");
                        Utility.CustomToast(FriendListActivity.this, "No Internet Connection..!");
                    }
                    break;

                case R.id.id_emergency:
                    if (friendListModel.isEmergency() && friendListModel.getStatus().equalsIgnoreCase("Accepted"))
                    {
                        if (Config.isConnectingToInternet(FriendListActivity.this))
                        {
                            loading.setVisibility(View.VISIBLE);
                            geemergencyList=new SetFrinedAsEmergencyContactAsyncTask(friendListModel,false,position,FriendListActivity.this);
                            geemergencyList.execute();
                            isRespondingAPI=false;
                            new CountDownTimer( 15000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                }
                                public void onFinish() {
                                    loading.setVisibility(View.GONE);
                                    if (!isRespondingAPI)
                                    {
                                        if (geemergencyList != null && geemergencyList.getStatus() != AsyncTask.Status.FINISHED) {
                                            geemergencyList.cancel(true);
                                            Config.alertDialog(FriendListActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                        }
                                    }
                                }
                            }.start();
                        }
                        else
                        {
                            //Config.alertDialog(FriendListActivity.this, "Network Error", "No Internet connection");
                            Utility.CustomToast(FriendListActivity.this, "No Internet Connection..!");
                        }
                    }
                    else  if (!friendListModel.isEmergency() && friendListModel.getStatus().equalsIgnoreCase("Accepted"))
                    {
                        if (Config.isConnectingToInternet(FriendListActivity.this))
                        {
                            loading.setVisibility(View.VISIBLE);
                            geemergencyList=new SetFrinedAsEmergencyContactAsyncTask(friendListModel,true,position,FriendListActivity.this);
                            geemergencyList.execute();
                            isRespondingAPI=false;
                            new CountDownTimer( 15000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                }
                                public void onFinish() {
                                    loading.setVisibility(View.GONE);
                                    if (!isRespondingAPI)
                                    {
                                        if (geemergencyList != null && geemergencyList.getStatus() != AsyncTask.Status.FINISHED) {
                                            geemergencyList.cancel(true);
                                            Config.alertDialog(FriendListActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                        }
                                    }
                                }
                            }.start();
                        }
                        else
                        {
                            //Config.alertDialog(FriendListActivity.this, "Network Error", "No Internet connection");
                            Utility.CustomToast(FriendListActivity.this, "No Internet Connection..!");
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public class SetFrinedAsEmergencyContactAsyncTask  extends AsyncTask<Void,Void,StringBuilder> {

        StringBuilder resultbuilder;
        Context mycontext;
        // private OnTaskCompleted callback;
        int posi;
        SharedPreferences sharedpreferences;
       // String friendId;
        FriendListModel frndModel;
        boolean is_emergency=false;
        String friendId="";
        String userID="";

        public SetFrinedAsEmergencyContactAsyncTask(FriendListModel model,boolean isemergrncy,int pos,Context mycontext) {
            this.mycontext=mycontext;
            //this.friendId=frndis;
            this.is_emergency=isemergrncy;
            this.frndModel=model;
            this.posi=pos;
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            // dialog=ProgressDialog.show(mycontext,"","Inserting Data...!");
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {
            if (isCancelled())
            {
                return (null); // don't forget to terminate this method
            }
            resultbuilder =new StringBuilder();
            HttpClient httpClient=new DefaultHttpClient();
            String url= Config.URL_Mmp+"SetFrinedAsEmergencyContact";
            HttpPost httpPost=new HttpPost(url);
            String json="";
            StringEntity se=null;
            JSONObject jsonObject=new JSONObject();
            try
            {
                sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mycontext);
                userID=sharedpreferences.getString("UserId","");
                if (userID.equalsIgnoreCase(frndModel.getUserId()))
                {
                    friendId=frndModel.getFriendId();
                }
                else
                {
                    friendId=frndModel.getUserId();
                }

                jsonObject.put("UserId",sharedpreferences.getString("UserId",""));
                jsonObject.put("friendId",friendId);
                jsonObject.put("isEmergencyContact",is_emergency);

                json=jsonObject.toString();

                se=new StringEntity(json);
                httpPost.setEntity(se);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                HttpResponse httpResponse=httpClient.execute(httpPost);
                StatusLine statusLine=httpResponse.getStatusLine();
                int statuscode=statusLine.getStatusCode();
                if (statuscode==200)
                {
                    HttpEntity entity=httpResponse.getEntity();
                    InputStream content=entity.getContent();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line=reader.readLine())!=null)
                    {
                        resultbuilder.append(line);
                    }
                }
                else
                {
                    StringBuilder exceptionString = new StringBuilder();
                    HttpEntity entity = httpResponse.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while((line=reader.readLine()) != null)
                    {
                        exceptionString.append(line);
                    }

                    NetworkException.insertNetworkException(mycontext, exceptionString.toString());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
                String msg=e.getCause().getMessage();
                if (msg.contains("302"))
                {
                    resultbuilder.append(msg);
                }
            }
            return resultbuilder;
        }

        @Override
        protected void onPostExecute(StringBuilder stringBuilder) {
            super.onPostExecute(stringBuilder);
            //  dialog.dismiss();
            if (stringBuilder.toString().equalsIgnoreCase("true"))
            {
                FriendListModel fm=frndModel;
                if (is_emergency) {
                    fm.setIsEmergency(true);
                    /*fm.setIsmessaging(frndModel.ismessaging());
                    fm.setIstracking(frndModel.istracking());
                    fm.setFriendName(frndModel.getFriendName());
                    fm.setFriendId(friendId);
                    fm.setUserId(userID);
                    fm.setThumbnailUrl(frndModel.getThumbnailUrl());
                    fm.setFrndPositio(posi);*/
                    listAdapter.setEnableEmergency(fm);
                    listAdapter.notifyDataSetChanged();

                    NewFriendListModel newFriendListModel = qr.getSingleFriend(userID);
                    newFriendListModel.setIsEmergencyAlert("true");

                   // qr.updateFriendLIst(newFriendListModel.getFriendsId(),newFriendListModel.getFriendName(),newFriendListModel.getIsEmergencyAlert(),newFriendListModel.getIsMessagingAllowed(),newFriendListModel.getIsTrackingAllowed(),newFriendListModel.getFriendThumbnailUrl(),newFriendListModel.getStatus(),newFriendListModel.getIsRequestSent(),newFriendListModel.getCreateTS(),newFriendListModel.getAllowTrackingTillDate(),newFriendListModel.getTrackingStatusChangeDate(),newFriendListModel.getIsDeleted());

                   // Toast.makeText(FriendListActivity.this, "Emergency Activated to "+frndModel.getFriendName()+" Friend", Toast.LENGTH_SHORT).show();
                    Config.alertDialog(FriendListActivity.this, "Friend List", "Emergency Activated to "+frndModel.getFriendName()+" Friend.");
                }
                else {
                    fm.setIsEmergency(false);
                   /* fm.setIsmessaging(frndModel.ismessaging());
                    fm.setIstracking(frndModel.istracking());
                    fm.setFriendName(frndModel.getFriendName());
                    fm.setFriendId(friendId);
                    fm.setUserId(userID);
                    fm.setThumbnailUrl(frndModel.getThumbnailUrl());
                    fm.setFrndPositio(posi);*/
                    listAdapter.setDisableEmergency(fm);
                    listAdapter.notifyDataSetChanged();

                    NewFriendListModel newFriendListModel = qr.getSingleFriend(userID);
                    newFriendListModel.setIsEmergencyAlert("false");

                  //  qr.updateFriendLIst(newFriendListModel.getFriendsId(),newFriendListModel.getFriendName(),newFriendListModel.getIsEmergencyAlert(),newFriendListModel.getIsMessagingAllowed(),newFriendListModel.getIsTrackingAllowed(),newFriendListModel.getFriendThumbnailUrl(),newFriendListModel.getStatus(),newFriendListModel.getIsRequestSent(),newFriendListModel.getCreateTS(),newFriendListModel.getAllowTrackingTillDate(),newFriendListModel.getTrackingStatusChangeDate(),newFriendListModel.getIsDeleted());


                    Config.alertDialog(FriendListActivity.this, "Friend List","Emergency Deactivated to "+frndModel.getFriendName()+" Friend.");
                   // Toast.makeText(FriendListActivity.this, "Emergency Deactivated to "+frndModel.getFriendName()+" Friend", Toast.LENGTH_SHORT).show();
                }

            }
            else
            {
                //Toast.makeText(FriendListActivity.this, frndModel.getFriendName()+" is not your Friend", Toast.LENGTH_SHORT).show();
                Config.alertDialog(FriendListActivity.this, "Friend List", frndModel.getFriendName()+" is not your Friend.");
            }
            loading.setVisibility(View.GONE);
            /*stringBuilder.append("@@@SendAlert");
            callback.onTaskCompleted(stringBuilder.toString());*/
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Intent reg=new Intent(FriendListActivity.this,DashboardActivity.class);
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

            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendListActivity.this);
            if(update.equals("Emergency")) {

                String notType=sharedpreferences.getString("Type", "");
                if (notType.equalsIgnoreCase("FriendRequest"))
                {
                    String reqstName=sharedpreferences.getString("RequsetByName", "");
                    String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
                    Config.showacceptrejectFriendRequest(reqstName,thumbnail,FriendListActivity.this);
                    //Config.showacceptrejectFriendRequest(reqstName,FriendListActivity.this);
                }
                else if (notType.equalsIgnoreCase("Emergency"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String trobler=sharedpreferences.getString("TroublerName", "");
                    String troblerid=sharedpreferences.getString("TroublerUserId", "");
                    Config.showEmergencyAcceptReject(msg,trobler,troblerid,FriendListActivity.this);
                }
                else if (notType.equalsIgnoreCase("EmergencyRecipt"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("HelperUserName", "");
                    String isResch=sharedpreferences.getString("isReaching", "");
                    if (isResch.equalsIgnoreCase("true")){}
                    //showEmergencyAckAlert(newMsg, helpername);
                    Config.showEmergencyAckAlert(msg,helpername,FriendListActivity.this);
                }
                else if (notType.equalsIgnoreCase("FriendRequestAccepted"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("AcceptByName", "");

                    Config.showAccptedRequestAlert(msg,helpername,FriendListActivity.this);
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
                FriendListActivity.this.runOnUiThread(new UpdateUI("Emergency"));
            }
            if(resultCode == 200){
                FriendListActivity.this.runOnUiThread(new UpdateUI("RefreshThreadList"));
            }

        }
    }

    public void showTrackAgainAlert(final String msg,final String friendid,final ArrayList<NearByFriends> nearFrndlist) {

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(FriendListActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Tracking Alert");

        // Setting Dialog Message
        alertDialog.setMessage(msg);

        // On pressing Settings button
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (Config.isConnectingToInternet(FriendListActivity.this))
                        {
                            //loading.setVisibility(View.VISIBLE);
                            TrackFriendAsynctask getListbyname=new TrackFriendAsynctask(friendid,FriendListActivity.this,FriendListActivity.this);
                            getListbyname.execute();
                        }
                        else
                        {
                            Config.alertDialog(FriendListActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                        }
                        dialog.dismiss();
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        realizer.com.makemepopular.utils.Singleton.setSingleNearFriendList(nearFrndlist);
                        Intent intent = new Intent(FriendListActivity.this, FriendNearActivity.class);
                        Bundle bu=new Bundle();
                        bu.putInt("Distance",0);
                        bu.putString("Interest","");
                        bu.putString("Flag","Single");
                        intent.putExtras(bu);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }

    public void getFriendList()
    {
        friendlist=new ArrayList<>();
        employeeQuickViewList = new ArrayList<>();
        ArrayList<NewFriendListModel> newFriendListModels=new ArrayList<>();
        newFriendListModels=qr.getAllFriendsData();
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendListActivity.this);
        String userID=sharedpreferences.getString("UserId","");
        for (int i=0;i<newFriendListModels.size();i++)
        {
            FriendListModel model=new FriendListModel();
            model.setUserId(userID);
            model.setFriendName(newFriendListModels.get(i).getFriendName());
            model.setIsEmergency(Boolean.valueOf(newFriendListModels.get(i).getIsEmergencyAlert()));
            model.setIsmessaging(Boolean.valueOf(newFriendListModels.get(i).getIsMessagingAllowed()));
            model.setIstracking(Boolean.valueOf(newFriendListModels.get(i).getIsTrackingAllowed()));
            model.setThumbnailUrl(newFriendListModels.get(i).getFriendThumbnailUrl());
            model.setFriendId(newFriendListModels.get(i).getFriendsId());
            model.setStatus(newFriendListModels.get(i).getStatus());
            model.setSentRequest(Boolean.valueOf(newFriendListModels.get(i).getIsRequestSent()));
            friendlist.add(model);
        }

        if (friendlist != null) {
            if (friendlist.size() > 0)
            {
                Collections.sort(friendlist, new ChatNoCaseComparator());
                employeeQuickViewList.clear();
                for (FriendListModel emp : friendlist) {
                    employeeQuickViewList.add(new FavoriteListAdapter.Friend(emp));
                }
                // Adding child data
                listAdapter.setChildList(employeeQuickViewList);
                //listAdapter= new InterestAdapter(this,afm);
                favoriteListView.setAdapter(listAdapter);
                listAdapter.notifyDataSetChanged();
                noData.setVisibility(View.GONE);
                favoriteListView.setVisibility(View.VISIBLE);
                // favoriteListView.setFastScrollAlwaysVisible(true);
                favoriteListView.smoothScrollToPosition(favoriteListView.getChildCount());
            }
            else
            {
                noData.setVisibility(View.VISIBLE);
                favoriteListView.setVisibility(View.GONE);
            }
        }
        else
        {
            noData.setVisibility(View.VISIBLE);
            favoriteListView.setVisibility(View.GONE);
        }
    }

    public class ChatNoCaseComparator implements Comparator<FriendListModel> {
        public int compare(FriendListModel s1, FriendListModel s2) {
            return s1.getFriendName().compareToIgnoreCase(s2.getFriendName());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent reg=new Intent(FriendListActivity.this,DashboardActivity.class);
        startActivity(reg);
        finish();
    }
}
