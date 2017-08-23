package realizer.com.makemepopular.newfriendlist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import realizer.com.makemepopular.DashboardActivity;
import realizer.com.makemepopular.R;
import realizer.com.makemepopular.asynctask.AcceptRejectFriendRequestAsyntask;
import realizer.com.makemepopular.asynctask.BlockFriendAsyntask;
import realizer.com.makemepopular.asynctask.GetFriendListAsynTask;
import realizer.com.makemepopular.asynctask.UnFriendAsyncTask;
import realizer.com.makemepopular.backend.DatabaseQueries;
import realizer.com.makemepopular.chat.model.NewFriendListModel;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.exceptionhandler.NetworkException;
import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.friendnear.FriendNearActivity;
import realizer.com.makemepopular.models.NearByFriends;
import realizer.com.makemepopular.newfriendlist.adapter.FriendListGridAdapter;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.GetImages;
import realizer.com.makemepopular.utils.ImageStorage;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 28-02-2017.
 */

public class NewFriendListActivity extends AppCompatActivity implements OnTaskCompleted {

    DatabaseQueries qr;
    GridView gridFriendList;
    ArrayList<FriendListModel> friendlist=new ArrayList<>();
    FriendListGridAdapter listAdapter;
    ProgressWheel loading,dialogueLoading;
    TextView emergency,noData;
    int position;
    AlertDialog alertDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, ""));
        qr=new DatabaseQueries(NewFriendListActivity.this);
        setContentView(R.layout.friend_list_activity_new);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Friends");

        gridFriendList = (GridView) findViewById(R.id.grid_friendlist);
        loading =(ProgressWheel) findViewById(R.id.loading);
        noData= (TextView) findViewById(R.id.noFriendFound);


        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NewFriendListActivity.this);

        if (sharedpreferences.getString("Type","").equalsIgnoreCase("FriendRequestAccepted"))
        {
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("Type", "");
            edit.putString("AcceptByName", "");
            edit.putString("AcceptByUserId","");
            edit.putString("ThumbnailUrl","");
            edit.commit();
        }


        if (Config.isConnectingToInternet(NewFriendListActivity.this))
        {
            loading.setVisibility(View.VISIBLE);
            String time="";
            int count=qr.chekFriendTable();
            if(count>0)
            {
                time=qr.getCreateTsFriend();
            }
            else{
                time="";

            }
            //
            GetFriendListAsynTask getfrnd=new GetFriendListAsynTask("Other",NewFriendListActivity.this,NewFriendListActivity.this,time);
            getfrnd.execute();
        }
        else
        {
            loading.setVisibility(View.VISIBLE);
            Utility.CustomToast(NewFriendListActivity.this, "No Internet Connection..!");
            getFriendList();
        }

        gridFriendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
             position = i;
             showSelectedUser(friendlist.get(i));
            }
        });
    }

    public void showSelectedUser(final FriendListModel frnd){
            LayoutInflater inflater= this.getLayoutInflater();
            View dialoglayout = inflater.inflate(R.layout.friend_list_grid_click_dialogue, null);
            final Button accept= (Button) dialoglayout.findViewById(R.id.btn_accept);
            final Button reject= (Button) dialoglayout.findViewById(R.id.btn_reject);
            dialogueLoading =(ProgressWheel) dialoglayout.findViewById(R.id.loading);
            LinearLayout btnLayout= (LinearLayout) dialoglayout.findViewById(R.id.ll_btn);
            TextView userName= (TextView) dialoglayout.findViewById(R.id.txt_userName);
            ImageView userPic = (ImageView) dialoglayout.findViewById(R.id.iv_userPic);
            TextView genderAge= (TextView) dialoglayout.findViewById(R.id.txt_genderage);
            TextView email= (TextView) dialoglayout.findViewById(R.id.txt_email);

        final TextView viewProfile = (TextView) dialoglayout.findViewById(R.id.txt_profile);
        viewProfile.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        final TextView unFriend = (TextView) dialoglayout.findViewById(R.id.txt_unfriend);
        unFriend.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        final TextView block = (TextView) dialoglayout.findViewById(R.id.txt_block);
        block.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));


        final TextView track = (TextView) dialoglayout.findViewById(R.id.txt_track);
        track.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        emergency = (TextView) dialoglayout.findViewById(R.id.txt_emergency);
        emergency.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

          SetThumbnail(userPic,frnd.getThumbnailUrl());
          userName.setText(frnd.getFriendName());
          genderAge.setText(frnd.getGender()+", "+frnd.getAge());
          email.setText(frnd.getEmail());

        if(!frnd.isSentRequest()){
            if(frnd.getStatus().equalsIgnoreCase("Pending")){
                btnLayout.setVisibility(View.VISIBLE);
            }
            else {
                btnLayout.setVisibility(View.GONE);
            }
        }
        else {
            btnLayout.setVisibility(View.GONE);
        }

        //Tracking

          if(frnd.istracking()){
              track.setTextColor(getResources().getColor(R.color.pink));
          }
        else {
              track.setTextColor(getResources().getColor(R.color.secondary_text));
          }


         //Emergency

         if(frnd.isEmergency()){
             emergency.setText(getResources().getString(R.string.fa_toggle_on_ico));
             emergency.setTextColor(getResources().getColor(R.color.forestgreen));
         }
        else {
             emergency.setText(getResources().getString(R.string.fa_toggle_off_ico));
             emergency.setTextColor(getResources().getColor(R.color.secondary_text));
         }

         if(!frnd.getStatus().equalsIgnoreCase("Accepted")){
             unFriend.setTextColor(getResources().getColor(R.color.secondary_text));
             block.setTextColor(getResources().getColor(R.color.secondary_text));
         }

        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        if(frnd.getStatus().equalsIgnoreCase("Accepted")) {

            unFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (frnd.getStatus().equalsIgnoreCase("Accepted")) {
                        if (Config.isConnectingToInternet(NewFriendListActivity.this)) {
                            dialogueLoading.setVisibility(View.VISIBLE);
                            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NewFriendListActivity.this);
                            String userID = sharedpreferences.getString("UserId", "");
                            String friendId = "";
                            if (userID.equalsIgnoreCase(frnd.getUserId())) {
                                friendId = frnd.getFriendId();
                            } else {
                                friendId = frnd.getUserId();
                            }
                            UnFriendAsyncTask getListbyname = new UnFriendAsyncTask(friendId, NewFriendListActivity.this, NewFriendListActivity.this);
                            getListbyname.execute();
                        } else {
                            //Config.alertDialog(FriendListActivity.this, "Network Error", "No Internet connection");
                            Utility.CustomToast(NewFriendListActivity.this, "No Internet Connection..!");
                        }
                    }
                }
            });

            block.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (frnd.getStatus().equalsIgnoreCase("Accepted")) {
                        if (Config.isConnectingToInternet(NewFriendListActivity.this)) {
                            dialogueLoading.setVisibility(View.VISIBLE);
                            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NewFriendListActivity.this);
                            String userID = sharedpreferences.getString("UserId", "");
                            String friendId = "";
                            if (userID.equalsIgnoreCase(frnd.getUserId())) {
                                friendId = frnd.getFriendId();
                            } else {
                                friendId = frnd.getUserId();
                            }
                            BlockFriendAsyntask getListbyname = new BlockFriendAsyntask(friendId, NewFriendListActivity.this, NewFriendListActivity.this);
                            getListbyname.execute();
                        } else {
                            //Config.alertDialog(FriendListActivity.this, "Network Error", "No Internet connection");
                            Utility.CustomToast(NewFriendListActivity.this, "No Internet Connection..!");
                        }
                    }
                }
            });

            track.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (frnd.getStatus().equalsIgnoreCase("Accepted")) {
                        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NewFriendListActivity.this);
                        String userID = sharedpreferences.getString("UserId", "");
                        String friendId = "";
                        if (userID.equalsIgnoreCase(frnd.getUserId())) {
                            friendId = frnd.getFriendId();
                        } else {
                            friendId = frnd.getUserId();
                        }

                        Intent intent = new Intent(NewFriendListActivity.this, FriendNearActivity.class);
                        Bundle bu = new Bundle();
                        bu.putInt("Distance", 0);
                        bu.putString("Interest", "");
                        bu.putString("Flag", "Single");
                        bu.putString("FROMWHERE", "");
                        bu.putString("FriendId", friendId);
                        intent.putExtras(bu);
                        startActivity(intent);
                        finish();
                    }

                }
            });

            emergency.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (frnd.isEmergency() && frnd.getStatus().equalsIgnoreCase("Accepted")) {
                        if (Config.isConnectingToInternet(NewFriendListActivity.this)) {
                            dialogueLoading.setVisibility(View.VISIBLE);
                            SetFrinedAsEmergencyContactAsyncTask getListbyname = new SetFrinedAsEmergencyContactAsyncTask(frnd, false, position, NewFriendListActivity.this);
                            getListbyname.execute();
                        } else {
                            //Config.alertDialog(FriendListActivity.this, "Network Error", "No Internet connection");
                            Utility.CustomToast(NewFriendListActivity.this, "No Internet Connection..!");
                        }
                    } else if (!frnd.isEmergency() && frnd.getStatus().equalsIgnoreCase("Accepted")) {
                        if (Config.isConnectingToInternet(NewFriendListActivity.this)) {
                            dialogueLoading.setVisibility(View.VISIBLE);
                            SetFrinedAsEmergencyContactAsyncTask getListbyname = new SetFrinedAsEmergencyContactAsyncTask(frnd, true, position, NewFriendListActivity.this);
                            getListbyname.execute();
                        } else {
                            //Config.alertDialog(FriendListActivity.this, "Network Error", "No Internet connection");
                            Utility.CustomToast(NewFriendListActivity.this, "No Internet Connection..!");
                        }
                    }
                }
            });
        }
        if(!frnd.isSentRequest()) {
            if (frnd.getStatus().equalsIgnoreCase("Pending")) {

                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (Config.isConnectingToInternet(NewFriendListActivity.this)) {

                            dialogueLoading.setVisibility(View.VISIBLE);
                            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NewFriendListActivity.this);
                            String userID = sharedpreferences.getString("UserId", "");
                            String friendId = "";
                            if (userID.equalsIgnoreCase(frnd.getUserId())) {
                                friendId = frnd.getFriendId();
                            } else {
                                friendId = frnd.getUserId();
                            }
                            AcceptRejectFriendRequestAsyntask sendalert = new AcceptRejectFriendRequestAsyntask("", "AcceptRequest", friendId, false, true, NewFriendListActivity.this, NewFriendListActivity.this);
                            sendalert.execute();
                        } else {
                            //Config.alertDialog(FriendListActivity.this, "Network Error", "No Internet connection");
                            Utility.CustomToast(NewFriendListActivity.this, "No Internet Connection..!");
                        }

                    }
                });

                reject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Config.isConnectingToInternet(NewFriendListActivity.this)) {
                            //  acceptMadel=friendListModel;
                            dialogueLoading.setVisibility(View.VISIBLE);
                            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NewFriendListActivity.this);
                            String userID = sharedpreferences.getString("UserId", "");
                            String friendId = "";
                            if (userID.equalsIgnoreCase(frnd.getUserId())) {
                                friendId = frnd.getFriendId();
                            } else {
                                friendId = frnd.getUserId();
                            }
                            AcceptRejectFriendRequestAsyntask sendalert = new AcceptRejectFriendRequestAsyntask("", "RejectRequest", friendId, false, false, NewFriendListActivity.this, NewFriendListActivity.this);
                            sendalert.execute();
                        } else {
                            //Config.alertDialog(FriendListActivity.this, "Network Error", "No Internet connection");
                            Utility.CustomToast(NewFriendListActivity.this, "No Internet Connection..!");
                        }
                    }
                });
            }
        }

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialoglayout);
            alertDialog = builder.create();
            alertDialog.show();
    }


    public void SetThumbnail(ImageView userimg,String profilePic) {
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

    public void getFriendList() {
        friendlist = new ArrayList<>();
        ArrayList<NewFriendListModel> newFriendListModels = new ArrayList<>();
        newFriendListModels = qr.getAllFriendsData();
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NewFriendListActivity.this);
        String userID = sharedpreferences.getString("UserId", "");
        for (int i = 0; i < newFriendListModels.size(); i++) {
            FriendListModel model = new FriendListModel();
            model.setUserId(userID);
            model.setFriendName(newFriendListModels.get(i).getFriendName());
            model.setIsEmergency(Boolean.valueOf(newFriendListModels.get(i).getIsEmergencyAlert()));
            model.setIsmessaging(Boolean.valueOf(newFriendListModels.get(i).getIsMessagingAllowed()));
            model.setIstracking(Boolean.valueOf(newFriendListModels.get(i).getIsTrackingAllowed()));
            model.setThumbnailUrl(newFriendListModels.get(i).getFriendThumbnailUrl());
            model.setFriendId(newFriendListModels.get(i).getFriendsId());
            model.setStatus(newFriendListModels.get(i).getStatus());
            model.setSentRequest(Boolean.valueOf(newFriendListModels.get(i).getIsRequestSent()));
            model.setLastupdated(newFriendListModels.get(i).getLastActive());
            model.setDob(newFriendListModels.get(i).getDob());
            model.setAge(newFriendListModels.get(i).getAge());
            model.setGender(newFriendListModels.get(i).getGender());
            model.setEmail(newFriendListModels.get(i).getEmail());

            friendlist.add(model);
        }

        loading.setVisibility(View.GONE);

        if(friendlist.size() > 0) {
            listAdapter = new FriendListGridAdapter(NewFriendListActivity.this, friendlist);
            gridFriendList.setAdapter(listAdapter);
            noData.setVisibility(View.GONE);
            gridFriendList.setVisibility(View.VISIBLE);
        }
        else {
            noData.setVisibility(View.VISIBLE);
            gridFriendList.setVisibility(View.GONE);
        }

    }

    @Override
    public void onTaskCompleted(String s) {
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
                            qr.insertFriendList(jsonobject.getString("friendsUserId"), jsonobject.getString("friendName"), String.valueOf(jsonobject.getBoolean("isEmergencyAlert")), String.valueOf(jsonobject.getBoolean("isMessagingAllowed")), String.valueOf(jsonobject.getBoolean("isTrackingAllowed")), jsonobject.getString("friendThumbnailUrl"), jsonobject.getString("status"), String.valueOf(jsonobject.getBoolean("isRequestSent")), jsonobject.getString("createTS"), jsonobject.getString("allowTrackingTillDate"), jsonobject.getString("trackingStatusChangeDate"), jsonobject.getString("isDeleted"),jsonobject.getString("lastActive"),jsonobject.getString("dob"),jsonobject.getString("age"),jsonobject.getString("gender"),jsonobject.getString("email"));
                        } else {
                            qr.updateFriendLIst(jsonobject.getString("friendsUserId"), jsonobject.getString("friendName"), String.valueOf(jsonobject.getBoolean("isEmergencyAlert")), String.valueOf(jsonobject.getBoolean("isMessagingAllowed")), String.valueOf(jsonobject.getBoolean("isTrackingAllowed")), jsonobject.getString("friendThumbnailUrl"), jsonobject.getString("status"), String.valueOf(jsonobject.getBoolean("isRequestSent")), jsonobject.getString("createTS"), jsonobject.getString("allowTrackingTillDate"), jsonobject.getString("trackingStatusChangeDate"), jsonobject.getString("isDeleted"),jsonobject.getString("lastActive"),jsonobject.getString("dob"),jsonobject.getString("age"),jsonobject.getString("gender"),jsonobject.getString("email"));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    loading.setVisibility(View.GONE);
                }

            }
            else if(arr[0].equalsIgnoreCase("ServerNotResponding"))
            {
                loading.setVisibility(View.GONE);
                Config.alertDialog(NewFriendListActivity.this,"Error","Server not responding.Please try again after sometime");
            }
            else
            {
                loading.setVisibility(View.GONE);
                //Config.alertDialog(FriendListActivity.this,"Error","Server not responding.Please try again after sometime");
            }
            getFriendList();

        }
        else if (arr[1].equalsIgnoreCase("TrackFriend"))
        {
            if(!arr[0].equalsIgnoreCase("302"))
            {

                JSONObject json = null;
                try {
                    json = new JSONObject(arr[0]);

                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NewFriendListActivity.this);
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
                        Intent intent = new Intent(NewFriendListActivity.this, FriendNearActivity.class);
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
                qr.deleteUserFromTable(friendid);
                friendlist.remove(position);
                listAdapter.notifyDataSetChanged();
                if(alertDialog.isShowing())
                    alertDialog.cancel();
            }
            else
            {
                //Toast.makeText(FriendListActivity.this, "Friend Is Not Unfriend", Toast.LENGTH_SHORT).show();
                Config.alertDialog(this, "Friend List", "Friend is not unfriend.");

            }
            dialogueLoading.setVisibility(View.GONE);
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
                qr.deleteUserFromTable(friendid);
                friendlist.remove(position);
                listAdapter.notifyDataSetChanged();
                if(alertDialog.isShowing())
                    alertDialog.cancel();
            }
            else
            {
                //Toast.makeText(FriendListActivity.this, "Friend Is Not Block", Toast.LENGTH_SHORT).show();
                Config.alertDialog(this, "Friend List", "Friend is not block.");
            }
            dialogueLoading.setVisibility(View.GONE);
        }

        else if (arr[1].equalsIgnoreCase("AcceptReject"))
        {
            if (arr[0].equalsIgnoreCase("true"))
            {
                FriendListModel acceptMadel=new FriendListModel();
                if (arr[2].equalsIgnoreCase("AcceptRequest")) {
                    Config.alertDialog(this, "Friend List", "Friend Request Accepted Successfully.");
                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NewFriendListActivity.this);
                    String userID=sharedpreferences.getString("UserId","");
                    String friendId="";
                    acceptMadel = friendlist.get(position);
                    if (userID.equalsIgnoreCase(acceptMadel.getUserId()))
                    {
                        friendId=acceptMadel.getFriendId();
                    }
                    else
                    {
                        friendId=acceptMadel.getUserId();
                    }


                    acceptMadel.setUserId(userID);
                    acceptMadel.setFriendId(friendId);
                    acceptMadel.setStatus("Accepted");

                    friendlist.set(position,acceptMadel);
                    listAdapter.notifyDataSetChanged();
                    // SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendListActivity.this);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("Type", "");
                    edit.putString("RequsetByName", "");
                    edit.putString("ThumbnailUrl","");
                    edit.putString("RequsetByUserId","");
                    edit.commit();
                    dialogueLoading.setVisibility(View.GONE);

                    if(alertDialog.isShowing())
                        alertDialog.cancel();
                    showSelectedUser(acceptMadel);
                }
                    //Toast.makeText(FriendListActivity.this, "Friend Request Accepted Successfully", Toast.LENGTH_SHORT).show();
                else {
                    Config.alertDialog(this, "Friend List", "Friend Request Rejected Successfully.");
                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NewFriendListActivity.this);
                    String userID=sharedpreferences.getString("UserId","");
                    String friendId="";
                  acceptMadel = friendlist.get(position);
                    if (userID.equalsIgnoreCase(acceptMadel.getUserId()))
                    {
                        friendId=acceptMadel.getFriendId();
                    }
                    else
                    {
                        friendId=acceptMadel.getUserId();
                    }


                    acceptMadel.setUserId(userID);
                    acceptMadel.setFriendId(friendId);
                    acceptMadel.setStatus("Rejected");

                    friendlist.set(position,acceptMadel);
                    listAdapter.notifyDataSetChanged();
                    // SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(FriendListActivity.this);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("Type", "");
                    edit.putString("RequsetByName", "");
                    edit.putString("ThumbnailUrl","");
                    edit.putString("RequsetByUserId","");
                    edit.commit();
                    dialogueLoading.setVisibility(View.GONE);
                    if(alertDialog.isShowing())
                        alertDialog.cancel();
                }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Intent reg=new Intent(NewFriendListActivity.this,DashboardActivity.class);
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

            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(NewFriendListActivity.this);
            if(update.equals("Emergency")) {

                String notType=sharedpreferences.getString("Type", "");
                if (notType.equalsIgnoreCase("FriendRequest"))
                {
                    String reqstName=sharedpreferences.getString("RequsetByName", "");
                    String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
                    Config.showacceptrejectFriendRequest(reqstName,thumbnail,NewFriendListActivity.this);
                    //Config.showacceptrejectFriendRequest(reqstName,FriendListActivity.this);
                }
                else if (notType.equalsIgnoreCase("Emergency"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String trobler=sharedpreferences.getString("TroublerName", "");
                    String troblerid=sharedpreferences.getString("TroublerUserId", "");
                    Config.showEmergencyAcceptReject(msg,trobler,troblerid,NewFriendListActivity.this);
                }
                else if (notType.equalsIgnoreCase("EmergencyRecipt"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("HelperUserName", "");
                    String isResch=sharedpreferences.getString("isReaching", "");
                    if (isResch.equalsIgnoreCase("true")){}
                    //showEmergencyAckAlert(newMsg, helpername);
                    Config.showEmergencyAckAlert(msg,helpername,NewFriendListActivity.this);
                }
                else if (notType.equalsIgnoreCase("FriendRequestAccepted"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("AcceptByName", "");

                    Config.showAccptedRequestAlert(msg,helpername,NewFriendListActivity.this);
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
                NewFriendListActivity.this.runOnUiThread(new UpdateUI("Emergency"));
            }
            if(resultCode == 200){
                NewFriendListActivity.this.runOnUiThread(new UpdateUI("RefreshThreadList"));
            }

        }
    }


    class SetFrinedAsEmergencyContactAsyncTask  extends AsyncTask<Void,Void,StringBuilder> {

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


                    NewFriendListModel newFriendListModel = qr.getSingleFriend(userID);
                    newFriendListModel.setIsEmergencyAlert("true");

                    qr.updateFriendLIst(newFriendListModel.getFriendsId(),newFriendListModel.getFriendName(),newFriendListModel.getIsEmergencyAlert(),newFriendListModel.getIsMessagingAllowed(),newFriendListModel.getIsTrackingAllowed(),newFriendListModel.getFriendThumbnailUrl(),newFriendListModel.getStatus(),newFriendListModel.getIsRequestSent(),newFriendListModel.getCreateTS(),newFriendListModel.getAllowTrackingTillDate(),newFriendListModel.getTrackingStatusChangeDate(),newFriendListModel.getIsDeleted(),newFriendListModel.getLastActive(),newFriendListModel.getDob(),newFriendListModel.getAge(),newFriendListModel.getGender(),newFriendListModel.getEmail());

                        friendlist.set(position,fm);
                        listAdapter.notifyDataSetChanged();
                        emergency.setText(getResources().getString(R.string.fa_toggle_on_ico));
                        emergency.setTextColor(getResources().getColor(R.color.forestgreen));


                    // Toast.makeText(FriendListActivity.this, "Emergency Activated to "+frndModel.getFriendName()+" Friend", Toast.LENGTH_SHORT).show();
                    Config.alertDialog(NewFriendListActivity.this, "Friend List", "Emergency Activated to "+frndModel.getFriendName()+" Friend.");
                }
                else {
                    fm.setIsEmergency(false);

                    NewFriendListModel newFriendListModel = qr.getSingleFriend(userID);
                    newFriendListModel.setIsEmergencyAlert("false");

                    qr.updateFriendLIst(newFriendListModel.getFriendsId(),newFriendListModel.getFriendName(),newFriendListModel.getIsEmergencyAlert(),newFriendListModel.getIsMessagingAllowed(),newFriendListModel.getIsTrackingAllowed(),newFriendListModel.getFriendThumbnailUrl(),newFriendListModel.getStatus(),newFriendListModel.getIsRequestSent(),newFriendListModel.getCreateTS(),newFriendListModel.getAllowTrackingTillDate(),newFriendListModel.getTrackingStatusChangeDate(),newFriendListModel.getIsDeleted(),newFriendListModel.getLastActive(),newFriendListModel.getDob(),newFriendListModel.getAge(),newFriendListModel.getGender(),newFriendListModel.getEmail());

                    friendlist.set(position,fm);
                    listAdapter.notifyDataSetChanged();
                    emergency.setText(getResources().getString(R.string.fa_toggle_off_ico));
                    emergency.setTextColor(getResources().getColor(R.color.secondary_text));

                    Config.alertDialog(NewFriendListActivity.this, "Friend List","Emergency Deactivated to "+frndModel.getFriendName()+" Friend.");
                    // Toast.makeText(FriendListActivity.this, "Emergency Deactivated to "+frndModel.getFriendName()+" Friend", Toast.LENGTH_SHORT).show();
                }

            }
            else
            {
                //Toast.makeText(FriendListActivity.this, frndModel.getFriendName()+" is not your Friend", Toast.LENGTH_SHORT).show();
                Config.alertDialog(NewFriendListActivity.this, "Friend List", frndModel.getFriendName()+" is not your Friend.");
            }
            dialogueLoading.setVisibility(View.GONE);
            /*stringBuilder.append("@@@SendAlert");
            callback.onTaskCompleted(stringBuilder.toString());*/
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent reg=new Intent(NewFriendListActivity.this,DashboardActivity.class);
        startActivity(reg);
        finish();
    }

}
