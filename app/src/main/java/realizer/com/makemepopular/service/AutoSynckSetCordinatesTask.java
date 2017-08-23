package realizer.com.makemepopular.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

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
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;

/**
 * Created by shree on 1/23/2017.
 */
public class AutoSynckSetCordinatesTask extends AsyncTask<Void,Void,StringBuilder> {

    StringBuilder resultbuilder;
    Context mycontext;
    Double latitude,longitude;
    private OnTaskCompleted callback;
    SharedPreferences sharedpreferences;

    public AutoSynckSetCordinatesTask(Double lat,Double log,Context mycontext, OnTaskCompleted cb) {
        this.mycontext=mycontext;
        this.callback=cb;
        this.latitude=lat;
        this.longitude=log;
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
        String url= Config.URL_Tracking+"setUserCordinates";
        HttpPost httpPost=new HttpPost(url);
        String json="";
        StringEntity se=null;
        JSONObject jsonObject=new JSONObject();
        try
        {
            sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mycontext);

            jsonObject.put("userId",sharedpreferences.getString("UserId",""));
            jsonObject.put("latitude",latitude);
            jsonObject.put("longitude",longitude);
            jsonObject.put("city","");

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
        //  dialog.dismiss();
        stringBuilder.append("@@@TrackFriend");

        callback.onTaskCompleted(stringBuilder.toString());
    }

}
