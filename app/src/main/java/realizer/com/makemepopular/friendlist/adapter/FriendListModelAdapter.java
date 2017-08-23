package realizer.com.makemepopular.friendlist.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;

/**
 * Created by Win on 12/01/2017.
 */
public class FriendListModelAdapter extends BaseAdapter
{
    private static ArrayList<FriendListModel> friendList;
    private LayoutInflater friendListInflater;
    private Context context1;
    View convrtview;
    ViewHolder holder;
    String fromWhere;
    //int pos;

    public FriendListModelAdapter(Context context1, ArrayList<FriendListModel> friendList1,String fromwhere) {
        friendList=friendList1;
        this.context1 = context1;
        this.fromWhere=fromwhere;
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

   /* @Override
    public int getViewTypeCount() {
        return friendList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }*/

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {


        convrtview = convertView;
        if (convertView == null) {
            convertView = friendListInflater.inflate(R.layout.friend_list_adapter, null);
            holder = new ViewHolder();
            holder.txtfriendname= (TextView) convertView.findViewById(R.id.txt_frndlst_frndname);
            holder.txtisemergency= (TextView) convertView.findViewById(R.id.txt_frndlst_setemergency);
            holder.txtismessaging= (TextView) convertView.findViewById(R.id.txt_frndlst_ismessage);
            holder.txtistracking= (TextView) convertView.findViewById(R.id.txt_frndlst_istracking);
            holder.frndimg= (ImageView) convertView.findViewById(R.id.frndlist_frndimg);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtfriendname.setText(friendList.get(position).getFriendName());
        holder.txtistracking.setTypeface(FontManager.getTypeface(context1, FontManager.FONTAWESOME));
        holder.txtismessaging.setTypeface(FontManager.getTypeface(context1, FontManager.FONTAWESOME));
        holder.txtisemergency.setTypeface(FontManager.getTypeface(context1, FontManager.FONTAWESOME));

        if (fromWhere.equalsIgnoreCase("Emergency"))
        {
            holder.txtistracking.setVisibility(View.GONE);
            holder.txtismessaging.setVisibility(View.GONE);
            holder.txtisemergency.setVisibility(View.GONE);
        }
        else {
            holder.txtistracking.setVisibility(View.VISIBLE);
            holder.txtismessaging.setVisibility(View.VISIBLE);
            holder.txtisemergency.setVisibility(View.VISIBLE);
        }

        if (friendList.get(position).istracking())
        {
            holder.txtistracking.setTextColor(Color.MAGENTA);
        }
        else if (!friendList.get(position).istracking())
        {
            holder.txtistracking.setTextColor(Color.LTGRAY);
        }

        if (friendList.get(position).ismessaging())
        {
            holder.txtismessaging.setTextColor(Color.MAGENTA);
        }
        else if (!friendList.get(position).ismessaging())
        {
            holder.txtismessaging.setTextColor(Color.LTGRAY);
        }

        if (friendList.get(position).isEmergency())
        {
            holder.txtisemergency.setTextColor(Color.MAGENTA);
            holder.txtisemergency.setText(R.string.fa_toggle_on_ico);
        }
        else if (!friendList.get(position).isEmergency())
        {
            holder.txtisemergency.setTextColor(Color.LTGRAY);
            holder.txtisemergency.setText(R.string.fa_toggle_off_ico);
        }

        //pos=position;

        holder.txtistracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (friendList.get(position).istracking())
                {

                }
                else
                {

                }
            }
        });

        holder.txtisemergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context1, "Emergency Activate to This Friend", Toast.LENGTH_SHORT).show();
                //int pos = convrtview.getParent().get;
                //Toast.makeText(context1,position + "",Toast.LENGTH_SHORT).show();
                if (friendList.get(position).isEmergency())
                {
                   /* fm.setIsEmergency(false);
                    fm.setFriendName(friendList.get(position).getFriendName());
                    fm.setFriendId(friendList.get(position).getFriendId());
                    friendList.set(position,fm);
                    holder.txtisemergency.setTextColor(Color.LTGRAY);
                    holder.txtisemergency.setText(R.string.fa_toggle_off_ico);
                    notifyDataSetChanged();*/

                    if (Config.isConnectingToInternet(context1))
                    {
                        SetFrinedAsEmergencyContactAsyncTask getListbyname=new SetFrinedAsEmergencyContactAsyncTask(friendList.get(position).getFriendId(),false,position,context1);
                        getListbyname.execute();
                    }
                    else
                    {
                        Config.alertDialog(context1, "Network Error", "No Internet connection");
                    }
                }
                else if (!friendList.get(position).isEmergency())
                {
                    /*fm.setIsEmergency(true);
                    fm.setFriendName(friendList.get(position).getFriendName());
                    fm.setFriendId(friendList.get(position).getFriendId());
                    friendList.set(position,fm);
                    holder.txtisemergency.setTextColor(Color.MAGENTA);
                    holder.txtisemergency.setText(R.string.fa_toggle_on_ico);
                    notifyDataSetChanged();
*/
                    if (Config.isConnectingToInternet(context1))
                    {
                        SetFrinedAsEmergencyContactAsyncTask getListbyname=new SetFrinedAsEmergencyContactAsyncTask(friendList.get(position).getFriendId(),true,position,context1);
                        getListbyname.execute();
                    }
                    else
                    {
                        Config.alertDialog(context1, "Network Error", "No Internet connection");
                    }
                }
            }
        });

        if (friendList.get(position).getThumbnailUrl()==null||friendList.get(position).getThumbnailUrl()==""||friendList.get(position).getThumbnailUrl()=="null")
        {

        }
        else
        {

        }

        return convertView;
    }

    static class ViewHolder {

        TextView txtfriendname,txtistracking,txtismessaging,txtisemergency;
        ImageView frndimg;
    }

    class SetFrinedAsEmergencyContactAsyncTask  extends AsyncTask<Void,Void,StringBuilder> {

        StringBuilder resultbuilder;
        Context mycontext;
       // private OnTaskCompleted callback;
        int posi;
        SharedPreferences sharedpreferences;
        String friendId;
        boolean is_emergency=false;

        public SetFrinedAsEmergencyContactAsyncTask(String frndis,boolean isemergrncy,int pos,Context mycontext) {
            this.mycontext=mycontext;
            this.friendId=frndis;
            this.is_emergency=isemergrncy;
            this.posi=pos;
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            // dialog=ProgressDialog.show(mycontext,"","Inserting Data...!");
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {
            resultbuilder =new StringBuilder();
            HttpClient httpClient=new DefaultHttpClient();
            String url= Config.URL_Mmp+"SetFrinedAsEmergencyContact";
            HttpPost httpPost=new HttpPost(url);
            String json="";
            StringEntity se=null;
            JSONObject jsonObject=new JSONObject();
            try
            {
                sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mycontext);

                jsonObject.put("UserId",sharedpreferences.getString("UserId",""));
                jsonObject.put("friendId",friendId);
                jsonObject.put("isEmergencyContact",is_emergency);

                json=jsonObject.toString();

                se=new StringEntity(json);
                httpPost.setEntity(se);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                HttpResponse httpResponse=httpClient.execute(httpPost);
                StatusLine statusLine=httpResponse.getStatusLine();
                int statuscode=statusLine.getStatusCode();
                if (statuscode==200)
                {
                    HttpEntity entity=httpResponse.getEntity();
                    InputStream content=entity.getContent();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line=reader.readLine())!=null)
                    {
                        resultbuilder.append(line);
                    }
                }
                else
                {

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
                String msg=e.getCause().getMessage();
                if (msg.contains("302"))
                {
                    resultbuilder.append(msg);
                }
            }
            return resultbuilder;
        }

        @Override
        protected void onPostExecute(StringBuilder stringBuilder) {
            super.onPostExecute(stringBuilder);
            //  dialog.dismiss();
            if (stringBuilder.toString().equalsIgnoreCase("true"))
            {
                FriendListModel fm=new FriendListModel();
                if (is_emergency) {
                    fm.setIsEmergency(true);
                    holder.txtisemergency.setTextColor(Color.MAGENTA);
                    holder.txtisemergency.setText(R.string.fa_toggle_on_ico);
                    Toast.makeText(context1, "Emergency Activated to "+friendList.get(posi).getFriendName()+" Friend", Toast.LENGTH_SHORT).show();
                }
                else {
                    fm.setIsEmergency(false);
                    holder.txtisemergency.setTextColor(Color.LTGRAY);
                    holder.txtisemergency.setText(R.string.fa_toggle_off_ico);
                    Toast.makeText(context1, "Emergency Deactivated to "+friendList.get(posi).getFriendName()+" Friend", Toast.LENGTH_SHORT).show();
                }
                fm.setFriendName(friendList.get(posi).getFriendName());
                fm.setFriendId(friendList.get(posi).getFriendId());
                friendList.set(posi,fm);

                notifyDataSetChanged();
            }
            else
            {
                Toast.makeText(context1, friendList.get(posi).getFriendName()+" is not your Friend", Toast.LENGTH_SHORT).show();
            }
            /*stringBuilder.append("@@@SendAlert");
            callback.onTaskCompleted(stringBuilder.toString());*/
        }
    }
}
