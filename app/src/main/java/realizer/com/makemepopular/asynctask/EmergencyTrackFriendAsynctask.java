package realizer.com.makemepopular.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import realizer.com.makemepopular.exceptionhandler.NetworkException;
import realizer.com.makemepopular.friendnear.FriendNearActivity;
import realizer.com.makemepopular.models.NearByFriends;
import realizer.com.makemepopular.service.TrackShowMap;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;

/**
 * Created by shree on 1/23/2017.
 */
public class EmergencyTrackFriendAsynctask extends AsyncTask<Void,Void,StringBuilder> {

    StringBuilder resultbuilder;
    Context mycontext;
    String friendID;
    //private OnTaskCompleted callback;
    SharedPreferences sharedpreferences;
    ProgressDialog dialog;

    public EmergencyTrackFriendAsynctask(String frndid, Context mycontext) {
        this.mycontext=mycontext;
        //this.callback=cb;
        this.friendID=frndid;
    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
        dialog=ProgressDialog.show(mycontext,"","Pulling coordinates,Please wait...!");
    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        resultbuilder =new StringBuilder();
        HttpClient httpClient=new DefaultHttpClient();
        String url= Config.URL_Tracking+"TrackFriend";
        HttpPost httpPost=new HttpPost(url);
        String json="";
        StringEntity se=null;
        JSONObject jsonObject=new JSONObject();
        try
        {
            sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mycontext);

            jsonObject.put("userId",sharedpreferences.getString("UserId",""));
            jsonObject.put("friendId",friendID);
            jsonObject.put("isNotify",true);

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
        dialog.dismiss();

        if(!stringBuilder.toString().equalsIgnoreCase("302"))
        {
            JSONObject json = null;
            try {
                json = new JSONObject(stringBuilder.toString());

                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mycontext);
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
                    near.setLastupdate(json.getString("lastUpdatedOn"));
                    nearFrndlist.add(near);

                    realizer.com.makemepopular.utils.Singleton.setSingleNearFriendList(nearFrndlist);

                    Intent intent = new Intent(mycontext, FriendNearActivity.class);
                    Bundle bu=new Bundle();
                    bu.putInt("Distance",0);
                    bu.putString("Interest","");
                    bu.putString("Flag","Single");
                    intent.putExtras(bu);
                    mycontext.startActivity(intent);
                   /* Intent intent = new Intent(mycontext, TrackShowMap.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("FriendName",json.getString("friendName"));
                    bundle.putDouble("LATITUDE",  json.getDouble("latitude"));
                    bundle.putDouble("LONGITUDE", json.getDouble("longitude"));
                    bundle.putString("Age",json.getString("age"));
                    bundle.putString("gender",json.getString("gender"));
                    bundle.putString("thumbnailUrl",json.getString("thumbnailUrl"));
                    bundle.putString("lastupdated",json.getString("lastUpdatedOn"));
                    intent.putExtras(bundle);
                    mycontext.startActivity(intent);*/
                }
                else
                {
                    Toast.makeText(mycontext, "No data available for this friend", Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(mycontext,"You have no permission to track this friend",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
