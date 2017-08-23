package realizer.com.makemepopular.invitejoin.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.invitejoin.model.ContactModel;
import realizer.com.makemepopular.view.RoundedImageView;


/**
 * Created by Win on 05/10/2016.
 */
public class ContactsAdapter extends BaseAdapter {
    Context mContext;
    LayoutInflater inflater;
    private List<ContactModel> mainDataList = null;
    private ArrayList<ContactModel> arraylist;
    public ContactsAdapter(Context context, List<ContactModel> mainDataList) {

        mContext = context;
        this.mainDataList = mainDataList;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<ContactModel>();
        this.arraylist.addAll(mainDataList);


    }
    static class ViewHolder {
        protected TextView name;
        protected TextView number;
        protected CheckBox check;
        protected RoundedImageView image;
        protected TextView imgText;
    }
    @Override
    public int getCount() {
        return mainDataList.size();
    }
    @Override
    public ContactModel getItem(int position) {
        return mainDataList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return mainDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.contact_list, null);
            holder.imgText= (TextView) view.findViewById(R.id.img_user_text_image);
            holder.name = (TextView) view.findViewById(R.id.contactname);
            holder.number = (TextView) view.findViewById(R.id.contactno);
            holder.check = (CheckBox) view.findViewById(R.id.contactcheck);
            holder.image = (RoundedImageView) view.findViewById(R.id.contactimage1);
            view.setTag(holder);
            view.setTag(R.id.contactname, holder.name);
            view.setTag(R.id.contactno, holder.number);
            view.setTag(R.id.contactcheck, holder.check);
            holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton vw,
                                                     boolean isChecked) {
                            int getPosition = (Integer) vw.getTag();
                            mainDataList.get(getPosition).setSelected(
                                    vw.isChecked());
                        }
                    });
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.check.setTag(position);
        holder.name.setText(mainDataList.get(position).getName());
        holder.number.setText(mainDataList.get(position).getNumber());

//        if(getByteContactPhoto(mainDataList.get(position).getImage())==null){
            holder.image.setBackgroundColor(Color.LTGRAY);
            String name1[]=holder.name.getText().toString().split(" ");
            String fname = name1[0].trim().toUpperCase().charAt(0)+"";
            holder.image.setVisibility(View.GONE);
            holder.imgText.setText(fname);
//        }
//        else{
//            holder.imgText.setVisibility(View.GONE);
//            holder.image.setImageBitmap(getByteContactPhoto(mainDataList.get(position).getImage()));
//        }

        holder.check.setChecked(mainDataList.get(position).isSelected());
        return view;
    }
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        mainDataList.clear();
        if (charText.length() == 0) {
            mainDataList.addAll(arraylist);
        } else {
            for (ContactModel wp : arraylist) {
                if (wp.getName().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    mainDataList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }
    public Bitmap getByteContactPhoto(String contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = mContext.getContentResolver().query(photoUri,
                new String[] {ContactsContract.Contacts.Photo.DATA15}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

}
