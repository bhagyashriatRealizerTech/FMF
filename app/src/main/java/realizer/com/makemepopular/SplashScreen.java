package realizer.com.makemepopular;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import realizer.com.makemepopular.introscreen.WelcomeActivity;
import realizer.com.makemepopular.service.AutoSyncService;

public class SplashScreen extends AppCompatActivity {

    int sleeptime=2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);

        android.support.v7.app.ActionBar ab=getSupportActionBar();
        ab.hide();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StartActivity();

    }

    public void StartActivity()
    {
        final Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(sleeptime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);
                    String LogCheck=sharedpreferences.getString("Login", "");
                    if (LogCheck.equals("true"))
                    {
                        Intent intent = new Intent(SplashScreen.this, DashboardActivity.class);
                        startActivity(intent);

                        Intent ser = new Intent(SplashScreen.this, AutoSyncService.class);
                        ser.putExtra("FirstTime", "");
                        startService(ser);
                    }
                    else {
                        Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                        startActivity(intent);
                    }
                    finish();
                }
            }
        };
        timerThread.start();
    }
}
