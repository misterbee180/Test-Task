package com.example.testtask;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Calendar;

import static com.example.testtask.DatabaseAccess.mDatabase;

public class Viewer_Task extends AppCompatActivity {

    static ArrayListContainer mTaskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer_task);

        ListView mTaskView = (ListView) findViewById(R.id.lsvTaskList);
        mTaskList = new ArrayListContainer();
        mTaskList.LinkArrayToListView(mTaskView, this);
        mTaskList.mListView.setOnItemClickListener(itemClickListener);
        mTaskList.mListView.setOnItemLongClickListener(itemLongClickListener);
    }

    protected void onResume(){
        super.onResume();
        setTaskList(this);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("TaskID", mTaskList.GetID(position));
            DialogFragment newFragment = new Viewer_Task.TaskEditConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Edit Task");
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("TaskID", mTaskList.GetID(position));
            DialogFragment newFragment = new Viewer_Task.TaskDeleteConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Delete Task");
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
        String rawGetTasks = "SELECT t.*\n" +
                "FROM tblTask t\n" +
                "WHERE ((flngSessionID <> -1 \n" +
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
                "AND t.fblnActive = 1\n" +
                "ORDER BY t.fstrTitle";
        cursor = mDatabase.rawQuery(rawGetTasks,null);

        mTaskList.Clear();
        while (cursor.moveToNext()){
            mTaskList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngTaskID")));
        }
        mTaskList.mAdapter.notifyDataSetChanged();
    }
}
