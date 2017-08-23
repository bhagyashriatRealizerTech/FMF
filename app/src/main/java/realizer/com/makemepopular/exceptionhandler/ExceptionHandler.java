package realizer.com.makemepopular.exceptionhandler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import realizer.com.makemepopular.DashboardActivity;
import realizer.com.makemepopular.LoginActivity;
import realizer.com.makemepopular.RegistrationActivity;
import realizer.com.makemepopular.mailsender.MailSender;
import realizer.com.makemepopular.utils.Config;

public class ExceptionHandler implements
		Thread.UncaughtExceptionHandler {
	private final Context myContext;
	private final String LINE_SEPARATOR = "\n";
    private static String fromWhere="";

	public ExceptionHandler(Context context,String fromwhere) {
		myContext = context;
        fromWhere=fromwhere;
	}

	public void uncaughtException(Thread thread, Throwable exception) {
		StringWriter stackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTrace));
		StringBuilder errorReport = new StringBuilder();
		errorReport.append("************ CAUSE OF ERROR ************\n\n");
		errorReport.append(stackTrace.toString());

		errorReport.append("\n************ DEVICE INFORMATION ***********\n");
		errorReport.append("Brand: ");
		errorReport.append(Build.BRAND);
		errorReport.append(LINE_SEPARATOR);
		errorReport.append("Device: ");
		errorReport.append(Build.DEVICE);
		errorReport.append(LINE_SEPARATOR);
		errorReport.append("Model: ");
		errorReport.append(Build.MODEL);
		errorReport.append(LINE_SEPARATOR);
		errorReport.append("Id: ");
		errorReport.append(Build.ID);
		errorReport.append(LINE_SEPARATOR);
		errorReport.append("Product: ");
		errorReport.append(Build.PRODUCT);
		errorReport.append(LINE_SEPARATOR);
		errorReport.append("\n************ FIRMWARE ************\n");
		errorReport.append("SDK: ");
		errorReport.append(Build.VERSION.SDK);
		errorReport.append(LINE_SEPARATOR);
		errorReport.append("Release: ");
		errorReport.append(Build.VERSION.RELEASE);
		errorReport.append(LINE_SEPARATOR);
		errorReport.append("Incremental: ");
		errorReport.append(Build.VERSION.INCREMENTAL);
		errorReport.append(LINE_SEPARATOR);

        //DatabaseQueries qr = new DatabaseQueries(myContext);
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(myContext);
        ExceptionModel obj = new ExceptionModel();
        obj.setUserId(sharedpreferences.getString("userLoginId", ""));
        obj.setExceptionDetails(stackTrace.toString());
        obj.setDeviceModel(Build.MODEL);
        obj.setAndroidVersion(Build.VERSION.SDK);
        obj.setApplicationSource("FMF Android");
        obj.setDeviceBrand(Build.BRAND);


        if(Config.isConnectingToInternet(myContext))
            sendEmail(obj);
       // Config.alertDialog(myContext,"Sorry","The application Find Me Friends has stopped unexpectedly. Please try again.");
        if (fromWhere.equalsIgnoreCase("Register"))
        {
            Intent i = new Intent(myContext,RegistrationActivity.class);
            myContext.startActivity(i);
        }
        else if (fromWhere.equalsIgnoreCase("Login"))
        {
            Intent i = new Intent(myContext,LoginActivity.class);
            myContext.startActivity(i);
        }
        else
        {
            Intent i = new Intent(myContext,DashboardActivity.class);
            myContext.startActivity(i);
        }

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
                    String TO = "Farhan.bodale@realizertech.com,satish.sawant@realizertech.com,sachin.shinde@realizertech.com,ramchadra.magar@realizertech.com";
                    MailSender sender = new MailSender("realizertech1@gmail.com","realizer@15");

                    // sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");

                    sender.sendMail("Critical App Error: Find Me Friends Android App",messageContent,"realizertech1@gmail.com",TO);

                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(10);
                } catch (Exception e) {

                    Log.d("Exception Mail",e.toString());
                    // Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();

                }

            }

        }).start();
    }
}