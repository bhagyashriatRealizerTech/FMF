package realizer.com.makemepopular.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.chat.model.AddedContactModel;

/**
 * Created by Win on 11/20/2015.
 */
public class ChatUserIdAutoCompleteListAdapter extends BaseAdapter {


    private static ArrayList<AddedContactModel> contactList;
    private LayoutInflater addedContact;
    private Context context1;
    boolean isImageFitToScreen;
    View convrtview;
   // PhotoViewAttacher mAttacher;
    private String Currentdate;


    public ChatUserIdAutoCompleteListAdapter(Context context, ArrayList<AddedContactModel> contactList1) {
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
            convertView = addedContact.inflate(R.layout.teacher_query_autocomplete_list_layout, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.txtFullName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(contactList.get(position).getFname());


        return convertView;
    }

    static class ViewHolder {

       TextView name;

    }
}
