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
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;

/**
 * Created by Win on 08/12/2016.
 */
public class ReceiveMessageAsyncTaskPut extends AsyncTask<Void,Void,StringBuilder> {

    ProgressDialog dialog;
    StringBuilder resultLogin;
    Context myContext;
    private OnTaskCompleted callback;
    SharedPreferences sharedpreferences;
    String messageId,userId;
    boolean isRead;

    public ReceiveMessageAsyncTaskPut(Context myContext, String messageId, boolean isread) {
        this.myContext = myContext;
        this.messageId=messageId;
        this.isRead=isread;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
       // dialog=ProgressDialog.show(myContext,"","Loading...!");
    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        if (isCancelled())
        {
            return (null); // don't forget to terminate this method
        }
        resultLogin = new StringBuilder();
        HttpClient httpclient = new DefaultHttpClient();
        String url = Config.URL_Chat+"ReceiveMessage";
        HttpPut httpPost = new HttpPut(url);
        System.out.println(url);

        String json = "";
       /* Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String date = df.format(calendar.getTime());*/
        StringEntity se = null;
        JSONObject jsonobj = new JSONObject();
        try {
            sharedpreferences = PreferenceManager.getDefaultSharedPreferences(myContext);
            jsonobj.put("messageId",messageId);
            jsonobj.put("userId",sharedpreferences.getString("UserId",""));
            jsonobj.put("isRead",isRead);

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
            }
            else
            {
                // Log.e("Error", "Failed to Login");
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
        //dialog.dismiss();
        //stringBuilder.append("@@@MessageList");
        //callback.onTaskCompleted(stringBuilder.toString());
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
