package realizer.com.makemepopular.asynctask;

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
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import realizer.com.makemepopular.exceptionhandler.NetworkException;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;

/**
 * Created by shree on 3/2/2017.
 */
public class GetDashboardCountAsyncTask extends AsyncTask<Void,Void,StringBuilder> {
    StringBuilder resultbuilder;
    Context mycontext;
    private OnTaskCompleted callback;
    SharedPreferences sharedpreferences;

    public GetDashboardCountAsyncTask(Context mycontext, OnTaskCompleted callback) {
        this.mycontext = mycontext;
        this.callback = callback;
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
        resultbuilder = new StringBuilder();
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mycontext);
        String my= Config.URL_Mmp+"getDashboardBadgeCounts/"+sharedpreferences.getString("UserId", "");
        Log.d("URL", my);
        HttpGet httpGet = new HttpGet(my);
        HttpClient client = new DefaultHttpClient();
        try
        {
           // SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mycontext);
            //httpGet.setHeader("fcmToken",sharedpreferences.getString("TockenID",""));

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
                    resultbuilder.append(line);
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

                NetworkException.insertNetworkException(mycontext, exceptionString.toString());
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
        return resultbuilder;
    }

    @Override
    protected void onPostExecute(StringBuilder stringBuilder) {
        super.onPostExecute(stringBuilder);
        stringBuilder.append("@@@GetDashBoardCount");
        callback.onTaskCompleted(stringBuilder.toString());
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
