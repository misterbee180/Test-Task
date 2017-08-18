package com.example.testtask;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class Task_Display extends AppCompatActivity {
    static ArrayListContainer mPriorityList;
    static ArrayListContainer mStandardList;
    static ArrayListContainer mTodayList;
    static ArrayListContainer mEventsList;
    static LinearLayout llEventDisplay;

    static ArrayList<ArrayListContainer> mEventList;

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
        initializeActivity();
    }

    private void initializeApplication() {
        DatabaseAccess.setContext(this);
    }

    private void initializeActivity() {
        //Set Member Values
        mPriorityList = new ArrayListContainer();
        mTodayList = new ArrayListContainer();
        mStandardList = new ArrayListContainer();
        llEventDisplay = (LinearLayout) findViewById(R.id.llEventsDisplay);

        ListView mPriorityView = (ListView) findViewById(R.id.lsvPriorityList);
        mPriorityList.LinkArrayToListView(mPriorityView, this);
        mPriorityList.mListView.setOnItemClickListener(itemClickListener);
        //mPriorityList.mListView.setOnItemLongClickListener(itemLongClickListener);

        ListView mStandardView = (ListView) findViewById(R.id.lsvStandardList);
        mStandardList.LinkArrayToListView(mStandardView, this);
        mStandardList.mListView.setOnItemClickListener(itemClickListener);
        //mStandardList.mListView.setOnItemLongClickListener(itemLongClickListener);

        ListView mTodayView = (ListView) findViewById(R.id.lsvTodayList);
        mTodayList.LinkArrayToListView(mTodayView, this);
        mTodayList.mListView.setOnItemClickListener(itemClickListener);
        //mTodayList.mListView.setOnItemLongClickListener(itemLongClickListener);

        mEventList = new ArrayList<ArrayListContainer>();
    }

    private static ArrayListContainer getSelectedArrayListContainer(AdapterView<?> parent){
        ArrayListContainer tmpArrayList = null;

        switch (parent.getId()){
            case R.id.lsvPriorityList:
                tmpArrayList = mPriorityList;
                break;
            case R.id.lsvTodayList:
                tmpArrayList = mTodayList;
                break;
            case R.id.lsvStandardList:
                tmpArrayList = mStandardList;
                break;
            default:
                //search array of ArrayListContainers
                for (int i=0; i<mEventList.size(); i++){
                    if (mEventList.get(i).mListView.getId() == parent.getId()){
                        tmpArrayList = mEventList.get(i);
                        break;
                    }
                }
                break;
        }

        return tmpArrayList;
    }

    static AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {
            ArrayListContainer tmpArrayList = getSelectedArrayListContainer(parent);
            Bundle bundle = new Bundle();
            bundle.putLong("TaskID", tmpArrayList.GetID(position));
            DialogFragment newFragment = new Task_Display.ConfirmationFragment();
            newFragment.setArguments(bundle);
            FragmentActivity activity = (FragmentActivity)parent.getContext();
            newFragment.show(activity.getSupportFragmentManager(), "Complete Task");
        }
    };

    public static class ConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpTaskID = getArguments().getLong("TaskID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Complete Task")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DatabaseAccess.updateTaskInstanceComplete(tmpTaskID);
                            loadTasksFromDatabase();
                            loadEventTasks(getActivity());
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
        loadTasksFromDatabase();
        loadEventTasks(this);
    }

    private void generateTaskInstances() {
        Cursor cursor = DatabaseAccess.getRepeatingTask();
        while (cursor.moveToNext()){
            Calendar currentCalendar = Calendar.getInstance();
            Calendar storedCalendar = Calendar.getInstance();
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

            if (!storedCalendar.getTime().equals(currentCalendar.getTime())){
                //complete any currently active task instance associated with Task ID
                Cursor taskInstance = DatabaseAccess.retrieveActiveTaskInstanceFromTask(cursor.getLong(cursor.getColumnIndex("flngID")));
                while (taskInstance.moveToNext()){
                    DatabaseAccess.updateTaskInstanceSystemComplete(taskInstance.getLong(taskInstance.getColumnIndex("flngID")));
                }

                //Evaluate repetition time and create new task instance if necessary
                if (evaluateTime((long)-1, cursor.getLong(cursor.getColumnIndex("flngTimeID")))){
                    DatabaseAccess.insertTaskInstance(cursor.getLong(cursor.getColumnIndex("flngID")));
                }

                //Update the evaluation date associated with the time element.
                DatabaseAccess.updateTimeEvaluated(currentCalendar.getTimeInMillis(),
                        cursor.getLong(cursor.getColumnIndex("flngTimeID")));
            }
        }
    }

    private boolean evaluateTime(Long plngSessionId,
                                 Long plngTimeId) {
        Cursor cursor;
        Calendar calendar = Calendar.getInstance();
        Boolean evaluation = false;
        Long timeID = plngTimeId;
        Long weekID;

        if (plngSessionId != -1) {
            cursor = DatabaseAccess.getRecordFromTable("tblSession", plngSessionId);
            while (cursor.moveToNext()){
                timeID = cursor.getLong(cursor.getColumnIndex("flngTimeID"));
            }
        }
        cursor = DatabaseAccess.getRecordFromTable("tblTime", timeID);
        cursor.moveToFirst();
        weekID = cursor.getLong(cursor.getColumnIndex("flngWeekID"));

        if (weekID != -1){
            cursor = DatabaseAccess.getRecordFromTable("tblWeek", weekID);
            cursor.moveToFirst();
            switch (calendar.get(Calendar.DAY_OF_WEEK)){
                case Calendar.SUNDAY:
                    evaluation = cursor.getInt(cursor.getColumnIndex("fblnSunday")) == 1;
                    break;
                case Calendar.MONDAY:
                    evaluation = cursor.getInt(cursor.getColumnIndex("fblnMonday")) == 1;
                    break;
                case Calendar.TUESDAY:
                    evaluation = cursor.getInt(cursor.getColumnIndex("fblnTuesday")) == 1;
                    break;
                case Calendar.WEDNESDAY:
                    evaluation = cursor.getInt(cursor.getColumnIndex("fblnWednesday")) == 1;
                    break;
                case Calendar.THURSDAY:
                    evaluation = cursor.getInt(cursor.getColumnIndex("fblnThursday")) == 1;
                    break;
                case Calendar.FRIDAY:
                    evaluation = cursor.getInt(cursor.getColumnIndex("fblnFriday")) == 1;
                    break;
                case Calendar.SATURDAY:
                    evaluation = cursor.getInt(cursor.getColumnIndex("fblnSaturday")) == 1;
                    break;
            }
        }

        return evaluation;
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

    public void viewEvents() {
        Intent intent = new Intent(this, Viewer_Events.class);
        startActivity(intent);
    }

    public static void loadEventTasks(Context pContext){
        Cursor cursor = DatabaseAccess.retrieveEventTaskInstances();
        Long lngEventId = (long)-1;
        ListView tmpEventListView = null;
        ArrayListContainer tmpEventALC = null;

        llEventDisplay.removeAllViews();
        mEventList.clear();
        while(cursor.moveToNext()){
            if (lngEventId != cursor.getLong(cursor.getColumnIndex("flngEventId"))){
                lngEventId = cursor.getLong(cursor.getColumnIndex("flngEventId"));
                TextView tmpEventTitle = new TextView(pContext);
                tmpEventTitle.setText(cursor.getString(cursor.getColumnIndex("fstrEventTitle")));
                tmpEventTitle.setTextSize(14);
                tmpEventTitle.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT,Toolbar.LayoutParams.WRAP_CONTENT));
                llEventDisplay.addView(tmpEventTitle);

                tmpEventListView = new ListView(pContext);
                tmpEventListView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT));
                tmpEventALC = new ArrayListContainer();
                tmpEventALC.LinkArrayToListView(tmpEventListView, pContext);
                tmpEventALC.mListView.setOnItemClickListener(itemClickListener);
                mEventList.add(tmpEventALC);
                llEventDisplay.addView(tmpEventListView);
                //llEvents.addView(tmpEventTitle);
            }
            tmpEventALC.Add(cursor.getString(cursor.getColumnIndex("fstrTaskTitle")), cursor.getLong(cursor.getColumnIndex("flngTaskInstanceId")));
            tmpEventALC.mAdapter.notifyDataSetChanged();
        }
    }

    public static void loadTasksFromDatabase(){
        Cursor cursor = DatabaseAccess.getTaskInstancesWithDetails();

        mPriorityList.Clear();
        mStandardList.Clear();
        mTodayList.Clear();
        while(cursor.moveToNext()){
            char result = determineListForTask(cursor.getLong(cursor.getColumnIndex("fdtmFrom")),
                    cursor.getLong(cursor.getColumnIndex("fdtmTo")),
                    cursor.getLong(cursor.getColumnIndex("flngWeekID")));
            if (result == 'P') {
                mPriorityList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngID")));
            } else if (result == 'T') {
                mTodayList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngID")));
            } else {
                mStandardList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngID")));
            }
        }
        mPriorityList.mAdapter.notifyDataSetChanged();
        mStandardList.mAdapter.notifyDataSetChanged();
        mTodayList.mAdapter.notifyDataSetChanged();
    }

    private static char determineListForTask(Long pdtmFrom, Long pdtmTo, Long plngWeekID) {
        char result = ' ';
        Calendar calNow = Calendar.getInstance();
        Calendar calInt = Calendar.getInstance();
        Calendar calTo = null;
        Calendar calFrom = null;
        if (pdtmFrom != 0) {
            calFrom = Calendar.getInstance();
            calInt.setTimeInMillis(pdtmFrom);
            //it'll retain today's date but take the time
            calFrom.set(Calendar.HOUR_OF_DAY, calInt.get(Calendar.HOUR_OF_DAY));
            calFrom.set(Calendar.MINUTE, calInt.get(Calendar.MINUTE));
        }
        if (pdtmTo != 0) {
            calTo = Calendar.getInstance();
            calInt.setTimeInMillis(pdtmTo);
            calTo.set(Calendar.HOUR_OF_DAY, calInt.get(Calendar.HOUR_OF_DAY));
            calTo.set(Calendar.MINUTE, calInt.get(Calendar.MINUTE));
        }
        if (calFrom != null && calTo != null){
            if (calNow.after(calFrom) && calNow.before(calTo)){
                result = 'P';
            }
        } else if (calFrom != null) {
            if (calNow.after(calFrom)) {
                result = 'P';
            }
        } else if (calTo != null) {
            Calendar tmpCalTo = calTo;
            tmpCalTo.add(Calendar.HOUR_OF_DAY,-2);
            if (calNow.after(tmpCalTo) && calNow.before(calTo)){
                result = 'P';
            }
        }

        //Using the fact that if a week id is set that it's repeating and also that it was generated so it has to be due today
        if (result == ' ') {
            if (plngWeekID != -1) {
                result = 'T';
            } else {
                result = 'S';
            }
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()){
            case R.id.action_settings:
                return true;
            case R.id.action_session:
                viewSessions();
                break;
            case R.id.action_task:
                viewTasks();
                break;
            case R.id.action_event  :
                viewEvents();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
