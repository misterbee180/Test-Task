package com.example.testtask;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.example.testtask.DatabaseAccess.mDatabase;

public class Viewer_Task extends AppCompatActivity {

    static ListView mTaskView;
    static Sorting mSorting;
    static CustomAdapter mAdapter;

    enum Sorting
    {
        Ascending, Created, Group, Session;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTaskView = (ListView) findViewById(R.id.lsvTaskList);
        mSorting = Sorting.Ascending;
    }

    protected void onResume(){
        super.onResume();
        setTaskList(this);
    }

    public static AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            FragmentActivity activity;
            int type = mAdapter.getItemViewType(position);
            switch (type) {
                case 0:
                    bundle.putLong("TaskID", Long.valueOf(((CustomAdapter.ViewHolder)view.getTag()).id.getText().toString()));
                    DialogFragment newFragment = new Viewer_Task.TaskEditConfirmationFragment();
                    newFragment.setArguments(bundle);
                    activity = (FragmentActivity)parent.getContext();
                    newFragment.show(activity.getSupportFragmentManager(), "Edit Task");
                    break;
            }
        }
    };

    public static AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            FragmentActivity activity;
            bundle.putLong("TaskID", Long.valueOf(((CustomAdapter.ViewHolder)view.getTag()).id.getText().toString()));
            DialogFragment newFragment = new Viewer_Task.TaskDeleteConfirmationFragment();
            newFragment.setArguments(bundle);
            activity = (FragmentActivity)parent.getContext();
            newFragment.show(activity.getSupportFragmentManager(), "Delete Task");
            return true;
        }
    };

    public static class TaskEditConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpTaskID = getArguments().getLong("TaskID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Edit Task")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getActivity(), Task_Task.class);
                            intent.putExtra("EXTRA_TASK_ID", tmpTaskID);
                            startActivity(intent);
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

    public static class TaskDeleteConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpTaskID = getArguments().getLong("TaskID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete Task")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteTask(tmpTaskID);
                            setTaskList(getContext());
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

    public static void deleteTask(Long plngTaskId){
        DatabaseAccess.updateRecordFromTable("tblTask",
                "flngTaskID",
                plngTaskId,
                new String[]{"fblnActive"},
                new Object[]{false});
        Cursor cursor = DatabaseAccess.retrieveActiveTaskInstanceFromTask(plngTaskId);
        if(cursor.moveToNext()){
            DatabaseAccess.updateRecordFromTable("tblTaskInstance",
                    "flngInstanceID",
                    cursor.getLong(cursor.getColumnIndex("flngInstanceID")),
                    new String[]{"fblnSystemComplete"},
                    new Object[]{true});
        }
    }

    public static void setTaskList(Context pContext){
        Cursor cursor;
        String rawGetTasks = "SELECT t.*, tm.fdtmCreated, g.fstrTitle as fstrGroup, s.fstrTitle as fstrSession\n" +
                "FROM tblTask t\n" +
                "LEFT JOIN tblTime tm\n" +
                "ON tm.flngTimeID = t.flngTimeID\n" +
                "LEFT JOIN tblGroup g\n" +
                "ON g.flngGroupID = t.flngGroupID\n" +
                "LEFT JOIN tblSession s\n" +
                "ON s.flngSessionID = t.flngSessionID\n" +
                "WHERE ((t.flngSessionID <> -1 \n" +
                    "\tAND (t.fblnOneOff = 0\n" +
                    "\tOR NOT EXISTS (SELECT 1\n" +
                        "\t\tFROM tblTaskInstance ti\n" +
                        "\t\tWHERE ti.flngTaskID = t.flngTaskID\n" +
                        "\t\tAND ti.fblnComplete = 1)))\n" +
                //Since we are changing the way one off's work we are going to leave this in so prior tasks don't show
                "OR EXISTS (SELECT 1\n" +
                    "\tFROM tblTime tm\n" +
                    "\tWHERE tm.flngTimeID = t.flngTimeID\n" +
                    "\tAND (tm.flngWeekID <> -1 OR tm.flngDayId <> -1 OR tm.flngMonthId <> -1 OR tm.flngYearID <> -1)\n" +
                    "\tAND t.fblnOneOff = 0)\n" + //Associated with a repetition & not one off
                "OR (t.fblnOneOff = 1\n" +
                    "\tAND NOT EXISTS (SELECT 1\n" +
                        "\t\tFROM tblTaskInstance ti\n" +
                        "\t\tWHERE ti.flngTaskID = t.flngTaskID\n" +
                        "\t\tAND ti.fblnComplete = 1))\n" + //One off and not yet complete
                "OR EXISTS (SELECT 1\n " +
                    "\tFROM tblTime tm\n" +
                    "\tWHERE tm.flngTimeID = t.flngTimeID\n" +
                    "\tAND (tm.fdtmFromDate >= " + Task_Display.getCurrentCalendar(pContext).getTimeInMillis() + " \n" +
                    "\tOR tm.fdtmToDate >= " + Task_Display.getCurrentCalendar(pContext).getTimeInMillis() + "))\n" + //There's a future task available
                "OR EXISTS (SELECT 1\n" +
                    "\tFROM tblTaskInstance ti\n" +
                    "\tWHERE ti.flngTaskID = t.flngTaskID\n" +
                    "\tAND ti.fblnComplete = 0\n" +
                    "\tAND ti.fblnSystemComplete = 0))\n" + //Has an active task instance
                "AND t.flngEventID = -1\n" +
                "AND t.fblnActive = 1\n";
        switch(mSorting) {
            case Ascending:
                rawGetTasks += "ORDER BY t.fstrTitle";
                break;
            case Created:
                rawGetTasks += "ORDER BY tm.fdtmCreated Desc";
                break;
            case Group:
                rawGetTasks += "ORDER BY ifNULL(g.fstrTitle,\"z\"), t.fstrTitle";
                break;
            case Session:
                rawGetTasks += "ORDER BY ifNULL(s.fstrTitle,\"z\"), t.fstrTitle";
                break;
//            case Group:
//                rawGetTasks += "ORDER BY g.fstrTitle, t.fstrTitle";
//                break;
//            case Session:
//                rawGetTasks += "ORDER BY s.fstrTitle, t.fstrTitle";
//                break;
        }
        cursor = mDatabase.rawQuery(rawGetTasks,null);
        mAdapter = new CustomAdapter(pContext);
        Calendar calCreated = Task_Display.getCurrentCalendar(pContext);
        calCreated.add(Calendar.DAY_OF_YEAR,1);
        Calendar calNewCreated;
        String fstrSession = "";
        String fstrGroup = "";

        while(cursor.moveToNext())
        {
            switch(mSorting) {
                case Ascending:
                    mAdapter.addItem(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngTaskID")));
                    break;
                case Created:
                    calNewCreated = Task_Display.getCurrentCalendar(pContext);
                    calNewCreated.setTimeInMillis(cursor.getLong(cursor.getColumnIndex("fdtmCreated")));
                    if (calNewCreated.get(Calendar.DAY_OF_YEAR) != calCreated.get(Calendar.DAY_OF_YEAR) ||
                    calNewCreated.get(Calendar.YEAR) != calCreated.get(Calendar.YEAR)){
                        calCreated = calNewCreated;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
                        mAdapter.addSeparatorItem(dateFormat.format(calCreated.getTime()));
                    }
                    mAdapter.addItem(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngTaskID")));
                    break;
                case Group:
                    if (cursor.getString(cursor.getColumnIndex("fstrGroup")) != null &&
                            fstrGroup.compareTo(cursor.getString(cursor.getColumnIndex("fstrGroup"))) != 0){
                        fstrGroup = cursor.getString(cursor.getColumnIndex("fstrGroup"));
                        mAdapter.addSeparatorItem(fstrGroup);
                    } else if (cursor.getString(cursor.getColumnIndex("fstrGroup")) == null && fstrGroup != null){
                        fstrGroup = null;
                        mAdapter.addSeparatorItem("No Group");
                    }
                    mAdapter.addItem(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngTaskID")));
                    break;
                case Session:
                    if (cursor.getString(cursor.getColumnIndex("fstrSession")) != null &&
                            fstrSession.compareTo(cursor.getString(cursor.getColumnIndex("fstrSession"))) != 0){
                        fstrSession = cursor.getString(cursor.getColumnIndex("fstrSession"));
                        mAdapter.addSeparatorItem(fstrSession);
                    } else if (cursor.getString(cursor.getColumnIndex("fstrSession")) == null && fstrSession != null){
                        fstrSession = null;
                        mAdapter.addSeparatorItem("No Session");
                    }
                    mAdapter.addItem(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngTaskID")));
                    break;
            }
        }

        mTaskView.setAdapter(mAdapter);
        mTaskView.setOnItemClickListener(itemClickListener);
        mTaskView.setOnItemLongClickListener(itemLongClickListener);

        //ORIGINAL
//        mTaskList.Clear();
//        while (cursor.moveToNext()){
//            mTaskList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngTaskID")));
//        }
//        mTaskList.mAdapter.notifyDataSetChanged();
    }

    public void viewComplete() {
        Intent intent = new Intent(this, Viewer_Task.class);
        startActivity(intent);
    }

    public void sortCreated() {
        mSorting = Sorting.Created;
        setTaskList(this);
    }

    public void sortGroup() {
        mSorting = Sorting.Group;
        setTaskList(this);
    }

    public void sortSession() {
        mSorting = Sorting.Session;
        setTaskList(this);
    }

    public void sortAscending() {
        mSorting = Sorting.Ascending;
        setTaskList(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()){
            case R.id.action_completed:
                viewComplete();
                break;
            case R.id.action_sort_created:
                sortCreated();
                break;
            case R.id.action_sort_group:
                sortGroup();
                break;
            case R.id.action_sort_session:
                sortSession();
                break;
            case R.id.action_sort_ascending:
                sortAscending();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
