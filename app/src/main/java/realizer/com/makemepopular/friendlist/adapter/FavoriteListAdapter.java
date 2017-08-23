package realizer.com.makemepopular.friendlist.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.GetImages;
import realizer.com.makemepopular.utils.ImageStorage;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.RoundedImageView;

/**
 * Created by shree on 1/20/2017.
 */
public class FavoriteListAdapter extends BaseAdapter {
    private ViewHolder holder = null;

    private Context mContext;
    private ArrayList<Friend> childList;

    //private List<UUID> deleteFavoriteList;


    public FavoriteListAdapter(Context context) {
        this.mContext = context;
        // init memory cache controller
        //deleteFavoriteList = new ArrayList<UUID>();
    }

    public static class Friend {
        private UUID id = UUID.randomUUID();
        public FriendListModel employeeQuickView;

        public Friend(FriendListModel employeeQuickView) {
            id = UUID.randomUUID();
            this.employeeQuickView = employeeQuickView;
        }
    }

    public void setChildList(ArrayList<Friend> childList) {
        this.childList = childList;
    }

   /* public List<UUID> getDeleteFavoriteList() {
        return deleteFavoriteList;
    }*/

    /**
     * @return List<EmployeeQuickView>
     */
    public List<Friend> getChildList() {
        return childList;
    }

    @Override
    public int getCount() {
        return childList.size();
    }

    @Override
    public Friend getItem(int position) {
        return childList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

      @Override
    public int getViewTypeCount() {
        return childList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = (LinearLayout) inflater.inflate(R.layout.favorite_list_items, parent, false);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
            holder.name = (TextView) view.findViewById(R.id.textnameuser);
            holder.name.setText(getItem(position).employeeQuickView.getFriendName());
            holder.track = (TextView) view.findViewById(R.id.id_track);
            holder.track.setTypeface(FontManager.getTypeface(mContext, FontManager.FONTAWESOME));
            holder.unfriend = (TextView) view.findViewById(R.id.id_unfriend);
            holder.unfriend.setTypeface(FontManager.getTypeface(mContext, FontManager.FONTAWESOME));
            holder.block = (TextView) view.findViewById(R.id.id_block);
            holder.block.setTypeface(FontManager.getTypeface(mContext, FontManager.FONTAWESOME));
            holder.emergency = (TextView) view.findViewById(R.id.id_emergency);
            holder.emergency.setTypeface(FontManager.getTypeface(mContext, FontManager.FONTAWESOME));
            holder.direction_ico= (TextView) view.findViewById(R.id.list_direction);
            holder.direction_ico.setTypeface(FontManager.getTypeface(mContext, FontManager.FONTAWESOME));
            holder.id_linearLayout1= (LinearLayout) view.findViewById(R.id.expandable);
            holder.id_linearLayout1.setVisibility(View.GONE);
            holder.img= (RoundedImageView) view.findViewById(R.id.profile_image_view);
        holder.unfriendLinearLayout = (TextView) view.findViewById(R.id.unfriendLinearLayout);
        holder.trackLinearLayout = (TextView) view.findViewById(R.id.trackLinearLayout);
        holder.blockLinearLayout = (TextView) view.findViewById(R.id.blockLinearLayout);
        holder.emergencyLinearLayout = (TextView) view.findViewById(R.id.emergencyLinearLayout);
        holder.list_name = (TextView) view.findViewById(R.id.list_name);
        holder.overallLinearlayout= (LinearLayout) view.findViewById(R.id.overallLinearlayout);
        holder.friendreqLinearlayout= (LinearLayout) view.findViewById(R.id.friendreqLinearlayout);

        if (childList.get(position).employeeQuickView.getStatus().equalsIgnoreCase("Pending"))
        {
            if (childList.get(position).employeeQuickView.isSentRequest())
            {
                holder.direction_ico.setTextColor(Color.LTGRAY);
                holder.direction_ico.setText(R.string.fa_hourglass_1);
                holder.list_name.setText("Pending");
                holder.list_name.setTextColor(Color.LTGRAY);
                holder.overallLinearlayout.setVisibility(View.VISIBLE);
                holder.friendreqLinearlayout.setVisibility(View.GONE);
                holder.unfriend.setTextColor(Color.LTGRAY);
                holder.unfriendLinearLayout.setTextColor(Color.LTGRAY);
                holder.blockLinearLayout.setTextColor(Color.LTGRAY);
                holder.block.setTextColor(Color.LTGRAY);
            }
            else
            {
                holder.overallLinearlayout.setVisibility(View.GONE);
                holder.friendreqLinearlayout.setVisibility(View.VISIBLE);
            }
            //holder.id_linearLayout1.setVisibility(View.GONE);
        }
        else  if (childList.get(position).employeeQuickView.getStatus().equalsIgnoreCase("Rejected"))
        {
            holder.direction_ico.setTextColor(Color.RED);
            holder.direction_ico.setText(R.string.fa_user_times);
            holder.list_name.setText("Rejected");
            holder.list_name.setTextColor(Color.RED);
            holder.overallLinearlayout.setVisibility(View.VISIBLE);
            holder.friendreqLinearlayout.setVisibility(View.GONE);
            holder.unfriend.setTextColor(Color.LTGRAY);
            holder.unfriendLinearLayout.setTextColor(Color.LTGRAY);
            holder.blockLinearLayout.setTextColor(Color.LTGRAY);
            holder.block.setTextColor(Color.LTGRAY);
            //holder.id_linearLayout1.setVisibility(View.GONE);
        }
        else
        {
            holder.direction_ico.setTextColor(mContext.getResources().getColor(R.color.forestgreen));
            holder.direction_ico.setText(R.string.fa_right_ico);
            holder.list_name.setText("Accepted");
            holder.list_name.setTextColor(mContext.getResources().getColor(R.color.forestgreen));
            holder.overallLinearlayout.setVisibility(View.VISIBLE);
            holder.friendreqLinearlayout.setVisibility(View.GONE);
            holder.unfriend.setTextColor(Color.RED);
            holder.unfriendLinearLayout.setTextColor(Color.RED);
            holder.blockLinearLayout.setTextColor(mContext.getResources().getColor(R.color.color8));
            holder.block.setTextColor(mContext.getResources().getColor(R.color.color8));
            //holder.id_linearLayout1.setVisibility(View.VISIBLE);

            if (childList.get(position).employeeQuickView.istracking())
            {
                holder.track.setTextColor(mContext.getResources().getColor(R.color.forestgreen));
                holder.trackLinearLayout.setTextColor(mContext.getResources().getColor(R.color.forestgreen));
                //holder.track.setText(R.string.fa_toggle_on_ico);
            }
            else
            {
                holder.track.setTextColor(Color.LTGRAY);
                holder.trackLinearLayout.setTextColor(Color.LTGRAY);
                // holder.track.setText(R.string.fa_toggle_off_ico);
            }

            if (childList.get(position).employeeQuickView.isEmergency())
            {
                holder.emergency.setTextColor(mContext.getResources().getColor(R.color.forestgreen));
                holder.emergencyLinearLayout.setTextColor(mContext.getResources().getColor(R.color.forestgreen));
                holder.emergency.setText(R.string.fa_toggle_on_ico);
            }
            else
            {
                holder.emergency.setTextColor(Color.LTGRAY);
                holder.emergency.setText(R.string.fa_toggle_off_ico);
                holder.emergencyLinearLayout.setTextColor(Color.LTGRAY);
            }
        }

        if (childList.get(position).employeeQuickView.getThumbnailUrl()==""||childList.get(position).employeeQuickView.getThumbnailUrl()==null||childList.get(position).employeeQuickView.getThumbnailUrl()=="null")
        {

        }
        else
        {
            String newURL= Utility.getURLImage(childList.get(position).employeeQuickView.getThumbnailUrl());
            if(!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                new GetImages(newURL,holder.img,newURL.split("/")[newURL.split("/").length-1]).execute(newURL);
            else
            {
                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length - 1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                //  bitmap = Bitmap.createScaledBitmap(bitmap,parent.getWidth(),parent.getHeight(),true);
                holder.img.setImageBitmap(bitmap);
            }
        }
        //notifyDataSetChanged();
        return view;
    }

    public static class ViewHolder {

        public TextView name,track,unfriend,block,emergency,direction_ico,list_name;
        public TextView unfriendLinearLayout,trackLinearLayout,blockLinearLayout,emergencyLinearLayout;
        public LinearLayout id_linearLayout1,overallLinearlayout,friendreqLinearlayout;
        RoundedImageView img;
    }


    public void setEnableEmergency(FriendListModel model)
    {
        if (model.getStatus().equalsIgnoreCase("Pending"))
        {
            holder.direction_ico.setTextColor(Color.LTGRAY);
            holder.direction_ico.setText(R.string.fa_hourglass_1);
        }
        else  if (model.getStatus().equalsIgnoreCase("Rejected"))
        {
            holder.direction_ico.setTextColor(Color.RED);
            holder.direction_ico.setText(R.string.fa_user_times);
        }
        else
        {
            holder.direction_ico.setTextColor(Color.GREEN);
            holder.direction_ico.setText(R.string.fa_right_ico);

            if (model.istracking())
            {
                holder.track.setTextColor(mContext.getResources().getColor(R.color.forestgreen));
                holder.trackLinearLayout.setTextColor(mContext.getResources().getColor(R.color.forestgreen));
            }
            else
            {
                holder.track.setTextColor(Color.LTGRAY);
                holder.trackLinearLayout.setTextColor(Color.LTGRAY);
            }

        }

        holder.emergency.setTextColor(Color.GREEN);
        holder.emergency.setText(R.string.fa_toggle_on_ico);
        childList.set(model.getFrndPositio(),new FavoriteListAdapter.Friend(model));
        notifyDataSetChanged();
    }

    public void setDisableEmergency(FriendListModel model)
    {
        if (model.getStatus().equalsIgnoreCase("Pending"))
        {
            holder.direction_ico.setTextColor(Color.LTGRAY);
            holder.direction_ico.setText(R.string.fa_hourglass_1);
        }
        else  if (model.getStatus().equalsIgnoreCase("Rejected"))
        {
            holder.direction_ico.setTextColor(Color.RED);
            holder.direction_ico.setText(R.string.fa_user_times);
        }
        else
        {
            holder.direction_ico.setTextColor(Color.GREEN);
            holder.direction_ico.setText(R.string.fa_right_ico);

            if (model.istracking())
            {
                holder.track.setTextColor(mContext.getResources().getColor(R.color.forestgreen));
                holder.trackLinearLayout.setTextColor(mContext.getResources().getColor(R.color.forestgreen));
            }
            else
            {
                holder.track.setTextColor(Color.LTGRAY);
                holder.trackLinearLayout.setTextColor(Color.LTGRAY);
            }
        }

        holder.emergency.setTextColor(Color.LTGRAY);
        holder.emergency.setText(R.string.fa_toggle_off_ico);
        childList.set(model.getFrndPositio(),new FavoriteListAdapter.Friend(model));
        notifyDataSetChanged();
    }
}