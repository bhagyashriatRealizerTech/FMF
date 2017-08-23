package realizer.com.makemepopular.chat.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.asynctask.GetThreadDetailsAsyncTask;
import realizer.com.makemepopular.asynctask.RemoveMemberFromGroupAsyncTask;
import realizer.com.makemepopular.chat.model.ChatThreadListModel;
import realizer.com.makemepopular.exceptionhandler.NetworkException;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.GetImages;
import realizer.com.makemepopular.utils.ImageStorage;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.Utility;

/**
 * Created by Win on 06/03/2017.
 */

public class ChatThreadListDetailsAdapter extends BaseAdapter {


    private static ArrayList<ChatThreadListModel> pList;
    private LayoutInflater publicholidayDetails;
    private Context context1;
    boolean isImageFitToScreen;
    View convrtview;
    //PhotoViewAttacher mAttacher;
    int memberPos=0;
    private String Currentdate;


    public ChatThreadListDetailsAdapter(Context context, ArrayList<ChatThreadListModel> dicatationlist) {
        pList = dicatationlist;
        publicholidayDetails = LayoutInflater.from(context);
        context1 = context;
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        Currentdate = df.format(c.getTime());

    }

    @Override
    public int getCount() {
        return pList.size();
    }

    @Override
    public Object getItem(int position) {

        return pList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }
    @Override
    public int getViewTypeCount() {
        return pList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        convrtview = convertView;
        if (convertView == null) {
            convertView = publicholidayDetails.inflate(R.layout.chat_group_member_list_adapter, null);
            holder = new ViewHolder();
            holder.userName = (TextView) convertView.findViewById(R.id.txt_grpfrndlst_frndname);
            holder.removeUser = (TextView) convertView.findViewById(R.id.removeIcon);
            holder.useImage = (ImageView)convertView.findViewById(R.id.grp_frndImg);
            holder.initial = (TextView) convertView.findViewById(R.id.txtinitial);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //String name[] = pList.get(position).getCustomThreadName().trim().split(" ");
        String name2[] = pList.get(position).getParticipant().trim().split(" ");
        if(pList.get(position).getProfileImg() != null && !pList.get(position).getProfileImg().equals("") && !pList.get(position).getProfileImg().equalsIgnoreCase("null"))
        {
            String urlString = pList.get(position).getProfileImg();
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
            holder.useImage.setVisibility(View.VISIBLE);

            if(!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                new GetImages(newURL,holder.useImage,newURL.split("/")[newURL.split("/").length-1]).execute(newURL);
            else
            {
                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length - 1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                holder.initial.setVisibility(View.GONE);
                holder.useImage.setVisibility(View.VISIBLE);
                holder.useImage.setImageBitmap(bitmap);

            }
        }
        else
        {
            holder.initial.setVisibility(View.VISIBLE);
            holder.useImage.setVisibility(View.GONE);
            if (name2.length==2)
            {
                char fchar = name2[0].toUpperCase().charAt(0);
                char lchar = name2[1].toUpperCase().charAt(0);
                for (int i = 0; i < name2.length; i++) {
                    if (!name2[i].equals("") && i == 0)
                        fchar = name2[i].toUpperCase().charAt(0);
                    else if (!name2.equals("") && i == (name2.length - 1))
                        lchar = name2[i].toUpperCase().charAt(0);
                }
                holder.initial.setText(fchar + "" + lchar);
            }
            else
            {
                char fchar = name2[0].toUpperCase().charAt(0);
                holder.initial.setText(""+fchar);
            }
        }

        /*String userName = "";

        for(int i=0;i<name.length;i++)
        {
            userName =userName+" "+ name[i];
        }*/
        holder.userName.setText(pList.get(position).getParticipant());


       /* holder.removeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String memberid= pList.get(position).getParticipentID();
                String threadid= pList.get(position).getThreadId();
                String memebr=  pList.get(position).getParticipant();
                memberPos=position;
                alertDialog(context1, "Remove Friend", "Are you sure you want to remove "+memebr+"?", threadid, memberid);
            }
        });
*/
        return convertView;
    }

    static class ViewHolder {

        TextView userName,removeUser,initial;
        ImageView useImage;
    }

    public void alertDialog(final Context context, String title, String message,final String tid, final String mid) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View dialoglayout = inflater.inflate(R.layout.alert_custom_messagebox, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialoglayout);

        Button buttonok= (Button) dialoglayout.findViewById(R.id.alert_btn_ok);
        buttonok.setText("Remove");
        Button buttoncencel= (Button) dialoglayout.findViewById(R.id.alert_btn_cancel);
        buttoncencel.setVisibility(View.VISIBLE);
        TextView titleName=(TextView) dialoglayout.findViewById(R.id.alert_dialog_title);
        TextView alertMsg=(TextView) dialoglayout.findViewById(R.id.alert_dialog_message);


        final AlertDialog alertDialog = builder.create();

        buttonok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isConnectingToInternet(context)) {
                    RemoveMemberFromGroupAsyncTask getDetails = new RemoveMemberFromGroupAsyncTask(tid,mid,context);
                    getDetails.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else
                {
                    Config.alertDialog(context, "Internet Connection", "Make sure your device is connected to the internet.");
                }
                alertDialog.dismiss();
            }
        });

        buttoncencel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        titleName.setText(title);
        alertMsg.setText(message);

        alertDialog.show();

    }


    public class RemoveMemberFromGroupAsyncTask extends AsyncTask<Void,Void,StringBuilder> {

        StringBuilder resultbuilder;
        Context mycontext;
        String memberid;
        String threadId;
        ProgressDialog dialog;
        SharedPreferences sharedpreferences;

        public RemoveMemberFromGroupAsyncTask(String tid, String mid, Context mycontext) {
            this.mycontext=mycontext;
            this.threadId=tid;
            this.memberid=mid;
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            dialog=ProgressDialog.show(mycontext,"","Removing Friend...");
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {
            if (isCancelled())
            {
                return (null); // don't forget to terminate this method
            }
            resultbuilder =new StringBuilder();
            HttpClient httpClient=new DefaultHttpClient();
            String url= Config.URL_Chat+"RemoveThreadMember";
            HttpPut httpPost=new HttpPut(url);
            String json="";
            StringEntity se=null;
            JSONObject jsonObject=new JSONObject();
            try
            {

                jsonObject.put("threadId",threadId);
                jsonObject.put("memberId",memberid);
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
                    StringBuilder exceptionString = new StringBuilder();
                    HttpEntity entity = httpResponse.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while((line=reader.readLine()) != null)
                    {
                        exceptionString.append(line);
                    }

                    NetworkException.insertNetworkException(mycontext, exceptionString.toString());
                    resultbuilder.append("Server not Responding");
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
            dialog.dismiss();
            if (stringBuilder.toString().equalsIgnoreCase("true"))
            {
                if (pList.size() >0)
                {
                    for (int i = 0; i < pList.size(); i++) {
                        if (memberid.equalsIgnoreCase(pList.get(i).getParticipentID())) {
                            pList.remove(i);
                            notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }
        }

        @Override
        protected void onCancelled() {
            dialog.dismiss();
            super.onCancelled();
        }

    }
}

