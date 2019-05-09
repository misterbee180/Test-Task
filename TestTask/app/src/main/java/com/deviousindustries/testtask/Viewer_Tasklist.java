package com.deviousindustries.testtask;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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


public class Viewer_Tasklist extends AppCompatActivity {
    ListView mDisplayListView;
    CustomAdapter mAdapter;
    static Context mContext;
    static SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_task_display);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createNotificationChannel();

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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTask();
            }
        });

        //This sets up static classes and other details for the entire program.
        DatabaseAccess.setContext(this);

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
            if(blnRedoSync){
                Intent intent = new Intent(this, AlarmReceiver.class);
                AlarmReceiver.cancelAlert(getApplicationContext(), intent);
            }
            if(mPrefs.getLong("general_last_sync",-1) == -1 || blnRedoSync){
                //THIS WILL RUN LITERALLY ONCE (the first time the applciation runs after this update). It should populate today's date and then never run again.
                //This is intended to set up the first alarm necessary to fire off background tasks to later create notifications.
                Intent intent = new Intent(this, AlarmReceiver.class);
                intent.setAction("com.deviousindustries.testtask.SYNC");
                //intent.setAction(Intent.ACTION_BOOT_COMPLETED);
                AlarmReceiver.generateAlert(getApplicationContext(), intent,  Calendar.getInstance().getTimeInMillis());
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

    public static void generateTaskInstances() {
        DatabaseAccess.mDatabase.beginTransaction();
        
        try{
            try(Cursor tblTime = DatabaseAccess.getRecordsFromTable("tblTime","fblnComplete = 0", null)){
                while (tblTime.moveToNext()) {
                    Time tempTime = new Time(tblTime.getLong(tblTime.getColumnIndex("flngTimeID")));
                    tempTime.buildTimeInstances(); //build generation points
                }

                try(Cursor tblTimeInstance = getValidGenerationPoints()){
                    while (tblTimeInstance.moveToNext()) {
                        Time tempTime = new Time(tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngTimeID")));
                        long tiGenerationID = tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngGenerationID"));
                        if (tiGenerationID > tempTime.mlngGenerationID) {
                            Calendar tempTo = getCalendar(tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("fdtmPriority")));
                            if (tempTime.mblnThru) {
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

    public static Cursor getValidGenerationPoints(){

        //NOTE: I was forced to "inline" all of the arguments because when doing match in android queries sometimes bugs are produced.
        String strSelection = "fdtmUpcoming <= " + getEndCurrentDay().getTimeInMillis();
        strSelection += " and fdtmPriority + 86400000 * fintThru >= " + getBeginningCurentDay().getTimeInMillis();

        return DatabaseAccess.mDatabase.query("tblTimeInstance",
                null,
                strSelection,
                null,
                null,
                null,
                null);
    }

    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        //Should probably be moved to the Alarm Receiver class so that it always has the channel generated. For purposes
        //of demo this is fine.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("STANDARD", "GENERAL", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("General alerts");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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

        String rawQuery = "SELECT i.*, ifNULL(lt.fstrTitle||': ','')||td.fstrTitle as fstrTitle, IFNULL(tm.fstrTitle,'') as fstrSessionTitle  \n" +
                "FROM tblTaskInstance i \n" +
                "JOIN tblTaskDetail td \n" +
                "ON td.flngTaskDetailID = i.flngTaskDetailID \n" +
                "JOIN tblTask t \n" +
                "ON t.flngTaskID = i.flngTaskID\n" +
                "AND t.fintTaskType <> 1\n" + //Event
                "LEFT JOIN tblTime tm\n" +
                "ON tm.flngTimeID = i.flngSessionID\n" +
                "LEFT JOIN tblLongTerm lt \n" +
                "ON lt.flngLongTermID = t.flngTaskTypeID \n" +
                "AND t.fintTaskType = 2 \n" +
                "WHERE i.fdtmCompleted = -1 \n" +
                "AND i.fdtmSystemCompleted = -1 \n" +
                "AND i.fdtmDeleted = -1 \n" +
                //"ORDER BY CASE WHEN t.fblnOneOff = 1 THEN -1 ELSE t.flngSessionID END ";
                "ORDER BY i.flngSessionID, i.flngTaskID";
        try(Cursor displayInstance = DatabaseAccess.mDatabase.rawQuery(rawQuery,null)){
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
            long lngEventId = -1;
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
        long lngSessionId = -1;
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
        if (mPrefs.getBoolean("upcoming_switch", true)){
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
        Calendar calNow = getCurrentCalendar(); //represents the time now
        Calendar calFromWithTime = null;
        Calendar calToWithTime = null;
        Calendar calFrom; //represents the from time of a task
        Calendar calTo; //represents the to time of a task
        Calendar calCreate = getCurrentCalendar(); //represents when the task was created
        calCreate.setTimeInMillis(pdtmCreated);

        //Start: Set General From Details
        if (pdtmFrom != -1) {
            calFrom = getCalendar(pdtmFrom);
        }else{
            calFrom = (Calendar)calCreate.clone();
        }

        //Start: Set General To Details
        if(pdtmTo != -1 && pblnToDateSet) calTo = getCalendar(pdtmTo);
        else calTo = (Calendar)calFrom.clone();

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
