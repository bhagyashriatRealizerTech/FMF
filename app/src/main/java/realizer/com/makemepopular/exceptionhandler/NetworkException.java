package realizer.com.makemepopular.exceptionhandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import realizer.com.makemepopular.mailsender.MailSender;
import realizer.com.makemepopular.utils.Config;

/**
 * Created by Bhagyashri on 11/17/2016.
 */
public class NetworkException {


    public static void insertNetworkException(Context myContext,String stackTrace) {
    //DatabaseQueries qr = new DatabaseQueries(myContext);
    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(myContext);
    ExceptionModel obj = new ExceptionModel();
    obj.setUserId(sharedpreferences.getString("userLoginId",""));
    obj.setExceptionDetails(stackTrace.toString());
    obj.setDeviceModel(Build.MODEL);
    obj.setAndroidVersion(Build.VERSION.SDK);
    obj.setApplicationSource("FMF Android");
    obj.setDeviceBrand(Build.BRAND);

    SimpleDateFormat df1 = new SimpleDateFormat("dd MMM hh:mm:ss a");
    String date = df1.format(Calendar.getInstance().getTime());

    if(Config.isConnectingToInternet(myContext))
        sendEmail(obj);

  }


    public static void sendEmail(final ExceptionModel obj)
    {
        new Thread(new Runnable() {

            public void run() {

                try {

                    String messageContent = "Application Source: "+obj.getApplicationSource()
                            +"\nDevice Model: "+obj.getDeviceModel()+"\nAndroid Version: "+obj.getAndroidVersion()
                            +"\nDevice Brand: "+obj.getDeviceBrand()+"\nUserID: "+obj.getUserId()
                            +"\nException: "+obj.getExceptionDetails();

                    String TO = "bhagyashri.salgare@realizertech.com,satish.sawant@realizertech.com,sachin.shinde@realizertech.com,ramchadra.magar@realizertech.com";
                    MailSender sender = new MailSender("realizertech1@gmail.com","realizer@17");

                   // sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");

                    sender.sendMail("Critical Network Error: Find Me Friends Android App",messageContent,"realizertech1@gmail.com",TO);

                } catch (Exception e) {

                    Log.d("Exception Mail",e.toString());
                   // Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();

                }

                               }

        }).start();
    }
}
