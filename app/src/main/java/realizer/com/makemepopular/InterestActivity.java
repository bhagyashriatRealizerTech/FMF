package realizer.com.makemepopular;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import realizer.com.makemepopular.asynctask.SetInterestAsyntask;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.interest.InterestAdapter;
import realizer.com.makemepopular.interest.InterestModel;
import realizer.com.makemepopular.introscreen.WelcomeActivity;
import realizer.com.makemepopular.service.AutoSyncService;
import realizer.com.makemepopular.utils.*;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 11/01/2017.
 */
public class InterestActivity extends AppCompatActivity implements OnTaskCompleted
{
    ArrayList<InterestModel> interestmodel=new ArrayList<>();
    String selectlist;
    TextView ico_trecking,ico_dating,ico_sports,ico_music,ico_dancing,ico_bikers,ico_gossip,ico_socialwork,ico_technical;
    ProgressWheel loading;
    SharedPreferences sharedpreferences;
    ArrayList<String> alreadySelectedlist;
    SetInterestAsyntask interest;
    static boolean isRespondingAPI=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,""));
        setContentView(R.layout.interest_dialog);
        setContentView(R.layout.interest_dialog);



        String[] Interests = getResources().getStringArray(R.array.interest_text);
        String[] Interestico=getResources().getStringArray(R.array.icofont);

        loading =(ProgressWheel) findViewById(R.id.loading);
        final GridView interestgridview= (GridView) findViewById(R.id.interest_gridview);
        Button btn_addinterest= (Button) findViewById(R.id.btn_addinterest);
        Button btn_skipinterest= (Button) findViewById(R.id.btn_skipinterest);

        Bundle bundle=getIntent().getExtras();
        String fromWhere=bundle.getString("FromWhere");
        if (fromWhere.equalsIgnoreCase("Register"))
        {
            getSupportActionBar().setTitle("Set Interests");
            btn_addinterest.setText("Add");
            btn_skipinterest.setVisibility(View.VISIBLE);
            for(int i=0;i<Interests.length;i++){

                InterestModel im=new InterestModel();
                im.setInteresticoText(Interestico[i]);
                im.setInterestText(Interests[i]);
                interestmodel.add(im);
            }
            interestgridview.setAdapter(new InterestAdapter(InterestActivity.this, interestmodel));
        }
        else
        {
            getSupportActionBar().setTitle("Update Interests");
            btn_addinterest.setText("Update");
            alreadySelectedlist=new ArrayList<>();
            alreadySelectedlist = realizer.com.makemepopular.utils.Singleton.getAlreadyselectedInterestList();
            btn_skipinterest.setVisibility(View.GONE);
            if (alreadySelectedlist != null)
            {
                for(int i=0;i<Interests.length;i++){

                    InterestModel im=new InterestModel();
                    im.setInteresticoText(Interestico[i]);
                    im.setInterestText(Interests[i]);
                    interestmodel.add(im);
                }
                for(int i=0;i<alreadySelectedlist.size();i++){
                    for(int j=0;j<interestmodel.size();j++){
                        if (interestmodel.get(j).getInterestText().equalsIgnoreCase(alreadySelectedlist.get(i)))
                        {
                            InterestModel im=new InterestModel();
                            im.setInteresticoText(interestmodel.get(j).getInteresticoText());
                            im.setInterestText(interestmodel.get(j).getInterestText());
                            im.setIs_selected(true);
                            interestmodel.set(j,im);
                        }
                    }
                }
                interestgridview.setAdapter(new InterestAdapter(InterestActivity.this, interestmodel));
            }
            else
            {
                for(int i=0;i<Interests.length;i++){

                    InterestModel im=new InterestModel();
                    im.setInteresticoText(Interestico[i]);
                    im.setInterestText(Interests[i]);
                    interestmodel.add(im);
                }
                interestgridview.setAdapter(new InterestAdapter(InterestActivity.this, interestmodel));
            }
        }

        interestgridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (interestmodel.get(position).is_selected()) {
                    interestmodel.get(position).setIs_selected(false);
                    interestgridview.setAdapter(new InterestAdapter(InterestActivity.this, interestmodel));
                } else {
                    interestmodel.get(position).setIs_selected(true);
                    interestgridview.setAdapter(new InterestAdapter(InterestActivity.this, interestmodel));
                }
            }
        });

        btn_addinterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<String> list = new ArrayList<String>();
                for (int i=0;i<interestmodel.size();i++)
                {
                    if (interestmodel.get(i).is_selected())
                    {
                        list.add(interestmodel.get(i).getInterestText().toString());
                    }
                }

                if (list.size()==0)
                {
                    Config.alertDialog(InterestActivity.this,"Alert","Please Select Minimum One Interest");
                }
                else
                {
                    if (Config.isConnectingToInternet(InterestActivity.this))
                    {
                        loading.setVisibility(View.VISIBLE);
                        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(InterestActivity.this);

                        interest=new SetInterestAsyntask(list,InterestActivity.this,InterestActivity.this);
                        interest.execute();
                        new CountDownTimer( 15000, 1000) {
                            public void onTick(long millisUntilFinished) {
                            }

                            public void onFinish() {
                                loading.setVisibility(View.GONE);
                                if (!isRespondingAPI)
                                {
                                    if (interest != null && interest.getStatus() != AsyncTask.Status.FINISHED) {
                                        interest.cancel(true);
                                        Config.alertDialog(InterestActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                    }
                                }
                            }

                        }.start();
                    }
                    else
                    {
                        Config.alertDialog(InterestActivity.this,"Internet Connection","Make sure your device is connected to the internet.");
                    }
                }
            }
        });

        btn_skipinterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(InterestActivity.this);
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("Login", "true");
                edit.commit();

                Intent interest=new Intent(InterestActivity.this,WelcomeActivity.class);
                startActivity(interest);

                Intent ser = new Intent(InterestActivity.this, AutoSyncService.class);
                ser.putExtra("FirstTime", "");
                startService(ser);

                finish();
            }
        });

    }

    @Override
    public void onTaskCompleted(String s) {
        if (s.equalsIgnoreCase("true"))
        {
            //Toast.makeText(InterestActivity.this, "You Have Successfully Added Interest", Toast.LENGTH_SHORT).show();
            //Config.alertDialog(this, "Error","Provider is null");
            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(InterestActivity.this);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("Login", "true");
            edit.commit();
            Bundle b=getIntent().getExtras();
            if (b.getString("FromWhere")!=null)
            {
                if (b.getString("FromWhere").equals("Dashboard"))
                {
                    Intent interest=new Intent(InterestActivity.this,DashboardActivity.class);
                    startActivity(interest);
                }
                else
                {
                    Intent interest=new Intent(InterestActivity.this,WelcomeActivity.class);
                    startActivity(interest);

                    Intent ser = new Intent(InterestActivity.this, AutoSyncService.class);
                    ser.putExtra("FirstTime", "");
                    startService(ser);

                }
            }
            else
            {
                Intent interest=new Intent(InterestActivity.this,WelcomeActivity.class);
                startActivity(interest);

                Intent ser = new Intent(InterestActivity.this, AutoSyncService.class);
                ser.putExtra("FirstTime", "");
                startService(ser);
            }
            finish();
        }
        else
        {
            //Toast.makeText(InterestActivity.this, "Error For Adding Interest", Toast.LENGTH_SHORT).show();
            Config.alertDialog(InterestActivity.this,"Error","Server not responding.Please try again after sometime");
        }
        loading.setVisibility(View.GONE);
    }
}
