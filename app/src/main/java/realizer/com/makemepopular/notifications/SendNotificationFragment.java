package realizer.com.makemepopular.notifications;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.Singleton;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by shree on 2/3/2017.
 */
public class SendNotificationFragment extends Fragment implements OnTaskCompleted{

    ListView notListView;
    ProgressWheel loading;
    ArrayList<NotificationModel> notificationList;
    ArrayList<NotificationModel> sendNotificationList;
    ArrayList<String> selectedType;
    TextView noData;
    String frndId="";
    MessageResultReceiver resultReceiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview=inflater.inflate(R.layout.activity_notificationlist,container,false);
        setHasOptionsMenu(true);
        notListView=(ListView)rootview.findViewById(R.id.list_notlist);
        loading=(ProgressWheel)rootview.findViewById(R.id.loading);
        noData=(TextView)rootview.findViewById(R.id.notext);

//        Bundle bundle=getIntent().getExtras();
//        frndId=bundle.getString("FriendID");

        realizer.com.makemepopular.utils.Singleton obj = realizer.com.makemepopular.utils.Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);
        notificationList=new ArrayList<>();
        sendNotificationList=new ArrayList<>();
        notificationList=Singleton.getNotificationList();

        if (notificationList.size()>0)
        {
            for (int i=0;i<notificationList.size();i++)
            {
                if (!notificationList.get(i).isReceived)
                {
                    sendNotificationList.add(notificationList.get(i));
                }
            }
            notListView.setAdapter(new NotificationListAdapter(getActivity(),sendNotificationList));
            noData.setVisibility(View.GONE);
            notListView.setVisibility(View.VISIBLE);
        }
        else
        {
            noData.setVisibility(View.VISIBLE);
            notListView.setVisibility(View.GONE);
        }

       /* if (Config.isConnectingToInternet(getActivity()))
        {
            loading.setVisibility(View.VISIBLE);
            ArrayList<String> list=new ArrayList<>();
            list.add("FriendRequest");
            NotificationAsyncTask notification=new NotificationAsyncTask(list,getActivity(),SendNotificationFragment.this);
            notification.execute();
        }
        else
        {
            Config.alertDialog(getActivity(), "Network Error", "No Internet connection");
        }*/

        notListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o=parent.getItemAtPosition(position);
                NotificationModel obj=(NotificationModel)o;

                Intent intent = new Intent(getActivity(), NotificationDetailsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("FriendName",obj.getNotiFromUserName());
                bundle.putString("Time", obj.getNotiTime());
                bundle.putString("Type", obj.getNotiType());
                bundle.putString("Message", obj.getNotiText());
                bundle.putString("FromUserID", obj.getNotiFromUserId());
                bundle.putString("ToUserId", obj.getNotiToUserId());
                bundle.putBoolean("IsReceived", obj.isReceived());
                bundle.putBoolean("IsRead", obj.isRead());
                bundle.putString("NotificationID", obj.getNotificationId());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        return rootview;
    }



    @Override
    public void onResume() {
        super.onResume();
        notificationList=new ArrayList<>();
        sendNotificationList=new ArrayList<>();
        notificationList=Singleton.getNotificationList();

        if (notificationList.size()>0)
        {
            for (int i=0;i<notificationList.size();i++)
            {
                if (!notificationList.get(i).isReceived)
                {
                    sendNotificationList.add(notificationList.get(i));
                }
            }
            notListView.setAdapter(new NotificationListAdapter(getActivity(),sendNotificationList));
            noData.setVisibility(View.GONE);
            notListView.setVisibility(View.VISIBLE);
        }
        else
        {
            noData.setVisibility(View.VISIBLE);
            notListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTaskCompleted(String s) {
        String arr[]=s.split("@@@");
        if (arr[1].equalsIgnoreCase("NotificationList"))
        {
            if (arr[0].equalsIgnoreCase("[]"))
            {
                noData.setVisibility(View.VISIBLE);
                notListView.setVisibility(View.GONE);
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

                        if(!model.isReceived)
                        notificationList.add(model);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (notificationList.size()>0)
                {
                    notListView.setAdapter(new NotificationListAdapter(getActivity(),notificationList));
                }
                noData.setVisibility(View.GONE);
                notListView.setVisibility(View.VISIBLE);
            }

            loading.setVisibility(View.GONE);
        }
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                getActivity().finish();
                break;

            case R.id.action_filter:
                getNotificationtypealert();
                return true;

          *//*  case R.id.action_add:

                return true;*//*
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.notification_filter, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
*/
    public void getNotificationtypealert()
    {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.notification_type_list_items, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialoglayout);

        final ListView alertList=(ListView) dialoglayout.findViewById(R.id.list_notTypelist);
        final Button btn_search=(Button) dialoglayout.findViewById(R.id.btn_search);

        final AlertDialog alertDialog = builder.create();

        String[] list={"Friend Request","Emergency","Friend Request Accepted","Friend Request Rejected","Emergency Receipt","Tracking"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
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
                if (Config.isConnectingToInternet(getActivity()))
                {
                    loading.setVisibility(View.VISIBLE);
                    NotificationAsyncTask notification=new NotificationAsyncTask(selectedType,getActivity(),SendNotificationFragment.this);
                    notification.execute();
                }
                else
                {
                    Config.alertDialog(getActivity(), "Internet Connection", "Make sure your device is connected to the internet.");
                }
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    class UpdateUI implements Runnable {
        String update;

        public UpdateUI(String update) {

            this.update = update;
        }

        public void run() {

            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if(update.equals("Emergency")) {

                String notType=sharedpreferences.getString("Type", "");
                if (notType.equalsIgnoreCase("FriendRequest"))
                {
                    String reqstName=sharedpreferences.getString("RequsetByName", "");
                    String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
                    Config.showacceptrejectFriendRequest(reqstName,thumbnail,getActivity());
                }
                else if (notType.equalsIgnoreCase("Emergency"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String newMsg="Hey Buddy..."+""+msg;
                    String trobler=sharedpreferences.getString("TroublerName", "");
                    String troblerid=sharedpreferences.getString("TroublerUserId", "");
                    Config.showEmergencyAcceptReject(newMsg,trobler,troblerid,getActivity());
                }
                else if (notType.equalsIgnoreCase("EmergencyRecipt"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String newMsg="Hey Buddy..."+""+msg;
                    String helpername=sharedpreferences.getString("HelperUserName", "");
                    String isResch=sharedpreferences.getString("isReaching", "");
                    if (isResch.equalsIgnoreCase("true")){}
                    //showEmergencyAckAlert(newMsg, helpername);
                    Config.showEmergencyAckAlert(newMsg,helpername,getActivity());
                }

            }

            else if(update.equals("NotificationList")) {
                notificationList=new ArrayList<>();
                sendNotificationList=new ArrayList<>();
                notificationList=Singleton.getNotificationList();

                if (notificationList.size()>0)
                {
                    for (int i=0;i<notificationList.size();i++)
                    {
                        if (!notificationList.get(i).isReceived)
                        {
                            sendNotificationList.add(notificationList.get(i));
                        }
                    }
                    notListView.setAdapter(new NotificationListAdapter(getActivity(),sendNotificationList));
                    noData.setVisibility(View.GONE);
                    notListView.setVisibility(View.VISIBLE);
                }
                else
                {
                    noData.setVisibility(View.VISIBLE);
                    notListView.setVisibility(View.GONE);
                }
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
                getActivity().runOnUiThread(new UpdateUI("Emergency"));
            }
            if(resultCode == 200){
                getActivity().runOnUiThread(new UpdateUI("RefreshThreadList"));
            }
            if(resultCode == 400){
                getActivity().runOnUiThread(new UpdateUI("NotificationList"));
            }

        }
    }
}
