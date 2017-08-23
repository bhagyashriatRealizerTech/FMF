package realizer.com.makemepopular.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.Singleton;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by shree on 2/3/2017.
 */
public class NotificationActivity extends AppCompatActivity implements OnTaskCompleted{

    //private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    //ProgressWheel loading;
    ArrayList<NotificationModel> notificationList;
    ArrayList<String> selectedType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_tab_layout);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle("Payment Details");
        //getSupportActionBar().show();

        //toolbar = (Toolbar) findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notifications");
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        //setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);



       /* Button paybutton = (Button) findViewById(R.id.paybutton);

        Intent intent = getIntent();
        String orderid=intent.getStringExtra("OrderId");
        TextView orderId= (TextView) findViewById(R.id.orderId);
        orderId.setText(orderid);

        // Array of choices
        String paymentoption[] = {"Credit Card","Debit Card","Net Banking","Cash On Delivery","EMI"};
// Selection of the spinner
        Spinner spinnerpaymentoption = (Spinner) findViewById(R.id.payopt);
// Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, paymentoption);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinnerpaymentoption.setAdapter(spinnerArrayAdapter1);


        // Array of choices
        String cardtype[] = {"Amex","MasterCard","RuPay","Visa","Discover", "Diners Club","Amex ezeClick"};
// Selection of the spinner
        Spinner spinnercardtype = (Spinner) findViewById(R.id.cardtype);
// Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, cardtype);
        spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinnercardtype.setAdapter(spinnerArrayAdapter2);

        final LinearLayout cardDetails=(LinearLayout) findViewById(R.id.cardDetails);

        spinnerpaymentoption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("Credit Card") || parent.getItemAtPosition(position).toString().equalsIgnoreCase("Debit Card"))
                {
                    cardDetails.setVisibility(View.VISIBLE);
                }
                else
                {
                    cardDetails.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        paybutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
              *//*  Intent intent = new Intent(CardDetailsActivity.this, MainActivity.class);
                startActivity(intent);*//*
            }
        });*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;

            case R.id.action_filter:
                getNotificationtypealert();
                return true;

          /*  case R.id.action_add:

                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notification_filter, menu);

        return true;
    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SendNotificationFragment(), "Sent");
        adapter.addFragment(new ReceiveNotificationFragment(), "Received");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onTaskCompleted(String s) {
        String arr[]=s.split("@@@");
        if (arr[1].equalsIgnoreCase("NotificationList"))
        {
            if (arr[0].equalsIgnoreCase("[]"))
            {
//                noData.setVisibility(View.VISIBLE);
//                notListView.setVisibility(View.GONE);
            }
            else
            {
                JSONArray jsonarray = null;
                try {
                    jsonarray = new JSONArray(arr[0]);
                    notificationList=new ArrayList<>();
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);

                        NotificationModel model = new NotificationModel();
                        model.setNotificationId(jsonobject.getString("notificationId"));
                        model.setNotiFromUserId(jsonobject.getString("notiFromUserId"));
                        model.setNotiFromUserName(jsonobject.getString("notiFromUserName"));
                        model.setNotiFromThumbnailUrl(jsonobject.getString("notiFromThumbnailUrl"));
                        model.setNotiToUserId(jsonobject.getString("notiToUserId"));
                        model.setNotiText(jsonobject.getString("notiText"));
                        model.setNotiTime(jsonobject.getString("notiTime"));
                        model.setNotiType(jsonobject.getString("notiType"));
                        model.setReceived(jsonobject.getBoolean("isReceived"));
                        model.setRead(jsonobject.getBoolean("isRead"));
                        notificationList.add(model);

                    }
                    Singleton.setNotificationList(notificationList);
                    Singleton obj = Singleton.getInstance();
                    if(obj.getResultReceiver() != null)
                    {
                        obj.getResultReceiver().send(400, null);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                if (notificationList.size()>0)
//                {
//                    notListView.setAdapter(new NotificationListAdapter(getActivity(),notificationList));
//                }
//                noData.setVisibility(View.GONE);
//                notListView.setVisibility(View.VISIBLE);
            }

            //loading.setVisibility(View.GONE);
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    SendNotificationFragment all = new SendNotificationFragment();
                    return all;
                case 1:
                    ReceiveNotificationFragment fav = new ReceiveNotificationFragment();
                    return fav;

                default:
                    return new SendNotificationFragment();
            }
            //return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }


    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(CardDetailsActivity.this, DrawerActivity.class);
//        startActivity(intent);
        // your code.
    }


    public void getNotificationtypealert()
    {
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.notification_type_list_items, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(NotificationActivity.this);
        builder.setView(dialoglayout);

        final ListView alertList=(ListView) dialoglayout.findViewById(R.id.list_notTypelist);
        final Button btn_search=(Button) dialoglayout.findViewById(R.id.btn_search);

        final AlertDialog alertDialog = builder.create();

        String[] list={"Friend Request","Emergency","Friend Request Accepted","Friend Request Rejected","Emergency Receipt","Tracking"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice,list);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_multiple_choice);
        alertList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        alertList.setAdapter(adapter);

        alertList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType.add(alertList.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> list=new ArrayList<String>();
                list.add("FriendRequest");
                list.add("Emergency");
                list.add("FriendRequestAccepted");
                list.add("FriendRequestRejected");
                list.add("EmergencyRecipt");
                list.add("TrackingStarted");
                selectedType=new ArrayList<String>();
                int len = alertList.getCount();
                SparseBooleanArray checked = alertList.getCheckedItemPositions();
                for (int i = 0; i < len; i++)
                    if (checked.get(i)) {
                        String item = list.get(i);
                        selectedType.add(item);
                    }
                if (Config.isConnectingToInternet(NotificationActivity.this))
                {
                    //loading.setVisibility(View.VISIBLE);
                    NotificationAsyncTask notification=new NotificationAsyncTask(selectedType,NotificationActivity.this,NotificationActivity.this);
                    notification.execute();
                }
                else
                {
                    Config.alertDialog(NotificationActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                }
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }
}