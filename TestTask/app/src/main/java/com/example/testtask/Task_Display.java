package com.example.testtask;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;

public class Task_Display extends AppCompatActivity {
    static ListView mDisplayListView;
    static CustomAdapter mAdapter;
    static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_task_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTask();
            }
        });

        //This sets up static classes and other details for the entire program.
        initializeApplication();

        //This sets up member variable and other details specific to this activity.
        mDisplayListView = (ListView) findViewById(R.id.lsvDisplayList);
    }

    private void initializeApplication() {
        DatabaseAccess.setContext(this);
    }


    static AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {
            Bundle bundle = new Bundle();
            int type = mAdapter.getItemViewType(position);
            DialogFragment newFragment;
            FragmentActivity activity;
            switch (type) {
                case 0:
                    bundle.putLong("InstanceID", Long.valueOf(((CustomAdapter.ViewHolder)v.getTag()).id.getText().toString()));
                    newFragment = new Task_Display.CompleteInstanceConfirmationFragment();
                    newFragment.setArguments(bundle);
                    activity = (FragmentActivity)parent.getContext();
                    newFragment.show(activity.getSupportFragmentManager(), "Complete Task");
                    break;
                case 2:
                    bundle.putLong("SessionID", Long.valueOf(((CustomAdapter.ViewHolder)v.getTag()).id.getText().toString()));
//                    bundle.putString("Section", ((CustomAdapter.ViewHolder)v.getTag()).section.getText().toString());
                    newFragment = new Task_Display.CompleteSessionConfirmationFragment();
                    newFragment.setArguments(bundle);
                    activity = (FragmentActivity)parent.getContext();
                    newFragment.show(activity.getSupportFragmentManager(), "Complete Session Task");
                    break;
            }
        }
    };

    static AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
            Intent intent = new Intent(mContext, Details_Instance.class);
            intent.putExtra("EXTRA_INSTANCE_ID",  Long.valueOf(((CustomAdapter.ViewHolder)v.getTag()).id.getText().toString()));
            mContext.startActivity(intent);
            return true;
        }
    };
//
    public static class CompleteInstanceConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpInstanceID = getArguments().getLong("InstanceID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Complete Task")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            TaskInstance ti = new TaskInstance(tmpInstanceID);
                            ti.finishInstance(1);
                            loadTasksFromDatabase(mContext);
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

    public static class ErrorConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String strError = getArguments().getString("Error");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(strError)
                    .setPositiveButton("Acknowledged", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            loadTasksFromDatabase(mContext);
                        }
                    })
                    .setNegativeButton("Report", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            loadTasksFromDatabase(mContext);
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public static class CompleteSessionConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpSessionID = getArguments().getLong("SessionID");
//            final String tmpSection = getArguments().getString("Section");
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
                            Cursor tblSession = DatabaseAccess.getRecordsFromTable("tblSession",
                                    "flngSessionID",
                                    tmpSessionID);
                            if(tblSession.moveToFirst()){
                                Time tempTime = new Time(tblSession.getLong(tblSession.getColumnIndex("flngTimeID")));
                                tempTime.finishTaskInstances(2);
                            }
                            tblSession.close();
                            loadTasksFromDatabase(mContext);
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

    @Override
    protected void onResume(){
        try{
            super.onResume();
            generateTaskInstances();
            loadTasksFromDatabase(this);
        }catch(Exception e){
            e.printStackTrace();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    //region CALENDER FUNCTIONS
    public static Calendar getBeginningCurentDay(){
        Calendar temp = getCurrentCalendar();
        temp.set(Calendar.HOUR_OF_DAY,0);
        temp.set(Calendar.MINUTE,0);
        temp.set(Calendar.SECOND,0);
        temp.set(Calendar.MILLISECOND,0);

        return temp;
    }

    public static Calendar getEndCurrentDay(){
        Calendar temp = getCurrentCalendar();
        temp.set(Calendar.HOUR_OF_DAY,23);
        temp.set(Calendar.MINUTE,59);
        temp.set(Calendar.SECOND,59);
        temp.set(Calendar.MILLISECOND,999);

        return temp;
    }

    public static Calendar getCalendar(long plngMiliDate){
        Calendar tempCal = getCurrentCalendar();
        tempCal.setTimeInMillis(plngMiliDate);
        return tempCal;
    }

    public static Calendar getCalendar(long plngMiliDate,
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

    public static Calendar getCurrentCalendar(){
        Calendar currentCalendar = Calendar.getInstance();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (prefs.getBoolean("enable_debug", false)) {
            //calNow.set(prefs.getString())
            String strDatePref = prefs.getString("DatePref", "");
            String strTimePref = prefs.getString("TimePref", "");
            if (strDatePref != "") {
                String[] datePieces = strDatePref.split("-");
                currentCalendar.set(Calendar.YEAR, Integer.parseInt(datePieces[0]));
                currentCalendar.set(Calendar.MONTH, Integer.parseInt(datePieces[1]) - 1);
                currentCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(datePieces[2]));

                if (prefs.getBoolean("enable_time", false) && strTimePref != "") {
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

    private void generateTaskInstances() {
        DatabaseAccess.mDatabase.beginTransaction();
        try{
            Cursor timeCursor = DatabaseAccess.getRecordsFromTable("tblTime",
                "fblnComplete = ?",
                new Object[]{0});

            while (timeCursor.moveToNext()) {
                Time tempTime = new Time(timeCursor.getLong(timeCursor.getColumnIndex("flngTimeID")));
                tempTime.buildTimeInstances(); //build generation points
                tempTime.generateInstances(false, -1); //Add any new instances that need adding
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
        Intent intent = new Intent(this, Viewer_Session.class);
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

    static void loadTasksFromDatabase(Context pContext){
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

        Cursor cursor = DatabaseAccess.getTaskInstancesWithDetails();

        ArrayList<taskInstances> priorityList = new ArrayList();
        ArrayList<taskInstances> todayList = new ArrayList();
        ArrayList<taskInstances> standardList = new ArrayList();
        ArrayList<taskInstances> upcomingList = new ArrayList();

        while(cursor.moveToNext()){
            char result = determineListForTask(cursor.getLong(cursor.getColumnIndex("fdtmFrom")),
                    cursor.getLong(cursor.getColumnIndex("fdtmTo")),
                    cursor.getLong(cursor.getColumnIndex("fblnFromTime")) == 1,
                    cursor.getLong(cursor.getColumnIndex("fblnToTime")) == 1,
                    cursor.getLong(cursor.getColumnIndex("fblnToDate")) == 1,
                    cursor.getLong(cursor.getColumnIndex("fdtmCreated")));
            if (result == 'P') {
                priorityList.add(new taskInstances(cursor.getString(cursor.getColumnIndex("fstrTitle")),
                        cursor.getLong(cursor.getColumnIndex("flngInstanceID")),
                        cursor.getLong(cursor.getColumnIndex("flngSessionID")),
                        cursor.getString(cursor.getColumnIndex("fstrSessionTitle"))));
            } else if (result == 'T') {
                todayList.add(new taskInstances(cursor.getString(cursor.getColumnIndex("fstrTitle")),
                        cursor.getLong(cursor.getColumnIndex("flngInstanceID")),
                        cursor.getLong(cursor.getColumnIndex("flngSessionID")),
                        cursor.getString(cursor.getColumnIndex("fstrSessionTitle"))));
            } else if (result == 'S') {
                standardList.add(new taskInstances(cursor.getString(cursor.getColumnIndex("fstrTitle")),
                        cursor.getLong(cursor.getColumnIndex("flngInstanceID")),
                        cursor.getLong(cursor.getColumnIndex("flngSessionID")),
                        cursor.getString(cursor.getColumnIndex("fstrSessionTitle"))));
            } else if (result == 'U') {
                upcomingList.add(new taskInstances(cursor.getString(cursor.getColumnIndex("fstrTitle")),
                        cursor.getLong(cursor.getColumnIndex("flngInstanceID")),
                        cursor.getLong(cursor.getColumnIndex("flngSessionID")),
                        cursor.getString(cursor.getColumnIndex("fstrSessionTitle"))));
            }
        }

        mAdapter = new CustomAdapter(pContext);

        //Load Events
          cursor = DatabaseAccess.retrieveEventTaskInstances();
        Long lngEventId = (long)-1;
        while(cursor.moveToNext()){
            if (lngEventId != cursor.getLong(cursor.getColumnIndex("flngEventID"))) {
                lngEventId = cursor.getLong(cursor.getColumnIndex("flngEventID"));
                mAdapter.addSeparatorItem(cursor.getString(cursor.getColumnIndex("fstrEventTitle")));
            }
            mAdapter.addItem(cursor.getString(cursor.getColumnIndex("fstrTaskTitle")),
                    cursor.getLong(cursor.getColumnIndex("flngInstanceID")));
        }

        //Load Regular Tasks
        mAdapter.addSeparatorItem("Priority");
        int i = 0;
        Long lngSessionId = (long)-1;
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
        lngSessionId = (long)-1;
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
        lngSessionId = (long)-1;
        while (i < standardList.size()){
            if (lngSessionId != standardList.get(i).mSession){
                mAdapter.addGroupItem("Session: " + standardList.get(i).mSessionTitle, standardList.get(i).mSession);
                lngSessionId = standardList.get(i).mSession;
            }
            mAdapter.addItem(standardList.get(i).mTitle, standardList.get(i).mId);
            i++;
        }
        mAdapter.addSeparatorItem("Upcoming");
        i = 0;
        lngSessionId = (long)-1;
        while (i < upcomingList.size()){
            if (lngSessionId != upcomingList.get(i).mSession){
                mAdapter.addGroupItem("Session: " + upcomingList.get(i).mSessionTitle, upcomingList.get(i).mSession);
                lngSessionId = upcomingList.get(i).mSession;
            }
            mAdapter.addItem(upcomingList.get(i).mTitle, upcomingList.get(i).mId);
            i++;
        }
        mDisplayListView.setAdapter(mAdapter);
        mDisplayListView.setOnItemClickListener(itemClickListener);
        mDisplayListView.setOnItemLongClickListener(itemLongClickListener);
    }

    private static char determineListForTask(Long pdtmFrom, Long pdtmTo, Boolean pblnFromTimeSet, Boolean pblnToTimeSet, Boolean pblnToDateSet, Long pdtmCreated) {
        char result = ' ';
        Calendar calNow = getCurrentCalendar(); //represents the time now
        Calendar calFromWithTime = null;
        Calendar calToWithTime = null;
        Calendar calFrom = null; //represents the from time of a task
        Calendar calTo = null; //represents the to time of a task
        Calendar calCreate = getCurrentCalendar(); //represents when the task was created
        calCreate.setTimeInMillis(pdtmCreated);

        //Start: Set General From Details
        if (pdtmFrom != -1) {
            calFrom = getCalendar(pdtmFrom);
        }else{
            calFrom = (Calendar)calCreate.clone();
        }

        //Start: Set General To Details
        if(pdtmTo != -1){
            calTo = getCalendar(pdtmTo);
        } else{
            calTo = (Calendar)calFrom.clone();
        }

        //if time details exists we need to make sure from and to w/ time details are populated
        if(pblnFromTimeSet || pblnToTimeSet){
            calFromWithTime = (Calendar) calFrom.clone();
            if(pblnToTimeSet) {
                calToWithTime = (Calendar) calTo.clone();
            } else {
                calToWithTime = (Calendar) calFrom.clone();
                //Must have been from time that was set so assume till end of day
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

        //if either of the time settings is set and the from and the to dates surround now then it's a priority
        //this will handle cases both are set and where only one or the other is set

        //Evaluate Time Details
        if((!pblnFromTimeSet && pblnToTimeSet) //Just to time set
        && calNow.after(calFrom) && calNow.before(calToWithTime)){
            result = 'P';
        } else if((pblnFromTimeSet && !pblnToTimeSet) && //Just from time set
                calNow.after(calFromWithTime) && calNow.before(calTo)){ //Exists w/i time bounds
            result = 'P';
        } else if((pblnFromTimeSet && pblnToTimeSet) && //Time details exist
                calNow.after(calFromWithTime) && calNow.before(calToWithTime)){ //Exists w/i time bounds
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
