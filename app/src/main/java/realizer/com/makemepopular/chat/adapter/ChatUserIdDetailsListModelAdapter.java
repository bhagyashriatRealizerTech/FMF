package realizer.com.makemepopular.chat.adapter;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.chat.model.AddedContactModel;
import realizer.com.makemepopular.utils.GetImages;
import realizer.com.makemepopular.utils.ImageStorage;


/**
 * Created by Win on 11/20/2015.
 */
public class ChatUserIdDetailsListModelAdapter extends BaseAdapter {


    private static ArrayList<AddedContactModel> contactList;
    private LayoutInflater addedContact;
    private Context context1;
    boolean isImageFitToScreen;
    View convrtview;
   // PhotoViewAttacher mAttacher;
    private String Currentdate;


    public ChatUserIdDetailsListModelAdapter(Context context, ArrayList<AddedContactModel> contactList1) {
        contactList = contactList1;
        addedContact = LayoutInflater.from(context);
        context1 = context;
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        Currentdate = df.format(c.getTime());
    }

    @Override
    public int getCount() {
        return contactList.size();
    }

    @Override
    public Object getItem(int position) {

        return contactList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public int getViewTypeCount() {
        return contactList.size();
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
            convertView = addedContact.inflate(R.layout.teacher_query_added_contact_list_layout, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.txtFullName);
            holder.initial = (TextView) convertView.findViewById(R.id.txtinitial);
            holder.profilepic = (ImageView)convertView.findViewById(R.id.profile_image_view);
            holder.deleteContact = (TextView)convertView.findViewById(R.id.txtdelete);
            holder.deleteContact.setTag(position);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String name[] = contactList.get(position).getFname().trim().split(" ");
        if(contactList.get(position).getProfileimage() != null && !contactList.get(position).getProfileimage().equals("") && !contactList.get(position).getProfileimage().equalsIgnoreCase("null"))
        {
            String urlString = contactList.get(position).getProfileimage();
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
            holder.profilepic.setVisibility(View.VISIBLE);
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
            holder.initial.setVisibility(View.VISIBLE);
            holder.profilepic.setVisibility(View.GONE);


            char fchar = name[0].toUpperCase().charAt(0);
            char lchar = name[0].toUpperCase().charAt(0);
            for (int i = 0; i < name.length; i++) {
                if (!name[i].equals("") && i == 0)
                    fchar = name[i].toUpperCase().charAt(0);
                else if (!name.equals("") && i == (name.length - 1))
                    lchar = name[i].toUpperCase().charAt(0);

            }

            holder.initial.setText(fchar + "" + lchar);
        }

        String userName = "";

        for(int i=0;i<name.length;i++)
        {
            userName = userName+" "+name[i];
        }
        holder.name.setText(userName.trim());

        holder.deleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (Integer)v.getTag();
                contactList.remove(pos);
               notifyDataSetChanged();
            }
        });


        return convertView;
    }

    static class ViewHolder {

       TextView name,initial,deleteContact;
       ImageView profilepic;

    }
}
