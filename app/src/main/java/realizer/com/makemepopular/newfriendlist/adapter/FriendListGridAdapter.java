package realizer.com.makemepopular.newfriendlist.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.interest.InterestModel;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.GetImages;
import realizer.com.makemepopular.utils.ImageStorage;
import realizer.com.makemepopular.utils.Utility;

/**
 * Created by Win on 11/01/2017.
 */
public class FriendListGridAdapter extends BaseAdapter {

    private static ArrayList<FriendListModel> friends;
    private LayoutInflater interestinflater;
    private Context context;
    View convrtview;

    public FriendListGridAdapter(Context context, ArrayList<FriendListModel> list) {
        friends=list;
        this.context = context;
        interestinflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return friends.size();
    }

    @Override
    public Object getItem(int position) {
        return friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return friends.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        convrtview = convertView;
        if (convertView == null) {
            convertView = interestinflater.inflate(R.layout.frined_list_grid_item_layout, null);
            holder = new ViewHolder();
            holder.status= (TextView) convertView.findViewById(R.id.status);
            holder.friendname= (TextView) convertView.findViewById(R.id.textnameuser);
            holder.lastUpdate= (TextView) convertView.findViewById(R.id.txtlastUpdate);
            holder.userPic= (ImageView) convertView.findViewById(R.id.profile_image_view);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.status.setTypeface(FontManager.getTypeface(context,FontManager.FONTAWESOME));

        if(friends.get(position).getStatus().equalsIgnoreCase("Pending")){

            holder.status.setTextColor(context.getResources().getColor(R.color.primary_text));
            holder.status.setText(R.string.fa_hourglass_1);
        }
        else if(friends.get(position).getStatus().equalsIgnoreCase("Accepted")){

            holder.status.setTextColor(context.getResources().getColor(R.color.forestgreen));
            holder.status.setText(R.string.fa_check_ico);
        }
        else if(friends.get(position).getStatus().equalsIgnoreCase("Rejected")){
            holder.status.setTextColor(Color.RED);
            holder.status.setText(R.string.fa_user_times);
        }

        holder.friendname.setText(friends.get(position).getFriendName());
        setDate(holder.lastUpdate,position);
        SetThumbnail(holder.userPic,friends.get(position).getThumbnailUrl());

        return convertView;
    }
    static class ViewHolder {

        TextView status,friendname,lastUpdate;
        ImageView userPic;
    }
    public void setDate(TextView txtdate,int pos){
        String date = Utility.convertUTCdate(friends.get(pos).getLastupdated().toString().replace("T", " "));
        String tp;
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
        txtdate.setText(datet[0]+"\n"+sentTm);
    }

    public void SetThumbnail(ImageView userimg,String profilePic) {
        if (profilePic.equals("") || profilePic.equals("null") || profilePic.equals(null)) {

        } else {
            String newURL = Utility.getURLImage(profilePic);
            if (!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                new GetImages(newURL, userimg, newURL.split("/")[newURL.split("/").length - 1]).execute(newURL);
            else {
                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length - 1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                userimg.setImageBitmap(bitmap);
            }
        }
    }
}
