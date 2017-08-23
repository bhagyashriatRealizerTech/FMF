package realizer.com.makemepopular.asynctask;

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
 * Created by shree on 1/24/2017.
 */
public class AcceptRejectFriendRequestAsyntask extends AsyncTask<Void,Void,StringBuilder> {

    StringBuilder resultbuilder;
    Context mycontext;
    private OnTaskCompleted callback;
    SharedPreferences sharedpreferences;
    String friendID;
    boolean isEmergency=false;
    boolean isAccepted=false;
    String fromWhere;
    String notificationId="";

    public AcceptRejectFriendRequestAsyntask(String notID,String fromwhr,String frndid,boolean iseme,boolean isaccept,Context mycontext, OnTaskCompleted cb) {
        this.mycontext=mycontext;
        this.callback=cb;
        this.friendID=frndid;
        this.isEmergency=iseme;
        this.isAccepted=isaccept;
        this.fromWhere=fromwhr;
        this.notificationId=notID;
    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
        // dialog=ProgressDialog.show(mycontext,"","Inserting Data...!");
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        if (isCancelled())
        {
            return (null); // don't forget to terminate this method
        }
        resultbuilder =new StringBuilder();
        HttpClient httpClient=new DefaultHttpClient();
        String url= Config.URL_Mmp+"AcceptRejectFriendRequest";
        HttpPost httpPost=new HttpPost(url);
        String json="";
        StringEntity se=null;
        JSONObject jsonObject=new JSONObject();
        try
        {
            sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mycontext);

            jsonObject.put("userId",sharedpreferences.getString("UserId",""));
            jsonObject.put("friendId",friendID);
            jsonObject.put("isEmergencyContact",isEmergency);
            jsonObject.put("isAccept",isAccepted);
            jsonObject.put("notificationId",notificationId);

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
                resultbuilder.append("ServerNotResponding");
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
        stringBuilder.append("@@@AcceptReject@@@"+fromWhere);
        callback.onTaskCompleted(stringBuilder.toString());
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
