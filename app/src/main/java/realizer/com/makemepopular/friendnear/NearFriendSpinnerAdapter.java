package realizer.com.makemepopular.friendnear;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.friendlist.model.FriendListModel;

/**
 * Created by shree on 1/24/2017.
 */
public class NearFriendSpinnerAdapter extends BaseAdapter
{

    private static ArrayList<FriendListModel> friendList;
    private LayoutInflater friendListInflater;
    private Context context1;
    View convrtview;
    ViewHolder holder;

    public NearFriendSpinnerAdapter(Context context1,ArrayList<FriendListModel> frndlist) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        convrtview = convertView;
        if (convertView == null) {
            convertView = friendListInflater.inflate(R.layout.custum_spinner_friend_layout, null);
            holder = new ViewHolder();
            holder.txtFname= (TextView) convertView.findViewById(R.id.txt_emerfrndlst_frndname);
            //holder.frndimg= (ImageView) convertView.findViewById(R.id.emerg_frndImg);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtFname.setText(friendList.get(position).getFriendName().toString());


        return convertView;
    }
    static class ViewHolder {

        TextView txtFname,txtLname;
        //ImageView frndimg;
    }
}

