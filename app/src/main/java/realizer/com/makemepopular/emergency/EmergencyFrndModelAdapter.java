package realizer.com.makemepopular.emergency;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.utils.GetImages;
import realizer.com.makemepopular.utils.ImageStorage;
import realizer.com.makemepopular.utils.Utility;

/**
 * Created by Win on 20/01/2017.
 */
public class EmergencyFrndModelAdapter extends BaseAdapter
{

    private static ArrayList<FriendListModel> friendList;
    private LayoutInflater friendListInflater;
    private Context context1;
    View convrtview;
    ViewHolder holder;

    public EmergencyFrndModelAdapter(Context context1,ArrayList<FriendListModel> frndlist) {
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
            convertView = friendListInflater.inflate(R.layout.emergency_friendlist_adapter, null);
            holder = new ViewHolder();
            holder.txtFname= (TextView) convertView.findViewById(R.id.txt_emerfrndlst_frndname);
            holder.frndimg= (ImageView) convertView.findViewById(R.id.emerg_frndImg);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtFname.setText(friendList.get(position).getFriendName().toString());

        if (friendList.get(position).getThumbnailUrl()==""||friendList.get(position).getThumbnailUrl()==null||friendList.get(position).getThumbnailUrl()=="null")
        {

        }
        else
        {
            String newURL= Utility.getURLImage(friendList.get(position).getThumbnailUrl());
            if(!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                new GetImages(newURL,holder.frndimg,newURL.split("/")[newURL.split("/").length-1]).execute(newURL);
            else
            {
                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length - 1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                //  bitmap = Bitmap.createScaledBitmap(bitmap,parent.getWidth(),parent.getHeight(),true);
                holder.frndimg.setImageBitmap(bitmap);
                // userimgbg.setImageBitmap(bitmap);

//                Drawable drawable = new BitmapDrawable(bitmap);
//                holder.frndimg.setBackground(drawable);
            }
        }


        return convertView;
    }
    static class ViewHolder {

        TextView txtFname,txtLname;
        ImageView frndimg;
    }
}
