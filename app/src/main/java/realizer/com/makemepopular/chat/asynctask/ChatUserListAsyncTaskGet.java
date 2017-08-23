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
 * Created by Win on 06/12/2016.
 */
public class ChatUserListAsyncTaskGet extends AsyncTask<Void,Void,StringBuilder> {

    StringBuilder resultLogin;
    Context myContext;
    private OnTaskCompleted callback;
    SharedPreferences sharedpreferences;
    ProgressDialog dialog;
    String lastupdatedtime="";

    public ChatUserListAsyncTaskGet(Context myContext, OnTaskCompleted callback,String lastupdatedtime) {
        this.myContext = myContext;
        this.callback = callback;
        this.lastupdatedtime=lastupdatedtime;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog= ProgressDialog.show(myContext, "", "Loading Contacts...!");
    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        if (isCancelled())
        {
            return (null); // don't forget to terminate this method
        }
        resultLogin = new StringBuilder();

        String my= Config.URL_Mmp+"getFriendList";
        Log.d("URL", my);
        HttpPost httpPost = new HttpPost(my);
        HttpClient client = new DefaultHttpClient();
        String json="";
        StringEntity se=null;

        JSONObject jsonObject = new JSONObject();
        try
        {

            sharedpreferences = PreferenceManager.getDefaultSharedPreferences(myContext);

            jsonObject.put("UserId",sharedpreferences.getString("UserId",""));
            jsonObject.put("searchText","");
            jsonObject.put("lastUpdatedDate",lastupdatedtime);

            json=jsonObject.toString();
            se=new StringEntity(json);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            httpPost.setEntity(se);
            HttpResponse response = client.execute(httpPost);
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
                resultLogin.append("Server not Responding");
            }
        }
        catch(ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally
        {
            client.getConnectionManager().closeExpiredConnections();
            client.getConnectionManager().shutdown();
        }
        return resultLogin;
    }

    @Override
    protected void onPostExecute(StringBuilder stringBuilder) {
        super.onPostExecute(stringBuilder);
        stringBuilder.append("@@@UserList");
        dialog.dismiss();
        callback.onTaskCompleted(stringBuilder.toString());
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
