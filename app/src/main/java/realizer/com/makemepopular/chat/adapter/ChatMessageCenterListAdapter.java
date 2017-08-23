package realizer.com.makemepopular.chat.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.chat.model.ChatMessageViewListModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.GetImages;
import realizer.com.makemepopular.utils.ImageStorage;
import realizer.com.makemepopular.utils.Utility;

/**
 * Created by Win on 11/26/2015.
 */
public class ChatMessageCenterListAdapter extends BaseAdapter {


    private static ArrayList<ChatMessageViewListModel> messageList;
    private LayoutInflater mhomeworkdetails;
    private String Currentdate;
    String date;
    int counter,unreadCount;
    String userFullName="";
    int datepos;
    ViewHolder holder;
    Context context;
    String username="";
    SharedPreferences sharedpreferences;
    String threadStatus="";
    String thumbnailurl="";
    boolean isThumbnailset=false;
    private int msgCounter = 0;


    public ChatMessageCenterListAdapter(Context context, ArrayList<ChatMessageViewListModel> messageList1,int unreadCount,String threadStatus) {
        messageList = messageList1;
        mhomeworkdetails = LayoutInflater.from(context);
        Calendar c = Calendar.getInstance();
        this.unreadCount=unreadCount;
        this.threadStatus=threadStatus;
        counter = 0;
        this.context=context;
        System.out.println("Current time => " + c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Currentdate = df.format(c.getTime());
        date = Currentdate;
        datepos = -1;
        Log.d("Date", Currentdate);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        username = preferences.getString("UserName", "");
        userFullName=preferences.getString("userLoginId", "");
        thumbnailurl = preferences.getString("ThumbnailID","");

        getLastMsgDate();

    }
    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {

        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public int getViewTypeCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mhomeworkdetails.inflate(R.layout.chat_messgagecenter_list_layout, null);
            holder = new ViewHolder();
            holder.date = (TextView) convertView.findViewById(R.id.txtdate);
            holder.date.setTag(position);
            holder.sendername = (TextView) convertView.findViewById(R.id.txtsenderName);
            holder.time = (TextView) convertView.findViewById(R.id.txttime);
            holder.message = (TextView) convertView.findViewById(R.id.txtMessage);
            holder.newmessage = (LinearLayout) convertView.findViewById(R.id.linlayoutnewmsgbar);
            holder.datelayout = (LinearLayout) convertView.findViewById(R.id.linlayoutdate);
            holder.initial = (TextView) convertView.findViewById(R.id.txtinitial);
            holder.newMessageCountTxt= (TextView) convertView.findViewById(R.id.txt_newMessage_count);
            holder.newMsgCountouter= (LinearLayout) convertView.findViewById(R.id.layout_new_message);
            holder.txt_read_delivered= (TextView) convertView.findViewById(R.id.txt_read_delivered);

            holder.messageouter= (LinearLayout) convertView.findViewById(R.id.linear_message_outer);
            holder.profilepic = (ImageView) convertView.findViewById(R.id.profile_image_view);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Log.d("Date", Currentdate);
        Log.d("Date1", messageList.get(position).getSendDate());

        holder.newmessage.setVisibility(View.GONE);

        if(position==0)
        {
            if(messageList.get(position).getSendDate().equals(Currentdate))
            {
                int datetag = (Integer)holder.date.getTag();
                if (counter == 0 || datetag == datepos)
                {
                    hideShowDate(true,messageList.get(position).getSendDate());
                    counter = counter + 1;
                    datepos= (Integer)holder.date.getTag();
                }
                else
                {
                    hideShowDate(false, "");
                }
            }
            else
            {
                hideShowDate(true,messageList.get(position).getSendDate());
            }
            if (messageList.get(position).getIsNewMessage().equals("true"))
            {
                if (unreadCount==0)
                {
                    holder.newMsgCountouter.setVisibility(View.GONE);
                }
                else
                {
                    if (unreadCount==1)
                    {
                        holder.newMessageCountTxt.setText("You have " + unreadCount + " new message.");
                    }
                    else
                    {
                        holder.newMessageCountTxt.setText("You have " + unreadCount + " new messages.");
                    }
                    holder.newMsgCountouter.setVisibility(View.VISIBLE);
                    unreadCount=0;
                }
            }
            else
            {
                holder.newMsgCountouter.setVisibility(View.GONE);
            }
        }
        else if(position>0) {
            if (messageList.get(position - 1).getSendDate().equals(messageList.get(position).getSendDate()) )
            {
                hideShowDate(false, "");
            }
            else
            {
                int datetag = (Integer)holder.date.getTag();
                if (counter == 0 || datetag == datepos)
                {
                    hideShowDate(true,messageList.get(position).getSendDate());
                    counter = counter + 1;
                    datepos= (Integer)holder.date.getTag();
                }
                else
                {
                    hideShowDate(true,messageList.get(position).getSendDate());
                }

            }
            if (messageList.get(position - 1).getIsNewMessage().equals(messageList.get(position).getIsNewMessage()))
            {
                holder.newMsgCountouter.setVisibility(View.GONE);
            }
            else
            {
                if (unreadCount==0)
                {
                    holder.newMsgCountouter.setVisibility(View.GONE);
                }
                else
                {
                    if (unreadCount==1)
                    {
                        holder.newMessageCountTxt.setText("You have " + unreadCount + " new message.");
                    }
                    else {
                        holder.newMessageCountTxt.setText("You have " + unreadCount + " new messages.");
                    }
                    holder.newMsgCountouter.setVisibility(View.VISIBLE);
                    unreadCount=0;
                }
            }
            /*String currentTime=messageList.get(position-1).getTimeStamp().toString();
            String lastTime=messageList.get(position).getTimeStamp().toString();*/
        }
        String name[] = messageList.get(position).getSenderName().trim().split(" ");

        if(messageList.get(position).getSenderThumbnail() != null && !messageList.get(position).getSenderThumbnail().equals("") && !messageList.get(position).getSenderThumbnail().equalsIgnoreCase("null"))
        {
            String newURL= Utility.getURLImage(messageList.get(position).getSenderThumbnail());
            holder.initial.setVisibility(View.GONE);
            holder.profilepic.setVisibility(View.VISIBLE);
            isThumbnailset=true;
            if(!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                new GetImages(newURL,holder.profilepic,newURL.split("/")[newURL.split("/").length-1]).execute(newURL);
            else
            {
                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length - 1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                holder.initial.setVisibility(View.GONE);
                holder.profilepic.setVisibility(View.VISIBLE);
                holder.profilepic.setImageBitmap(bitmap);
            }
        }
        else {
            isThumbnailset=false;
            holder.initial.setVisibility(View.VISIBLE);
            holder.profilepic.setVisibility(View.GONE);

           if (name.length==2)
           {
               char fchar = name[0].toUpperCase().charAt(0);
               char lchar = name[1].toUpperCase().charAt(0);
               for (int i = 0; i < name.length; i++) {
                   if (!name[i].equals("") && i == 0)
                       fchar = name[i].toUpperCase().charAt(0);
                   else if (!name.equals("") && i == (name.length - 1))
                       lchar = name[i].toUpperCase().charAt(0);
               }
               holder.initial.setText(fchar + "" + lchar);
           }
            else
           {
               char fchar = name[0].toUpperCase().charAt(0);
               holder.initial.setText(fchar+"");
           }
        }
        //ParentQueriesTeacherNameListModel result=dbQ.GetQueryTableData(messageList.get(position).getTname());
        /*if (result.getFname().equals(""))
        {*/
        holder.sendername.setText(messageList.get(position).getSenderName());

        holder.time.setText(messageList.get(position).getSendTime());

        //holder.message.setText(messageList.get(position).getMessage());

        String serverResponse =messageList.get(position).getMessage();
        String fromServerUnicodeDecoded = StringEscapeUtils.unescapeJava(serverResponse);
        holder.message.setText(fromServerUnicodeDecoded);

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (threadStatus.equals("group"))
        {
            holder.txt_read_delivered.setVisibility(View.GONE);
        }
        else if (threadStatus.equals("single"))
        {
          /*  holder.txt_read_delivered.setVisibility(View.VISIBLE);*/
            if (messageList.get(position).getSenderId().equals(sharedpreferences.getString("UserId", "")))
            {
                holder.txt_read_delivered.setVisibility(View.VISIBLE);
                if (messageList.get(position).getIsDelivered().equals("true")&&messageList.get(position).getIsRead().equals("false"))
                {
                    holder.txt_read_delivered.setBackground(context.getResources().getDrawable(R.drawable.msg_delivered_img));
                } else if (messageList.get(position).getIsRead().equals("true"))
                {
                    holder.txt_read_delivered.setBackground(context.getResources().getDrawable(R.drawable.msg_read_img));
                } else
                {
                    holder.txt_read_delivered.setBackground(context.getResources().getDrawable(R.drawable.msg_sent_img));
                }
            }
            else
            {
                holder.txt_read_delivered.setVisibility(View.GONE);
            }
        }

        //****** for hour Grouping ************//

        final ChatMessageViewListModel model = messageList.get(position);
        if (position>0)
        {
            final ChatMessageViewListModel previousModel = messageList.get(position - 1);
            /*if (!model.getSenderId().toString().equalsIgnoreCase(previousModel.getSenderId().toString())) {
               *//* holder.layoutsendername.setVisibility(View.VISIBLE);
                showHideThumbnail(true, position);*//*
            } else if ((previousModel.getMessageType() != ChatMessageType.CHAT.getId()) && previousModel.getMessageType() != ChatMessageType.ATTACHMENT.getId()) { //For system messages or attachment of same user
                if (model.getMessageType() == ChatMessageType.CHAT.getId()
                        || model.getMessageType() == ChatMessageType.ATTACHMENT.getId()) {
                    holder.layoutsendername.setVisibility(View.VISIBLE);
                    showHideThumbnail(true, position);
                } else {
                    holder.layoutsendername.setVisibility(View.GONE);
                    showHideThumbnail(false, position);
                }
            } else { //For chat of same user
                holder.layoutsendername.setVisibility(View.GONE);
                showHideThumbnail(false, position);
            }*/
            if (model.getLastmsgTime().equals(null)||model.getLastmsgTime().equals("")){
              /*  holder.txtTime.setText(getDate(model.getCreatedAt(), "T"));
                holder.layoutsendername.setVisibility(View.VISIBLE);
                showHideThumbnail(true, position);*/
                HideThumbnail();
            }
            else
            {
                ShowThumbnail();
            }


        }

        return convertView;
    }


    public void ShowThumbnail()
    {
        if (!isThumbnailset)
        {
            holder.initial.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.profilepic.setVisibility(View.VISIBLE);
        }
        holder.sendername.setVisibility(View.VISIBLE);

    }
    public void HideThumbnail()
    {
        holder.profilepic.setVisibility(View.GONE);
        holder.initial.setVisibility(View.VISIBLE);
        holder.initial.setBackgroundColor(Color.WHITE);
        holder.sendername.setVisibility(View.GONE);

        /*LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)holder.messageouter.getLayoutParams();
        params.setMargins(70, 0, 0, 0);
        holder.messageouter.setLayoutParams(params);*/
    }

    public void hideShowDate(boolean flag,String setdate)
    {
        if(flag)
        {
            String qdate[]=setdate.split("-");
            String newDate=qdate[0]+"/"+qdate[1]+"/"+qdate[2];
            ViewGroup.LayoutParams layoutParams = holder.datelayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.datelayout.setLayoutParams(layoutParams);
            holder.date.setText(Config.getDate(newDate, "D"));
            date = newDate;

           /* LinearLayout.LayoutParams layoutmargin = (LinearLayout.LayoutParams)holder.layoutallview.getLayoutParams();
            layoutmargin.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutmargin.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutmargin.setMargins(0,20,0,0);
            holder.layoutallview.setLayoutParams(layoutmargin);*/
        }
        else
        {
            ViewGroup.LayoutParams layoutParams = holder.datelayout.getLayoutParams();
            layoutParams.width = 0;
            layoutParams.height = 0;
            holder.datelayout.setLayoutParams(layoutParams);

            /*LinearLayout.LayoutParams layoutmargin = (LinearLayout.LayoutParams)holder.layoutallview.getLayoutParams();
            layoutmargin.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutmargin.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutmargin.setMargins(0,0,0,0);
            holder.layoutallview.setLayoutParams(layoutmargin);*/
        }
    }

    static class ViewHolder
    {
        TextView date,sendername,time,initial,message,newMessageCountTxt,txt_read_delivered;
        LinearLayout newmessage,datelayout,messageouter,newMsgCountouter;
        ImageView profilepic;
    }
    public long GetMiliSec(String dates)
    {
        long Sec=0;
        SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        try {
            Date date=outputFmt.parse(dates);
            Sec=date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Sec;
    }
    private void getLastMsgDate() {
        Log.i("CHAT", "getLastMsgDate(), START");
        msgCounter = 0;
        try {
            int size = messageList.size();
            for (int i = 0; i < size; i++) {
                if (i > 0) { //Condition satisfy if index greater than 0.
                    try {
                        //Condition satisfy if current user and previous users are same. And belong into same section.
                        if ((messageList.get(i).getSenderId().toString()).equals(messageList.get(i - 1).getSenderId().toString())) {

                            //Fetching previous set time by msgcounter index.
                            long lastMsgSec = GetMiliSec(messageList.get(msgCounter).getTimeStamp());
                            long currentSec = GetMiliSec(messageList.get(i).getTimeStamp());

                            //assign time difference between previous msg time with current message time.
                            double time = (double) ((currentSec - lastMsgSec) / 1000);

                            /*double timespan = time;
                            if (time >= 60) {
                                timespan = (time / 60);
                                if (timespan >= 60) {
                                    timespan = (time / 60) / 60;
                                } else {
                                    timespan = 1;
                                }
                            } else {
                                timespan = 1;
                            }*/

                            //Condition satisfy if time greater than 1 hour. Here comparing in seconds (3600 secends)
                            if (time > 3600) {
                                msgCounter = i;
                                messageList.get(i).setLastmsgTime(messageList.get(i).getTimeStamp());
                            } else {

                              /*  if ((lstmessage.get(i - 1).getMessageType() != ChatMessageType.CHAT.getId() && lstmessage.get(i - 1).getMessageType() != ChatMessageType.ATTACHMENT.getId())
                                        && (lstmessage.get(i).getMessageType() == ChatMessageType.ATTACHMENT.getId() || lstmessage.get(i).getMessageType() == ChatMessageType.CHAT.getId())) {
                                    msgCounter = i;
                                    lstmessage.get(i).setLastmsgTime(lstmessage.get(i).getCreatedAt());
                                    //Log.i("TIME", "LastMessageTime: "+i+" "+getDate(lstmessage.get(i).getCreatedAt(), "T"));
                                } else {
                                    if (lstmessage.get(i).getMessageType() == ChatMessageType.ATTACHMENT.getId() || lstmessage.get(i).getMessageType() == ChatMessageType.CHAT.getId()) {
                                        //lstmessage.get(i).setLastmsgTime(lstmessage.get(i).getCreatedAt());
                                    } else { //For system messages.
                                        //lstmessage.get(i).setLastmsgTime(lstmessage.get(i).getCreatedAt());
                                    }
                                }*/
                            }
                        } else {
                            msgCounter = i;
                            messageList.get(i).setLastmsgTime(messageList.get(i).getTimeStamp());
                            /*if (lstmessage.get(i).getMessageType() == (ChatMessageType.CHAT.getId()) || lstmessage.get(i).getMessageType() == (ChatMessageType.ATTACHMENT.getId())) {
                                msgCounter = i;
                                lstmessage.get(i).setLastmsgTime(lstmessage.get(i).getCreatedAt());
                                //Log.i("TIME", "LastMessageTime: "+i+" "+getDate(lstmessage.get(i).getCreatedAt(), "T"));
                            } else { //For system messages.
                                //lstmessage.get(i).setLastmsgTime(lstmessage.get(i).getCreatedAt());
                            }*/
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else { //Condition only satisfy for 0th item.
                    msgCounter = i;
                    messageList.get(i).setLastmsgTime(messageList.get(i).getTimeStamp());
                   /* if (lstmessage.get(i).getMessageType() == (ChatMessageType.CHAT.getId()) || lstmessage.get(i).getMessageType() == (ChatMessageType.ATTACHMENT.getId())) {
                        msgCounter = i;
                        lstmessage.get(i).setLastmsgTime(lstmessage.get(i).getCreatedAt());
                        //Log.i("TIME", "LastMessageTime: "+i+" "+getDate(lstmessage.get(i).getCreatedAt(), "T"));
                    } else { //For system messages.
                        //lstmessage.get(i).setLastmsgTime(lstmessage.get(i).getCreatedAt());
                    }*/
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("CHAT", "getLastMsgDate(), END");
    }
}

