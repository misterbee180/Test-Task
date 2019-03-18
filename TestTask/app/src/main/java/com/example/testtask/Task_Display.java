package com.example.testtask;

import android.app.Dialog;
import android.content.ContentValues;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeSet;

public class Task_Display extends AppCompatActivity {
    static ListView mDisplayListView;
    static CustomAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTask(view);
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
            Bundle bundle = new Bundle();
            Cursor tmpCursor = DatabaseAccess.getRecordsFromTable("tblTaskInstance","flngInstanceID", Long.valueOf(((CustomAdapter.ViewHolder)v.getTag()).id.getText().toString()));
            tmpCursor.moveToFirst();
            bundle.putLong("TaskID", tmpCursor.getLong(tmpCursor.getColumnIndex("flngTaskID")));
            DialogFragment newFragment = new Viewer_Task.TaskEditConfirmationFragment();
            newFragment.setArguments(bundle);
            FragmentActivity activity = (FragmentActivity)parent.getContext();
            newFragment.show(activity.getSupportFragmentManager(), "Edit Task");
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
                            DatabaseAccess.updateRecordFromTable("tblTaskInstance",
                                    "flngInstanceID",
                                    tmpInstanceID,
                                    new String[]{"fblnComplete"},
                                    new Object[]{true});
                            loadTasksFromDatabase(getContext());
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

    public static class CompleteSessionConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpSessionID = getArguments().getLong("SessionID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Complete Session Tasks?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String rawSessionTasksComplete = "Update tblTaskInstance " +
                                    "set fblnComplete = 1 " +
                                    "WHERE flngTaskID IN (SELECT t.flngTaskID " +
                                    "FROM tblTask t " +
                                    "WHERE t.flngSessionID = ? " +
                                    "and t.fblnOneOff <> 1)";
                            String[] parameters = {Long.toString(tmpSessionID)};
                            Cursor cursor = DatabaseAccess.mDatabase.rawQuery(rawSessionTasksComplete, parameters);
                            cursor.moveToFirst();
                            while(cursor.moveToNext()){}

                            loadTasksFromDatabase(getContext());
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
        super.onResume();
        generateTaskInstances();
        loadTasksFromDatabase(this);
    }

    public static Calendar getCurrentCalendar(Context pContext){
        Calendar currentCalendar = Calendar.getInstance();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
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

    private void generateTaskInstances() {
        String rawGetRepeatingTasks = "SELECT t.flngTaskID, tm.flngTimeID, tm.fdtmEvaluated, t.fblnOneOff" +
                ", t.fstrTitle, tm.fdtmToDate, tm.fdtmFromDate, t.flngSessionID, t.flngLongTermID" +
                ", tm.flngWeekID, tm.flngDayID, tm.flngMonthID, tm.flngYearID \n" +
                "FROM tblTask t\n" +
                "LEFT JOIN tblSession s\n" +
                    "ON s.flngSessionID = t.flngSessionID\n" +
                "JOIN tblTime tm\n" +
                    "ON tm.flngTimeID = IFNULL(s.flngTimeID, t.flngTimeID)\n" +
                "WHERE (t.flngSessionID <> -1 \n" +
                "OR ((tm.flngWeekID <> -1 \n" +
                "OR tm.flngDayID <> -1 \n" +
                "OR tm.flngMonthID <> -1 \n" +
                "OR tm.flngYearID <> -1) \n" +
                "AND t.fblnOneOff = 0)\n" +
                "OR (t.fblnOneOff = 1\n" +
                "AND NOT EXISTS (SELECT 1\n" +
                "FROM tblTaskInstance ti\n" +
                "WHERE ti.flngTaskID = t.flngTaskID\n" +
                "AND ti.fblnComplete = 1))\n" + //One off and not yet complete
                "OR tm.fdtmFromDate >= " + Task_Display.getCurrentCalendar(this).getTimeInMillis() + "\n" +
                "OR tm.fdtmToDate >= " + Task_Display.getCurrentCalendar(this).getTimeInMillis() + ")\n" +
                "AND t.flngEventID = -1\n" +
                "AND t.fblnActive = 1";

        Cursor cursor = DatabaseAccess.mDatabase.rawQuery(rawGetRepeatingTasks,null);

        while (cursor.moveToNext()){
            Calendar currentCalendar = getCurrentCalendar(this);
            Calendar storedCalendar = getCurrentCalendar(this);
            storedCalendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndexOrThrow("fdtmEvaluated")));

            //Need to clear out unnecessary date time details before comparison
            currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
            currentCalendar.set(Calendar.MINUTE, 0);
            currentCalendar.set(Calendar.SECOND, 0);
            currentCalendar.set(Calendar.MILLISECOND, 0);
            storedCalendar.set(Calendar.HOUR_OF_DAY, 0);
            storedCalendar.set(Calendar.MINUTE, 0);
            storedCalendar.set(Calendar.SECOND, 0);
            storedCalendar.set(Calendar.MILLISECOND, 0);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (!storedCalendar.getTime().equals(currentCalendar.getTime()) || prefs.getBoolean("enable_debug", false)){
                //Evaluate repetition time and create new task instance if necessary
                if (evaluateTaskInstanceCreation((long) -1,
                        cursor.getLong(cursor.getColumnIndex("flngTimeID")),
                        cursor.getLong(cursor.getColumnIndex("flngTaskID")),
                        cursor.getLong(cursor.getColumnIndex("flngLongTermID")),
                        this)){

                    //complete any currently active task instance associated with Task ID
                    Cursor taskInstance = DatabaseAccess.retrieveActiveTaskInstanceFromTask(cursor.getLong(cursor.getColumnIndex("flngTaskID")));
                    while (taskInstance.moveToNext()){
                        DatabaseAccess.updateRecordFromTable("tblTaskInstance",
                                "flngInstanceID",
                                taskInstance.getLong(taskInstance.getColumnIndex("flngInstanceID")),
                                new String[]{"fblnSystemComplete"},
                                new Object[]{true});
                    }

                    DatabaseAccess.addRecordToTable("tblTaskInstance",
                            new String[]{"flngTaskID","fblnComplete","fblnSystemComplete","fdtmCreated"},
                            new Object[]{cursor.getLong(cursor.getColumnIndex("flngTaskID")), false, false, currentCalendar.getTimeInMillis()});
                }

                //Update the evaluation date associated with the time element.
                DatabaseAccess.updateRecordFromTable("tblTime",
                        "flngTimeID",
                        cursor.getLong(cursor.getColumnIndex("flngTimeID")),
                        new String[]{"fdtmEvaluated"},
                        new Object[]{currentCalendar.getTimeInMillis()});
            }
        }
    }

    public static boolean evaluateTaskInstanceCreation(Long plngSessionId, Long plngTimeId, Long plngTaskId, Long plngLongTermId, Context pContext){
        if (evaluateTime(plngSessionId,plngTimeId, plngLongTermId, pContext)){
            if(plngSessionId != -1 || !excludeOneOffTasks(plngTaskId)){
                return true;
            }
        }
        return false;
    }

    public static boolean excludeOneOffTasks(Long plngTaskId){
        Cursor cur = DatabaseAccess.getRecordsFromTable("tblTask","flngTaskId",plngTaskId);
        cur.moveToFirst();
        if (cur.getLong(cur.getColumnIndex("fblnOneOff")) == 1){
            cur = DatabaseAccess.getRecordsFromTable("tblTaskInstance", "flngTaskId", plngTaskId);
            while (cur.moveToNext()){
                //Only exclude it if it's been manually completed. System completes don't count
                if(cur.getLong(cur.getColumnIndex("fblnComplete")) == 1) return true;
            }
        }
        return false;
    }

    public static boolean evaluateTime(Long plngSessionId,
                                       Long plngTimeId,
                                       Long plngLongTermId,
                                       Context pContext) {
        Cursor cursor;
        Boolean evaluation = false;
        Long timeID = plngTimeId;
        Long weekID, dayID, monthID, yearID, fromDate, toDate;

        if (plngSessionId != -1) {
            cursor = DatabaseAccess.getRecordsFromTable("tblSession", "flngSessionID", plngSessionId);
            while (cursor.moveToNext()){
                timeID = cursor.getLong(cursor.getColumnIndex("flngTimeID"));
            }
        }
        cursor = DatabaseAccess.getRecordsFromTable("tblTime", "flngTimeID", timeID);
        cursor.moveToFirst();
        weekID = cursor.getLong(cursor.getColumnIndex("flngWeekID"));
        dayID = cursor.getLong(cursor.getColumnIndex("flngDayID"));
        monthID = cursor.getLong(cursor.getColumnIndex("flngMonthID"));
        yearID = cursor.getLong(cursor.getColumnIndex("flngYearID"));
        fromDate =  cursor.getLong(cursor.getColumnIndex("fdtmFromDate"));
        toDate =  cursor.getLong(cursor.getColumnIndex("fdtmToDate"));

        if (weekID != -1) evaluation = evaluateWeekGeneration(weekID,
                cursor.getLong(cursor.getColumnIndex("fdtmCreated")),
                cursor.getLong(cursor.getColumnIndex("flngRepetition")),
                pContext);
        else if (dayID != -1) evaluation = evaluateDayGeneration(cursor.getLong(cursor.getColumnIndex("fdtmCreated")),
                cursor.getLong(cursor.getColumnIndex("flngRepetition")),
                pContext);
        else if (monthID != -1) evaluation = evaluateMonthGeneration(monthID,
                cursor.getLong(cursor.getColumnIndex("fdtmCreated")),
                cursor.getLong(cursor.getColumnIndex("flngRepetition")),
                pContext);
        else if (yearID != -1) evaluation = evaluateYearGeneration(cursor.getLong(cursor.getColumnIndex("fdtmCreated")),
                cursor.getLong(cursor.getColumnIndex("fdtmFromDate")),
                cursor.getLong(cursor.getColumnIndex("fdtmToDate")),
                cursor.getLong(cursor.getColumnIndex("flngRepetition")),
                pContext);
        else if (fromDate == -1 && toDate == -1 && plngLongTermId == -1) return true;
        else evaluation = evaluateDate(fromDate, toDate, pContext);

        return evaluation;
    }

    private static boolean evaluateDate(Long plngFromDate, Long plngToDate, Context pContext){
        Calendar calNow = getCurrentCalendar(pContext);
        Calendar calFrom = getCurrentCalendar(pContext);
        if (plngFromDate != -1) calFrom.setTimeInMillis(plngFromDate);
        Calendar calTo = getCurrentCalendar(pContext);
        if (plngToDate != -1) calTo.setTimeInMillis(plngToDate);

        if (plngFromDate != -1 && plngToDate != -1){
            if(calNow.after(calFrom) && calNow.before(calTo)){
                return true;
            }
        } else if (plngFromDate != -1){
            if(calNow.get(Calendar.DAY_OF_YEAR) == calFrom.get(Calendar.DAY_OF_YEAR) &&
                    calNow.get(Calendar.YEAR) == calFrom.get(Calendar.YEAR)){
                return true;
            }
        }
        //Currently not doing to only

        return false;
    }

    private static boolean evaluateWeekGeneration(Long plngWeekID, Long pdtmCreated, Long pintRepetition, Context pContext){
        Cursor cursor;
        Calendar calCreate = getCurrentCalendar(pContext);
        Calendar calNow = getCurrentCalendar(pContext);
        calCreate.setTimeInMillis(pdtmCreated);

        calCreate.set(Calendar.HOUR_OF_DAY,0);
        calCreate.set(Calendar.MINUTE,0);
        calCreate.set(Calendar.SECOND,0);
        calCreate.set(Calendar.MILLISECOND,0);

        //todo: Add an hour to clock if spring forward occurs between create and now
        //todo: Subtract an hour from clock if fall back occurs between create and now
        //This should really only be important w/i the first few hours of the day (or at the last few hours of the previous day) because it's just dumb

        //Trim off time details from fdtmCreated and fdtmNow
        Long diffInMillisec = calNow.getTimeInMillis() - calCreate.getTimeInMillis();
        Long diffInWeeks = diffInMillisec / (7 * 24 * 60 * 60 * 1000);
        if (diffInWeeks % (pintRepetition) != 0) return false;

        cursor = DatabaseAccess.getRecordsFromTable("tblWeek", "flngWeekID", plngWeekID);
        cursor.moveToFirst();
        switch (calNow.get(Calendar.DAY_OF_WEEK)){
            case Calendar.SUNDAY:
                return cursor.getInt(cursor.getColumnIndex("fblnSunday")) == 1;
            case Calendar.MONDAY:
                return cursor.getInt(cursor.getColumnIndex("fblnMonday")) == 1;
            case Calendar.TUESDAY:
                return cursor.getInt(cursor.getColumnIndex("fblnTuesday")) == 1;
            case Calendar.WEDNESDAY:
                return cursor.getInt(cursor.getColumnIndex("fblnWednesday")) == 1;
            case Calendar.THURSDAY:
                return cursor.getInt(cursor.getColumnIndex("fblnThursday")) == 1;
            case Calendar.FRIDAY:
                return cursor.getInt(cursor.getColumnIndex("fblnFriday")) == 1;
            case Calendar.SATURDAY:
                return cursor.getInt(cursor.getColumnIndex("fblnSaturday")) == 1;
        }
        return false;
    }

    private static boolean evaluateDayGeneration(Long pdtmCreated, Long pintRepetition, Context pContext) {
        Calendar calCreate = getCurrentCalendar(pContext);
        Calendar calNow = getCurrentCalendar(pContext);
        calCreate.setTimeInMillis(pdtmCreated);

        calCreate.set(Calendar.HOUR_OF_DAY,0);
        calCreate.set(Calendar.MINUTE,0);
        calCreate.set(Calendar.SECOND,0);
        calCreate.set(Calendar.MILLISECOND,0);

        Long diffInMillisec = calNow.getTimeInMillis() - calCreate.getTimeInMillis();
        Long diffInDays = diffInMillisec / (24 * 60 * 60 * 1000);
        return diffInDays % (pintRepetition) == 0;

    }

    private static boolean evaluateMonthGeneration(Long plngMonthID, Long pdtmCreated, Long pintRepetition, Context pContext) {
        Calendar calCreate = getCurrentCalendar(pContext);
        Calendar calNow = getCurrentCalendar(pContext);
        calCreate.setTimeInMillis(pdtmCreated);

        calCreate.set(Calendar.HOUR_OF_DAY,0);
        calCreate.set(Calendar.MINUTE,0);
        calCreate.set(Calendar.SECOND,0);
        calCreate.set(Calendar.MILLISECOND,0);

        int diffYear = calNow.get(Calendar.YEAR) - calCreate.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + calNow.get(Calendar.MONTH) - calCreate.get(Calendar.MONTH);
        if (diffMonth % (pintRepetition) != 0) return false;

        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblMonth","flngMonthID", plngMonthID);
        while (cursor.moveToNext()){
            if (cursor.getString(cursor.getColumnIndex("fstrSpecific")).equals("")){
                if (cursor.getLong(cursor.getColumnIndex("fblnFirst")) == 1){
                    Integer dayOfMonth = 1;
                    if(cursor.getLong(cursor.getColumnIndex("fblnAfterWkn")) == 1){
                        Calendar calProject = getCurrentCalendar(pContext);
                        calProject.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        switch (calProject.get(Calendar.DAY_OF_WEEK)){
                            case 1:
                                dayOfMonth = dayOfMonth + 1;
                                break;
                            case 7:
                                dayOfMonth = dayOfMonth + 2;
                                break;
                            default: break;
                        }
                    }
                    if (calNow.get(Calendar.DAY_OF_MONTH) == dayOfMonth) {
                        return true;
                    }
                }

                if (cursor.getLong(cursor.getColumnIndex("fblnLast")) == 1){
                    //Establish Last Day of Month
                    Calendar lastDayOfMonth = getCurrentCalendar(pContext);
                    lastDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
                    lastDayOfMonth.add(Calendar.MONTH, 1);
                    lastDayOfMonth.add(Calendar.DAY_OF_YEAR,-1);

                    //Establish Prior Last Day of Month
                    Calendar priorLastDayOfMonth = getCurrentCalendar(pContext);
                    priorLastDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
                    priorLastDayOfMonth.add(Calendar.DAY_OF_YEAR,-1);
                    switch (priorLastDayOfMonth.get(Calendar.DAY_OF_WEEK)){
                        case 1:
                            priorLastDayOfMonth.add(Calendar.DAY_OF_YEAR,1);
                            break;
                        case 7:
                            priorLastDayOfMonth.add(Calendar.DAY_OF_YEAR,2);
                            break;
                        default:
                            break;
                    }

                    Integer dayOfMonth = 0;
                    if(cursor.getLong(cursor.getColumnIndex("fblnAfterWkn")) == 1){
                        dayOfMonth = priorLastDayOfMonth.get(Calendar.DAY_OF_MONTH);
                        if (calNow.get(Calendar.DAY_OF_MONTH) == dayOfMonth) {
                            return true;
                        } else {
                            dayOfMonth = lastDayOfMonth.get(Calendar.DAY_OF_MONTH);
                            Calendar calProject = getCurrentCalendar(pContext);
                            calProject.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                            switch (calProject.get(Calendar.DAY_OF_WEEK)){
                                case 1:
                                    dayOfMonth = dayOfMonth + 1;
                                    break;
                                case 7:
                                    dayOfMonth = dayOfMonth + 2;
                                    break;
                                default:
                                   break;
                            }
                            if (calNow.get(Calendar.DAY_OF_MONTH) == dayOfMonth) {
                                return true;
                            }
                        }
                    } else {
                        dayOfMonth = lastDayOfMonth.get(Calendar.DAY_OF_MONTH);
                        if (calNow.get(Calendar.DAY_OF_MONTH) == dayOfMonth) {
                            return true;
                        }
                    }
                }

                if (cursor.getLong(cursor.getColumnIndex("fblnMiddle")) == 1) {
                    Integer dayOfMonth = 15;
                    if (cursor.getLong(cursor.getColumnIndex("fblnAfterWkn")) == 1) {
                        Calendar calProject = getCurrentCalendar(pContext);
                        calProject.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        switch (calProject.get(Calendar.DAY_OF_WEEK)) {
                            case 1:
                                dayOfMonth = dayOfMonth + 1;
                                break;
                            case 7:
                                dayOfMonth = dayOfMonth + 2;
                                break;
                            default:
                                break;
                        }
                    }
                    if (calNow.get(Calendar.DAY_OF_MONTH) == dayOfMonth) {
                        return true;
                    }
                }
            } else {
                String strSpecificDays[] = cursor.getString(cursor.getColumnIndex("fstrSpecific")).split(",");
                for (int i = 0; i < strSpecificDays.length; i++){
                    if (calNow.get(Calendar.DAY_OF_MONTH) == Long.parseLong(strSpecificDays[i].trim())){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean evaluateYearGeneration(Long pdtmCreated, Long pdtmFromDate, Long pdtmToDate, Long pintRepetition, Context pContext) {
        Calendar calCreate = getCurrentCalendar(pContext);
        Calendar calNow = getCurrentCalendar(pContext);
        calCreate.setTimeInMillis(pdtmCreated);

        calCreate.set(Calendar.HOUR_OF_DAY,0);
        calCreate.set(Calendar.MINUTE,0);
        calCreate.set(Calendar.SECOND,0);
        calCreate.set(Calendar.MILLISECOND,0);

        int diffYear = calNow.get(Calendar.YEAR) - calCreate.get(Calendar.YEAR);
        if (diffYear % (pintRepetition) != 0) return false;

        Calendar calFrom = Calendar.getInstance(), calTo = getCurrentCalendar(pContext);
        calFrom.setTimeInMillis(pdtmFromDate);
        if (pdtmToDate != -1){
            calTo.setTimeInMillis(pdtmToDate);
            if (calNow.after(calFrom) && calNow.before(calTo)){
                return true;
            }
        } else {
            if (calNow.get(Calendar.DAY_OF_YEAR) == calFrom.get(Calendar.DAY_OF_YEAR)){
                return true;
            }
        }
        return false;
    }

    /** Called when the user taps the Send button */
    public void createTask(View view) {
        Intent intent = new Intent(this, Task_Task.class);
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
        Cursor cursor = DatabaseAccess.getTaskInstancesWithDetails();
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

        ArrayList<taskInstances> priorityList = new ArrayList();
        ArrayList<taskInstances> todayList = new ArrayList();
        ArrayList<taskInstances> standardList = new ArrayList();

        while(cursor.moveToNext()){
            Boolean blnRepeat = cursor.getLong(cursor.getColumnIndex("flngRepetition")) != 0;
            char result = determineListForTask(cursor.getLong(cursor.getColumnIndex("fdtmFrom")),
                    cursor.getLong(cursor.getColumnIndex("fdtmTo")),
                    cursor.getLong(cursor.getColumnIndex("fdtmCreated")),
                    blnRepeat, pContext);
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
            } else {
                standardList.add(new taskInstances(cursor.getString(cursor.getColumnIndex("fstrTitle")),
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

        mDisplayListView.setAdapter(mAdapter);
        mDisplayListView.setOnItemClickListener(itemClickListener);
        mDisplayListView.setOnItemLongClickListener(itemLongClickListener);
    }

    private static char determineListForTask(Long pdtmFrom, Long pdtmTo, Long pdtmCreated, Boolean fblnRepeat, Context pContext) {
        char result = ' ';
        Calendar calNow = getCurrentCalendar(pContext);
        Calendar calInt = getCurrentCalendar(pContext);
        Calendar calCreate = getCurrentCalendar(pContext);
        Calendar calTo = null;
        Calendar calFrom = null;
        Calendar calFromBefore = null;
        Calendar calToBefore = null;

        calCreate.setTimeInMillis(pdtmCreated);

        if (pdtmFrom != -1) {
            calFrom = getCurrentCalendar(pContext);
            calInt.setTimeInMillis(pdtmFrom);
            //it'll retain today's date but take the time
            calFrom.set(Calendar.HOUR_OF_DAY, calInt.get(Calendar.HOUR_OF_DAY));
            calFrom.set(Calendar.MINUTE, calInt.get(Calendar.MINUTE));
            calFromBefore = (Calendar) calFrom.clone();
            calFromBefore.add(Calendar.DAY_OF_YEAR, -1);

        }
        if (pdtmTo != -1) {
            calTo = getCurrentCalendar(pContext);
            calInt.setTimeInMillis(pdtmTo);
            calTo.set(Calendar.HOUR_OF_DAY, calInt.get(Calendar.HOUR_OF_DAY));
            calTo.set(Calendar.MINUTE, calInt.get(Calendar.MINUTE));
            calToBefore = (Calendar) calTo.clone();
            calToBefore.add(Calendar.DAY_OF_YEAR, -1);
        }

        //Evaluate Time Details
        if (calFrom != null && calTo != null){
            //Handles where time is between from and to
            if (calNow.get(Calendar.DAY_OF_YEAR) == calCreate.get(Calendar.DAY_OF_YEAR)){
                if (calNow.after(calFrom) && calNow.before(calTo)){
                    result = 'P';
                } else result = 'T';
            } else if (calCreate.after(calFromBefore) && !fblnRepeat){
                if (calNow.after(calFrom) && calNow.before(calTo)){
                    result = 'P';
                } else result = 'T';
            } else result = 'S';
        } else if (calFrom != null) {
            if (calNow.get(Calendar.DAY_OF_YEAR) == calCreate.get(Calendar.DAY_OF_YEAR)){
                if(calNow.after(calFrom)) {
                    result = 'P';
                } else result = 'T';
            } else if (calCreate.after(calFromBefore) && !fblnRepeat){
                if (calNow.after(calFrom)){
                    result = 'P';
                } else result = 'T';
            } else result = 'S';
        } else if (calTo != null) {
            if (calNow.get(Calendar.DAY_OF_YEAR) == calCreate.get(Calendar.DAY_OF_YEAR)){
                if(calNow.before(calTo)) {
                    result = 'P';
                } else result = 'T';
            } else if (calCreate.after(calToBefore) && !fblnRepeat){
                if (calNow.after(calTo)){
                    result = 'P';
                } else result = 'T';
            } else result = 'S';
        } else if (calNow.get(Calendar.DAY_OF_YEAR) == calCreate.get(Calendar.DAY_OF_YEAR)) result = 'T';
        else result = 'S';
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
