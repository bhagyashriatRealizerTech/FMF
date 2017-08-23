package realizer.com.makemepopular.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import realizer.com.makemepopular.exceptionhandler.NetworkException;
import realizer.com.makemepopular.models.RegisterUserRequestModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;


/**
 * Created by Win on 07/09/2016.
 */
public class RegistrationAsyncTaskPost extends AsyncTask<Void,Void,StringBuilder> {

    StringBuilder resultbuilder;
    RegisterUserRequestModel rgm;
    ProgressDialog dialog;
    Context mycontext;
    private OnTaskCompleted callback;

    public RegistrationAsyncTaskPost(RegisterUserRequestModel rgm, Context mycontext, OnTaskCompleted cb) {
        this.rgm = rgm;
        this.mycontext=mycontext;
        this.callback=cb;
    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
       // dialog=ProgressDialog.show(mycontext,"","Inserting Data...!");
    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        if (isCancelled())
        {
            return (null); // don't forget to terminate this method
        }
        resultbuilder =new StringBuilder();
        HttpClient httpClient=new DefaultHttpClient();
        String url= Config.URL_Account+"RegisterUser";
        HttpPost httpPost=new HttpPost(url);
        String json="";
        StringEntity se=null;
        JSONObject jsonObject=new JSONObject();
        try
        {
            jsonObject.put("FName",rgm.getfName());
            jsonObject.put("LName",rgm.getlName());
            jsonObject.put("EmailId",rgm.getEmailId());
            jsonObject.put("ContactNo",rgm.getContactNo());
            jsonObject.put("AccountType",rgm.getAccountType());
            jsonObject.put("Gender", rgm.getGender());
            jsonObject.put("ThumbnailUrl",rgm.getThumbnailUrl());
            jsonObject.put("Dob",rgm.getDob());
            jsonObject.put("lastCity",rgm.getLastCity());
            jsonObject.put("DeviceId",rgm.getDeviceId());
            jsonObject.put("fcmRegId",rgm.getFcmRegId());

            json=jsonObject.toString();

            se=new StringEntity(json);
            httpPost.setEntity(se);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpContext localContext = new BasicHttpContext();
            HttpResponse httpResponse=httpClient.execute(httpPost,localContext);
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
            else  if (statuscode==302)
            {
                resultbuilder.append("Already registered user");
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
                resultbuilder.append("Server not Responding");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
            String msg=e.getCause().getMessage();
            if (msg.contains("302"))
            {
                resultbuilder.append("Already registered user");
            }
            else
            {
                resultbuilder.append("Server not Responding");
            }
        }
        return resultbuilder;
    }

    @Override
    protected void onPostExecute(StringBuilder stringBuilder) {
        super.onPostExecute(stringBuilder);
      //  dialog.dismiss();

        callback.onTaskCompleted(stringBuilder.toString());
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

}
