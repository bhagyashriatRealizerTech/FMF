package realizer.com.makemepopular.utils;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.asynctask.AddParticipantsinThreadAsyncTask;
import realizer.com.makemepopular.asynctask.GetThreadDetailsAsyncTask;
import realizer.com.makemepopular.backend.DatabaseQueries;
import realizer.com.makemepopular.chat.model.AddedContactModel;
import realizer.com.makemepopular.chat.model.ChatUserIdDetailsListModel;
import realizer.com.makemepopular.view.ProgressWheel;

public class ChatSectionIndexer extends AppCompatActivity implements OnTouchListener, View.OnClickListener,OnTaskCompleted{

    private AlphabetListAdapter adapter = new AlphabetListAdapter();
    private GestureDetector mGestureDetector;
    private List<Object[]> alphabet = new ArrayList<Object[]>();
    private HashMap<String, Integer> sections = new HashMap<String, Integer>();
    private int sideIndexHeight;
    private static float sideIndexX;
    private static float sideIndexY;
    private int indexListSize;
    ListView listView;
    ArrayList<AddedContactModel> selectedTeam;
    ArrayList<AddedContactModel> AlreadyselectedList;
    ArrayList<AddedContactModel> temp;
    private MenuItem done;
    ArrayList<AddedContactModel> contactlist;
    LinearLayout innerLayout;
    TextView noSearchData;
    private LinearLayout searchWidgetLayout;
    private EditText mSearchView;
    private ImageView searchBackPress;
    private LinearLayout newLayoutRef;
    String fromWhere="";
    ProgressWheel loading;
    ArrayList<AddedContactModel> filterlist=new ArrayList<>();
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        } else {

            switch (v.getId()) {
                case R.id.search_view:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (event.getRawX() >= (mSearchView.getRight() - mSearchView.getCompoundDrawables()[Config.DRAWABLE_RIGHT].getBounds().width())) {
                            mSearchView.setText("");
                            mSearchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_black, 0, 0, 0);
                            return true;
                        } else {
                            Config.showSoftKeyboard(ChatSectionIndexer.this, mSearchView);
                            return false;
                        }
                    }
                    return false;

                default:
                    if (mSearchView != null) {
                        Config.hideSoftKeyboardWithoutReq(ChatSectionIndexer.this, mSearchView);
                    }
                    return false;
            }

        }
    }

    /**
     * method to show search widget on clicking on search icon at actionbar
     */
    public void showMySearchWidget() {
        searchWidgetLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.id_searchViewBackPressed:
                searchBackPress();
                break;
        }
    }

    private void searchBackPress() {
        mSearchView.setText("");
        mSearchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_black, 0, 0, 0);
        Config.hideSoftKeyboardWithoutReq(ChatSectionIndexer.this, mSearchView);
        searchWidgetLayout.setVisibility(View.GONE);
        getSupportActionBar().show();
    }

    class SideIndexGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            sideIndexX = sideIndexX - distanceX;
            sideIndexY = sideIndexY - distanceY;

            if (sideIndexX >= 0 && sideIndexY >= 0) {
                displayListItem();
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_alphabet);


        //getSupportActionBar().setTitle(Config.actionBarTitle("Select User", ChatSectionIndexer.this));
        getSupportActionBar().setTitle("Select Friend");
        getSupportActionBar().show();

        listView= (ListView) findViewById(R.id.list);
        mSearchView = (EditText) findViewById(R.id.search_view);
        mSearchView.setHint("Search");
        searchWidgetLayout = (LinearLayout) findViewById(R.id.id_searchWidget);
        setLayoutRef(searchWidgetLayout);
        searchWidgetLayout.setVisibility(View.GONE);
        searchBackPress = (ImageView) findViewById(R.id.id_searchViewBackPressed);
        searchBackPress.setOnClickListener(this);
        mGestureDetector = new GestureDetector(this, new SideIndexGestureListener());
        innerLayout = (LinearLayout) findViewById(R.id.innerLayout);
        noSearchData = (TextView) findViewById(R.id.noResults);
        loading=(ProgressWheel) findViewById(R.id.loading);
        selectedTeam=new ArrayList<AddedContactModel>();
        selectedTeam = Singleton.getSelectedStudentList();
        temp = Singleton.getSelectedStudeonBackKeyPress();
        contactlist = GetStudentList();
        getList(contactlist);
        updateList();
        mSearchView.addTextChangedListener(filterTextWatcher);
        Config.hideSoftKeyboardWithoutReq(ChatSectionIndexer.this, mSearchView);

        Bundle b=getIntent().getExtras();
        fromWhere=b.getString("GroupFromWhere");
        if (fromWhere.equalsIgnoreCase("GroupDetails")) {
            AlreadyselectedList = new ArrayList<AddedContactModel>();
            for (AddedContactModel model : selectedTeam) {
                AlreadyselectedList.add(model);
            }
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position) instanceof AlphabetListAdapter.Section)
                    return;
                AddedContactModel modelView = ((AlphabetListAdapter.Item) parent.getItemAtPosition(position)).text;
                if (fromWhere.equalsIgnoreCase("GroupDetails"))
                {
                    if (!AlreadyselectedList.contains(modelView)) {
                        if (!selectedTeam.contains(modelView)) {
                            selectedTeam.add(modelView);
                            filterlist.add(modelView);
                            listView.setItemChecked(position, true);
                        }
                        else
                        {
                            listView.setItemChecked(position, false);
                            selectedTeam.remove(modelView);
                            filterlist.remove(modelView);
                        }
                        adapter.setCheckedEmployeeList(selectedTeam);
                        adapter.setFlag("Attendance");
                        adapter.notifyDataSetChanged();
                    }
                    else
                    {
                        listView.setItemChecked(position, true);
                        adapter.notifyDataSetChanged();
                    }
                }
                else
                {
                    if (!selectedTeam.contains(modelView)) {
                        selectedTeam.add(modelView);
                        listView.setItemChecked(position, true);
                    }
                    else
                    {
                        listView.setItemChecked(position, false);
                        selectedTeam.remove(modelView);
                    }
                    adapter.setCheckedEmployeeList(selectedTeam);
                    adapter.setFlag("Attendance");
                    adapter.notifyDataSetChanged();
                   /* if(selectedTeam.size()==0) {
                        if (!selectedTeam.contains(modelView)) {
                            selectedTeam.add(modelView);
                            listView.setItemChecked(position, true);
                        }
                        else
                        {
                            listView.setItemChecked(position, false);
                            selectedTeam.remove(modelView);
                        }
                        adapter.setCheckedEmployeeList(selectedTeam);
                        adapter.setFlag("Attendance");
                        adapter.notifyDataSetChanged();
                    }
                    else if(selectedTeam.size()==1) {
                        if (!selectedTeam.contains(modelView)) {
                            listView.setItemChecked(position, false);
                            Config.alertDialog(ChatSectionIndexer.this,"Suggestion","You can add only one friend for chatting.");
                        }
                        else
                        {
                            listView.setItemChecked(position, false);
                            selectedTeam.remove(modelView);
                            adapter.setCheckedEmployeeList(selectedTeam);
                            adapter.setFlag("Attendance");
                            adapter.notifyDataSetChanged();
                        }
                    }
                    else
                    {
                        listView.setItemChecked(position, false);
                        Config.alertDialog(ChatSectionIndexer.this,"Suggestion","You can add only one friend for chatting.");
                    }*/
                }
            }
        });

    }

    private TextWatcher filterTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {
            String str = s.toString().trim();
            if (str != null)
                new SelectContactFilter().getFilter().filter(str);
            if (!"".equals(mSearchView.getText().toString())) {
                mSearchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_black, 0, R.drawable.ic_close_black, 0);
                mSearchView.setOnTouchListener(ChatSectionIndexer.this);
            } else {
                mSearchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_black, 0, 0, 0);
                mSearchView.setOnTouchListener(null);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    };

    public void setLayoutRef(LinearLayout searchWidgetLayout) {
        this.newLayoutRef = searchWidgetLayout;
    }

    private class SelectContactFilter implements Filterable {
        @Override
        public Filter getFilter() {
            return new ListFilter();
        }
    }

    /**
     * ListFilter will work from SelectRecipientsAdapter.
     * filter class for search to employees
     */
    public class ListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            //string  method replaceAll(" +", " ") will replace the multiple space to single space between two string
            String constraintStr = constraint.toString().trim().replaceAll(" +", " ").toLowerCase(Locale.getDefault());
            FilterResults result = new FilterResults();
            if (constraint != null && constraint.toString().length() > 0) {
                List<AddedContactModel> filterItems = new ArrayList<>();
                synchronized (this) {
                    for (AddedContactModel item : contactlist) {
                        if (item.getFname().toLowerCase(Locale.getDefault()).contains(constraintStr)) {
                            filterItems.add(item);
                        }
                    }
                    result.count = filterItems.size();
                    result.values = filterItems;
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<AddedContactModel> filtered = (ArrayList<AddedContactModel>) results.values;
            if (results.values != null) {
                if (filtered.isEmpty()) {
                    innerLayout.setVisibility(View.GONE);
                    noSearchData.setVisibility(View.VISIBLE);
                } else {
                    innerLayout.setVisibility(View.VISIBLE);
                    noSearchData.setVisibility(View.GONE);
                }
                getList(filtered);
            } else {
                innerLayout.setVisibility(View.VISIBLE);
                noSearchData.setVisibility(View.GONE);
                getList(contactlist);
            }
        }
    }


    public void getList(List<AddedContactModel> list)
    {
        Collections.sort(list, new ChatNoCaseComparator());

        List<AlphabetListAdapter.Row> rows = new ArrayList<AlphabetListAdapter.Row>();
        int start = 0;
        int end = 0;
        String previousLetter = null;
        Object[] tmpIndexItem = null;
        Pattern numberPattern = Pattern.compile("[0-9]");

        for (AddedContactModel contact : list) {
            String arr[]=contact.getFname().split(" ");
            String firstLetter="";
            for (int i=0;i<arr.length;i++)
            {
                if (arr[i].equals(""))
                {
                }
                else
                {
                    firstLetter = arr[i].substring(0,1);
                    break;
                }
            }
            // Group numbers together in the scroller
            if (numberPattern.matcher(firstLetter).matches()) {
                firstLetter = "#";
            }

            // If we've changed to a new letter, add the previous letter to the alphabet scroller
            if (previousLetter != null && !firstLetter.equalsIgnoreCase(previousLetter)) {
                end = rows.size() - 1;
                tmpIndexItem = new Object[3];
                tmpIndexItem[0] = previousLetter.toUpperCase(Locale.UK);
                tmpIndexItem[1] = start;
                tmpIndexItem[2] = end;
                alphabet.add(tmpIndexItem);

                start = end + 1;
            }

            // Check if we need to add a header row
            if (!firstLetter.equalsIgnoreCase(previousLetter)) {
                rows.add(new AlphabetListAdapter.Section(firstLetter));
                sections.put(firstLetter.toLowerCase(), start);
            }

            // Add the country to the list
            rows.add(new AlphabetListAdapter.Item(contact));
            previousLetter = firstLetter;
        }

        if (previousLetter != null) {
            // Save the last letter
            tmpIndexItem = new Object[3];
            tmpIndexItem[0] = previousLetter.toUpperCase(Locale.UK);
            tmpIndexItem[1] = start;
            tmpIndexItem[2] = rows.size() - 1;
            alphabet.add(tmpIndexItem);
        }
        adapter.setRows(rows);
        adapter.setCheckedEmployeeList(selectedTeam);
        adapter.setFlag("SelectStudent");
        listView.setAdapter(adapter);
    }


    public class ChatNoCaseComparator implements Comparator<AddedContactModel> {
        public int compare(AddedContactModel s1, AddedContactModel s2) {
            return s1.getFname().compareToIgnoreCase(s2.getFname());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_student, menu);
        done = menu.findItem(R.id.add_contact_done);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Singleton.setSelectedStudentList(Singleton.getSelectedStudentList());
                finish();
                return true;
            case R.id.add_contact_done:
                if (fromWhere.equalsIgnoreCase("GroupDetails"))
                {

                    if(filterlist.size() > 0) {
                        if (Config.isConnectingToInternet(ChatSectionIndexer.this)) {
                            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ChatSectionIndexer.this);
                            loading.setVisibility(View.VISIBLE);
                            AddParticipantsinThreadAsyncTask getDetails = new AddParticipantsinThreadAsyncTask(sharedpreferences.getString("ChatThreadId", ""), filterlist, ChatSectionIndexer.this, ChatSectionIndexer.this);
                            getDetails.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            Config.alertDialog(ChatSectionIndexer.this, "Internet Connection", "Make sure your device is connected to the internet.");
                        }
                    }
                    else
                    {
                        Singleton.setSelectedStudentList(AlreadyselectedList);
                        Singleton.setIsDonclick(Boolean.TRUE);
                        finish();
                    }
                }
                else
                {
                    Singleton.setSelectedStudentList(selectedTeam);
                    Singleton.setIsDonclick(Boolean.TRUE);
                    finish();
                }

                return true;
            case R.id.action_search:
                getSupportActionBar().hide();
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
                showMySearchWidget();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskCompleted(String s) {

        loading.setVisibility(View.GONE);
        String ontask[]=s.split("@@@");
        if (ontask[1].equals("AddParticipants"))
        {
            if (ontask[0].equalsIgnoreCase("true"))
            {
                Singleton.setSelectedStudentList(selectedTeam);
                Singleton.setIsDonclick(Boolean.TRUE);
                finish();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        } else {
            return false;
        }
    }


    public void updateList() {
        LinearLayout sideIndex = (LinearLayout) findViewById(R.id.sideIndex);
        sideIndex.removeAllViews();
        indexListSize = alphabet.size();
        if (indexListSize < 1) {
            return;
        }

        int indexMaxSize = (int) Math.floor(sideIndex.getHeight() / 20);
        int tmpIndexListSize = indexListSize;
        while (tmpIndexListSize > indexMaxSize) {
            tmpIndexListSize = tmpIndexListSize / 2;
        }
        double delta;
        if (tmpIndexListSize > 0) {
            delta = indexListSize / tmpIndexListSize;
        } else {
            delta = 1;
        }

        TextView tmpTV;
        for (double i = 1; i <= indexListSize; i = i + delta) {
            Object[] tmpIndexItem = alphabet.get((int) i - 1);
            String tmpLetter = tmpIndexItem[0].toString();

            tmpTV = new TextView(this);
            tmpTV.setText(tmpLetter);
            tmpTV.setGravity(Gravity.CENTER);
            tmpTV.setTextSize(15);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            tmpTV.setLayoutParams(params);
            sideIndex.addView(tmpTV);
        }

        sideIndexHeight = sideIndex.getHeight();

        sideIndex.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // now you know coordinates of touch
                sideIndexX = event.getX();
                sideIndexY = event.getY();

                // and can display a proper item it country list
                displayListItem();

                return false;
            }
        });
    }

    public void displayListItem() {
        LinearLayout sideIndex = (LinearLayout) findViewById(R.id.sideIndex);
        sideIndexHeight = sideIndex.getHeight();
        // compute number of pixels for every side index item
        double pixelPerIndexItem = (double) sideIndexHeight / indexListSize;

        // compute the item index for given event position belongs to
        int itemPosition = (int) (sideIndexY / pixelPerIndexItem);

        // get the item (we can do it since we know item index)
        if (itemPosition < alphabet.size()) {
            Object[] indexItem = alphabet.get(itemPosition);
            int subitemPosition = sections.get(indexItem[0].toString().toLowerCase());

            //ListView listView = (ListView) findViewById(android.R.id.list);
            listView.setSelection(subitemPosition);
        }
    }


    private ArrayList<AddedContactModel> GetStudentList()
    {
        ArrayList<AddedContactModel> results = new ArrayList<>();
        DatabaseQueries qr = new DatabaseQueries(getApplicationContext());
        ArrayList<ChatUserIdDetailsListModel> tnamelst = qr.GetFriendFromFriendList("Accepted","true");
        boolean memberPresentInList = false;
        for(int i=0;i<tnamelst.size();i++)
        {
            if (null != results)
                for (int k = 0; k < results.size(); k++) {
                    memberPresentInList = results.get(k).getUserId().equals(tnamelst.get(i).getFname());
                    if (memberPresentInList)
                        break;
                }

            if (!memberPresentInList) {
                AddedContactModel contactModel = new AddedContactModel();
                ChatUserIdDetailsListModel tDetails = tnamelst.get(i);
                contactModel.setFname(tDetails.getFname());
                contactModel.setUserId(tDetails.getUserid());
                contactModel.setProfileimage(tDetails.getThumbnail());
                results.add(contactModel);
            }
        }

        return results;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Config.hideSoftKeyboardWithoutReq(ChatSectionIndexer.this, mSearchView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Singleton.setSelectedStudentList(temp);
        finish();
    }
}
