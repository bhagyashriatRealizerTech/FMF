package realizer.com.makemepopular.invitejoin;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.invitejoin.adapter.ContactsAdapter;
import realizer.com.makemepopular.invitejoin.adapter.ContactsListClass;
import realizer.com.makemepopular.invitejoin.model.ContactModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnBackPressFragment;


/**
 * Created by Win on 05/10/2016.
 */
public class InviteToOthersActivity extends AppCompatActivity implements OnBackPressFragment
{
    ListView lst;
    String htext,std,div;
    EditText edtmessage;
    TextView textView,noContact;
    String mobno;
    String SENT="SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";
    Context context = null;
    ContactsAdapter objAdapter;
    ListView lv = null;
    List mobList=new ArrayList<String>();
    EditText edtSearch = null;
    LinearLayout llContainer = null;
    //    Button btnOK = null;
    RelativeLayout rlPBContainer = null;
    StringBuffer sb = new StringBuffer();
    SharedPreferences sharedpreferences;
    StringBuffer sb2=new StringBuffer();
    String s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,""));
        setContentView(R.layout.contact_list_activity);
        //StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskWrites().penaltyLog().penaltyDeath().build());
//        sharedpreferences= PreferenceManager.getDefaultSharedPreferences(getActivity());

        context=this;
        rlPBContainer = (RelativeLayout) findViewById(R.id.pbcontainer);
        edtSearch = (EditText) findViewById(R.id.input_search);
        llContainer = (LinearLayout) findViewById(R.id.data_container);
        noContact= (TextView) findViewById(R.id.textviewNocontact);
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                                      int arg3) {
                // When user changed the Text
                String text = edtSearch.getText().toString().toLowerCase(Locale.getDefault());
                objAdapter.filter(text);
                if (objAdapter.getCount()==0)
                {
//                    Toast.makeText(getActivity(), "No Contacts", Toast.LENGTH_SHORT).show();
                    llContainer.setVisibility(View.GONE);
                    noContact.setVisibility(View.VISIBLE);
                }
                else
                {
                    llContainer.setVisibility(View.VISIBLE);
                    noContact.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
        // addContactsInList();
        GetContact g=new GetContact();
        g.execute();
    }

    private void getSelectedContacts() {
        // TODO Auto-generated method stub
        StringBuffer sb3=new StringBuffer();
        for (ContactModel bean : ContactsListClass.phoneList) {
            if (bean.isSelected()) {
                sb.append(bean.getNumber().replace(" ",""));
                sb.append(",");
                sb2.append(bean.getName().replace(" ",""));
                sb2.append(",");
                sb3.append(bean.getImage());
                sb3.append(",");
            }
        }
        s = sb.toString().trim();
        String name=sb2.toString().trim();
        if (TextUtils.isEmpty(s)) {
//            Toast.makeText(context, "Select atleast one Contact",
//                    Toast.LENGTH_SHORT).show();
            Config.alertDialog(this, "Suggestion", "Select atleast one contact.");
        } else {
            s = s.substring(0, s.length() - 1);
            name=name.substring(0, name.length() - 1);
//            Toast.makeText(context, "Selected Contacts : " +name+" "+s,
//                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void OnBackPress() {
        /*Intent intent=new Intent(getActivity(),DrawerActivity.class);
        startActivity(intent);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_sendmessage, menu);
        return true;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                edtSearch.setText("");
                getSelectedContacts();
                //final Typeface face= Typeface.createFromAsset(getActivity().getAssets(), "fonts/font.ttf");

                if (TextUtils.isEmpty(s))
                {
                    //Toast.makeText(getActivity(), "Please Select Contact", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    LayoutInflater inflater= this.getLayoutInflater();
                    View dialoglayout = inflater.inflate(R.layout.invite_message_dialog, null);
                    Button send = (Button) dialoglayout.findViewById(R.id.btn_send);
                    Button cancel = (Button)dialoglayout.findViewById(R.id.btn_cancel);
                    edtmessage=(EditText)dialoglayout.findViewById(R.id.edtmessage);
                    textView=(TextView) dialoglayout.findViewById(R.id.txtappLink);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setView(dialoglayout);
                    final AlertDialog alertDialog = builder.create();

                    send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            StringBuilder sb1=new StringBuilder();
                            String[] nums =s.split(",");
                            for (int i=0;i<nums.length;i++)
                            {
                                nums[i].replace("(","");
                                nums[i].replace(")","");
                                mobList.add(nums[i]);
                            }
                            //Toast.makeText(getActivity(), mobList.toString(), Toast.LENGTH_SHORT).show();
                            String inviteMsg = edtmessage.getText().toString();
                            if (inviteMsg.equals("")) {
                                //Toast.makeText(InviteToOthersActivity.this, "Enter Message", Toast.LENGTH_SHORT).show();
                                Config.alertDialog(InviteToOthersActivity.this, "Suggestion", "Please Enter Message.");
                            }
                            else if (TextUtils.isEmpty(inviteMsg))
                            {
                                //Toast.makeText(InviteToOthersActivity.this, "Enter Message", Toast.LENGTH_SHORT).show();
                                Config.alertDialog(InviteToOthersActivity.this, "Suggestion", "Please Enter Message.");
                            }
                            else {
                                if (!mobList.equals(null)) {
                                    for (int i = 1; i <= mobList.size(); i++) {
                                        //sb.append(mobList.get(i)+" ");
                                        if (mobList.size() == 1) {
                                            sb1.append(mobList.get(i - 1));
                                        } else if (i == mobList.size()) {
                                            sb1.append(mobList.get(i - 1));
                                        } else {
                                            sb1.append(mobList.get(i - 1) + ";");
                                        }
                                    }
                                    mobno = sb1.toString();
                                    //Toast.makeText(getActivity(), sb1.toString(), Toast.LENGTH_SHORT).show();
                                    String allmobno[] = mobno.split(";");
                                    String smss = edtmessage.getText().toString() + " \n" +"Standard : "+std+"\nDivision : "+div+ " \n" + textView.getText().toString();
                                    sendSMS(allmobno, smss);
                                    //Toast.makeText(getActivity(), "Message sent" , Toast.LENGTH_SHORT).show();
                                    sb1 = null;
                                    edtmessage.setText("");
                                    alertDialog.dismiss();

                                   finish();

                                } else {
                                    //Toast.makeText(InviteToOthersActivity.this, "Enter Mobile Number", Toast.LENGTH_SHORT).show();
                                    Config.alertDialog(InviteToOthersActivity.this, "Suggestion", "Please Enter Mobile Number.");
                                }
                            }
                        }
                    });
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void sendSMS(String phoneNumber[], String message)
    {
        SmsManager sms = SmsManager.getDefault();
        for(String number : phoneNumber) {
            sms.sendTextMessage(number, null, message, null, null);
        }
    }

    public class GetContact extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rlPBContainer.setVisibility(View.VISIBLE);
            edtSearch.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            Cursor phones=getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
            try {
                ContactsListClass.phoneList.clear();
            } catch (Exception e) {
            }
            while (phones.moveToNext()) {
                String phoneName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String phoneImage = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

                ContactModel cp = new ContactModel();


                cp.setName(phoneName);
                cp.setNumber(phoneNumber);
                cp.setImage(phoneImage);
                ContactsListClass.phoneList.add(cp);
            }
            phones.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //super.onPostExecute(aVoid);
            lv = new ListView(context);
            lv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    llContainer.addView(lv);
                }
            });
            Collections.sort(ContactsListClass.phoneList,
                    new Comparator<ContactModel>() {
                        @Override
                        public int compare(ContactModel lhs,
                                           ContactModel rhs) {
                            return lhs.getName().compareTo(
                                    rhs.getName());
                        }
                    });
            objAdapter = new ContactsAdapter(InviteToOthersActivity.this, ContactsListClass.phoneList);

            lv.setAdapter(objAdapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CheckBox chk = (CheckBox) view.findViewById(R.id.contactcheck);
                    ContactModel bean = ContactsListClass.phoneList.get(position);
                    if (bean.isSelected()) {
                        bean.setSelected(false);
                        chk.setChecked(false);
                    } else {
                        bean.setSelected(true);
                        chk.setChecked(true);
                    }
                    //Toast.makeText(InviteToOthersActivity.this, "ListClicked", Toast.LENGTH_SHORT).show();
                }
            });
            rlPBContainer.setVisibility(View.GONE);
            edtSearch.setVisibility(View.VISIBLE);
        }
    }
}
