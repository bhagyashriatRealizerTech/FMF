package realizer.com.makemepopular.addfriend;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.GetImages;
import realizer.com.makemepopular.utils.ImageStorage;
import realizer.com.makemepopular.utils.Utility;

/**
 * Created by Win on 13/01/2017.
 */
public class AddFriendModelAdapter extends BaseAdapter {
    private static ArrayList<AddFriendModel> friendList;
    private LayoutInflater friendListInflater;
    private Context context1;
    View convrtview;
    ViewHolder holder;

    public AddFriendModelAdapter(Context context1,ArrayList<AddFriendModel> frndlst) {
        friendList=frndlst;
        this.context1 = context1;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        convrtview = convertView;
        if (convertView == null) {
            convertView = friendListInflater.inflate(R.layout.addfriend_adapter, null);
            holder = new ViewHolder();
            holder.txtfriendname= (TextView) convertView.findViewById(R.id.txt_addfrnd_frndname);
            holder.txt_addfrnd_frndGender= (TextView) convertView.findViewById(R.id.txt_addfrnd_frndGender);
            holder.txtAddFriend= (TextView) convertView.findViewById(R.id.txt_addfrnd_addfriendbtn);
            holder.img= (ImageView) convertView.findViewById(R.id.add_friend_img);
            holder.txt_status= (TextView) convertView.findViewById(R.id.txt_status);
            holder.addFriendLinearLayout = (LinearLayout) convertView.findViewById(R.id.addFriendLinearLayout);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtfriendname.setText(friendList.get(position).getFriendName());
        holder.txt_addfrnd_frndGender.setText(friendList.get(position).getGender()+", "+friendList.get(position).getAge()+" Yrs");
        holder.txtAddFriend.setTypeface(FontManager.getTypeface(context1, FontManager.FONTAWESOME));
        if (friendList.get(position).getStatus().equalsIgnoreCase("Pending"))
        {
            holder.txtAddFriend.setText(R.string.fa_hourglass_1);
            holder.txtAddFriend.setTextColor(context1.getResources().getColor(R.color.darkGrey));
            if (friendList.get(position).isRequestSent())
                holder.txt_status.setText("Pending");
            else
                holder.txt_status.setText("Requested");
        }
        else  if (friendList.get(position).getStatus().equalsIgnoreCase("Accepted"))
        {
            holder.txtAddFriend.setText(R.string.fa_check_ico);
            holder.txtAddFriend.setTextColor(context1.getResources().getColor(R.color.limegreen));
            holder.txt_status.setText("Accepted");
        }
        else
        {
            holder.txtAddFriend.setText(R.string.fa_addfriend_ico);
            holder.txtAddFriend.setTextColor(context1.getResources().getColor(R.color.colorAccent));
            holder.txt_status.setText("Add Friend");
        }

        if (friendList.get(position).getThumbnailUrl()==""||friendList.get(position).getThumbnailUrl()==null||friendList.get(position).getThumbnailUrl()=="null")
        {

        }
        else
        {
            String newURL= Utility.getURLImage(friendList.get(position).getThumbnailUrl());
            if(!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                new GetImages(newURL,holder.img,newURL.split("/")[newURL.split("/").length-1]).execute(newURL);
            else
            {
                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length-1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                //  bitmap = Bitmap.createScaledBitmap(bitmap,parent.getWidth(),parent.getHeight(),true);
                holder.img.setImageBitmap(bitmap);
            }
        }

        holder.addFriendLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(context1, "Name1="+holder.txtfriendname.getText().toString()+"name2="+holder.txtAddFriend.getText().toString(), Toast.LENGTH_SHORT).show();

                String stutusImage= holder.txtAddFriend.getText().toString();
                if (friendList.get(position).getStatus().equalsIgnoreCase("null") || friendList.get(position).getStatus().equalsIgnoreCase("Rejected") || friendList.get(position).getStatus().equalsIgnoreCase("Blocked"))
                {
                    if (Config.isConnectingToInternet(context1))
                    {
                        SendFriendRequest1Asyntask sendreq=new SendFriendRequest1Asyntask(friendList.get(position).getFriendid(),position,context1);
                        sendreq.execute();
                    }
                    else
                    {
                        Config.alertDialog(context1,"Internet Connection","Make sure your device is connected to the internet.");
                    }
                }
                else
                {
                    //T
                }
            }
        });

        return convertView;
    }
    static class ViewHolder {

        TextView txtfriendname,txtAddFriend,txt_status,txt_addfrnd_frndGender;
        ImageView img;
        LinearLayout addFriendLinearLayout;
    }

    class SendFriendRequest1Asyntask extends AsyncTask<Void,Void,StringBuilder> {

        StringBuilder resultbuilder;
        Context mycontext;
        // private OnTaskCompleted callback;
        ProgressDialog dialog;
        SharedPreferences sharedpreferences;
        String friendID;
        int posi;

        public SendFriendRequest1Asyntask(String frndid,int pos,Context mycontext) {
            this.mycontext=mycontext;
            //this.callback=cb;
            this.posi=pos;
            this.friendID=frndid;
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            dialog=ProgressDialog.show(mycontext,"","Sending Request...");
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {
            resultbuilder =new StringBuilder();
            HttpClient httpClient=new DefaultHttpClient();
            String url= Config.URL_Mmp+"sendFriendRequest";
            HttpPost httpPost=new HttpPost(url);
            String json="";
            StringEntity se=null;
            JSONObject jsonObject=new JSONObject();
            try
            {
                sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mycontext);

                jsonObject.put("UserId",sharedpreferences.getString("UserId",""));
                jsonObject.put("friendId",friendID);
                jsonObject.put("isEmergencyContact",false);

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
            }
            return resultbuilder;
        }

        @Override
        protected void onPostExecute(StringBuilder stringBuilder) {
            super.onPostExecute(stringBuilder);
            if (stringBuilder.toString().equalsIgnoreCase("true"))
            {
                AddFriendModel afm=new AddFriendModel();
                afm.setFriendName(friendList.get(posi).getFriendName());
                afm.setFriendid(friendList.get(posi).getFriendid());
                afm.setStatus("pending");
                afm.setAge(friendList.get(posi).getAge());
                afm.setGender(friendList.get(posi).getGender());
                friendList.set(posi,afm);

                holder.txtAddFriend.setText(R.string.fa_check_ico);
                holder.txtAddFriend.setTextColor(Color.GREEN);
                notifyDataSetChanged();
                // Toast.makeText(context1, "Friend Request Sent Successfully", Toast.LENGTH_SHORT).show();
                Config.alertDialog(context1, "Add Friend", "Friend Request Sent Successfully.");
                dialog.dismiss();
            }
            else
            {
                Config.alertDialog(context1, "Add Friend", "Friend Request Not Send.");
                dialog.dismiss();
            }

        }
    }
}



