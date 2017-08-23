package realizer.com.makemepopular.chat;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import realizer.com.makemepopular.DashboardActivity;
import realizer.com.makemepopular.MyFirebaseMessagingService;
import realizer.com.makemepopular.R;
import realizer.com.makemepopular.asynctask.GetFriendListAsynTask;
import realizer.com.makemepopular.backend.DatabaseQueries;
import realizer.com.makemepopular.chat.adapter.ChatMessageCenterListAdapter;
import realizer.com.makemepopular.chat.asynctask.GetThreadMessageAsyncTaskPost;
import realizer.com.makemepopular.chat.asynctask.ReceiveMessageAsyncTaskPut;
import realizer.com.makemepopular.chat.asynctask.SendMessgeAsyncTaskPost;
import realizer.com.makemepopular.chat.model.ChatMessageViewListModel;
import realizer.com.makemepopular.chat.model.ChatThreadListModel;
import realizer.com.makemepopular.emoji.EmojiconEditText;
import realizer.com.makemepopular.emoji.EmojiconGridView;
import realizer.com.makemepopular.emoji.EmojiconsPopup;
import realizer.com.makemepopular.emoji.emoji.Emojicon;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.friendnear.FriendNearActivity;
import realizer.com.makemepopular.newfriendlist.NewFriendListActivity;
import realizer.com.makemepopular.service.ServiceSendMessgeAsyncTaskPost;
import realizer.com.makemepopular.utils.ChatSectionIndexer;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.Singleton;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 30/11/2016.
 */
public class ChatMessageCenterActicity extends AppCompatActivity implements AbsListView.OnScrollListener,OnTaskCompleted
{
    DatabaseQueries qr;
    boolean chkUser;
    TextView send;
    ProgressWheel loading;
    EmojiconEditText emojiconEditText;
    int mCurrentX ;
    int  mCurrentY;
    int currentPosition,unreadCount;
    int lstsize,num;
    String dateForMsgId="",timeForMsgId="";
    MessageResultReceiver resultReceiver;
    String ThreadID,ReceiverID,UserID,InitiateID,SendtoMSGID,UserFullName,SenderThumbnail;
    ListView lsttname;
    String IsFirstTimeLogin="";
    SharedPreferences sharedpreferences;
    ArrayList<ChatMessageViewListModel> chatMessages=new ArrayList<>();
    ChatMessageCenterListAdapter adapter;
    LinearLayout messagesendOuter;
    TextView noLongerFriend,noData;
    EditText msg;
    int qid;
    String  uniqueID;
    static boolean isRespondingAPI=false;
    GetFriendListAsynTask getfrnd;
    ServiceSendMessgeAsyncTaskPost sendMsg;
    GetThreadMessageAsyncTaskPost getMessage;
    ReceiveMessageAsyncTaskPut receiveMsg;
    int partcipantLength=0;
    MediaPlayer mMediaPlayer;
    String ThreadStatus="";
    static String isComposeEnable="";
    LinearLayout emojiiconouter,submitbtn_outer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,""));
        setContentView(R.layout.activity_chat_message_center);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        qr=new DatabaseQueries(ChatMessageCenterActicity.this);
        noData= (TextView) findViewById(R.id.noData);
        lsttname = (ListView) findViewById(R.id.lstviewquery);
        //msg = (EditText) findViewById(R.id.edtmsgtxt);
        loading = (ProgressWheel) findViewById(R.id.loading);
        mMediaPlayer = MediaPlayer.create(ChatMessageCenterActicity.this,R.raw.send_message_new);

        messagesendOuter= (LinearLayout) findViewById(R.id.ll_compose_layout);
        noLongerFriend= (TextView) findViewById(R.id.noLongerFriendTxt);
        emojiiconouter= (LinearLayout) findViewById(R.id.emoji_btn_linearouter);
        submitbtn_outer= (LinearLayout) findViewById(R.id.submitbtn_outer);

        /****** Emojies Start*******/

        // emojies
        emojiconEditText= (EmojiconEditText) findViewById(R.id.emojicon_edit_text);
        final View rootView = findViewById(R.id.root_view);
        final ImageView emojiButton = (ImageView) findViewById(R.id.emoji_btn);
        final ImageView submitButton = (ImageView) findViewById(R.id.submit_btn);


        // Give the topmost view of your activity layout hierarchy. This will be used to measure soft keyboard height
        final EmojiconsPopup popup = new EmojiconsPopup(rootView, this);

        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard();

        new MyFirebaseMessagingService().setCountZero("SendMessage");
        //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(emojiButton, R.drawable.smiley);
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if (popup.isShowing())
                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (emojiconEditText == null || emojicon == null) {
                    return;
                }

                int start = emojiconEditText.getSelectionStart();
                int end = emojiconEditText.getSelectionEnd();
                if (start < 0) {
                    emojiconEditText.append(emojicon.getEmoji());
                } else {
                    emojiconEditText.getText().replace(Math.min(start, end),
                            Math.max(start, end), emojicon.getEmoji(), 0,
                            emojicon.getEmoji().length());
                }
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                emojiconEditText.dispatchKeyEvent(event);
            }
        });

        // To toggle between text keyboard and emoji keyboard keyboard(Popup)
        emojiButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if(!popup.isShowing()){

                    //If keyboard is visible, simply show the emoji popup
                    if(popup.isKeyBoardOpen()){
                        popup.showAtBottom();
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                    }

                    //else, open the text keyboard first and immediately after that show the emoji popup
                    else{
                        emojiconEditText.setFocusableInTouchMode(true);
                        emojiconEditText.requestFocus();
                        popup.showAtBottomPending();
                        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(emojiconEditText, InputMethodManager.SHOW_IMPLICIT);
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                    }
                }

                //If popup is showing, simply dismiss it to show the undelying text keyboard
                else{
                    popup.dismiss();
                }
            }
        });
        /****** Emojies Code End*******/

        qid=0;
        lstsize = chatMessages.size();
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ChatMessageCenterActicity.this);
        UserID = sharedpreferences.getString("UserId", "");
        UserFullName = sharedpreferences.getString("userLoginId", "");
        SenderThumbnail = sharedpreferences.getString("ThumbnailURL", "");
        IsFirstTimeLogin=sharedpreferences.getString("FirstTimeLogin1","");


        lsttname.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String CLIPBOARD_TEXT="";
                CLIPBOARD_TEXT=chatMessages.get(position).getMessage();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(CLIPBOARD_TEXT, CLIPBOARD_TEXT);
                clipboard.setPrimaryClip(clip);
                Object o = lsttname.getItemAtPosition(position);
                ChatMessageViewListModel chatThreadlist = (ChatMessageViewListModel) o;
                Bundle bundle = new Bundle();
                bundle.putString("SName",chatThreadlist.getSenderName());
                bundle.putString("MEssage",chatThreadlist.getMessage());
                bundle.putString("MSgtime",chatThreadlist.getSendTime());
               /* Intent i = new Intent(ChatMessageCenterActicity.this, ChatMessageInfo.class);
                i.putExtras(bundle);
                startActivity(i);*/
                int PICKFILE_RESULT_CODE=1;
                Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
                intent.putExtra("CONTENT_TYPE", "*/*");
                startActivityForResult(intent,PICKFILE_RESULT_CODE);
                Toast.makeText(ChatMessageCenterActicity.this, "Message Copied", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Singleton obj = Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        ThreadID = b.getString("THREADID");
        ReceiverID = sharedpreferences.getString("RECEIVERID", "");
        InitiateID=b.getString("InitiatedID");
        unreadCount=b.getInt("UnreadCountThread");

        //SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ChatThreadListActivity.this);
        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString("ActiveThread", ThreadID);
        edit.commit();

        getSupportActionBar().setTitle(b.getString("ActionBarTitle"));
        if (ReceiverID.contains(","))
        {
            String receiver[]=ReceiverID.split(",");
            if(receiver.length==2)
            {
                ThreadStatus="single";
            }
            else if (receiver.length>2)
            {
                ThreadStatus="group";
            }
        }
        else
        {
            ThreadStatus="single";
        }

        GetThreadMessages();

        NotificationManager notifManager= (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();

        submitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                String status=qr.getUserIsPending(SendtoMSGID);
                if (status.equals("Pending"))
                {

                }
                else
                {
                    GetThreadMessages();
                    if (emojiconEditText.getText().toString().trim().equals(""))
                    {
                        Toast.makeText(ChatMessageCenterActicity.this, "Enter Message", Toast.LENGTH_SHORT).show();
                    } else {
                        String toServer = emojiconEditText.getText().toString().trim();
                        String toServerUnicodeEncoded = StringEscapeUtils.escapeJava(toServer);

                        if (toServer.length() != 0) {

                            emojiconEditText.setEnabled(false);
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
                            String date = df.format(calendar.getTime());
                                uniqueID = UUID.randomUUID().toString();
                               /* sendMsg = new SendMessgeAsyncTaskPost(ChatMessageCenterActicity.this, ThreadID, UserID, date.toString(), toServerUnicodeEncoded, SendtoMSGID,uniqueID, ChatMessageCenterActicity.this);
                                sendMsg.execute();*/


                                String[] datenew = date.split(" ");
                                String newdate = datenew[0];
                                String[] senddate = newdate.split("/");

                                dateForMsgId = senddate[1] + "-" + senddate[0] + "-" + senddate[2];
                                timeForMsgId = GetMSGTime(date);
                                String timestamp=GetUtcDate();
                             Log.d("UTCTime", emojiconEditText.getText()+":"+timestamp);
                                qr.insertMessageDtls(uniqueID, UserID, timestamp, toServerUnicodeEncoded, ThreadID, SendtoMSGID, UserFullName, SenderThumbnail, senddate[1] + "-" + senddate[0] + "-" + senddate[2], GetMSGTime(date), "false", "false", "false");
                            ChatMessageViewListModel obj=new ChatMessageViewListModel();
                            obj.setMessageId(uniqueID);
                            obj.setSenderId(UserID);
                            obj.setTimeStamp(timestamp);
                            obj.setMessage(toServerUnicodeEncoded);
                            obj.setThreadId(ThreadID);
                            obj.setReceiverId(SendtoMSGID);
                            obj.setSenderName(UserFullName);
                            obj.setSenderThumbnail(SenderThumbnail);
                            obj.setSendDate(senddate[1] + "-" + senddate[0] + "-" + senddate[2]);
                            obj.setSendTime(GetMSGTime(date));
                            obj.setIsNewMessage("false");
                            obj.setIsDelivered("false");
                            obj.setIsRead("false");
                          /*  chatMessages.add(obj);
                            adapter.notifyDataSetChanged();*/
                             DisplayMessagesinList();
                                emojiconEditText.getText().clear();
                                emojiconEditText.setEnabled(true);

                            if (Config.isConnectingToInternet(ChatMessageCenterActicity.this)) {
                                isRespondingAPI=false;


                                sendMsg=new ServiceSendMessgeAsyncTaskPost(ChatMessageCenterActicity.this,obj,ChatMessageCenterActicity.this);
                                sendMsg.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                                new CountDownTimer( 15000, 1000) {
                                    public void onTick(long millisUntilFinished) {
                                    }
                                    public void onFinish() {
                                        loading.setVisibility(View.GONE);
                                        if (!isRespondingAPI)
                                        {
                                            if (sendMsg != null && sendMsg.getStatus() != AsyncTask.Status.FINISHED) {
                                                sendMsg.cancel(true);
                                                //Config.alertDialog(ChatMessageCenterActicity.this, "Error", "Server is not responding.Please try again after sometime.");
                                            }
                                        }
                                    }
                                }.start();
                            }
                            else
                            {
                            }
                        }
                    /*}*/
                    }
                }

            }
        });

        DisplayMessagesinList();
        Log.d("InvokedC","onStop");
    }
    public static Date localToGMT() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date gmt = new Date(sdf.format(date));
        return gmt;
    }
    public String GetUtcDate()
    {
        Date myDate = new Date();

/*
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(myDate);
*/
        //Date time = Calendar.getInstance().getTime();
        SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateAsString = outputFmt.format(myDate);
        return dateAsString;
    }

    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId){
        iconToBeChanged.setImageResource(drawableResourceId);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mCurrentX = view.getScrollX();
        mCurrentY = view.getScrollY();
        currentPosition = lsttname.getSelectedItemPosition();
        Log.d("Position", "" + currentPosition);
    }

    public void GetThreadMessages()
    {
        if (Config.isConnectingToInternet(ChatMessageCenterActicity.this))
        {
            if (IsFirstTimeLogin.equals("true")) {
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("FirstTimeLogin1", "False");
                edit.commit();
                /*loading.setVisibility(View.VISIBLE);*/
                isRespondingAPI=false;
                getMessage = new GetThreadMessageAsyncTaskPost(ChatMessageCenterActicity.this, ThreadID,"", ChatMessageCenterActicity.this);
                getMessage.execute();

                new CountDownTimer(15000, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }
                    public void onFinish() {
                        loading.setVisibility(View.GONE);
                        if (!isRespondingAPI)
                        {
                            if (getMessage != null && getMessage.getStatus() != AsyncTask.Status.FINISHED) {
                                getMessage.cancel(true);
                               // Config.alertDialog(ChatMessageCenterActicity.this, "Error", "Server is not responding.Please try again after sometime.");
                            }
                        }
                    }
                }.start();

            }
            else
            {
                String lastMsgTime=qr.getLastMsgTimeStamp(ThreadID);
                if (lastMsgTime.equals(""))
                {
                    loading.setVisibility(View.VISIBLE);
                }
                getMessage = new GetThreadMessageAsyncTaskPost(ChatMessageCenterActicity.this, ThreadID,lastMsgTime, ChatMessageCenterActicity.this);
                getMessage.execute();
                isRespondingAPI=false;

                new CountDownTimer(15000, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }
                    public void onFinish() {
                        loading.setVisibility(View.GONE);
                        if (!isRespondingAPI)
                        {
                            if (getMessage != null && getMessage.getStatus() != AsyncTask.Status.FINISHED) {
                                getMessage.cancel(true);
                                //Config.alertDialog(ChatMessageCenterActicity.this, "Error", "Server is not responding.Please try again after sometime.");
                            }
                        }
                    }
                }.start();
            }
        }
        else
        {
            DisplayMessagesinList();
            Config.alertDialog(ChatMessageCenterActicity.this, "Internet Connection", "Make sure your device is connected to the internet.");
        }
    }
    @Override
    public void onTaskCompleted(String s) {
        isRespondingAPI=true;
        String tp;
        emojiconEditText.setEnabled(true);
        if (s.equals("@@@MessageList")) {
        }
        else
        {
            String[] onTask = s.split("@@@");
            if (onTask[1].equals("MessageList"))
            {
                loading.setVisibility(View.GONE);

                JSONArray rootObj = null;
                Log.d("String", onTask[0]);
                try {

                    rootObj = new JSONArray(onTask[0]);
                    int i = rootObj.length();

                    for (int j = 0; j < i; j++) {
                        JSONObject obj = rootObj.getJSONObject(j);
                        String date = Utility.convertUTCdate(obj.getString("timeStamp").replace("T", " "));

                        String datet[] = date.split(" ");
                        String time[] = datet[1].split(":");
                        int t1 = Integer.valueOf(time[0]);
                        String sentTm = "";
                        if (t1 == 12) {
                            tp = "PM";
                            sentTm = "" + t1 + ":" + time[1] + " " + tp;
                        } else if (t1 > 12) {
                            int t2 = t1 - 12;
                            tp = "PM";
                            sentTm = "" + t2 + ":" + time[1] + " " + tp;
                        } else {
                            tp = "AM";
                            sentTm = time[0] + ":" + time[1] + " " + tp;
                        }
                        isComposeEnable=obj.getString("isComposeEnable");
                        String serverResponse = obj.getString("message");
                        String fromServerUnicodeDecoded = StringEscapeUtils.unescapeJava(serverResponse);
                        ChatMessageViewListModel obj1=new ChatMessageViewListModel();
                       /* if (SendtoMSGID.equals(obj.getString("senderId"))) {*/
                            boolean chk = qr.ChekMessageInMessageList(obj.getString("messageId"));
                            if (!chk)
                            {
                                qr.insertMessageDtls(obj.getString("messageId"), obj.getString("senderId"), obj.getString("timeStamp"), fromServerUnicodeDecoded, obj.getString("threadId"), obj.getString("receiverId"), obj.getString("senderName"), obj.getString("senderThumbnail"), datet[0], sentTm, obj.getString("isNewMessage"),obj.getString("isDeliver"),obj.getString("isRead"));
                            }
                            else
                            {
                                qr.updateMessageDtls(obj.getString("messageId"), obj.getString("senderId"), obj.getString("timeStamp"), fromServerUnicodeDecoded, obj.getString("threadId"), obj.getString("receiverId"), obj.getString("senderName"), obj.getString("senderThumbnail"), datet[0], sentTm, obj.getString("isNewMessage"),obj.getString("isDeliver"),obj.getString("isRead"));
                            }
                        /*}*/
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    num = 1;
                    Log.e("JSON", e.toString());
                    Log.e("Login.JLocalizedMessage", e.getLocalizedMessage());
                    Log.e("Login(JStackTrace)", e.getStackTrace().toString());
                    Log.e("Login(JCause)", e.getCause().toString());
                    Log.wtf("Login(JMsg)", e.getMessage());
                }
                DisplayMessagesinList();
            }
            else if (onTask[1].equals("SendMsg"))
            {
                mMediaPlayer.start();
                loading.setVisibility(View.GONE);
                if (onTask[0].equals(""))
                {

                } else {
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(onTask[0]);
                        String Success = obj.getString("success");
                        if (Success.equals("true"))
                        {

                        }
                        else
                        {
                            Config.alertDialog(ChatMessageCenterActicity.this, "Error", "Message Not Sent");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        num = 1;
                        Log.e("JSON", e.toString());
                        Log.e("Login.JLocalizedMessage", e.getLocalizedMessage());
                        Log.e("Login(JStackTrace)", e.getStackTrace().toString());
                        Log.e("Login(JCause)", e.getCause().toString());
                        Log.wtf("Login(JMsg)", e.getMessage());
                    }
                }
            }
            else if (onTask[1].equals("NoFriend"))
            {
                loading.setVisibility(View.GONE);
                if (onTask[0].equals("")) {

                } else {
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(onTask[0]);
                        String Success = obj.getString("message");
                        if (Success.equals("No Longer Friend")) {
                            qr.deleteUserFromTable(SendtoMSGID);
                            qr.deleteMessageFromTable(uniqueID);
                            /*noLongerFriend.setVisibility(View.VISIBLE);
                            messagesendOuter.setVisibility(View.GONE);*/
                            hideEmojiEdittext();
                            chkUser = false;
                            emojiconEditText.setText("No Longer Friends");
                            emojiconEditText.setEnabled(false);
                        } else {
                            //Config.alertDialog(ChatMessageCenterActicity.this, "Error", "Message Not Sent");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        num = 1;
                        Log.e("JSON", e.toString());
                        Log.e("Login.JLocalizedMessage", e.getLocalizedMessage());
                        Log.e("Login(JStackTrace)", e.getStackTrace().toString());
                        Log.e("Login(JCause)", e.getCause().toString());
                        Log.wtf("Login(JMsg)", e.getMessage());
                    }
                }
            } else if (onTask[1].equals("Blocked")) {
                Toast.makeText(ChatMessageCenterActicity.this, "You can't send message.\n This user blocked you", Toast.LENGTH_SHORT).show();
            }
            if (onTask[1].equalsIgnoreCase("FriendList"))
            {
                if (! onTask[0].equalsIgnoreCase("[]"))
                {
                    JSONArray jsonarray = null;
                    try {
                        jsonarray = new JSONArray(onTask[0]);

                        for (int i = 0; i < jsonarray.length(); i++) {
                            JSONObject jsonobject = jsonarray.getJSONObject(i);
                            String ismessaging=jsonobject.getString("isMessagingAllowed");
                            String status=jsonobject.getString("status");

                            boolean frndChk = qr.ChekFriendinFrndList(jsonobject.getString("friendsUserId"));
                            if (!frndChk) {
                                qr.insertFriendList(jsonobject.getString("friendsUserId"), jsonobject.getString("friendName"), String.valueOf(jsonobject.getBoolean("isEmergencyAlert")), String.valueOf(jsonobject.getBoolean("isMessagingAllowed")), String.valueOf(jsonobject.getBoolean("isTrackingAllowed")), jsonobject.getString("friendThumbnailUrl"), jsonobject.getString("status"), String.valueOf(jsonobject.getBoolean("isRequestSent")), jsonobject.getString("createTS"), jsonobject.getString("allowTrackingTillDate"), jsonobject.getString("trackingStatusChangeDate"), jsonobject.getString("isDeleted"),jsonobject.getString("lastActive"),jsonobject.getString("dob"),jsonobject.getString("age"),jsonobject.getString("gender"),jsonobject.getString("email"));
                            } else {
                                qr.updateFriendLIst(jsonobject.getString("friendsUserId"), jsonobject.getString("friendName"), String.valueOf(jsonobject.getBoolean("isEmergencyAlert")), String.valueOf(jsonobject.getBoolean("isMessagingAllowed")), String.valueOf(jsonobject.getBoolean("isTrackingAllowed")), jsonobject.getString("friendThumbnailUrl"), jsonobject.getString("status"), String.valueOf(jsonobject.getBoolean("isRequestSent")), jsonobject.getString("createTS"), jsonobject.getString("allowTrackingTillDate"), jsonobject.getString("trackingStatusChangeDate"), jsonobject.getString("isDeleted"),jsonobject.getString("lastActive"),jsonobject.getString("dob"),jsonobject.getString("age"),jsonobject.getString("gender"),jsonobject.getString("email"));
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        loading.setVisibility(View.GONE);
                    }
                    /*chkUser = qr.ChekFriendinFrndList(SendtoMSGID);
                    if (!chkUser)
                    {
                       *//* noLongerFriend.setVisibility(View.VISIBLE);
                        messagesendOuter.setVisibility(View.GONE);*//*
                        hideEmojiEdittext();
                    }

                    String status=qr.getUserIsPending(SendtoMSGID);
                    if (status.equals("Pending"))
                    {
                       *//* noLongerFriend.setVisibility(View.VISIBLE);
                        messagesendOuter.setVisibility(View.GONE);*//*
                        hideEmojiEdittext();
                        noLongerFriend.setText("Your friend request is pending.");
                    }
                    else if (status.equals(""))
                    {

                    }
                    else
                    {*//* noLongerFriend.setVisibility(View.GONE);
                        messagesendOuter.setVisibility(View.VISIBLE);*//*
                        showEmojiEdittext();
                    }*/


                }
                else if(onTask[0].equalsIgnoreCase("ServerNotResponding"))
                {
                    loading.setVisibility(View.GONE);
                    //Config.alertDialog(ChatMessageCenterActicity.this,"Error","Server not responding.Please try again after sometime");
                }
                else
                {
                    loading.setVisibility(View.GONE);
                    /*chkUser = qr.ChekFriendinFrndList(SendtoMSGID);
                    if (!chkUser)
                    {
                        *//*noLongerFriend.setVisibility(View.VISIBLE);
                        messagesendOuter.setVisibility(View.GONE);*//*
                        hideEmojiEdittext();
                    }
                    String status=qr.getUserIsPending(SendtoMSGID);
                    if (status.equals("Pending"))
                    {
                        *//*noLongerFriend.setVisibility(View.VISIBLE);
                        messagesendOuter.setVisibility(View.GONE);*//*
                        hideEmojiEdittext();
                        noLongerFriend.setText("Your friend request is pending.");

                    }
                    else if (status.equals(""))
                    {}
                    else
                    { *//*noLongerFriend.setVisibility(View.GONE);
                        messagesendOuter.setVisibility(View.VISIBLE);*//*
                        showEmojiEdittext();
                    }*/
                    //Config.alertDialog(FriendListActivity.this,"Error","Server not responding.Please try again after sometime");
                }

            }
            else
            {

            }
        }
        qr.updateThreadUnreadCount(ThreadID);

        if (isComposeEnable.equals("true"))
        {
            showEmojiEdittext();
        }
        else if (isComposeEnable.equals("false"))
        {
            hideEmojiEdittext();
        }
    }
    public void DisplayMessagesinList()
    {
        chatMessages=new ArrayList<>();
        chatMessages=qr.getThreadMessage(ThreadID);
        if(chatMessages.size()!=0)
        {
            adapter = new ChatMessageCenterListAdapter(this,chatMessages,unreadCount,ThreadStatus);
            lsttname.setAdapter(adapter);

            lsttname.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            //lsttname.setFastScrollEnabled(true);
            //lsttname.setScrollY(lsttname.getCount());
            lsttname.setSelection(lsttname.getCount() - 1);
            //lsttname.smoothScrollToPosition(lsttname.getCount());
            lsttname.setOnScrollListener(this);
            lstsize =  chatMessages.size();
            noData.setVisibility(View.GONE);
            lsttname.setVisibility(View.VISIBLE);
        }
        else
        {
            noData.setVisibility(View.VISIBLE);
            lsttname.setVisibility(View.GONE);
        }
        qr.updateisNewMessage(ThreadID,"true");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString("ActiveThread", "");
        edit.commit();
        finish();
    }

    class UpdateUI implements Runnable
    {
        String update;
        Bundle b;

        public UpdateUI(String update,Bundle data) {

            this.update = update;
            b=data;
        }

        public void run() {

            if(update.equals("RecieveMessage")) {

                String tp;
                String url=b.getString("ReceiverURL");
                String time1=b.getString("ReceiveTime");


                String date = Utility.convertUTCdate(time1.replace("T", " "));
                String datet[] = date.split(" ");

                String time[] = datet[1].split(":");
                int t1 = Integer.valueOf(time[0]);
                String sentTm = "";
                if (t1 == 12) {
                    tp = "PM";
                    sentTm = "" + t1 + ":" + time[1] + " " + tp;
                } else if (t1 > 12) {
                    int t2 = t1 - 12;
                    tp = "PM";
                    sentTm = "" + t2 + ":" + time[1] + " " + tp;
                } else {
                    tp = "AM";
                    sentTm = time[0] + ":" + time[1] + " " + tp;
                }
                String serverResponse = b.getString("ReceiveMSG");
                String fromServerUnicodeDecoded = StringEscapeUtils.unescapeJava(serverResponse);
                String messageId=b.getString("MessageId");
                String ReceiverId=b.getString("ReceiverId");
                String participentId=b.getString("ParticipentId");
                String SenderId=b.getString("SenderIDNew");
                String ReceivId=sharedpreferences.getString("UserId","");

                boolean chk = qr.ChekMessageInMessageList(messageId);
                if (!chk) {
                    qr.insertMessageDtls(messageId, SenderId, time1, fromServerUnicodeDecoded, ThreadID, ReceivId, b.getString("SenderNameNew"), url, datet[0], sentTm,"false","false","false");
                } else {
                }
                receiveMsg=new ReceiveMessageAsyncTaskPut(getApplicationContext(),messageId,true);
                receiveMsg.execute();
                isRespondingAPI=false;
                new CountDownTimer( 15000, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }
                    public void onFinish() {
                        loading.setVisibility(View.GONE);
                        if (!isRespondingAPI)
                        {
                            if (receiveMsg != null && receiveMsg.getStatus() != AsyncTask.Status.FINISHED) {
                                receiveMsg.cancel(true);
                                Config.alertDialog(ChatMessageCenterActicity.this, "Error", "Server is not responding.Please try again after sometime.");
                            }
                        }
                    }
                }.start();
                qr.updateAllIsReadDeliver(ThreadID, "false");

                DisplayMessagesinList();


                //GetThreadMessages();
            }


            else if(update.equals("Emergency")) {

                String notType=sharedpreferences.getString("Type", "");
                if (notType.equalsIgnoreCase("FriendRequest"))
                {
                    String reqstName=sharedpreferences.getString("RequsetByName", "");
                    String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
                    Config.showacceptrejectFriendRequest(reqstName,thumbnail,ChatMessageCenterActicity.this);
                    //Config.showacceptrejectFriendRequest(reqstName,FriendListActivity.this);
                }
                else if (notType.equalsIgnoreCase("Emergency"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String trobler=sharedpreferences.getString("TroublerName", "");
                    String troblerid=sharedpreferences.getString("TroublerUserId", "");
                    Config.showEmergencyAcceptReject(msg,trobler,troblerid,ChatMessageCenterActicity.this);
                }
                else if (notType.equalsIgnoreCase("EmergencyRecipt"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("HelperUserName", "");
                    String isResch=sharedpreferences.getString("isReaching", "");
                    if (isResch.equalsIgnoreCase("true")){}
                    //showEmergencyAckAlert(newMsg, helpername);
                    Config.showEmergencyAckAlert(msg,helpername,ChatMessageCenterActicity.this);
                }
                else if (notType.equalsIgnoreCase("FriendRequestAccepted"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("AcceptByName", "");

                    Config.showAccptedRequestAlert(msg,helpername,ChatMessageCenterActicity.this);
                }
            }
            else if(update.equals("SendMessageMessage")) {

            }
            else if (update.equals("ReceptMessage"))
            {
                String receiptMessageId=b.getString("ReceiptMessageId");
                String receiptIsRead=b.getString("ReceiptIsRead");
                String receiptThreadId=b.getString("ReceiptThreadId");
                String receptIsDelivered=b.getString("ReceiptIsDeliver");
                if (!receiptThreadId.equals("")||!receiptMessageId.equals(""))
                {
                    if (receiptIsRead.equals("false")&&receptIsDelivered.equals("true")) {
                        qr.updateIsReadDeliver(receiptThreadId, receiptMessageId, receiptIsRead, receptIsDelivered);
                    }
                    else if (receptIsDelivered.equals("true")&&receiptIsRead.equals("true"))
                    {
                        qr.updateAllIsReadDeliver(receiptThreadId,"false");
                    }
                }
                DisplayMessagesinList();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.group_info_menu, menu);
        MenuItem details = menu.findItem(R.id.action_details);
      /*  if (ThreadStatus.equals("single"))
        {
            details.setVisible(false);
        }
        else
        {
            details.setVisible(true);
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("ActiveThread", "");
                edit.commit();
                finish();
                break;

            case R.id.action_details:
                Intent intent = new Intent(ChatMessageCenterActicity.this, GroupThreadDetailsActivity.class);
                Bundle b=new Bundle();
                b.putString("ThreadID",ThreadID);
                intent.putExtras(b);
                startActivity(intent);
                finish();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    //Recive the result when new Message Arrives
    class MessageResultReceiver extends ResultReceiver
    {
        public MessageResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if(resultCode == 100){
                ChatMessageCenterActicity.this.runOnUiThread(new UpdateUI("RecieveMessage",resultData));
            }
            if(resultCode == 200){
                ChatMessageCenterActicity.this.runOnUiThread(new UpdateUI("SendMessageMessage",resultData));
            }
            if(resultCode == 300){
                ChatMessageCenterActicity.this.runOnUiThread(new UpdateUI("Emergency",resultData));
            }
            if (resultCode==500){
                ChatMessageCenterActicity.this.runOnUiThread(new UpdateUI("ReceptMessage",resultData));
            }

        }
    }
    public String GetMSGTime(String time1)
    {
        String returntime;
        String tp;
        String datet[] = time1.split(" ");

        String time[] = datet[1].split(":");
        int t1 = Integer.valueOf(time[0]);
        if (t1==12)
        {
            tp = "PM";
            returntime=("" + t1 + ":" + time[1] + " " + tp);
        } else if (t1 > 12) {
            int t2 = t1-12;
            tp = "PM";
            returntime=("" + t2 + ":" + time[1] + " " + tp);
        }
        else
        {
            tp = "AM";
            returntime=(time[0] + ":" + time[1] + " " + tp);
        }
        return returntime;
    }
    public void hideEmojiEdittext()
    {
        emojiiconouter.setVisibility(View.GONE);
        submitbtn_outer.setVisibility(View.GONE);
        emojiconEditText.setVisibility(View.GONE);

        noLongerFriend.setVisibility(View.VISIBLE);
    }
    public void showEmojiEdittext()
    {
        emojiiconouter.setVisibility(View.VISIBLE);
        submitbtn_outer.setVisibility(View.VISIBLE);
        emojiconEditText.setVisibility(View.VISIBLE);

        noLongerFriend.setVisibility(View.GONE);
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d("InvokedC","onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("InvokedC","onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("InvokedC","onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("InvokedC","onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("InvokedC","onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("InvokedC","onRestart");
    }

}
