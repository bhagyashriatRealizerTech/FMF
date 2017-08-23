package realizer.com.makemepopular.friendnear;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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

import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;

/**
 * Created by Win on 17/01/2017.
 */
public class FriendNearAsyncTaskPost extends AsyncTask<Void,Void,StringBuilder> {

    ProgressDialog dialog;
    StringBuilder result;
    Context myContext;
    Double longitude,latitude;
    private OnTaskCompleted callback;

    public FriendNearAsyncTaskPost(Context myContext, Double longitude, Double latitude, OnTaskCompleted callback) {
        this.myContext = myContext;
        this.longitude=longitude;
        this.latitude=latitude;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        result = new StringBuilder();
        HttpClient httpclient = new DefaultHttpClient();
        String url = Config.URL+"getNearPath";
        HttpPost httpPost = new HttpPost(url);

        System.out.println(url);

        String json = "";
       /* Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String date = df.format(calendar.getTime());*/
        StringEntity se = null;
        JSONObject jsonobj = new JSONObject();
        try {
            jsonobj.put("Latitude",latitude);
            jsonobj.put("Longitude",longitude);

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
                    result.append(line);
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
        return result;
    }

    @Override
    protected void onPostExecute(StringBuilder stringBuilder) {
        super.onPostExecute(stringBuilder);
        stringBuilder.append("@@@FriendList");
        callback.onTaskCompleted(stringBuilder.toString());
    }
}
