package realizer.com.makemepopular.addfriend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import realizer.com.makemepopular.DashboardActivity;
import realizer.com.makemepopular.R;
import realizer.com.makemepopular.asynctask.GetFriendListByNameAsyncTask;
import realizer.com.makemepopular.emergency.EmergencyActivity;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.Singleton;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by shree on 2/25/2017.
 */
public class AddFriendAtivity extends AppCompatActivity implements OnTaskCompleted{

    TextView searchbyName,noIntrest,noName,searchbyNameInterest;
    ListView lvFriendLIst,lvfriendlistbyinterest;
    ProgressWheel loading;
    LinearLayout addfriendbynamelinear,addfriendbyInterestlinear;
    RadioButton addfrndbyname,addfrndbyinterest;
    EditText edtFriendName,edtFriendAddress;
    Spinner spnInterestList;
    static String getInterest;
    String[] spnlist;
    GetFriendListByNameAsyncTask getListbyname;
    static boolean isRespondingAPI=false;
    MessageResultReceiver resultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, ""));
        setContentView(R.layout.add_friend_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Friends");
        realizer.com.makemepopular.utils.Singleton obj = realizer.com.makemepopular.utils.Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);
        initializeControls();
        spnlist=Singleton.getSpnlist();

        spnInterestList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getInterest = spnInterestList.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        searchbyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(DashboardActivity.this, "Search Button by name Clicked", Toast.LENGTH_SHORT).show();
                //alertDialog.dismiss();
                String username=edtFriendName.getText().toString();
                int length = edtFriendName.getText().length();
                String city=edtFriendAddress.getText().toString();
                if (length >=4 || edtFriendAddress.getText().length() > 0)
                {
                    if (!username.equalsIgnoreCase("") || !city.equalsIgnoreCase(""))
                    {
                        if (Config.isConnectingToInternet(AddFriendAtivity.this))
                        {
                            loading.setVisibility(View.VISIBLE);
                            lvFriendLIst.setVisibility(View.GONE);
                            noName.setVisibility(View.INVISIBLE);
                            ArrayList<String> list = new ArrayList<String>();
                            isRespondingAPI=false;
                            getListbyname=new GetFriendListByNameAsyncTask(username,city,list,AddFriendAtivity.this,AddFriendAtivity.this);
                            getListbyname.execute();

                            new CountDownTimer( 15000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                }
                                public void onFinish() {
                                    loading.setVisibility(View.GONE);
                                    if (!isRespondingAPI)
                                    {
                                        if (getListbyname != null && getListbyname.getStatus() != AsyncTask.Status.FINISHED) {
                                            getListbyname.cancel(true);
                                            Config.alertDialog(AddFriendAtivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                        }
                                    }
                                }
                            }.start();

                        }
                        else
                        {
                            Utility.CustomToast(AddFriendAtivity.this, "No Internet Connection..!");
                        }
                    }
                    else
                    {
                        //Toast.makeText(getApplicationContext(),"Please enter name or address for search...",Toast.LENGTH_LONG).show();
                        Config.alertDialog(AddFriendAtivity.this, "Error", "Please enter name or address for search.");
                    }
                }
                else
                {
                    Config.alertDialog(AddFriendAtivity.this, "Suggestion", "Type Atleast Four Characters.");
                }
            }
        });

        addfrndbyname.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (addfrndbyname.isChecked() == true) {
                    addfriendbynamelinear.setVisibility(View.VISIBLE);
                    lvFriendLIst.setVisibility(View.GONE);
                    noName.setVisibility(View.INVISIBLE);
                } else {
                    addfriendbynamelinear.setVisibility(View.GONE);
                    lvfriendlistbyinterest.setVisibility(View.GONE);
                    noIntrest.setVisibility(View.INVISIBLE);
                }
            }
        });


        searchbyNameInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(DashboardActivity.this, "Search Button by interest Clicked", Toast.LENGTH_SHORT).show();

                String username="";
                String city="";
                if (username.equalsIgnoreCase("") || city.equalsIgnoreCase(""))
                {
                    if (Config.isConnectingToInternet(AddFriendAtivity.this))
                    {
                        loading.setVisibility(View.VISIBLE);
                        lvfriendlistbyinterest.setVisibility(View.GONE);
                        noIntrest.setVisibility(View.INVISIBLE);
                        ArrayList<String> list = new ArrayList<String>();
                        list.add(getInterest);
                        isRespondingAPI=false;
                        getListbyname=new GetFriendListByNameAsyncTask(username,city,list,AddFriendAtivity.this,AddFriendAtivity.this);
                        getListbyname.execute();

                        new CountDownTimer( 15000, 1000) {
                            public void onTick(long millisUntilFinished) {
                            }
                            public void onFinish() {
                                loading.setVisibility(View.GONE);
                                if (!isRespondingAPI)
                                {
                                    if (getListbyname != null && getListbyname.getStatus() != AsyncTask.Status.FINISHED) {
                                        getListbyname.cancel(true);
                                        Config.alertDialog(AddFriendAtivity.this, "Error", "Server is not responding.Please try again after sometime.");
                                    }
                                }
                            }
                        }.start();
                    }
                    else
                    {
                        Utility.CustomToast(AddFriendAtivity.this, "No Internet Connection..!");
                    }
                }
                else
                {
                    //Toast.makeText(getApplicationContext(),"Please enter name or address for search...",Toast.LENGTH_LONG).show();
                }
            }
        });


          if(Config.isConnectingToInternet(AddFriendAtivity.this)) {

              if (spnlist == null) {
                  Config.alertDialog(AddFriendAtivity.this, "Add Friend", "No Interest Added.");
              } else {
                  ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddFriendAtivity.this,
                          R.layout.interest_spinner_custom, spnlist);
                  adapter.setDropDownViewResource(R.layout.interest_spinner_custom);
                  spnInterestList.setAdapter(adapter);
                  spnInterestList.setBackground(getResources().getDrawable(R.drawable.square_line));
              }
          }
        else {
              Config.alertDialog(AddFriendAtivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
          }


        addfrndbyinterest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Config.hideSoftKeyboardWithoutReq(AddFriendAtivity.this,edtFriendName);
                if (addfrndbyinterest.isChecked() == true) {

                    if (Config.isConnectingToInternet(AddFriendAtivity.this)) {
                        if(spnlist != null && spnlist.length >0)
                        {
                            addfriendbyInterestlinear.setVisibility(View.VISIBLE);
                            addfriendbynamelinear.setVisibility(View.GONE);
                            lvfriendlistbyinterest.setVisibility(View.GONE);
                            noIntrest.setVisibility(View.INVISIBLE);
                        }
                        else
                            Config.alertDialog(AddFriendAtivity.this, "Error", "No Interest Found");
                    } else {
                        Utility.CustomToast(AddFriendAtivity.this, "No Internet Connection..!");
                    }
                } else {
                    addfriendbyInterestlinear.setVisibility(View.GONE);
                    addfriendbynamelinear.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    public void initializeControls()
    {
        addfriendbynamelinear= (LinearLayout) findViewById(R.id.linearlayout_addfriendbyname);
        addfriendbyInterestlinear= (LinearLayout) findViewById(R.id.linearlayout_addfriendbyInterest);
        addfrndbyname= (RadioButton) findViewById(R.id.rdo_addfriend_byname);
        addfrndbyinterest= (RadioButton) findViewById(R.id.rdo_addfriend_byinterest);
        edtFriendName= (EditText) findViewById(R.id.edt_addfrnd_frndname);
        edtFriendAddress= (EditText) findViewById(R.id.edt_addfrnd_frndaddress);
        searchbyName= (TextView) findViewById(R.id.txt_addfrnd_searchfrndbyname);
        searchbyName.setTypeface(FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME));
        noIntrest= (TextView) findViewById(R.id.notxtInterstet);
        noName= (TextView) findViewById(R.id.notxtName);
        lvFriendLIst= (ListView) findViewById(R.id.list_addfrndbynamelist);
        loading =(ProgressWheel) findViewById(R.id.loading);
        searchbyNameInterest= (TextView) findViewById(R.id.txt_addfrnd_searchfrndbyinterest);
        searchbyNameInterest.setTypeface(FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME));
        spnInterestList= (Spinner) findViewById(R.id.spn_addfrnd_interestlist);
        lvfriendlistbyinterest= (ListView)findViewById(R.id.list_addfrndbyinterestlist);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent reg=new Intent(AddFriendAtivity.this,DashboardActivity.class);
        startActivity(reg);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Intent reg=new Intent(AddFriendAtivity.this,DashboardActivity.class);
                startActivity(reg);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskCompleted(String s) {
        isRespondingAPI=true;
        String arr[]=s.split("@@@");
        if (arr[1].equalsIgnoreCase("searchbyName"))
        {
            if (!arr[0].equalsIgnoreCase("[]"))
            {
                ArrayList<AddFriendModel> searchnameFrndlist=new ArrayList<>();
                JSONArray jsonarray = null;
                try {
                    jsonarray = new JSONArray(arr[0]);

                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        AddFriendModel afm=new AddFriendModel();
                        String fname = jsonobject.getString("fName");
                        String lname = jsonobject.getString("lName");
                        String userId = jsonobject.getString("userId");
                        afm.setFriendName(fname+" "+lname);
                        afm.setFriendid(userId);
                        afm.setThumbnailUrl(jsonobject.getString("thumbnailUrl"));
                        afm.setStatus(jsonobject.getString("requestStatus"));
                        afm.setRequestSent(jsonobject.getBoolean("isRequestSent"));
                        afm.setGender(jsonobject.getString("gender"));
                        afm.setAge(jsonobject.getInt("age"));
                        searchnameFrndlist.add(afm);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Collections.sort(searchnameFrndlist, new ChatNoCaseComparator());
                if (arr[2].equalsIgnoreCase("Name"))
                {
                    lvFriendLIst.setAdapter(new AddFriendModelAdapter(this, searchnameFrndlist));
                    lvFriendLIst.setVisibility(View.VISIBLE);
                    noName.setVisibility(View.GONE);
                }
                else
                {
                    lvfriendlistbyinterest.setAdapter(new AddFriendModelAdapter(this, searchnameFrndlist));
                    lvfriendlistbyinterest.setVisibility(View.VISIBLE);
                    noIntrest.setVisibility(View.GONE);
                }
                loading.setVisibility(View.GONE);
            }
            else {
                //Toast.makeText(DashboardActivity.this, "No Search Found", Toast.LENGTH_SHORT).show();
                Config.alertDialog(AddFriendAtivity.this, "Add Friend", "No People Found Matching Your Specified Search Criteria.");
                loading.setVisibility(View.GONE);
                if (arr[2].equalsIgnoreCase("Name"))
                {
                    lvFriendLIst.setVisibility(View.GONE);
                    noName.setVisibility(View.VISIBLE);
                }
                else
                {
                    lvfriendlistbyinterest.setVisibility(View.GONE);
                    noIntrest.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public class ChatNoCaseComparator implements Comparator<AddFriendModel> {
        public int compare(AddFriendModel s1, AddFriendModel s2) {
            return s1.getFriendName().compareToIgnoreCase(s2.getFriendName());
        }
    }


    class UpdateUI implements Runnable {
        String update;

        public UpdateUI(String update) {

            this.update = update;
        }

        public void run() {

            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(AddFriendAtivity.this);
            if(update.equals("Emergency")) {

                String notType=sharedpreferences.getString("Type", "");
                if (notType.equalsIgnoreCase("FriendRequest"))
                {
                    String reqstName=sharedpreferences.getString("RequsetByName", "");
                    String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
                    Config.showacceptrejectFriendRequest(reqstName, thumbnail, AddFriendAtivity.this);
                    //Config.showacceptrejectFriendRequest(reqstName,EmergencyActivity.this);
                }
                else if (notType.equalsIgnoreCase("Emergency"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String trobler=sharedpreferences.getString("TroublerName", "");
                    String troblerid=sharedpreferences.getString("TroublerUserId", "");
                    Config.showEmergencyAcceptReject(msg,trobler,troblerid,AddFriendAtivity.this);
                }
                else if (notType.equalsIgnoreCase("EmergencyRecipt"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("HelperUserName", "");
                    String isResch=sharedpreferences.getString("isReaching", "");
                    if (isResch.equalsIgnoreCase("true")){}
                    //showEmergencyAckAlert(newMsg, helpername);
                    Config.showEmergencyAckAlert(msg,helpername,AddFriendAtivity.this);
                }
                else if (notType.equalsIgnoreCase("FriendRequestAccepted"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("AcceptByName", "");

                    Config.showAccptedRequestAlert(msg,helpername,AddFriendAtivity.this);
                }

            }

            else if(update.equals("RefreshThreadList")) {

            }
        }
    }

    class MessageResultReceiver extends ResultReceiver
    {
        public MessageResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if(resultCode == 300){
                AddFriendAtivity.this.runOnUiThread(new UpdateUI("Emergency"));
            }
            if(resultCode == 200){
                AddFriendAtivity.this.runOnUiThread(new UpdateUI("RefreshThreadList"));
            }

        }
    }

}
