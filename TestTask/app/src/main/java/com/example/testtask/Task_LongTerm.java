package com.example.testtask;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;

public class Task_LongTerm extends AppCompatActivity {
    static ArrayListContainer mLongTermTasksUnc = new ArrayListContainer();
    static ArrayListContainer mLongTermTasksCmp = new ArrayListContainer();
    static Long mlngTaskCount = (long)0;
    static Long mlngLongTermID = (long)-1;

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_long_term);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BeginAddTaskToLongTerm();
            }
        });

        //set up listviewer for later task insersion
        ListView LongTermTaskViewUnc = (ListView) findViewById(R.id.lsvLongTermTaskListUnc);
        mLongTermTasksUnc.LinkArrayToListView(LongTermTaskViewUnc, this);
        mLongTermTasksUnc.mListView.setOnItemClickListener(itemClickListener);
        mLongTermTasksUnc.mListView.setOnItemLongClickListener(itemLongClickListener);
        ListView LongTermTaskViewCmp = (ListView) findViewById(R.id.lsvLongTermTaskListCmp);
        mLongTermTasksCmp.LinkArrayToListView(LongTermTaskViewCmp, this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        retrieveExtras();
        retrieveLongTermTasks();
        setupInitialVisibility();
        setupViews();
    }


    public  void BeginAddTaskToLongTerm() {
        Intent intent = new Intent(this, Details_Task.class);
        intent.putExtra("EXTRA_LONGTERM_ID", mlngLongTermID);
        startActivity(intent);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("TaskID", mLongTermTasksUnc.getID(position));
            DialogFragment newFragment = new TaskCompleteConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Complete Task");
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("TaskID", mLongTermTasksUnc.getID(position));
            bundle.putLong("LongTermID",mlngLongTermID);
            DialogFragment newFragment = new TaskEditConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Edit Task");
            return true;
        }
    };

    public static class TaskCompleteConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long lngTaskId = getArguments().getLong("TaskID");
            final Calendar currentCalendar = Task_Display.getCurrentCalendar();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Complete Task")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DatabaseAccess.generateTaskInstance(lngTaskId,
                                    (long)-1,
                                    (long)-1,
                                    false,
                                    false,
                                    true,
                                    false,
                                    getContext());
//                            }
                            retrieveLongTermTasks();
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

    public static class TaskEditConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long lngTaskId = getArguments().getLong("TaskID");
            final Long lngLongTermId = getArguments().getLong("LongTermID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Edit Long Term Task")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getActivity(), Details_Task.class);
                            intent.putExtra("EXTRA_LONGTERM_ID", lngLongTermId);
                            intent.putExtra("EXTRA_TASK_ID", lngTaskId);
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
            final Long lngTaskId = getArguments().getLong("TaskID");
//            final Long lngLongTermId = getArguments().getLong("LongTermID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete Long Term Task")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteTask(lngTaskId);
                            retrieveLongTermTasks();
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
                    new String[]{"fdtmSystemCompleted"},
                    new Object[]{Task_Display.getCurrentCalendar().getTimeInMillis()});
        }
    }

    private static void retrieveLongTermTasks() {
        String rawGetUnCompleteLongTermTasks = "SELECT t.fstrTitle, t.flngTaskID \n" +
                "FROM tblTask t \n" +
                "JOIN tblLongTerm lt \n" +
                "ON lt.flngLongTermID = t.flngLongTermID \n" +
                "WHERE t.flngLongTermID = ? \n" +
                "AND t.fblnActive = 1 \n" +
                "AND NOT EXISTS (SELECT 1 \n" +
                "FROM tblTaskInstance i \n" +
                "WHERE i.flngTaskID = t.flngTaskID \n" +
                "AND i.fblnComplete = 1) \n" +
                "ORDER BY t.flngTaskID";

        String rawGetCompleteLongTermTasks = "SELECT t.fstrTitle, t.flngTaskID \n" +
                "FROM tblTask t \n" +
                "JOIN tblLongTerm lt \n" +
                "ON lt.flngLongTermID = t.flngLongTermID \n" +
                "WHERE t.flngLongTermID = ? \n" +
                "AND t.fblnActive = 1 \n" +
                "AND EXISTS (SELECT 1 \n" +
                "FROM tblTaskInstance i \n" +
                "WHERE i.flngTaskID = t.flngTaskID \n" +
                "AND i.fblnComplete = 1) \n" +
                "ORDER BY t.flngTaskID";

        String[] parameters = {Long.toString(mlngLongTermID)};

        Cursor cursor = DatabaseAccess.mDatabase.rawQuery(rawGetUnCompleteLongTermTasks,parameters);

        mLongTermTasksUnc.Clear();
        while (cursor.moveToNext()){
            mlngTaskCount++;
            mLongTermTasksUnc.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngTaskID")));
        }
        mLongTermTasksUnc.mAdapter.notifyDataSetChanged();

        cursor = DatabaseAccess.mDatabase.rawQuery(rawGetCompleteLongTermTasks,parameters);

        mLongTermTasksCmp.Clear();
        while (cursor.moveToNext()){
            mlngTaskCount++;
            mLongTermTasksCmp.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngTaskID")));
        }
        mLongTermTasksCmp.mAdapter.notifyDataSetChanged();
    }

    private void retrieveExtras(){
        mlngLongTermID = getIntent().getLongExtra("EXTRA_LONGTERM_ID",-1);
    }

    private void setupInitialVisibility() {
        //New LongTerm Add - Force adding the LongTerm before the ability to add tasks is available
        if (mlngLongTermID == -1){
            fab.setVisibility(View.GONE);
            findViewById(R.id.lsvLongTermTaskListUnc).setVisibility(View.GONE);
            findViewById(R.id.lsvLongTermTaskListCmp).setVisibility(View.GONE);
            findViewById(R.id.txtLongTermAddReq).setVisibility(View.GONE);
        } else {
            //No Tasks Associated with LongTerm
            fab.setVisibility(View.VISIBLE);
            findViewById(R.id.btnLongTermConfirm).setVisibility(View.GONE);
            if (mlngTaskCount == 0){
                findViewById(R.id.txtLongTermAddReq).setVisibility(View.VISIBLE);
                findViewById(R.id.txtLongTermUnc).setVisibility(View.GONE);
                findViewById(R.id.txtLongTermCmp).setVisibility(View.GONE);
                findViewById(R.id.lsvLongTermTaskListUnc).setVisibility(View.GONE);
                findViewById(R.id.lsvLongTermTaskListCmp).setVisibility(View.GONE);
            } else {
                findViewById(R.id.txtLongTermAddReq).setVisibility(View.GONE);
                findViewById(R.id.txtLongTermUnc).setVisibility(View.VISIBLE);
                findViewById(R.id.txtLongTermCmp).setVisibility(View.VISIBLE);
                findViewById(R.id.lsvLongTermTaskListUnc).setVisibility(View.VISIBLE);
                findViewById(R.id.lsvLongTermTaskListCmp).setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupViews() {
        if (mlngLongTermID != -1){
            Cursor cursor = DatabaseAccess.getRecordsFromTable("tblLongTerm", "flngLongTermID", mlngLongTermID);
            cursor.moveToFirst();
            setLongTermTitle(cursor.getString(cursor.getColumnIndex("fstrTitle")));
            setLongTermDescription(cursor.getString(cursor.getColumnIndex("fstrDescription")));
        }
    }

    public void confirmActivity(View view){
        if (mlngLongTermID == -1){
            mlngLongTermID = DatabaseAccess.addRecordToTable("tblLongTerm",
                    new String[] {"fstrTitle","fstrDescription"},
                    new String[] {getLongTermTitle(), getLongTermDescription()});
            setupInitialVisibility();
        } else {
            setResult(RESULT_OK);
            finish();
        }
    }

    public void cancelActivity(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    public String getLongTermTitle() {
        return ((TextView) findViewById(R.id.txbLongTermTitle)).getText().toString();
    }

    public void setLongTermTitle(String pstrTitle) {
        ((TextView) findViewById(R.id.txbLongTermTitle)).setText(pstrTitle);
    }

    public String getLongTermDescription() {
        return ((TextView) findViewById(R.id.txbLongTermDesc)).getText().toString();
    }

    public void setLongTermDescription(String pstrTitle) {
        ((TextView) findViewById(R.id.txbLongTermDesc)).setText(pstrTitle);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mlngLongTermID != -1){
            getMenuInflater().inflate(R.menu.longterm_edit_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Bundle bundle;
        DialogFragment newFragment;

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()){
            case R.id.action_delete_longterm:
                bundle = new Bundle();
                bundle.putLong("LongTermID", mlngLongTermID);
                newFragment = new LongTermDeleteConfirmationFragment();
                newFragment.setArguments(bundle);
                newFragment.show(getSupportFragmentManager(), "Delete Task");
                break;
            case R.id.action_clear_longterm:
                bundle = new Bundle();
                bundle.putLong("LongTermID", mlngLongTermID);
                newFragment = new LongTermClearConfirmationFragment();
                newFragment.setArguments(bundle);
                newFragment.show(getSupportFragmentManager(), "Clear Task");
            /*case R.id.action_session:
                viewSessions();
                break;
            case R.id.action_task:
                viewTasks();
                break;
            case R.id.action_LongTerm  :
                viewLongTerms();
                break;*/
        }

        return super.onOptionsItemSelected(item);
    }

    public static class LongTermDeleteConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpLongTermId = getArguments().getLong("LongTermID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete LongTerm? This will also delete all tasks associated with LongTerm.")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteLongTerm(tmpLongTermId);
                            getActivity().setResult(RESULT_OK);
                            getActivity().finish();
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

    public static class LongTermClearConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpLongTermId = getArguments().getLong("LongTermID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Clear LongTerm")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            clearLongTerm(tmpLongTermId);
                            retrieveLongTermTasks();
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

    public static void deleteLongTerm(Long plngLongTermId){
        DatabaseAccess.deleteRecordFromTable("tblLongTerm",
                "flngLongTermID",
                plngLongTermId);

        Cursor cursor = DatabaseAccess.retrieveTasksAssociatedWithLongTerm(plngLongTermId);
        while(cursor.moveToNext()){
            DatabaseAccess.deleteRecordFromTable("tblTask",
                    "flngTaskID",
                    cursor.getLong(cursor.getColumnIndex("flngTaskID")));
            DatabaseAccess.deleteTaskInstances(cursor.getLong(cursor.getColumnIndex("flngTaskID")));
        }
    }

    public static void clearLongTerm(Long plngLongTermId){
        Cursor cursor = DatabaseAccess.retrieveTasksAssociatedWithLongTerm(plngLongTermId);
        while(cursor.moveToNext()){
            DatabaseAccess.deleteRecordFromTable("tblTask",
                    "flngTaskID",
                    cursor.getLong(cursor.getColumnIndex("flngTaskID")));
            DatabaseAccess.deleteTaskInstances(cursor.getLong(cursor.getColumnIndex("flngTaskID")));
        }
    }
}
