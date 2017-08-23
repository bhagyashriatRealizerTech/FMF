package realizer.com.makemepopular.chat.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import realizer.com.makemepopular.chat.model.ChatMessageSendModel;
import realizer.com.makemepopular.exceptionhandler.NetworkException;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;

/**
 * Created by Win on 07/12/2016.
 */
public class ThreadListAsyncTaskGet extends AsyncTask<Void,Void,StringBuilder> {
    ProgressDialog dialog;
    StringBuilder resultLogin;
    ChatMessageSendModel obj ;
    String UserId="";
    Context myContext;
    String Time;
    private OnTaskCompleted callback;

    public ThreadListAsyncTaskGet(Context myContext, OnTaskCompleted callback, String Userid, String time) {
        this.myContext = myContext;
        this.callback = callback;
        this.UserId=Userid;
        this.Time=time;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        if (isCancelled())
        {
            return (null); // don't forget to terminate this method
        }
        resultLogin = new StringBuilder();

        String my= Config.URL_Chat+"GetThreadList";
        Log.d("URL", my);
        HttpGet httpGet = new HttpGet(my);
        HttpClient client = new DefaultHttpClient();
        try
        {
            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(myContext);
            httpGet.setHeader("userId",UserId);
            httpGet.setHeader("Time",Time);
            httpGet.setHeader("fcmToken",sharedpreferences.getString("TockenID",""));
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();


            int statusCode = statusLine.getStatusCode();
            if(statusCode == 200)
            {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while((line=reader.readLine()) != null)
                {
                    resultLogin.append(line);
                }
            }
            else
            {
                StringBuilder exceptionString = new StringBuilder();
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while((line=reader.readLine()) != null)
                {
                    exceptionString.append(line);
                }

                NetworkException.insertNetworkException(myContext, exceptionString.toString());
            }
        }
        catch(ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            client.getConnectionManager().closeExpiredConnections();
            client.getConnectionManager().shutdown();
        }
        return resultLogin;
    }

    @Override
    protected void onPostExecute(StringBuilder stringBuilder) {
        super.onPostExecute(stringBuilder);
        stringBuilder.append("@@@ThreadList");
        callback.onTaskCompleted(stringBuilder.toString());
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
