package realizer.com.makemepopular.backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

import realizer.com.makemepopular.chat.adapter.ChatThreadListModelAdapter;
import realizer.com.makemepopular.chat.model.ChatMessageSendModel;
import realizer.com.makemepopular.chat.model.ChatMessageViewListModel;
import realizer.com.makemepopular.chat.model.ChatThreadListModel;
import realizer.com.makemepopular.chat.model.ChatUserIdDetailsListModel;
import realizer.com.makemepopular.chat.model.NewFriendListModel;


/**
 * Created by Win on 12/21/2015.
 */
public class DatabaseQueries
{
    SQLiteDatabase db;
    Context context;
    String UserD[];
    String scode;

    public DatabaseQueries(Context context) {
        this.context = context;
        SQLiteOpenHelper myHelper = SqliteHelper.getInstance(context);
        this.db = myHelper.getWritableDatabase();
    }

    // Insert Userlist
    public long insertUserLIst(String userid,String fname,String thumbnailUrl)
    {
        ContentValues conV = new ContentValues();
        conV.put("userId", userid);
        conV.put("firstName", fname);
        conV.put("thumbnailUrl", thumbnailUrl);
        long newRowInserted = db.insert("UserList", null, conV);
        return newRowInserted;
    }

    // Insert UserInfo
    public long insertUserInfo(String userid,String fullname,String initial,String thumbnailUrl)
    {
        ContentValues conV = new ContentValues();
        conV.put("userId", userid);
        conV.put("firstFullName", fullname);
        conV.put("userInitial", initial);
        conV.put("thumbnailUrl", thumbnailUrl);
        long newRowInserted = db.insert("UserInfo", null, conV);
        return newRowInserted;
    }

    // Check User in List Details
    public boolean ChekUserInList(String userId)
    {
        boolean chek=false;
        Cursor c = db.rawQuery("SELECT * FROM UserList", null);
        String listUserid="";
        Log.d("LENGTH CURSOR", "" + c.getCount());
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    listUserid = c.getString(c.getColumnIndex("userId"));
                    if (listUserid.equals(userId))
                    {
                        chek=true;
                        break;
                    }
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return chek;
    }



    public ArrayList<ChatUserIdDetailsListModel> GetQueryTableData() {
        Cursor c = db.rawQuery("SELECT * FROM UserList", null);
        ArrayList<ChatUserIdDetailsListModel> result = new ArrayList<>();
        Log.d("LENGTH CURSOR", "" + c.getCount());
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    ChatUserIdDetailsListModel o = new ChatUserIdDetailsListModel();
                    o.setFname(c.getString(c.getColumnIndex("firstName")));
                    o.setUserid(c.getString(c.getColumnIndex("userId")));
                    o.setLname(c.getString(c.getColumnIndex("lastName")));
                    o.setThumbnail(c.getString(c.getColumnIndex("thumbnailUrl")));
                    result.add(o);
                    cnt = cnt+1;
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return result;
    }

    //Insert Query Information
    public long insertQuery(String fromUserid,String fromName,String sendName,String senduserId,String text,String dtime,String flag,Date sentDate)
    {
        ContentValues conV = new ContentValues();
        conV.put("fromUserId", fromUserid);
        conV.put("fromName", fromName);
        conV.put("sendName", sendName);
        conV.put("sendUserId",senduserId);
        conV.put("msg", text);
        conV.put("sentTime", dtime);
        conV.put("HasSyncedUp", flag);
        conV.put("sentDate", sentDate.getTime());
        long newRowInserted = db.insert("Query", null, conV);
        return newRowInserted;
    }

    // get ID
    public int getQueryId() {
        Cursor c = db.rawQuery("SELECT QueryId FROM Query ", null);
        int cnt = 1;
        int att=0;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {

                    att = c.getInt(0);
                    cnt = cnt+1;
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return att;
    }


    //Insert Queue Infromation
    public long insertQueue(int id,String type,String priority,String time) {
        ContentValues conV = new ContentValues();
        conV.put("Id", id);
        conV.put("Type", type);
        conV.put("SyncPriority", priority);
        conV.put("Time", time);
        long newRowInserted = db.insert("SyncUPQueue", null, conV);
        return newRowInserted;
    }


    //select Query
    public ChatMessageSendModel GetQuery(int id) {
        Cursor c = db.rawQuery("SELECT * FROM Query WHERE QueryId="+id, null);
        ChatMessageSendModel o = new ChatMessageSendModel();
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    o.setConversationId(c.getInt(c.getColumnIndex("QueryId")));
                    o.setFromName(c.getString(c.getColumnIndex("fromName")));
                    o.setSentTime(c.getString(c.getColumnIndex("sentTime")));
                    o.setFromUserId(c.getString(c.getColumnIndex("fromUserId")));
                    o.setSendtoName(c.getString(c.getColumnIndex("sendName")));
                    o.setSendtouserId(c.getString(c.getColumnIndex("sendUserId")));
                    o.setText(c.getString(c.getColumnIndex("msg")));
                    o.setHassync(c.getString(c.getColumnIndex("HasSyncedUp")));
                    cnt = cnt+1;
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return o;
    }

    public void deleteAllData()
    {
        long deleterow =0;
        deleterow = db.delete("UserInfo",null, null);
        deleterow = db.delete("InitiatedChat",null, null);
        deleterow = db.delete("Query",null, null);
        deleterow = db.delete("SyncUPQueue",null, null);
        deleterow = db.delete("ThreadList",null, null);
        deleterow = db.delete("MessageDtls",null, null);
        deleterow = db.delete("FriendList",null, null);
        deleterow = db.delete("UserList",null, null);
    }

    public Long deleteChatData()
    {
        long deleterow =0;
        deleterow= db.delete("UserList",null, null);
        deleterow=db.delete("ThreadList",null, null);
        deleterow=db.delete("MessageDtls",null, null);
        return deleterow;
    }



    /******* Friend List Offlice Table Details *******/

    // Insert Friend Details in FriendList
    public long insertFriendList(String FriendId,String FriendName,String isEmergencyAlert,String isMessagingAllowed,String isTrackingAllowed,String FriendThumbnail,String status,String isRequestSent,String CreateTs,String allowTrackingTillDate,String trackingStatusChangeDate,String isDeleted,String LastActive,String DOB,String age,String Gender,String Email)
    {
        ContentValues conV = new ContentValues();
        conV.put("friendId", FriendId);
        conV.put("friendName", FriendName);
        conV.put("isEmergencyAlert", isEmergencyAlert);
        conV.put("isMessageAllowed", isMessagingAllowed);
        conV.put("isTrackingAllowed", isTrackingAllowed);
        conV.put("friendThumbnail", FriendThumbnail);
        conV.put("status", status);
        conV.put("isRequestSent", isRequestSent);
        conV.put("createTs", CreateTs);
        conV.put("allowTrackingTillDate", allowTrackingTillDate);
        conV.put("trackingStatusChangeDate", trackingStatusChangeDate);
        conV.put("isDeleted",isDeleted);
        conV.put("LastActive",LastActive);
        conV.put("DOB",DOB);
        conV.put("age",age);
        conV.put("Gender",Gender);
        conV.put("Email",Email);
        long newRowInserted = db.insert("FriendList", null, conV);
        return newRowInserted;
    }


    //getting emergency contacts
    public ArrayList<NewFriendListModel> getAllEmergencyConatcts() {
        String emergency="true";
        Cursor c = db.rawQuery("SELECT * FROM FriendList WHERE isEmergencyAlert='"+emergency+"' order by friendName ASC", null);
        ArrayList<NewFriendListModel> result = new ArrayList<>();
        Log.d("LENGTH CURSOR", "" + c.getCount());
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    NewFriendListModel o=new NewFriendListModel();
                    o.setFriendsId(c.getString(c.getColumnIndex("friendId")));
                    o.setFriendName(c.getString(c.getColumnIndex("friendName")));
                    o.setIsEmergencyAlert(c.getString(c.getColumnIndex("isEmergencyAlert")));
                    o.setIsMessagingAllowed(c.getString(c.getColumnIndex("isMessageAllowed")));
                    o.setIsTrackingAllowed(c.getString(c.getColumnIndex("isTrackingAllowed")));
                    o.setFriendThumbnailUrl(c.getString(c.getColumnIndex("friendThumbnail")));
                    o.setStatus(c.getString(c.getColumnIndex("status")));
                    o.setIsRequestSent(c.getString(c.getColumnIndex("isRequestSent")));
                    o.setCreateTS(c.getString(c.getColumnIndex("createTs")));
                    o.setAllowTrackingTillDate(c.getString(c.getColumnIndex("allowTrackingTillDate")));
                    o.setTrackingStatusChangeDate(c.getString(c.getColumnIndex("trackingStatusChangeDate")));
                    o.setIsDeleted(c.getString(c.getColumnIndex("isDeleted")));
                    result.add(o);
                    cnt = cnt+1;
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return result;
    }

    public ArrayList<ChatUserIdDetailsListModel> GetFriendFromFriendList(String status,String ismess) {
        Cursor c = db.rawQuery("SELECT * FROM FriendList where status='"+status+"' AND isMessageAllowed='"+ismess+"'", null);
        ArrayList<ChatUserIdDetailsListModel> result = new ArrayList<>();
        Log.d("LENGTH CURSOR", "" + c.getCount());
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst())
            {
                System.out.print("while moving  - C != null");
                do {
                    ChatUserIdDetailsListModel o = new ChatUserIdDetailsListModel();
                    o.setFname(c.getString(c.getColumnIndex("friendName")));
                    o.setUserid(c.getString(c.getColumnIndex("friendId")));
                    o.setThumbnail(c.getString(c.getColumnIndex("friendThumbnail")));
                    result.add(o);
                    cnt = cnt+1;
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return result;
    }

    public ArrayList<NewFriendListModel> getAllFriendsData() {
        Cursor c = db.rawQuery("SELECT * FROM FriendList", null);
        ArrayList<NewFriendListModel> result = new ArrayList<>();
        Log.d("LENGTH CURSOR", "" + c.getCount());
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    NewFriendListModel o=new NewFriendListModel();
                    o.setFriendsId(c.getString(c.getColumnIndex("friendId")));
                    o.setFriendName(c.getString(c.getColumnIndex("friendName")));
                    o.setIsEmergencyAlert(c.getString(c.getColumnIndex("isEmergencyAlert")));
                    o.setIsMessagingAllowed(c.getString(c.getColumnIndex("isMessageAllowed")));
                    o.setIsTrackingAllowed(c.getString(c.getColumnIndex("isTrackingAllowed")));
                    o.setFriendThumbnailUrl(c.getString(c.getColumnIndex("friendThumbnail")));
                    o.setStatus(c.getString(c.getColumnIndex("status")));
                    o.setIsRequestSent(c.getString(c.getColumnIndex("isRequestSent")));
                    o.setCreateTS(c.getString(c.getColumnIndex("createTs")));
                    o.setAllowTrackingTillDate(c.getString(c.getColumnIndex("allowTrackingTillDate")));
                    o.setTrackingStatusChangeDate(c.getString(c.getColumnIndex("trackingStatusChangeDate")));
                    o.setIsDeleted(c.getString(c.getColumnIndex("isDeleted")));
                    o.setLastActive(c.getString(c.getColumnIndex("LastActive")));
                    o.setDob(c.getString(c.getColumnIndex("DOB")));
                    o.setAge(c.getString(c.getColumnIndex("age")));
                    o.setEmail(c.getString(c.getColumnIndex("Email")));
                    o.setGender(c.getString(c.getColumnIndex("Gender")));
                    result.add(o);
                    cnt = cnt+1;
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return result;
    }

    //select Query
    public NewFriendListModel getSingleFriend(String friendId) {
        Cursor c = db.rawQuery("SELECT * FROM FriendList WHERE friendId='"+friendId+"'", null);
        NewFriendListModel o = new NewFriendListModel();
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    o.setFriendsId(c.getString(c.getColumnIndex("friendId")));
                    o.setFriendName(c.getString(c.getColumnIndex("friendName")));
                    o.setIsEmergencyAlert(c.getString(c.getColumnIndex("isEmergencyAlert")));
                    o.setIsMessagingAllowed(c.getString(c.getColumnIndex("isMessageAllowed")));
                    o.setIsTrackingAllowed(c.getString(c.getColumnIndex("isTrackingAllowed")));
                    o.setFriendThumbnailUrl(c.getString(c.getColumnIndex("friendThumbnail")));
                    o.setStatus(c.getString(c.getColumnIndex("status")));
                    o.setIsRequestSent(c.getString(c.getColumnIndex("isRequestSent")));
                    o.setCreateTS(c.getString(c.getColumnIndex("createTs")));
                    o.setAllowTrackingTillDate(c.getString(c.getColumnIndex("allowTrackingTillDate")));
                    o.setTrackingStatusChangeDate(c.getString(c.getColumnIndex("trackingStatusChangeDate")));
                    o.setIsDeleted(c.getString(c.getColumnIndex("isDeleted")));
                    o.setLastActive(c.getString(c.getColumnIndex("LastActive")));
                    o.setDob(c.getString(c.getColumnIndex("DOB")));
                    o.setAge(c.getString(c.getColumnIndex("age")));
                    o.setEmail(c.getString(c.getColumnIndex("Email")));
                    o.setGender(c.getString(c.getColumnIndex("Gender")));
                    cnt = cnt+1;
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return o;
    }


    //check FriendId in FriendList table
    public boolean ChekFriendinFrndList(String friendId)
    {
        boolean chek=false;
        Cursor c = db.rawQuery("SELECT * FROM FriendList where friendId='"+friendId+"'", null);
        String listUserid="";
        Log.d("LENGTH CURSOR", "" + c.getCount());
        int cnt = 1;
        if (c != null) {
           /* if (c.getCount()>0)
            {
                String status=c.getString(c.getColumnIndex("status"));
                if (status.equals("Accepted"))
                {
                    chek=true;
                }
                else
                {
                    chek=false;
                }
            }*/
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    listUserid = c.getString(c.getColumnIndex("friendId"));
                    if (listUserid.equals(friendId))
                    {
                        chek=true;
                    }
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return chek;
    }

    //Update Friend Detail in FriendList
    public long updateFriendLIst(String FriendId,String FriendName,String isEmergencyAlert,String isMessagingAllowed,String isTrackingAllowed,String FriendThumbnail,String status,String isRequestSent,String CreateTs,String allowTrackingTillDate,String trackingStatusChangeDate,String isDeleted,String LastActive,String DOB,String age,String Gender,String Email)
    {
        ContentValues conV = new ContentValues();
        conV.put("isEmergencyAlert", isEmergencyAlert);
        conV.put("isMessageAllowed", isMessagingAllowed);
        conV.put("isTrackingAllowed", isTrackingAllowed);
        conV.put("friendThumbnail", FriendThumbnail);
        conV.put("status", status);
        conV.put("createTs", CreateTs);
        conV.put("allowTrackingTillDate", allowTrackingTillDate);
        conV.put("trackingStatusChangeDate", trackingStatusChangeDate);
        conV.put("isDeleted", isDeleted);
        conV.put("LastActive",LastActive);
        conV.put("DOB",DOB);
        conV.put("age",age);
        conV.put("Gender",Gender);
        conV.put("Email",Email);
        long newRowInserted = db.update("FriendList", conV, "friendId='"+FriendId+"'",null);
        return newRowInserted;
    }

    public int chekFriendTable()
    {
        int count=0;
        Cursor cur = db.rawQuery("SELECT * FROM FriendList", null);
        if (cur != null && cur.moveToFirst()) {
            count=cur.getCount();
        }
        cur.close();
        return count;
    }
    public String getCreateTsFriend()
    {
        String time="";
        Cursor c = db.rawQuery("SELECT createTs FROM FriendList ORDER BY createTs DESC", null);
        Log.d("LENGTH CURSOR", "" + c.getCount());
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                    time = c.getString(c.getColumnIndex("createTs"));
            }
        }
        c.close();
        return time;
    }


    /******* New Chat Offline Database Details *******/

    //Insert Threads
    public long insertThread(String threadId,String threadName,String thumbnailURL,String lastsenderName,String unreadCount,String timestamp,String lastsenderById,String lastmessageId,String lastMessageText,String initiatedId, String initiateName, String participentList,String threadCustomName,String isLastmsgRead,String isLstMsgDeliver)
    {
        ContentValues conV = new ContentValues();
        conV.put("threadId",threadId);
        conV.put("threadName",threadName);
        conV.put("thumbnailUrl",thumbnailURL);
        conV.put("lastsendername",lastsenderName);
        conV.put("unreadCount",unreadCount);
        conV.put("timeStamp",timestamp);
        conV.put("lastsenderByid",lastsenderById);
        conV.put("lastMessageId",lastmessageId);
        conV.put("lastMessageText",lastMessageText);
        conV.put("initiatedId",initiatedId);
        conV.put("initiateName",initiateName);
        conV.put("participentList", participentList);
        conV.put("threadCustomName", threadCustomName);
        conV.put("isLastMsgRead",isLastmsgRead);
        conV.put("isLastMsgDeliver",isLstMsgDeliver);
        long newRowInserted = db.insert("ThreadList", null, conV);
        return newRowInserted;
    }
    //update ThreadList Display Message
    public long updateThreadDispMessage(String threadId,String lastsenderName,String lastMessageText,String TimeStamp,String unreadCnt,String thumbnailURL,String threadCustomName,String isLastmsgRead,String isLstMsgDeliver,String lastsenderById)
    {
        ContentValues conV = new ContentValues();
        conV.put("lastsendername",lastsenderName);
        conV.put("lastMessageText",lastMessageText);
        conV.put("thumbnailUrl",thumbnailURL);
        conV.put("timeStamp",TimeStamp);
        conV.put("lastsenderByid",lastsenderById);
        conV.put("unreadCount",unreadCnt);
        conV.put("threadCustomName",threadCustomName);
        conV.put("isLastMsgRead",isLastmsgRead);
        conV.put("isLastMsgDeliver",isLstMsgDeliver);
        long newRowInserted = db.update("ThreadList", conV, "threadId='" + threadId + "'", null);
        return newRowInserted;
    }

    //update ThreadList Display Message
    public long updateThreadname(String threadId,String threadCustomName)
    {
        ContentValues conV = new ContentValues();
        conV.put("threadCustomName",threadCustomName);
        long newRowInserted = db.update("ThreadList", conV, "threadId='" + threadId + "'", null);
        return newRowInserted;
    }

    public long updateThreadUnreadCount(String threadId)
    {
        ContentValues conV = new ContentValues();
        conV.put("unreadCount","0");
        long newRowInserted = db.update("ThreadList", conV, "threadId='" + threadId + "'", null);
        return newRowInserted;
    }

    // Check Thread in ThreadList
    public boolean ChekThreadInThreadList(String ThreadId)
    {
        boolean chek=false;
        Cursor c = db.rawQuery("SELECT * FROM ThreadList", null);
        String listThreadid="";
        Log.d("LENGTH CURSOR", "" + c.getCount());
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    listThreadid = c.getString(c.getColumnIndex("threadId"));
                    if (listThreadid.equals(ThreadId))
                    {
                        chek=true;
                    }
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return chek;
    }

    //get Thread List
    public ArrayList<ChatThreadListModel> getThreadList() {
        ArrayList<ChatThreadListModel> list=new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM ThreadList ORDER BY timeStamp DESC", null);
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    ChatThreadListModel o = new ChatThreadListModel();
                    o.setThreadId(c.getString(c.getColumnIndex("threadId")));
                    o.setThreadName(c.getString(c.getColumnIndex("threadName")));
                    o.setProfileImg(c.getString(c.getColumnIndex("thumbnailUrl")));
                    o.setLastSenderName(c.getString(c.getColumnIndex("lastsendername")));
                    o.setUnreadCount(c.getInt(c.getColumnIndex("unreadCount")));
                    o.setTimeStamp(c.getString(c.getColumnIndex("timeStamp")));
                    o.setLastSenderId(c.getString(c.getColumnIndex("lastsenderByid")));
                    o.setLastMessageId(c.getString(c.getColumnIndex("lastMessageId")));
                    o.setLastMessageText(c.getString(c.getColumnIndex("lastMessageText")));
                    o.setInitiateId(c.getString(c.getColumnIndex("initiatedId")));
                    o.setInitiateName(c.getString(c.getColumnIndex("initiateName")));
                    o.setParticipentID(c.getString(c.getColumnIndex("participentList")));
                    o.setCustomThreadName(c.getString(c.getColumnIndex("threadCustomName")));
                    o.setIsLastMsgRead(c.getString(c.getColumnIndex("isLastMsgRead")));
                    o.setIsLastMsgDeliver(c.getString(c.getColumnIndex("isLastMsgDeliver")));
                    list.add(o);
                    cnt = cnt+1;
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return list;
    }

    //Insert Messages Details
    public long insertMessageDtls(String messageId,String senderId,String timeStamp,String message,String threadId,String receiverId,String SenderName,String senderThumbnail,String sendDate,String sendTime,String isnewMessage,String isDeliver,String isRead)
    {
        ContentValues conV = new ContentValues();
        conV.put("messageId",messageId);
        conV.put("senderId ",senderId);
        conV.put("timeStamp",timeStamp);
        conV.put("message",message);
        conV.put("threadId",threadId);
        conV.put("receiverId",receiverId);
        conV.put("SenderName",SenderName);
        conV.put("senderThumbnail",senderThumbnail);
        conV.put("sendDate",sendDate);
        conV.put("sendTime", sendTime);
        conV.put("isNewMessage", isnewMessage);
        conV.put("isDeliver",isDeliver);
        conV.put("isRead", isRead);
        conV.put("hasSyncUp","false");
        long newRowInserted = db.insert("MessageDtls", null, conV);
        return newRowInserted;
    }

    public long updateMessageDtls(String messageId,String senderId,String timeStamp,String message,String threadId,String receiverId,String SenderName,String senderThumbnail,String sendDate,String sendTime,String isnewMessage,String isDeliver,String isRead)
    {
        ContentValues conV = new ContentValues();
        conV.put("senderId ",senderId);
        conV.put("timeStamp",timeStamp);
        conV.put("message",message);
        conV.put("threadId",threadId);
        conV.put("receiverId",receiverId);
        conV.put("SenderName",SenderName);
        conV.put("senderThumbnail",senderThumbnail);
        conV.put("sendDate",sendDate);
        conV.put("sendTime", sendTime);
        conV.put("isNewMessage", isnewMessage);
        conV.put("isDeliver",isDeliver);
        conV.put("isRead",isRead);
        long newRowInserted = db.update("MessageDtls", conV, "messageId='" + messageId + "'", null);
        return newRowInserted;
    }

    public long updateSyncUpFlag(ChatMessageViewListModel obj)
    {
        ContentValues conV = new ContentValues();
        conV.put("senderId ",obj.getSenderId());
        conV.put("timeStamp",obj.getTimeStamp());
        conV.put("message",obj.getMessage());
        conV.put("threadId",obj.getThreadId());
        conV.put("receiverId",obj.getReceiverId());
        conV.put("SenderName",obj.getSenderName());
        conV.put("senderThumbnail",obj.getSenderThumbnail());
        conV.put("sendDate",obj.getSendDate());
        conV.put("sendTime", obj.getSendTime());
        conV.put("isNewMessage", obj.getIsNewMessage());
        conV.put("isDeliver",obj.getIsDelivered());
        conV.put("isRead",obj.getIsRead());
        conV.put("hasSyncUp","true");
        long newRowInserted = db.update("MessageDtls", conV, "messageId='" + obj.getMessageId() + "'", null);
        return newRowInserted;
    }

    public long updateMessageId(String messageId,String sendDate,String SentTime)
    {
        ContentValues conV = new ContentValues();
        conV.put("messageId",messageId);
        long newRowInserted = db.update("MessageDtls", conV, "sendDate='" + sendDate + "' AND sendTime='" + SentTime + "'", null);
        return newRowInserted;
    }
    public long updateThreadId(String threadId,String sendDate,String SentTime)
    {
        ContentValues conV = new ContentValues();
        conV.put("threadId",threadId);
        long newRowInserted = db.update("MessageDtls", conV, "sendDate='" + sendDate + "' AND sendTime='" + SentTime + "'", null);
        return newRowInserted;
    }
    //update Isnew Message to false
    public long updateisNewMessage(String threadId,String isnewMessage)
    {
        ContentValues conV = new ContentValues();
        conV.put("isNewMessage","false");
        long newRowInserted = db.update("MessageDtls", conV, "isNewMessage='" + isnewMessage + "' AND threadId='" + threadId + "'", null);
        return newRowInserted;
    }
    public long updateIsReadDeliver(String threadId,String messageid,String isRead,String isDeliver)
    {
        ContentValues conV = new ContentValues();
        conV.put("isDeliver",isDeliver);
        conV.put("isRead",isRead);
        long newRowInserted = db.update("MessageDtls", conV, "messageId='" + messageid + "' AND threadId='" + threadId + "'", null);
        return newRowInserted;
    }
    public long updateAllIsReadDeliver(String threadId,String isRead)
    {
        ContentValues conV = new ContentValues();
        conV.put("isDeliver","true");
        conV.put("isRead","true");
        long newRowInserted = db.update("MessageDtls", conV, "isRead='" + isRead + "' AND threadId='" + threadId + "'", null);
        return newRowInserted;
    }
    // Check Message in MessageList
    public boolean ChekMessageInMessageList(String MessageId)
    {
        boolean chek=false;
        Cursor c = db.rawQuery("SELECT * FROM MessageDtls", null);
        String ListMessagID="";
        Log.d("LENGTH CURSOR", "" + c.getCount());
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    ListMessagID = c.getString(c.getColumnIndex("messageId"));
                    if (ListMessagID.equals(MessageId))
                    {
                        chek=true;
                        break;
                    }
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return chek;
    }
    //Get New Message Count From Table
    public int getNewMessageCount(String threadId)
    {
        int count=0;
        String message="true";
        Cursor c = db.rawQuery("SELECT * FROM MessageDtls where isNewMessage='"+message+"' AND threadId='"+threadId+"'", null);
        Log.d("LENGTH CURSOR", "" + c.getCount());
        int cnt = 1;
        if (c != null && c.moveToFirst())
        {
            count = c.getCount();
        }
        c.close();
        return count;
    }

    //Get Thread Messages
    public ArrayList<ChatMessageViewListModel> getThreadMessage(String ThreadId) {
        Cursor c = db.rawQuery("SELECT * FROM MessageDtls WHERE threadId='"+ThreadId+"' order By timeStamp ASC", null);
        ArrayList<ChatMessageViewListModel> chatMessages=new ArrayList<>();
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    ChatMessageViewListModel o = new ChatMessageViewListModel();
                    o.setMessageId(c.getString(c.getColumnIndex("messageId")));
                    o.setSenderId(c.getString(c.getColumnIndex("senderId")));
                    o.setTimeStamp(c.getString(c.getColumnIndex("timeStamp")));
                    o.setMessage(c.getString(c.getColumnIndex("message")));
                    o.setThreadId(c.getString(c.getColumnIndex("threadId")));
                    o.setReceiverId(c.getString(c.getColumnIndex("receiverId")));
                    o.setSenderName(c.getString(c.getColumnIndex("SenderName")));
                    o.setSenderThumbnail(c.getString(c.getColumnIndex("senderThumbnail")));
                    o.setSendDate(c.getString(c.getColumnIndex("sendDate")));
                    o.setSendTime(c.getString(c.getColumnIndex("sendTime")));
                    o.setIsNewMessage(c.getString(c.getColumnIndex("isNewMessage")));
                    o.setIsDelivered(c.getString(c.getColumnIndex("isDeliver")));
                    o.setIsRead(c.getString(c.getColumnIndex("isRead")));
                    chatMessages.add(o);
                    cnt = cnt+1;
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return chatMessages;
    }

    public ArrayList<ChatMessageViewListModel> getSyncupFalseThreadMessage() {
        Cursor c = db.rawQuery("SELECT * FROM MessageDtls WHERE hasSyncUp='false' order By timeStamp ASC", null);
        ArrayList<ChatMessageViewListModel> chatMessages=new ArrayList<>();
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    ChatMessageViewListModel o = new ChatMessageViewListModel();
                    o.setMessageId(c.getString(c.getColumnIndex("messageId")));
                    o.setSenderId(c.getString(c.getColumnIndex("senderId")));
                    o.setTimeStamp(c.getString(c.getColumnIndex("timeStamp")));
                    o.setMessage(c.getString(c.getColumnIndex("message")));
                    o.setThreadId(c.getString(c.getColumnIndex("threadId")));
                    o.setReceiverId(c.getString(c.getColumnIndex("receiverId")));
                    o.setSenderName(c.getString(c.getColumnIndex("SenderName")));
                    o.setSenderThumbnail(c.getString(c.getColumnIndex("senderThumbnail")));
                    o.setSendDate(c.getString(c.getColumnIndex("sendDate")));
                    o.setSendTime(c.getString(c.getColumnIndex("sendTime")));
                    o.setIsNewMessage(c.getString(c.getColumnIndex("isNewMessage")));
                    o.setIsDelivered(c.getString(c.getColumnIndex("isDeliver")));
                    o.setIsRead(c.getString(c.getColumnIndex("isRead")));
                    chatMessages.add(o);
                    cnt = cnt+1;
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return chatMessages;
    }

    //Delete User From List
    public long deleteUserFromTable(String userid)
    {
        long deleteUser = db.delete("FriendList", "friendId='" + userid + "'", null);
        return deleteUser;
    }

    public long deleteMessageFromTable(String userid)
    {
        long deleteUser = db.delete("MessageDtls", "messageId='" + userid + "'", null);
        return deleteUser;
    }

    public String getUserIsPending(String userid)
    {
        boolean chek=false;
        Cursor c = db.rawQuery("SELECT status FROM FriendList where friendId='"+userid+"'", null);
        String status="";
        Log.d("LENGTH CURSOR", "" + c.getCount());
        int cnt = 1;
        if (c != null)
        {
            if (c.moveToFirst())
            {
                System.out.print("while moving  - C != null");
                do {
                    status = c.getString(c.getColumnIndex("status"));
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return status;
    }

    //get Last Message TimeStamp from Table
    public String getLastMsgTimeStamp(String threadId)
    {
        String lastMsgTm="";
        Cursor c = db.rawQuery("SELECT timeStamp FROM MessageDtls where threadId='"+threadId+"' order by timeStamp ASC", null);
        Log.d("LENGTH CURSOR", "" + c.getCount());
        if (c != null) {
            if (c.moveToLast()) {
                System.out.print("while moving  - C != null");
                    lastMsgTm = c.getString(c.getColumnIndex("timeStamp"));
            }
        }
        c.close();
        return lastMsgTm;
    }
    public String getLastThreadTime()
    {
        String lastMsgTm="";
        Cursor c = db.rawQuery("SELECT timeStamp FROM ThreadList order by timeStamp ASC", null);
        Log.d("LENGTH CURSOR", "" + c.getCount());
        if (c != null) {
            if (c.moveToLast()) {
                System.out.print("while moving  - C != null");
                do {
                    lastMsgTm = c.getString(c.getColumnIndex("timeStamp"));
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return lastMsgTm;
    }
}
