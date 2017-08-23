package realizer.com.makemepopular.notifications;

import android.app.ProgressDialog;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import realizer.com.makemepopular.exceptionhandler.NetworkException;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;

/**
 * Created by Win on 25/01/2017.
 */
public class NotificationAsyncTask extends AsyncTask<Void,Void,StringBuilder> {

    StringBuilder resultbuilder;
    Context mycontext;
    private OnTaskCompleted callback;
    SharedPreferences sharedpreferences;
    ArrayList<String> notificationType;
    ProgressDialog dialog;

    public NotificationAsyncTask(ArrayList<String> notType,Context mycontext, OnTaskCompleted cb) {
        this.mycontext=mycontext;
        this.callback=cb;
        this.notificationType=notType;
    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
        //dialog=ProgressDialog.show(mycontext,"","Loading Data...!!!");
    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        if (isCancelled())
        {
            return (null); // don't forget to terminate this method
        }
        resultbuilder =new StringBuilder();
        HttpClient httpClient=new DefaultHttpClient();
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mycontext);
        String my= Config.URL_Mmp+"GetMyNotifications/"+sharedpreferences.getString("UserId", "");
        HttpPost httpPost=new HttpPost(my);
        String json="";
        StringEntity se=null;
        JSONObject jsonObject=new JSONObject();
        try
        {

            jsonObject.put("types",new JSONArray(notificationType));


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
        //dialog.dismiss();
        stringBuilder.append("@@@NotificationList");
        callback.onTaskCompleted(stringBuilder.toString());
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
