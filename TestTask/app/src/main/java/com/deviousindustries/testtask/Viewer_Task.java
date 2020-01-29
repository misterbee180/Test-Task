package com.deviousindustries.testtask;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import androidx.annotation.NonNull;

import com.deviousindustries.testtask.classes.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import static com.deviousindustries.testtask.constants.ConstantsKt.*;

public class Viewer_Task extends AppCompatActivity {

    ListView mTaskView;
    Sorting mSorting;
    CustomAdapter mAdapter;
    Context mContext;

    enum Sorting
    {
        Ascending, Created, Group, Session
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
            setContentView(R.layout.activity_viewer_task);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            Utilities.Companion.instantiate(getApplicationContext());

            FloatingActionButton fab = findViewById(R.id.AddSession_FAB);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createTask();
                }
            });

            mTaskView = findViewById(R.id.InstanceList_Recycle);
            mSorting = Sorting.Ascending;
            mContext = this;
        } catch (Exception e) {
            e.printStackTrace();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    protected void onResume(){
        super.onResume();
        try{
            setTaskList();
        }
        catch(Exception e){
            e.printStackTrace();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    public AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int type = mAdapter.getItemViewType(position);
            if (type == 0) {
                Intent intent = new Intent(mContext, Details_Task.class);
                intent.putExtra("EXTRA_TASK_ID", Long.valueOf(((CustomAdapter.ViewHolder) view.getTag()).id.getText().toString()));
                mContext.startActivity(intent);
            }
        }
    };

    public AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
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

    public static class TaskDeleteConfirmationFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpTaskID = getArguments().getLong("TaskID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete Task")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Task tempTask = new Task(tmpTaskID);
                            tempTask.deleteTask();
                            ((Viewer_Task)getActivity()).setTaskList();
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

    public void createTask() {
        Intent intent = new Intent(this, Details_Task.class);
        startActivity(intent);
    }

    public void setTaskList(){
        String rawGetTasks = "SELECT t.*,td.fstrTitle, td.fstrDescription, tm.fdtmCreated, g.fstrTitle as fstrGroup, " +
                "CASE WHEN tm.fstrTitle = '' THEN 'No Session' ELSE tm.fstrTitle END as fstrSession\n" +
                "FROM tblTask t\n" +
                "JOIN tblTaskDetail td\n" +
                "ON td.flngTaskDetailID = t.flngTaskDetailID\n" +
                "JOIN tblTime tm\n" +
                "ON tm.flngTimeID = t.flngTimeID\n" +
                "LEFT JOIN tblGroup g\n" +
                "ON g.flngGroupID = t.flngTaskTypeID\n" +
                "AND t.fintTaskType = 3\n" +
                "WHERE (tm.fblnComplete = " + NULL_OBJECT + "\n"+
                "OR EXISTS (SELECT 1\n" +
                "FROM tblTaskInstance ti\n" +
                "WHERE ti.flngTaskID = t.flngTaskID\n" +
                "AND (ti.fdtmCompleted = " + NULL_DATE + "\n" +
                "AND ti.fdtmSystemCompleted = " + NULL_DATE + "\n" +
                "AND ti.fdtmDeleted = " + NULL_DATE + "\n" +
                ")))\n" +
                "AND t.fdtmDeleted = "+ NULL_DATE + "\n";

        switch(mSorting) {
            case Ascending:
                rawGetTasks += "ORDER BY td.fstrTitle";
                break;
            case Created:
                rawGetTasks += "ORDER BY tm.fdtmCreated Desc";
                break;
            case Group:
                rawGetTasks += "ORDER BY ifNULL(g.fstrTitle,\"z\"), td.fstrTitle";
                break;
            case Session:
                rawGetTasks += "ORDER BY ifNULL(tm.fstrTitle,\"z\"), td.fstrTitle";
                break;
//            case Group:
//                rawGetTasks += "ORDER BY g.fstrTitle, t.fstrTitle";
//                break;
//            case Session:
//                rawGetTasks += "ORDER BY s.fstrTitle, t.fstrTitle";
//                break;
        }
        mAdapter = new CustomAdapter(mContext);
        Calendar calCreated = Utilities.Companion.getCurrentCalendar();
        calCreated.add(Calendar.DAY_OF_YEAR,1);
        Calendar calNewCreated;
        String fstrSession = "";
        String fstrGroup = "";

        try(Cursor curTaskList = DatabaseAccess.getTasks(mSorting.ordinal())){
            while(curTaskList.moveToNext())
            {
                switch(mSorting) {
                    case Ascending:
                        mAdapter.addItem(curTaskList.getString(curTaskList.getColumnIndex("fstrTitle")),curTaskList.getLong(curTaskList.getColumnIndex("flngTaskID")));
                        break;
                    case Created:
                        calNewCreated = Utilities.Companion.getCurrentCalendar();
                        calNewCreated.setTimeInMillis(curTaskList.getLong(curTaskList.getColumnIndex("fdtmCreated")));
                        if (calNewCreated.get(Calendar.DAY_OF_YEAR) != calCreated.get(Calendar.DAY_OF_YEAR) ||
                                calNewCreated.get(Calendar.YEAR) != calCreated.get(Calendar.YEAR)){
                            calCreated = calNewCreated;
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
                            mAdapter.addSeparatorItem(dateFormat.format(calCreated.getTime()));
                        }
                        mAdapter.addItem(curTaskList.getString(curTaskList.getColumnIndex("fstrTitle")),curTaskList.getLong(curTaskList.getColumnIndex("flngTaskID")));
                        break;
                    case Group:
                        if (curTaskList.getString(curTaskList.getColumnIndex("fstrGroup")) != null &&
                                fstrGroup.compareTo(curTaskList.getString(curTaskList.getColumnIndex("fstrGroup"))) != 0){
                            fstrGroup = curTaskList.getString(curTaskList.getColumnIndex("fstrGroup"));
                            mAdapter.addSeparatorItem(fstrGroup);
                        } else if (curTaskList.getString(curTaskList.getColumnIndex("fstrGroup")) == null && fstrGroup != null){
                            fstrGroup = null;
                            mAdapter.addSeparatorItem("No Group");
                        }
                        mAdapter.addItem(curTaskList.getString(curTaskList.getColumnIndex("fstrTitle")),curTaskList.getLong(curTaskList.getColumnIndex("flngTaskID")));
                        break;
                    case Session:
                        if (curTaskList.getString(curTaskList.getColumnIndex("fstrSession")) != null &&
                                fstrSession.compareTo(curTaskList.getString(curTaskList.getColumnIndex("fstrSession"))) != 0){
                            fstrSession = curTaskList.getString(curTaskList.getColumnIndex("fstrSession"));
                            mAdapter.addSeparatorItem(fstrSession);
                        } else if (curTaskList.getString(curTaskList.getColumnIndex("fstrSession")) == null && fstrSession != null){
                            fstrSession = null;
                            mAdapter.addSeparatorItem("No Session");
                        }
                        mAdapter.addItem(curTaskList.getString(curTaskList.getColumnIndex("fstrTitle")),curTaskList.getLong(curTaskList.getColumnIndex("flngTaskID")));
                        break;
                }
            }
        }

        mTaskView.setAdapter(mAdapter);
        mTaskView.setOnItemClickListener(itemClickListener);
        mTaskView.setOnItemLongClickListener(itemLongClickListener);
    }

    public void viewComplete() {
        Intent intent = new Intent(this, Viewer_Task.class);
        startActivity(intent);
    }

    public void sortCreated() {
        mSorting = Sorting.Created;
        setTaskList();
    }

    public void sortGroup() {
        mSorting = Sorting.Group;
        setTaskList();
    }

    public void sortSession() {
        mSorting = Sorting.Session;
        setTaskList();
    }

    public void sortAscending() {
        mSorting = Sorting.Ascending;
        setTaskList();
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
