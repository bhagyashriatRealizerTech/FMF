package realizer.com.makemepopular.chat.asynctask;

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

import realizer.com.makemepopular.exceptionhandler.NetworkException;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;

/**
 * Created by Win on 08/12/2016.
 */
public class SendMessgeAsyncTaskPost extends AsyncTask<Void,Void,StringBuilder> {


    StringBuilder resultLogin;
    Context myContext;
    String ThreadId,SenderId,timeStamp,Message,ReceiverId,messageId;
    private OnTaskCompleted callback;

    public SendMessgeAsyncTaskPost(Context myContext, String threadId, String senderId, String timeStamp, String message, String receiverId,String messageId, OnTaskCompleted callback) {
        this.myContext = myContext;
        ThreadId = threadId;
        SenderId = senderId;
        this.timeStamp = timeStamp;
        Message = message;
        ReceiverId = receiverId;
        this.messageId=messageId;
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
        resultLogin = new StringBuilder();
        HttpClient httpclient = new DefaultHttpClient();
        String url = Config.URL_Chat+"sendMessage";
        HttpPost httpPost = new HttpPost(url);

        System.out.println(url);



        String json = "";
       /* Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String date = df.format(calendar.getTime());*/
        StringEntity se = null;
        JSONObject jsonobj = new JSONObject();
        try {
            jsonobj.put("messageId",messageId);
            jsonobj.put("senderId",SenderId);
            String date[] = timeStamp.split(" ");
            String date1[] = date[0].split("/");
            String resdate = date1[1]+"/"+date1[0]+"/"+date1[2]+" "+date[1];
            //jsonobj.put("timeStamp",resdate);
            jsonobj.put("message",Message);
            jsonobj.put("threadId",ThreadId);
            //jsonobj.put("receiverId",ReceiverId);

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
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return resultLogin;
    }

    @Override
    protected void onPostExecute(StringBuilder stringBuilder) {
        super.onPostExecute(stringBuilder);

        stringBuilder.append("@@@SendMsg");
        callback.onTaskCompleted(stringBuilder.toString());
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
