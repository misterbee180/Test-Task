package com.example.testtask;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
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
    static ArrayListContainer mPriorityList;
    static ArrayListContainer mStandardList;
    static ArrayListContainer mTodayList;
    static DatabaseAccess.TblTaskOpenHelper TaskDatabase;
    static SQLiteDatabase mDataBase;

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

        //Set Member Values
        mPriorityList = new ArrayListContainer();
        mTodayList = new ArrayListContainer();
        mStandardList = new ArrayListContainer();
        TaskDatabase = new DatabaseAccess.TblTaskOpenHelper(this);
        mDataBase = TaskDatabase.getWritableDatabase();

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

    private ArrayListContainer getSelectedArrayListContainer(AdapterView<?> parent){
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

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {
            ArrayListContainer tmpArrayList = getSelectedArrayListContainer(parent);
            Bundle bundle = new Bundle();
            bundle.putLong("TaskID", tmpArrayList.GetID(position));
            DialogFragment newFragment = new Task_Display.ConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Complete Task");
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
                            completeTaskInstance(tmpTaskID);
                            loadTasksFromDatabase();
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
    }

    private void addEventList(){
        //Create a new Event
        ArrayListContainer tmpALContainer = new ArrayListContainer();
        ListView tmpEventListView = new ListView(this);
        tmpALContainer.LinkArrayToListView(tmpEventListView, this);
        tmpALContainer.mListView.setOnItemClickListener(itemClickListener);
        mEventList.add(tmpALContainer);
    }

    private void generateTaskInstances() {
        Cursor cursor = null;
        String rawGetRepeatingTasks = "SELECT t.flngID as flngID, IFNULL(tm.flngID,tms.flngID) as flngTimeID, IFNULL(tm.fdtmEvaluated,tms.fdtmEvaluated) as fdtmEvaluated \n" +
                "FROM tblTask t\n" +
                "LEFT JOIN tblTime tm\n" +
                "ON t.flngTimeID = tm.flngID\n" +
                "LEFT JOIN tblSession s\n" +
                "ON s.flngID = t.flngSessionID\n" +
                "LEFT JOIN tblTime tms\n" +
                "ON tms.flngID = s.flngTimeID\n" +
                "WHERE IFNULL(tm.flngWeekID,tms.flngWeekID) <> -1";

        cursor = mDataBase.rawQuery(rawGetRepeatingTasks,null);
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
                systemCompleteTaskInstance(cursor.getLong(cursor.getColumnIndex("flngID")));

                if (evaluateTime((long)-1, cursor.getLong(cursor.getColumnIndex("flngTimeID")))){
                    //create a new task instance for that task id
                    createTaskInstance(cursor.getLong(cursor.getColumnIndex("flngID")));
                }
                //Update the evaluation date associated with the time element.
                updateTimeEvaluated(currentCalendar.getTimeInMillis(),
                        cursor.getLong(cursor.getColumnIndex("flngTimeID")));
            }
        }
    }

    private boolean evaluateTime(Long plngSessionId,
                                 Long plngTimeId) {
        Cursor cursor = null;
        Calendar calendar = Calendar.getInstance();
        Boolean evaluation = false;
        if (plngSessionId != -1) {
            String rawGetTimeDetailsFromSession = "SELECT t.* \n" +
                    "FROM tblTime t \n" +
                    "JOIN tblSession s \n" +
                    "ON s.flngTimeID = t.flngID \n" +
                    "JOIN tblWeek w \n" +
                    "ON w.flngID = t.flngWeekID \n" +
                    "WHERE s.flngID = ? \n";
            String[] parameters = {Long.toString(plngSessionId)};
            cursor = mDataBase.rawQuery(rawGetTimeDetailsFromSession,parameters);
        } else if (plngTimeId != -1){
            String rawGetTimeDetailsFromSession = "SELECT w.* \n" +
                    "FROM tblTime t \n" +
                    "JOIN tblWeek w \n" +
                    "ON w.flngID = t.flngWeekID \n" +
                    "WHERE t.flngID = ?";
            String[] parameters = {Long.toString(plngTimeId)};
            cursor = mDataBase.rawQuery(rawGetTimeDetailsFromSession,parameters);
        }

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

        return evaluation;
    }

    private void updateTimeEvaluated(Long fdtmTimeMili, Long flngTimeID) {

        String rawUpdateTimeEvaluated = "UPDATE tblTime " +
                "SET fdtmEvaluated = " + Long.toString(fdtmTimeMili) + " " +
                "WHERE flngID = " + Long.toString(flngTimeID);
        Cursor c = mDataBase.rawQuery(rawUpdateTimeEvaluated,null);
        c.moveToFirst();
        c.close();
    }

    private long createTaskInstance(Long plngTaskID) {
        ContentValues values = new ContentValues();
        values.put("flngTaskID",plngTaskID);
        values.put("fblnComplete",0);
        values.put("fblnSystemComplete",0);
        return mDataBase.insertOrThrow("tblTaskInstance",null,values);
    }

    /** Called when the user taps the Send button */
    public void createTask(View view) {
        Intent intent = new Intent(this, Task_Task.class);
        startActivity(intent);
    }

    public void viewTasks() {
        Intent intent = new Intent(this, Task_Viewer.class);
        startActivity(intent);
    }

    public void viewSessions() {
        Intent intent = new Intent(this, Session_Viewer.class);
        startActivity(intent);
    }

    public static void completeTaskInstance(Long plngTaskId){
        String rawCompleteTaskInstance = "UPDATE tblTaskInstance " +
                "SET fblnComplete = 1 " +
                "WHERE flngID = " + Long.toString(plngTaskId) + " " +
                "AND fblnSystemComplete = 0 AND fblnComplete = 0";
        Cursor c = mDataBase.rawQuery(rawCompleteTaskInstance,null);
        c.moveToFirst();
        c.close();
    }

    public static void systemCompleteTaskInstance(Long plngTaskId) {
        String rawSystemCompleteTaskInstance = "UPDATE tblTaskInstance " +
                "SET fblnSystemComplete = 1 " +
                "WHERE flngID = " + Long.toString(plngTaskId) + " " +
                "AND fblnSystemComplete = 0 AND fblnComplete = 0";
        Cursor c = mDataBase.rawQuery(rawSystemCompleteTaskInstance,null);
        c.moveToFirst();
        c.close();
    }

    public static void loadTasksFromDatabase(){
        String rawQuery = "SELECT i.flngID, t.fstrTitle, IFNULL(tm.fdtmFrom,tms.fdtmFrom) as fdtmFrom, IFNULL(tm.fdtmTo,tms.fdtmTo) as fdtmTo, IFNULL(tm.flngWeekID, tms.flngWeekID) as flngWeekID \n" +
                "FROM tblTask t \n" +
                "JOIN tblTaskInstance i \n" +
                "ON t.flngId = i.flngTaskId \n" +
                "LEFT JOIN tblTime tm \n" +
                "ON tm.flngID = t.flngTimeID \n" +
                "LEFT JOIN tblSession s \n" +
                "ON s.flngID = t.flngSessionID \n" +
                "LEFT JOIN tblTime tms \n" +
                "ON tms.flngID = s.flngTimeID \n" +
                "WHERE i.fblnComplete = 0 \n" +
                "AND i.fblnSystemComplete = 0 \n";

        Cursor cursor = mDataBase.rawQuery(rawQuery,null);

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
        }

        return super.onOptionsItemSelected(item);
    }

}
