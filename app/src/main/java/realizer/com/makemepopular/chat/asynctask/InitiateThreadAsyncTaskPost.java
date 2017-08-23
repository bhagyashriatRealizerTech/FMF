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

import realizer.com.makemepopular.chat.model.AddedContactModel;
import realizer.com.makemepopular.chat.model.ChatMessageSendModel;
import realizer.com.makemepopular.exceptionhandler.NetworkException;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;


/**
 * Created by Win on 07/12/2016.
 */
public class InitiateThreadAsyncTaskPost extends AsyncTask<Void,Void,StringBuilder> {

    ProgressDialog dialog;
    StringBuilder resultLogin;
    //ChatMessageSendModel obj1 ;
    Context myContext;
    ArrayList<AddedContactModel> obj;
    private OnTaskCompleted callback;
    String sDate;
    String mesage;
    SharedPreferences sharedpreferences;

    public InitiateThreadAsyncTaskPost(String msg,String sdate,ArrayList<AddedContactModel> obj, Context myContext, OnTaskCompleted callback) {
        this.obj = obj;
        this.myContext = myContext;
        this.callback = callback;
        this.sDate=sdate;
        this.mesage=msg;
    }

    @Override
    protected void onPreExecute() {
        // super.onPreExecute();

        //dialog=ProgressDialog.show(myContext,"","Authenticating credentials...");

    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        if (isCancelled())
        {
            return (null); // don't forget to terminate this method
        }
        resultLogin = new StringBuilder();
        HttpClient httpclient = new DefaultHttpClient();
        String url = Config.URL_Chat+"initiateThread";
        HttpPost httpPost = new HttpPost(url);

        System.out.println(url);



        String json = "";
       /* Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String date = df.format(calendar.getTime());*/
        StringEntity se = null;
        JSONObject jsonobj = new JSONObject();
        try {
            sharedpreferences = PreferenceManager.getDefaultSharedPreferences(myContext);
            jsonobj.put("lastSenderById",sharedpreferences.getString("UserId",""));
            jsonobj.put("lastMessageText",mesage);

            JSONArray userArray=new JSONArray();
            for (int i=0;i<obj.size();i++)
            {
                userArray.put(obj.get(i).getUserId());
            }
            jsonobj.put("participantList",userArray);

            //jsonobj.put("participantList", new JSONArray(list));
            json = jsonobj.toString();
            Log.d("RES", json);
            se = new StringEntity(json);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            httpPost.setEntity(se);
            HttpResponse httpResponse = httpclient.execute(httpPost);
            StatusLine statusLine = httpResponse.getStatusLine();

            int statusCode = statusLine.getStatusCode();
            Log.d("StatusCode", "" + statusCode);
            if(statusCode == 200)
            {
                HttpEntity entity = httpResponse.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while((line=reader.readLine()) != null)
                {
                    resultLogin.append(line);
                }
                resultLogin.append("@@@InitThread");
            }
            else if (statusCode==500)
            {
                HttpEntity entity = httpResponse.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while((line=reader.readLine()) != null)
                {
                    resultLogin.append(line);
                }
                NetworkException.insertNetworkException(myContext, resultLogin.toString());
                resultLogin.append("@@@QueryError");
            }
            else if (statusCode==410)
            {
                HttpEntity entity = httpResponse.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while((line=reader.readLine()) != null)
                {
                    resultLogin.append(line);
                }
                resultLogin.append("@@@NoFriend");
            }
            else if (statusCode==302)
            {
                HttpEntity entity = httpResponse.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while((line=reader.readLine()) != null)
                {
                    resultLogin.append(line);
                }
                resultLogin.append("@@@Blocked");
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

                NetworkException.insertNetworkException(myContext, exceptionString.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return resultLogin;
    }

    @Override
    protected void onPostExecute(StringBuilder stringBuilder) {
        super.onPostExecute(stringBuilder);
        // dialog.dismiss();
        Log.d("RESULTASYNC", stringBuilder.toString());
        //Pass here result of async task
        callback.onTaskCompleted(stringBuilder.toString());
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
