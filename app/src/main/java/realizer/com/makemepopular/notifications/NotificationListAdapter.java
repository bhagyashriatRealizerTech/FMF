package realizer.com.makemepopular.notifications;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.GetImages;
import realizer.com.makemepopular.utils.ImageStorage;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.RoundedImageView;

/**
 * Created by shree on 1/25/2017.
 */
public class NotificationListAdapter extends BaseAdapter
{

    private static ArrayList<NotificationModel> friendList;
    private LayoutInflater friendListInflater;
    private Context context1;
    View convrtview;
    ViewHolder holder;

    public NotificationListAdapter(Context context1,ArrayList<NotificationModel> frndlist) {
        this.context1 = context1;
        friendList=frndlist;
        friendListInflater=LayoutInflater.from(context1);
    }

    @Override
    public int getCount() {
        return friendList.size();
    }

    @Override
    public Object getItem(int position) {
        return friendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return friendList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convrtview = convertView;
        if (convertView == null) {
            convertView = friendListInflater.inflate(R.layout.notification_list_adapter, null);
            holder = new ViewHolder();
            holder.txt_noti_notibyname= (TextView) convertView.findViewById(R.id.txt_noti_notibyname);
            holder.txt_noti_notiftext= (TextView) convertView.findViewById(R.id.txt_noti_notiftext);
            holder.txt_notTime= (TextView) convertView.findViewById(R.id.txt_notTime);
            holder.txt_notType= (TextView) convertView.findViewById(R.id.txt_notType);
            holder.urlImage= (RoundedImageView) convertView.findViewById(R.id.img_notif_frnd);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txt_noti_notibyname.setText(friendList.get(position).getNotiFromUserName());
        holder.txt_noti_notiftext.setText(friendList.get(position).getNotiText());
        holder.txt_notTime.setText(DateFormat(friendList.get(position).getNotiTime()));
        if (!friendList.get(position).getNotiType().equalsIgnoreCase("null")) {
            if (friendList.get(position).getNotiType().equalsIgnoreCase("FriendRequest")) {
                holder.txt_notType.setText("Friend Request");
            }
            else  if (friendList.get(position).getNotiType().equalsIgnoreCase("FriendRequestAccepted")) {
                holder.txt_notType.setText("Friend Request Accepted");
            }
            else  if (friendList.get(position).getNotiType().equalsIgnoreCase("FriendRequestRejected")) {
                holder.txt_notType.setText("Friend Request Rejected");
            }
            else  if (friendList.get(position).getNotiType().equalsIgnoreCase("EmergencyRecipt")) {
                holder.txt_notType.setText("Emergency Receipt");
            }
            else  if (friendList.get(position).getNotiType().equalsIgnoreCase("TrackingStarted")) {
                holder.txt_notType.setText("Tracking");
            }
            else
            {
                holder.txt_notType.setText(friendList.get(position).getNotiType());
            }
        }

        if (friendList.get(position).getNotiFromThumbnailUrl()==""||friendList.get(position).getNotiFromThumbnailUrl()==null||friendList.get(position).getNotiFromThumbnailUrl()=="null")
        {

        }
        else
        {
            String newURL= Utility.getURLImage(friendList.get(position).getNotiFromThumbnailUrl());
            if(!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                new GetImages(newURL,holder.urlImage,newURL.split("/")[newURL.split("/").length-1]).execute(newURL);
            else
            {
                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length - 1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                //  bitmap = Bitmap.createScaledBitmap(bitmap,parent.getWidth(),parent.getHeight(),true);
                holder.urlImage.setImageBitmap(bitmap);
            }
        }

        return convertView;
    }
    static class ViewHolder {

        TextView txt_noti_notibyname,txt_noti_notiftext,txt_notTime,txt_notType;
        RoundedImageView urlImage;
    }

    public String DateFormat(String dateinput)
    {
        String[] setnttime=dateinput.split("T");
        String[] date=setnttime[0].split("-");
        String month= Config.getMonth(Integer.valueOf(date[1]));
        String newdate=date[2]+"-"+ month+"-"+date[0];
        return newdate;
    }
}
