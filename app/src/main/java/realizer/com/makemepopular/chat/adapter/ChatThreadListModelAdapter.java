package realizer.com.makemepopular.chat.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.chat.model.ChatThreadListModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.GetImages;
import realizer.com.makemepopular.utils.ImageStorage;
import realizer.com.makemepopular.utils.Utility;


/**
 * Created by Win on 11/20/2015.
 */
public class ChatThreadListModelAdapter extends BaseAdapter {


    private static ArrayList<ChatThreadListModel> pList;
    private LayoutInflater publicholidayDetails;
    private Context context1;
    boolean isImageFitToScreen;
    SharedPreferences sharedpreferences;
    View convrtview;
    //PhotoViewAttacher mAttacher;
    private String Currentdate;


    public ChatThreadListModelAdapter(Context context, ArrayList<ChatThreadListModel> dicatationlist) {
        pList = dicatationlist;
        publicholidayDetails = LayoutInflater.from(context);
        context1 = context;
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        Currentdate = df.format(c.getTime());

    }

    @Override
    public int getCount() {
        return pList.size();
    }

    @Override
    public Object getItem(int position) {

        return pList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }
    @Override
    public int getViewTypeCount() {
        return pList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        convrtview = convertView;
        if (convertView == null) {
            convertView = publicholidayDetails.inflate(R.layout.chat_list_view_layout, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.txtthreadname);
            holder.unreadcount = (TextView) convertView.findViewById(R.id.txtunreadcount);
            holder.initial = (TextView) convertView.findViewById(R.id.txtinitial);
            holder.lastmsgtime = (TextView) convertView.findViewById(R.id.txtdate);
            holder.lstmsgsendername = (TextView) convertView.findViewById(R.id.txtsendername);
            holder.useImage = (ImageView)convertView.findViewById(R.id.img_user_image);
            holder.txt_read_delivered= (TextView) convertView.findViewById(R.id.txt_read_delivered);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final String name[] = pList.get(position).getCustomThreadName().trim().split(" ");
        final String name2[] = pList.get(position).getLastSenderName().trim().split(" ");
        if(pList.get(position).getProfileImg() != null && !pList.get(position).getProfileImg().equals("") && !pList.get(position).getProfileImg().equalsIgnoreCase("null"))
        {
            String urlString = pList.get(position).getProfileImg();
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<urlString.length();i++)
            {
                char c='\\';
                if (urlString.charAt(i) =='\\')
                {
                    urlString.replace("\"","");
                    sb.append("/");
                }
                else
                {
                    sb.append(urlString.charAt(i));
                }
            }
            String newURL=sb.toString();
            holder.initial.setVisibility(View.GONE);
            holder.useImage.setVisibility(View.VISIBLE);

            if(!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                new GetImages(newURL,holder.useImage,newURL.split("/")[newURL.split("/").length-1]).execute(newURL);
            else
            {
                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length - 1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                holder.initial.setVisibility(View.GONE);
                holder.useImage.setVisibility(View.VISIBLE);
                holder.useImage.setImageBitmap(bitmap);

            }
        }
        else
        {
            holder.initial.setVisibility(View.VISIBLE);
            holder.useImage.setVisibility(View.GONE);
            if (name2.length==2)
            {
                char fchar = name2[0].toUpperCase().charAt(0);
                char lchar = name2[1].toUpperCase().charAt(0);
                for (int i = 0; i < name2.length; i++) {
                    if (!name2[i].equals("") && i == 0)
                        fchar = name2[i].toUpperCase().charAt(0);
                    else if (!name2.equals("") && i == (name2.length - 1))
                        lchar = name2[i].toUpperCase().charAt(0);
                }
                holder.initial.setText(fchar + "" + lchar);
            }
            else
            {
                char fchar = name2[0].toUpperCase().charAt(0);
                holder.initial.setText(""+fchar);
            }
        }
        holder.initial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Config.showUserThumbnailinDialog(context1,pList.get(position).getProfileImg(),name);
            }
        });
        holder.useImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Config.showUserThumbnailinDialog(context1,pList.get(position).getProfileImg(),name);
            }
        });

        String userName = "";

        for(int i=0;i<name.length;i++)
        {
            userName =userName+" "+ name[i];
        }
          holder.name.setText(userName.trim());
        //ParentQueriesTeacherNameListModel result=dbQ.GetQueryTableData(pList.get(position).getFromSenderName());
        String serverResponse =pList.get(position).getLastMessageText() ;
        String fromServerUnicodeDecoded = StringEscapeUtils.unescapeJava(serverResponse);
        holder.lstmsgsendername.setText(fromServerUnicodeDecoded);
        //String qtime[]=pList.get(position).getDate().split(" ");
        String time=pList.get(position).getTimeStamp();
        String date1= Utility.convertUTCdate(pList.get(position).getTimeStamp().replace("T", " "));
        String[] setnttime=date1.split(" ");
        String[] date=setnttime[0].split("-");
        String newtime=setnttime[1];
        String newdate=date[0]+"/"+date[1]+"/"+date[2]+" "+newtime;
        Log.d("NewDate", newdate);
        /*02/08/2017 10:07:01.1*/
        if(Config.getDate(newdate, "D").equalsIgnoreCase("Today"))
            holder.lastmsgtime.setText(Config.getDate(newdate, "T"));
        else
            holder.lastmsgtime.setText(Config.getDate(newdate, "D"));
        //holder.lastmsg.setText(pList.get(position).getLastMessage());

        if(pList.get(position).getUnreadCount() != 0)
        {
            holder.unreadcount.setVisibility(View.VISIBLE);
            holder.unreadcount.setText(""+pList.get(position).getUnreadCount());

        }
        else
        {
            holder.unreadcount.setVisibility(View.INVISIBLE);
        }
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(context1);
        if (pList.get(position).getParticipentID().contains(","))
        {
            String receiver[]=pList.get(position).getParticipentID().split(",");
            if(receiver.length==2)
            {
                /*holder.txt_read_delivered.setVisibility(View.VISIBLE);*/
                if (pList.get(position).getLastSenderId().equals(sharedpreferences.getString("UserId", "")))
                {
                    holder.txt_read_delivered.setVisibility(View.VISIBLE);
                    if (pList.get(position).getIsLastMsgDeliver().equals("true")&&pList.get(position).getIsLastMsgRead().equals("false"))
                    {
                        holder.txt_read_delivered.setBackground(context1.getResources().getDrawable(R.drawable.msg_delivered_img));
                    } else if (pList.get(position).getIsLastMsgRead().equals("true"))
                    {
                        holder.txt_read_delivered.setBackground(context1.getResources().getDrawable(R.drawable.msg_read_img));
                    } else
                    {
                        holder.txt_read_delivered.setBackground(context1.getResources().getDrawable(R.drawable.msg_sent_img));
                    }
                }
                else
                {
                    holder.txt_read_delivered.setVisibility(View.GONE);
                }

            }
            else if (receiver.length>2)
            {
                holder.txt_read_delivered.setVisibility(View.GONE);
            }

        }
        else
        {
            holder.txt_read_delivered.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    static class ViewHolder {

        TextView name,unreadcount,initial,lastmsgtime,lstmsgsendername,txt_read_delivered;
        ImageView useImage;
    }
}
