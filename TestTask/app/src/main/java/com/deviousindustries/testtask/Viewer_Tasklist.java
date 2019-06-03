package com.deviousindustries.testtask;

import android.app.AlarmManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.deviousindustries.testtask.classes.TaskInstance;
import com.deviousindustries.testtask.classes.Time;
import com.deviousindustries.testtask.data.TaskDatabase;
import com.deviousindustries.testtask.session_viewer.SessionViewer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import static com.deviousindustries.testtask.constants.ConstantsKt.*;

public class Viewer_Tasklist extends AppCompatActivity {
    ListView mDisplayListView;
    CustomAdapter mAdapter;
    static Context mContext;
    public static SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        DatabaseAccess.getInstance(TaskDatabase.Companion.getInstance(getApplicationContext()).getOpenHelper(),
                TaskDatabase.Companion.getInstance(getApplicationContext()).getTaskDatabaseDao());
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_task_display);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Utilities.Companion.instantiate(getApplicationContext());
//        Bundle extras = getIntent().getExtras();
//        if (extras != null){
//            Bundle bundle = new Bundle();
//            bundle.putString("Error", getIntent().getStringExtra("EXTRA_ERROR"));
//
//            DialogFragment newFragment;
//            FragmentActivity activity;
//            newFragment = new ErrorConfirmationFragment();
//            newFragment.setArguments(bundle);
//            newFragment.show(this.getSupportFragmentManager(), "Error Handled");
//        }

        FloatingActionButton fab = findViewById(R.id.AddSession_FAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTask();
            }
        });

        //This sets up static classes and other details for the entire program.
        //DatabaseAccess.setContext(getApplicationContext());

        //This sets up member variable and other details specific to this activity.
        mDisplayListView = findViewById(R.id.lsvDisplayList);
    }

    //region Listers
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {
            Bundle bundle = new Bundle();
            int type = mAdapter.getItemViewType(position);
            DialogFragment newFragment;
            FragmentActivity activity;
            switch (type) {
                case 0:
                    bundle.putLong("InstanceID", Long.valueOf(((CustomAdapter.ViewHolder)v.getTag()).id.getText().toString()));
                    newFragment = new Viewer_Tasklist.CompleteInstanceConfirmationFragment();
                    newFragment.setArguments(bundle);
                    activity = (FragmentActivity)parent.getContext();
                    newFragment.show(activity.getSupportFragmentManager(), "Complete Task");
                    break;
                case 2:
//                    bundle.putLong("TimeID", Long.valueOf(((CustomAdapter.ViewHolder)v.getTag()).id.getText().toString()));
////                    bundle.putString("Section", ((CustomAdapter.ViewHolder)v.getTag()).section.getText().toString());
//                    newFragment = new Viewer_Tasklist.CompleteSessionConfirmationFragment();
//                    newFragment.setArguments(bundle);
//                    activity = (FragmentActivity)parent.getContext();
//                    newFragment.show(activity.getSupportFragmentManager(), "Complete Session Task");
                    break;
            }
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
            Intent intent = new Intent(mContext, Details_Instance.class);
            intent.putExtra("EXTRA_INSTANCE_ID",  Long.valueOf(((CustomAdapter.ViewHolder)v.getTag()).id.getText().toString()));
            mContext.startActivity(intent);
            return true;
        }
    };
    //endregion

    //region Fragments
    public static class CompleteInstanceConfirmationFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpInstanceID = getArguments().getLong("InstanceID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Complete Task")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            TaskInstance ti = new TaskInstance(tmpInstanceID);
                            ti.finishInstance(1);
                            ((Viewer_Tasklist)getActivity()).loadTasksFromDatabase();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

//    public static class ErrorConfirmationFragment extends DialogFragment {
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            final String strError = getArguments().getString("Error");
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setMessage(strError)
//                    .setPositiveButton("Acknowledged", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            loadTasksFromDatabase();
//                        }
//                    })
//                    .setNegativeButton("Report", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            loadTasksFromDatabase();
//                        }
//                    });
//            // Create the AlertDialog object and return it
//            return builder.create();
//        }
//    }

    public static class CompleteSessionConfirmationFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Complete Session Tasks?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
//                            for(int i=0; i<mAdapter.getCount(); i++){
//                                if(((CustomAdapter.itemDetail)mAdapter.getItem(i)).mSection == tmpSection){
//                                    if(mAdapter.getItemViewType(i) == CustomAdapter.TYPE_ITEM){
//                                        TaskInstance ti = new TaskInstance(((CustomAdapter.itemDetail)mAdapter.getItem(i)).mId);
//                                        ti.finishInstance(2);
//                                    }
//                                }
//                            }
                            Time tempTime = new Time(getArguments().getLong("TimeID"));
                            tempTime.finishTaskInstances(2);
                            ((Viewer_Tasklist)getActivity()).loadTasksFromDatabase();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
    //endregion

    @Override
    protected void onResume(){
        try{
            super.onResume();
            boolean blnRedoSync = false;
            //THE ONLY TIME that this should run is if the alarm is somehow not ran at the designated mTime
            // or the application is accessed between the beginning of the next day and the alarm kicking off.
            if(mPrefs.getLong("general_last_sync",NULL_DATE) < getBeginningCurentDay().getTimeInMillis() || blnRedoSync){
                //This is so that it displays the right things the first mTime the app opens
                generateTaskInstances();

                //Cancel any alarms which may already be set up to run
                Intent intent = new Intent(this, AlarmReceiver.class);
                intent.setAction("com.deviousindustries.testtask.SYNC");
                new AlarmReceiver().cancelAlert(getApplicationContext(), intent);

                //re set up the alarm and anything else needing to be done.
                new AlarmReceiver().generateAlert(getApplicationContext(), intent,
                        Calendar.getInstance().getTimeInMillis(), 0, AlarmManager.RTC_WAKEUP);
            } else {
                generateTaskInstances();
            }
            loadTasksFromDatabase();

        }catch(Exception e){
            e.printStackTrace();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    //region CALENDER FUNCTIONS
    static public Calendar getBeginningCurentDay(){
        Calendar temp = getCurrentCalendar();
        temp.set(Calendar.HOUR_OF_DAY,0);
        temp.set(Calendar.MINUTE,0);
        temp.set(Calendar.SECOND,0);
        temp.set(Calendar.MILLISECOND,0);

        return temp;
    }

    static public Calendar getEndCurrentDay(){
        Calendar temp = getCurrentCalendar();
        temp.set(Calendar.HOUR_OF_DAY,23);
        temp.set(Calendar.MINUTE,59);
        temp.set(Calendar.SECOND,59);
        temp.set(Calendar.MILLISECOND,999);

        return temp;
    }

    static public Calendar getCalendar(long plngMiliDate){
        Calendar tempCal = getCurrentCalendar();
        tempCal.setTimeInMillis(plngMiliDate);
        return tempCal;
    }

    static public Calendar getCalendar(long plngMiliDate,
                                boolean pblnBeginning,
                                boolean pblnEnd){
        Calendar tempCal = getCurrentCalendar();
        tempCal.setTimeInMillis(plngMiliDate);
        if(pblnBeginning){
            tempCal.set(Calendar.HOUR_OF_DAY,0);
            tempCal.set(Calendar.MINUTE,0);
            tempCal.set(Calendar.SECOND,0);
            tempCal.set(Calendar.MILLISECOND,0);
        }
        if(pblnEnd){
            tempCal.set(Calendar.HOUR_OF_DAY,23);
            tempCal.set(Calendar.MINUTE,59);
            tempCal.set(Calendar.SECOND,59);
            tempCal.set(Calendar.MILLISECOND,999);
        }
        return tempCal;
    }

    static public Calendar getCurrentCalendar(){
        Calendar currentCalendar = Calendar.getInstance();
        if (mPrefs.getBoolean("enable_debug", false)) {
            //calNow.set(mPrefs.getString())
            String strDatePref = mPrefs.getString("DatePref", "");
            String strTimePref = mPrefs.getString("TimePref", "");
            if (!strDatePref.equals("")) {
                String[] datePieces = strDatePref.split("-");
                currentCalendar.set(Calendar.YEAR, Integer.parseInt(datePieces[0]));
                currentCalendar.set(Calendar.MONTH, Integer.parseInt(datePieces[1]) - 1);
                currentCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(datePieces[2]));

                if (mPrefs.getBoolean("enable_time", false) && !strTimePref.equals("")) {
                    String[] timePieces = strTimePref.split(":");
                    currentCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timePieces[0]));
                    currentCalendar.set(Calendar.MINUTE, Integer.parseInt(timePieces[1]));
                } else {
                    currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    currentCalendar.set(Calendar.MINUTE, 0);
                }
            }
            currentCalendar.set(Calendar.SECOND,0);
            currentCalendar.set(Calendar.MILLISECOND,0);
        }
        return currentCalendar;
    }

    //endregion

    public void generateTaskInstances() {
        DatabaseAccess.mDatabase.beginTransaction();

        try{
            try(Cursor tblTime = DatabaseAccess.getRecordsFromTable("tblTime","fblnComplete = 0", null)){
                while (tblTime.moveToNext()) {
                    Time tempTime = new Time(tblTime.getLong(tblTime.getColumnIndex("flngTimeID")));
                    tempTime.buildTimeInstances(); //build generation points
                }

                try(Cursor tblTimeInstance = DatabaseAccess.getValidGenerationPoints(getEndCurrentDay().getTimeInMillis(), getBeginningCurentDay().getTimeInMillis())){
                    while (tblTimeInstance.moveToNext()) {
                        Time tempTime = new Time(tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngTimeID")));
                        long tiGenerationID = tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngGenerationID"));
                        if (tiGenerationID > tempTime.flngGenerationID) {
                            Calendar tempTo = getCalendar(tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("fdtmPriority")));
                            if (tempTime.fblnThru) {
                                tempTo.add(Calendar.DAY_OF_YEAR, tblTimeInstance.getInt(tblTimeInstance.getColumnIndex("fintThru")));
                            }
                            tempTime.generateInstance(tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("fdtmPriority")),
                                    tempTo.getTimeInMillis()); //Add any new instances that need adding
                            tempTime.updateGenerationID(tiGenerationID);
                        }
                    }
                }
            }
            DatabaseAccess.mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatabaseAccess.mDatabase.endTransaction();
    }

    /** Called when the user taps the Send button */
    public void createTask() {
        Intent intent = new Intent(this, Details_Task.class);
        startActivity(intent);
    }

    public void viewTasks() {
        Intent intent = new Intent(this, Viewer_Task.class);
        startActivity(intent);
    }

    public void viewSessions() {
        Intent intent = new Intent(this, SessionViewer.class);
        startActivity(intent);
    }

    public void viewGroups() {
        Intent intent = new Intent(this, Viewer_Groups.class);
        startActivity(intent);
    }

    public void viewLongTerm() {
        Intent intent = new Intent(this, Viewer_LongTerm.class);
        startActivity(intent);
    }

    public void viewEvents() {
        Intent intent = new Intent(this, Viewer_Events.class);
        startActivity(intent);
    }

    public void viewSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    void loadTasksFromDatabase(){
        class taskInstances{
            private String mTitle;
            private Long mId;
            private Long mSession;
            private String mSessionTitle;

            private taskInstances(String pTitle, Long pId, Long pSession, String pSessionTitle){
                mTitle = pTitle;
                mId = pId;
                mSession = pSession;
                mSessionTitle = pSessionTitle;
            }
        }

        mAdapter = new CustomAdapter(mContext);

        ArrayList<taskInstances> priorityList = new ArrayList<>();
        ArrayList<taskInstances> todayList = new ArrayList<>();
        ArrayList<taskInstances> standardList = new ArrayList<>();
        ArrayList<taskInstances> upcomingList = new ArrayList<>();

        try(Cursor displayInstance = DatabaseAccess.getInstancesForTasklist()){
            while(displayInstance.moveToNext()){
                char result = determineListForTask(displayInstance.getLong(displayInstance.getColumnIndex("fdtmFrom")),
                        displayInstance.getLong(displayInstance.getColumnIndex("fdtmTo")),
                        displayInstance.getLong(displayInstance.getColumnIndex("fblnFromTime")) == 1,
                        displayInstance.getLong(displayInstance.getColumnIndex("fblnToTime")) == 1,
                        displayInstance.getLong(displayInstance.getColumnIndex("fblnToDate")) == 1,
                        displayInstance.getLong(displayInstance.getColumnIndex("fdtmCreated")));
                if (result == 'P') {
                    priorityList.add(new taskInstances(displayInstance.getString(displayInstance.getColumnIndex("fstrTitle")),
                            displayInstance.getLong(displayInstance.getColumnIndex("flngInstanceID")),
                            displayInstance.getLong(displayInstance.getColumnIndex("flngSessionID")),
                            displayInstance.getString(displayInstance.getColumnIndex("fstrSessionTitle"))));
                } else if (result == 'T') {
                    todayList.add(new taskInstances(displayInstance.getString(displayInstance.getColumnIndex("fstrTitle")),
                            displayInstance.getLong(displayInstance.getColumnIndex("flngInstanceID")),
                            displayInstance.getLong(displayInstance.getColumnIndex("flngSessionID")),
                            displayInstance.getString(displayInstance.getColumnIndex("fstrSessionTitle"))));
                } else if (result == 'S') {
                    standardList.add(new taskInstances(displayInstance.getString(displayInstance.getColumnIndex("fstrTitle")),
                            displayInstance.getLong(displayInstance.getColumnIndex("flngInstanceID")),
                            displayInstance.getLong(displayInstance.getColumnIndex("flngSessionID")),
                            displayInstance.getString(displayInstance.getColumnIndex("fstrSessionTitle"))));
                } else if (result == 'U') {
                    upcomingList.add(new taskInstances(displayInstance.getString(displayInstance.getColumnIndex("fstrTitle")),
                            displayInstance.getLong(displayInstance.getColumnIndex("flngInstanceID")),
                            displayInstance.getLong(displayInstance.getColumnIndex("flngSessionID")),
                            displayInstance.getString(displayInstance.getColumnIndex("fstrSessionTitle"))));
                }
            }
        }
        
        //Load Events
        try(Cursor eventDisplayInstance = DatabaseAccess.retrieveEventTaskInstances()){
            long lngEventId = NULL_OBJECT;
            while(eventDisplayInstance.moveToNext()){
                if (lngEventId != eventDisplayInstance.getLong(eventDisplayInstance.getColumnIndex("flngEventID"))) {
                    lngEventId = eventDisplayInstance.getLong(eventDisplayInstance.getColumnIndex("flngEventID"));
                    mAdapter.addSeparatorItem(eventDisplayInstance.getString(eventDisplayInstance.getColumnIndex("fstrEventTitle")));
                }
                mAdapter.addItem(eventDisplayInstance.getString(eventDisplayInstance.getColumnIndex("fstrTaskTitle")),
                        eventDisplayInstance.getLong(eventDisplayInstance.getColumnIndex("flngInstanceID")));
            }
        }

        //Load Regular Tasks
        mAdapter.addSeparatorItem("Priority");
        int i = 0;
        long lngSessionId = NULL_OBJECT;
        while (i < priorityList.size()){
            if (lngSessionId != priorityList.get(i).mSession){
                mAdapter.addGroupItem("Session: " + priorityList.get(i).mSessionTitle, priorityList.get(i).mSession);
                lngSessionId = priorityList.get(i).mSession;
            }
            mAdapter.addItem(priorityList.get(i).mTitle, priorityList.get(i).mId);
            i++;
        }
        mAdapter.addSeparatorItem("Today");
        i = 0;
        lngSessionId = NULL_OBJECT;
        while (i < todayList.size()){
            if (lngSessionId != todayList.get(i).mSession){
                mAdapter.addGroupItem("Session: " + todayList.get(i).mSessionTitle, todayList.get(i).mSession);
                lngSessionId = todayList.get(i).mSession;
            }
            mAdapter.addItem(todayList.get(i).mTitle, todayList.get(i).mId);
            i++;
        }
        mAdapter.addSeparatorItem("Standard");
        i = 0;
        lngSessionId = NULL_OBJECT;
        while (i < standardList.size()){
            if (lngSessionId != standardList.get(i).mSession){
                mAdapter.addGroupItem("Session: " + standardList.get(i).mSessionTitle, standardList.get(i).mSession);
                lngSessionId = standardList.get(i).mSession;
            }
            mAdapter.addItem(standardList.get(i).mTitle, standardList.get(i).mId);
            i++;
        }
        if (mPrefs.getBoolean("upcoming_switch", true)){
            mAdapter.addSeparatorItem("Upcoming");
            i = 0;
            lngSessionId = NULL_OBJECT;
            while (i < upcomingList.size()){
                if (lngSessionId != upcomingList.get(i).mSession){
                    mAdapter.addGroupItem("Session: " + upcomingList.get(i).mSessionTitle, upcomingList.get(i).mSession);
                    lngSessionId = upcomingList.get(i).mSession;
                }
                mAdapter.addItem(upcomingList.get(i).mTitle, upcomingList.get(i).mId);
                i++;
            }
        }
        mDisplayListView.setAdapter(mAdapter);
        mDisplayListView.setOnItemClickListener(itemClickListener);
        mDisplayListView.setOnItemLongClickListener(itemLongClickListener);
    }

    private char determineListForTask(Long pdtmFrom,
                                             Long pdtmTo,
                                             Boolean pblnFromTimeSet,
                                             Boolean pblnToTimeSet,
                                             Boolean pblnToDateSet,
                                             Long pdtmCreated) {

        //Todo: redesign to not use as many booleans. Make considerations for thru tasks.
        char result;
        Calendar calNow = getCurrentCalendar(); //represents the mTime now
        Calendar calFromWithTime = null;
        Calendar calToWithTime = null;
        Calendar calFrom; //represents the from mTime of a task
        Calendar calTo; //represents the to mTime of a task
        Calendar calCreate = getCurrentCalendar(); //represents when the task was created
        calCreate.setTimeInMillis(pdtmCreated);

        //Start: Set General From Details
        if (pdtmFrom != NULL_OBJECT) {
            calFrom = getCalendar(pdtmFrom);
        }else{
            calFrom = (Calendar)calCreate.clone();
        }

        //Start: Set General To Details
        if(pdtmTo != NULL_DATE && pblnToDateSet) calTo = getCalendar(pdtmTo);
        else {
            calTo = (Calendar)calFrom.clone();
            if(pblnToTimeSet){
                Calendar temp = getCalendar(pdtmTo);
                calTo.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
                calTo.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
                calTo.set(Calendar.SECOND, temp.get(Calendar.SECOND));
                calTo.set(Calendar.MILLISECOND, temp.get(Calendar.MILLISECOND));
            }
        }

        //if mTime details exists we need to make sure from and to w/ mTime details are populated
        if(pblnFromTimeSet || pblnToTimeSet){
            calFromWithTime = (Calendar) calFrom.clone();
            if(pblnToTimeSet) {
                calToWithTime = (Calendar) calTo.clone();
            } else {
                calToWithTime = (Calendar) calFrom.clone();
                //Must have been from mTime that was set so assume till end of day
                calToWithTime.add(Calendar.DAY_OF_YEAR,1);
                calToWithTime.set(Calendar.HOUR_OF_DAY, 0);
                calToWithTime.set(Calendar.MINUTE,0);
            }
        }

        //End: Set General From Details
        calFrom.set(Calendar.HOUR_OF_DAY,0);
        calFrom.set(Calendar.MINUTE, 0);
        calFrom.set(Calendar.SECOND, 0);
        calFrom.set(Calendar.MILLISECOND, 0);

        //End: Set General To Details
        calTo.add(Calendar.DAY_OF_YEAR,1);
        calTo.set(Calendar.HOUR_OF_DAY, 0);
        calTo.set(Calendar.MINUTE,0);
        calTo.set(Calendar.MILLISECOND,0);

        //if either of the mTime settings is set and the from and the to dates surround now then it's a priority
        //this will handle cases both are set and where only one or the other is set

        //Evaluate Time Details
        if((!pblnFromTimeSet && pblnToTimeSet) //Just to mTime set
        && calNow.after(calFrom) && calNow.before(calToWithTime)){
            result = 'P';
        } else if((pblnFromTimeSet && !pblnToTimeSet) && //Just from mTime set
                calNow.after(calFromWithTime) && calNow.before(calTo)){ //Exists w/i mTime bounds
            result = 'P';
        } else if((pblnFromTimeSet && pblnToTimeSet) && //Time details exist
                calNow.after(calFromWithTime) && calNow.before(calToWithTime)){ //Exists w/i mTime bounds
            result = 'P';
        } else if ((calNow.after(calFrom) && calNow.before(calTo)) ||
                (calNow.equals(calFrom))) {
            result = 'T';
        } else //At this point it will either be past happening (S) or not yet ready (U)
            if(calNow.after(calTo) || calNow.equals(calTo)) {
                result = 'S';
        } else {
                result = 'U';
            }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()){
            case R.id.action_settings:
                viewSettings();
                break;
            case R.id.action_session:
                viewSessions();
                break;
            case R.id.action_task:
                viewTasks();
                break;
            case R.id.action_event:
                viewEvents();
                break;
            case R.id.action_group:
                viewGroups();
                break;
            case R.id.action_longTerm:
                viewLongTerm();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
