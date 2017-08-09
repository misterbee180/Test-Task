package com.example.testtask;

import android.app.Dialog;
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

public class Task_Viewer extends AppCompatActivity {

    static ArrayListContainer mTaskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_viewer);

        ListView mTaskView = (ListView) findViewById(R.id.lsvTaskList);
        mTaskList = new ArrayListContainer();
        mTaskList.LinkArrayToListView(mTaskView, this);
        mTaskList.mListView.setOnItemLongClickListener(itemLongClickListener);
    }

    protected void onResume(){
        super.onResume();
        setTaskList();
    }

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("TaskID", mTaskList.GetID(position));
            //Todo: Replace all of this with logic to select from various options associated with the item
            DialogFragment newFragment = new Task_Viewer.TaskEditConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Edit Task");
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

                            //loadTasksFromDatabase();
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

    public void setTaskList(){
        Cursor cursor;
        String rawGetTasks = "SELECT t.*\n" +
                "FROM tblTask t\n" +
                "WHERE flngSessionID <> -1\n" +
                "OR EXISTS (SELECT 1\n" +
                "\tFROM tblTime tm\n" +
                "\tWHERE tm.flngID = t.flngTimeID\n" +
                "\tAND tm.flngWeekID <> -1)\n" +
                "OR EXISTS (SELECT 1\n" +
                "\tFROM tblTaskInstance ti\n" +
                "\tWHERE ti.flngTaskID = t.flngID\n" +
                "\tAND ti.fblnComplete = 0\n" +
                "\tAND ti.fblnSystemComplete = 0)\n";
        cursor = Task_Display.mDataBase.rawQuery(rawGetTasks,null);

        mTaskList.Clear();
        while (cursor.moveToNext()){
            mTaskList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngID")));
        }
        mTaskList.mAdapter.notifyDataSetChanged();
    }
}
