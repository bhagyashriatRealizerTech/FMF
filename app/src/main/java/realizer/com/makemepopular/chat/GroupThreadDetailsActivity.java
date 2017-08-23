package realizer.com.makemepopular.chat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import realizer.com.makemepopular.DashboardActivity;
import realizer.com.makemepopular.R;
import realizer.com.makemepopular.asynctask.GetThreadDetailsAsyncTask;
import realizer.com.makemepopular.asynctask.SetThreadNameAsyncTask;
import realizer.com.makemepopular.backend.DatabaseQueries;
import realizer.com.makemepopular.chat.adapter.ChatThreadListDetailsAdapter;
import realizer.com.makemepopular.chat.model.AddedContactModel;
import realizer.com.makemepopular.chat.model.ChatMessageViewListModel;
import realizer.com.makemepopular.chat.model.ChatThreadListModel;
import realizer.com.makemepopular.exceptionhandler.NetworkException;
import realizer.com.makemepopular.friendnear.FriendNearActivity;
import realizer.com.makemepopular.utils.ChatSectionIndexer;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.Singleton;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 06/03/2017.
 */

public class GroupThreadDetailsActivity extends AppCompatActivity implements OnTaskCompleted
{
    EditText edtGroupName;
    TextView btnSetGroupName;
    ListView participentListview;
    ChatThreadListDetailsAdapter details;
    private static ArrayList<ChatThreadListModel> participantList;
    ArrayList<AddedContactModel> selectedList;
    String threadId="";
    ProgressWheel loading;
    String threadName="";
    String oldThreadName="";
    SharedPreferences sharedpreferences;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_thread_details_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        edtGroupName = (EditText) findViewById(R.id.edt_setGroupName);
        btnSetGroupName= (TextView) findViewById(R.id.btn_setGroupName);
        btnSetGroupName.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
        participentListview= (ListView) findViewById(R.id.list_participentList);
        loading=(ProgressWheel) findViewById(R.id.loading);
        Bundle b=getIntent().getExtras();
        threadId=b.getString("ThreadID");
        getSupportActionBar().setTitle("Thread Detail");
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(GroupThreadDetailsActivity.this);

        if (Config.isConnectingToInternet(GroupThreadDetailsActivity.this)) {
            loading.setVisibility(View.VISIBLE);
            GetThreadDetailsAsyncTask getDetails = new GetThreadDetailsAsyncTask(threadId,GroupThreadDetailsActivity.this,GroupThreadDetailsActivity.this);
            getDetails.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            Config.alertDialog(GroupThreadDetailsActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
        }

        btnSetGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               threadName=edtGroupName.getText().toString().trim();
                if (!threadName.equals(""))
                {
                    if (!oldThreadName.equals(threadName)) {
                        if (Config.isConnectingToInternet(GroupThreadDetailsActivity.this)) {
                            edtGroupName.setEnabled(false);
                            loading.setVisibility(View.VISIBLE);
                            SetThreadNameAsyncTask setName = new SetThreadNameAsyncTask(threadId, threadName, GroupThreadDetailsActivity.this, GroupThreadDetailsActivity.this);
                            setName.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            Config.alertDialog(GroupThreadDetailsActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
                        }
                    }
                }
            }
        });

        participentListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (participantList.size() >1) {
                    String memberid = participantList.get(position).getParticipentID();
                    String threadid = participantList.get(position).getThreadId();
                    String memebr = participantList.get(position).getParticipant();
                    alertDialog(GroupThreadDetailsActivity.this, "Remove Friend", "Are you sure you want to remove " + memebr + "?", threadid, memberid);
                }
                else
                {
                    Config.alertDialog(GroupThreadDetailsActivity.this,"Suggestion","You can't remove this user.");
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Config.isConnectingToInternet(GroupThreadDetailsActivity.this)) {
            loading.setVisibility(View.VISIBLE);
            GetThreadDetailsAsyncTask getDetails = new GetThreadDetailsAsyncTask(threadId,GroupThreadDetailsActivity.this,GroupThreadDetailsActivity.this);
            getDetails.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            Config.alertDialog(GroupThreadDetailsActivity.this, "Internet Connection", "Make sure your device is connected to the internet.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.group_chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Intent i=new Intent(GroupThreadDetailsActivity.this,ChatMessageCenterActicity.class);
                Bundle bundle = new Bundle();
                bundle.putString("THREADID",threadId );
                bundle.putString("ActionBarTitle",oldThreadName);
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(GroupThreadDetailsActivity.this);
                bundle.putString("InitiatedID", sharedpreferences.getString("UserId", ""));
                bundle.putInt("UnreadCountThread", 0);
                i.putExtras(bundle);
                startActivity(i);
                finish();
                break;

            case R.id.action_add_participent:
                Intent intent = new Intent(GroupThreadDetailsActivity.this, ChatSectionIndexer.class);
                Bundle b=new Bundle();
                b.putString("GroupFromWhere","GroupDetails");
                intent.putExtras(b);
                startActivity(intent);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskCompleted(String s) {
        loading.setVisibility(View.GONE);
        String[] onTask = s.split("@@@");
        if (onTask[1].equals("GetThreadDetails")) {
            if (!onTask[0].equals("")) {
                try {
                    JSONObject json = new JSONObject(s);

                    String threadid = json.getString("threadId");
                    oldThreadName = json.getString("threadCustomName");
                    edtGroupName.setText(oldThreadName);
                    int position = oldThreadName.length();
                    Editable etext = edtGroupName.getText();
                    Selection.setSelection(etext, position);

                    JSONArray rootObj=new JSONArray( json.getString("participantListModel"));
                    participantList=new ArrayList<>();
                    selectedList=new ArrayList<>();
                    for (int j = 0; j < rootObj.length(); j++) {
                        JSONObject obj = rootObj.getJSONObject(j);
                        ChatThreadListModel a=new ChatThreadListModel();
                        if (obj.getString("participantId").equals(sharedpreferences.getString("UserId","")))
                        {
                        }
                        else
                        {
                            a.setParticipant(obj.getString("participantName"));
                            a.setParticipentID(obj.getString("participantId"));
                            a.setProfileImg(obj.getString("thumbnailUrl"));
                            a.setThreadId(threadid);
                            participantList.add(a);
                            AddedContactModel addedContactModel=new AddedContactModel();
                            addedContactModel.setFname(obj.getString("participantName"));
                            addedContactModel.setLname(obj.getString("participantName"));
                            addedContactModel.setUserId(obj.getString("participantId"));
                            addedContactModel.setProfileimage(obj.getString("thumbnailUrl"));
                            selectedList.add(addedContactModel);
                        }
                    }
                    Singleton.setSelectedStudentList(selectedList);
                    if (participantList.size() > 0)
                    {
                        details=new ChatThreadListDetailsAdapter(GroupThreadDetailsActivity.this,participantList);
                        participentListview.setAdapter(details);
                    }
                    else
                    {
                        //Config.alertDialog(GroupThreadDetailsActivity.this,"");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        if (onTask[1].equals("ThreadName")) {
            edtGroupName.setEnabled(true);
            if (onTask[0].equals("true")) {
                Config.alertDialog(GroupThreadDetailsActivity.this,"Group Rename","Group rename successfully done.");
                DatabaseQueries qr=new DatabaseQueries(GroupThreadDetailsActivity.this);
                qr.updateThreadname(threadId,threadName);
            }
        }
    }

    public void alertDialog(final Context context, String title, String message,final String tid, final String mid) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View dialoglayout = inflater.inflate(R.layout.alert_custom_messagebox, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialoglayout);

        Button buttonok= (Button) dialoglayout.findViewById(R.id.alert_btn_ok);
        buttonok.setText("Remove");
        Button buttoncencel= (Button) dialoglayout.findViewById(R.id.alert_btn_cancel);
        buttoncencel.setVisibility(View.VISIBLE);
        TextView titleName=(TextView) dialoglayout.findViewById(R.id.alert_dialog_title);
        TextView alertMsg=(TextView) dialoglayout.findViewById(R.id.alert_dialog_message);


        final AlertDialog alertDialog = builder.create();

        buttonok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isConnectingToInternet(context)) {
                    RemoveMemberFromGroupAsyncTask getDetails = new RemoveMemberFromGroupAsyncTask(tid,mid,context);
                    getDetails.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else
                {
                    Config.alertDialog(context, "Internet Connection", "Make sure your device is connected to the internet.");
                }
                alertDialog.dismiss();
            }
        });

        buttoncencel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        titleName.setText(title);
        alertMsg.setText(message);

        alertDialog.show();

    }
    public class RemoveMemberFromGroupAsyncTask extends AsyncTask<Void,Void,StringBuilder> {

        StringBuilder resultbuilder;
        Context mycontext;
        String memberid;
        String threadId;
        ProgressDialog dialog;
        SharedPreferences sharedpreferences;

        public RemoveMemberFromGroupAsyncTask(String tid, String mid, Context mycontext) {
            this.mycontext = mycontext;
            this.threadId = tid;
            this.memberid = mid;
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            dialog = ProgressDialog.show(mycontext, "", "Removing Friend...");
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {
            if (isCancelled()) {
                return (null); // don't forget to terminate this method
            }

            resultbuilder = new StringBuilder();
            HttpClient httpClient = new DefaultHttpClient();
            String url = Config.URL_Chat + "RemoveThreadMember";
            HttpPut httpPost = new HttpPut(url);
            String json = "";
            StringEntity se = null;
            JSONObject jsonObject = new JSONObject();
            try {

                jsonObject.put("threadId", threadId);
                jsonObject.put("memberId", memberid);

                json = jsonObject.toString();

                se = new StringEntity(json);
                httpPost.setEntity(se);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                HttpResponse httpResponse = httpClient.execute(httpPost);
                StatusLine statusLine = httpResponse.getStatusLine();
                int statuscode = statusLine.getStatusCode();
                if (statuscode == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        resultbuilder.append(line);
                    }
                } else {
                    StringBuilder exceptionString = new StringBuilder();
                    HttpEntity entity = httpResponse.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        exceptionString.append(line);
                    }

                    NetworkException.insertNetworkException(mycontext, exceptionString.toString());
                    resultbuilder.append("Server not Responding");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultbuilder;
        }

        @Override
        protected void onPostExecute(StringBuilder stringBuilder) {
            super.onPostExecute(stringBuilder);
            dialog.dismiss();
            if (stringBuilder.toString().equalsIgnoreCase("true")) {
                if (participantList.size() > 0) {
                    for (int i = 0; i < participantList.size(); i++) {
                        if (memberid.equalsIgnoreCase(participantList.get(i).getParticipentID())) {
                            participantList.remove(i);

                            break;
                        }
                    }
                }
                details.notifyDataSetChanged();
            }
        }
    }
}
