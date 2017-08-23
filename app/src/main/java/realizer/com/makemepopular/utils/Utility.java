/*
 * Copyright (C) 2014 Mukesh Y authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package realizer.com.makemepopular.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import realizer.com.makemepopular.R;

/**
 * @author Mukesh Y
 */
public class Utility {
	public static ArrayList<String> nameOfEvent = new ArrayList<String>();
	public static ArrayList<String> startDates = new ArrayList<String>();
	public static ArrayList<String> endDates = new ArrayList<String>();
	public static ArrayList<String> descriptions = new ArrayList<String>();

	/*public static ArrayList<String> readCalendarEvent(Context context) {
		*//*Cursor cursor = context.getContentResolver()
				.query(Uri.parse("content://com.android.calendar/events"),
						new String[] { "calendar_id", "title", "description",
								"dtstart", "dtend", "eventLocation" }, null,
						null, null);
		cursor.moveToFirst();*//*
		// fetching calendars name
		//String CNames[] = new String[cursor.getCount()];
		DALGeneralCommunication qr = new DALGeneralCommunication(context);


        ArrayList<String> CNames = qr.GetAttDateTableData();

		//String CNames[]= new String[3];
		String name[] = new String[CNames.size()];

		// fetching calendars id
		nameOfEvent.clear();
		startDates.clear();
		endDates.clear();
		descriptions.clear();
		for (int i = 0; i < CNames.size(); i++) {

			String C[] = CNames.get(i).split("@@@");
			nameOfEvent.add(C[1]);
            String timestamp="";
            if (C[0].contains("Date")) {
               timestamp = C[0].split("\\(")[1].split("\\-")[0];
            }else
            {
                timestamp=C[0].split("-")[0];
            }
            Date createdOn = new Date(Long.parseLong(timestamp));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(createdOn);

			startDates.add(formattedDate);
			endDates.add(formattedDate);
			descriptions.add(C[1]);
			name[i] = C[1];
		}

		return nameOfEvent;
	}*/

	public static String getDate(long milliSeconds) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);
		return formatter.format(calendar.getTime());
	}

    public static String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }


    public static String getURLImage(String url)
    {
        StringBuilder sb=new StringBuilder();
        for(int k=0;k<url.length();k++)
        {

            if (url.charAt(k) =='\\')
            {
                url.replace("\"","");
                sb.append("/");
            }
            else
            {
                sb.append(url.charAt(k));
            }
        }
        return sb.toString();
    }

    //Set List View Height Based on Children in List
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup)
                listItem.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
    public static String convertUTCdate(String OurDate)
    {
        try
        {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date value = formatter.parse(OurDate);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy HH:mm"); //this format changeable
            dateFormatter.setTimeZone(TimeZone.getDefault());
            OurDate = dateFormatter.format(value);

            //Log.d("OurDate", OurDate);
        }
        catch (Exception e)
        {
            OurDate = "00-00-0000 00:00";
        }
        return OurDate;
    }
   public static void CustomToastAlbum(Context context,String str)
    {
        LayoutInflater li = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = li.inflate(R.layout.custom_toast_layout,null);
        TextView tv= (TextView) layout.findViewById(R.id.custom_toast_message);
        tv.setText(str);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 117);
        toast.setView(layout);//setting the view of custom toast layout
        toast.show();
    }

    public static void CustomToast(Context context,String str)
    {
       /* LayoutInflater li = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = li.inflate(R.layout.custom_toast_layout,null);
        TextView tv= (TextView) layout.findViewById(R.id.custom_toast_message);
        tv.setText(str);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 117);
        toast.setView(layout);//setting the view of custom toast layout
        toast.show();*/
        Config.alertDialog(context,"Internet Connection","Make sure your device is connected to the internet.");
    }
    public static void customtoastnew(Context context,String str)
    {
        LayoutInflater li = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = li.inflate(R.layout.custom_toast_layout,null);
        TextView tv= (TextView) layout.findViewById(R.id.custom_toast_message);
        tv.setText(str);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 117);
        toast.setView(layout);//setting the view of custom toast layout
        toast.show();
    }
}
