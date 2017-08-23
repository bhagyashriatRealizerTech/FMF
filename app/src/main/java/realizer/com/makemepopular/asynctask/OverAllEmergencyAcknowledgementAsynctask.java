package realizer.com.makemepopular.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.View;

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

import realizer.com.makemepopular.exceptionhandler.NetworkException;
import realizer.com.makemepopular.friendnear.FriendNearActivity;
import realizer.com.makemepopular.service.TrackShowMap;
import realizer.com.makemepopular.utils.Config;

/**
 * Created by shree on 1/24/2017.
 */
public class OverAllEmergencyAcknowledgementAsynctask extends AsyncTask<Void,Void,StringBuilder> {

    StringBuilder resultbuilder;
    Context mycontext;
    //private OnTaskCompleted callback;
    SharedPreferences sharedpreferences;
    String friendID;
    boolean isReaching=false;
    String message;
    String fromeWhere;
    ProgressDialog dialog;
    static boolean isRespondingAPI=false;

    public OverAllEmergencyAcknowledgementAsynctask(String fromwre, String frndid, boolean isreach, String msg, Context mycontext) {
        this.mycontext=mycontext;
        //this.callback=cb;
        this.friendID=frndid;
        this.isReaching=isreach;
        this.message=msg;
        this.fromeWhere=fromwre;
    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
         dialog=ProgressDialog.show(mycontext,"","Emergency Acknowledgement...");
        isRespondingAPI=false;
        new CountDownTimer( 15000, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                if (!isRespondingAPI)
                {
                    isCancelled();
                    Config.alertDialog(mycontext, "Error", "Server is not responding.Please try again after sometime.");
                }
            }
        }.start();
    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        if (isCancelled())
        {
            return (null); // don't forget to terminate this method
        }
        resultbuilder =new StringBuilder();
        HttpClient httpClient=new DefaultHttpClient();
        String url= Config.URL_Mmp+"EmergencyAcknowledgement";
        HttpPost httpPost=new HttpPost(url);
        String json="";
        StringEntity se=null;
        JSONObject jsonObject=new JSONObject();
        try
        {
            sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mycontext);

            jsonObject.put("userId",sharedpreferences.getString("UserId",""));
            jsonObject.put("friendId",friendID);
            jsonObject.put("message",message);
            jsonObject.put("isReaching",isReaching);

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
        }
        return resultbuilder;
    }

    @Override
    protected void onPostExecute(StringBuilder stringBuilder) {
        super.onPostExecute(stringBuilder);
        isRespondingAPI=true;
        //stringBuilder.append("@@@EmergencyAck@@@"+fromeWhere);

        if (stringBuilder.toString().equalsIgnoreCase("true"))
        {
            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mycontext);
            if (fromeWhere.equalsIgnoreCase("ACK & Map")) {

                Intent intent = new Intent(mycontext, FriendNearActivity.class);
                Bundle bu=new Bundle();
                bu.putInt("Distance",0);
                bu.putString("Interest","");
                bu.putString("Flag","Single");
                bu.putString("FROMWHERE","Emergency");
                bu.putString("FriendId",friendID);
                intent.putExtras(bu);
                mycontext.startActivity(intent);
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
        dialog.dismiss();

        //callback.onTaskCompleted(stringBuilder.toString());
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        dialog.dismiss();
    }
}
