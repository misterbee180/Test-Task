package com.deviousindustries.testtask;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

public class Details_Event extends AppCompatActivity {

    ArrayListContainer mEventTasks = new ArrayListContainer();
    long mlngEventID = -1;

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_task_event);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BeginAddTaskToEvent();
            }
        });

        //set up listviewer for later task insersion
        ListView eventTaskView = findViewById(R.id.lsvEventTaskList);
        mEventTasks.LinkArrayToListView(eventTaskView, this);
        mEventTasks.mListView.setOnItemClickListener(itemClickListener);
        mEventTasks.mListView.setOnItemLongClickListener(itemLongClickListener);

    }

    @Override
    protected void onResume(){
        try{
            super.onResume();
            retrieveExtras();
            if (mlngEventID != -1){
                retrieveEventTasks();
            }
            setupInitialVisibility();
            setupViews();
        } catch(Exception e) {
            e.printStackTrace();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getBaseContext(), Details_Task.class);
            intent.putExtra("EXTRA_EVENT_ID", mlngEventID);
            intent.putExtra("EXTRA_TASK_ID", mEventTasks.getID(position));
            startActivity(intent);
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("TaskID", mEventTasks.getID(position));
            bundle.putLong("EventID",mlngEventID);
            DialogFragment newFragment = new Details_Event.TaskDeleteConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Delete Task");
            return true;
        }
    };

    public static class TaskDeleteConfirmationFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete Event Task")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Task tempTask = new Task(getArguments().getLong("TaskID"));
                            tempTask.deleteTask();
                            ((Details_Event)getActivity()).retrieveEventTasks();
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

    private void retrieveEventTasks() {
        Cursor TaskCursor = DatabaseAccess.retrieveEventTasksFromEvent(mlngEventID);

        mEventTasks.Clear();
        while (TaskCursor.moveToNext()){
            Task tempTask = new Task(TaskCursor.getLong(TaskCursor.getColumnIndex("flngTaskID")));
            mEventTasks.Add(tempTask.mstrTitle,
                    tempTask.mlngTaskID);
        }
        mEventTasks.mAdapter.notifyDataSetChanged();
    }

    private void retrieveExtras(){
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null){
            mlngEventID = getIntent().getLongExtra("EXTRA_EVENT_ID",-1);
        }
    }

    private void setupInitialVisibility() {
        //New Event Add - Force adding the event before the ability to add tasks is available
        if (mlngEventID == -1){
            fab.setVisibility(View.GONE);
            findViewById(R.id.lsvEventTaskList).setVisibility(View.GONE);
            findViewById(R.id.txtEventAddReq).setVisibility(View.GONE);
            findViewById(R.id.btnEventConfirm).setVisibility(View.VISIBLE);
        } else {
            //No Tasks Associated with Event
            fab.setVisibility(View.VISIBLE);
            if (mEventTasks.mArrayList.size() == 0){
                findViewById(R.id.txtEventAddReq).setVisibility(View.VISIBLE);
                findViewById(R.id.lsvEventTaskList).setVisibility(View.GONE);
            } else {
                findViewById(R.id.txtEventAddReq).setVisibility(View.GONE);
                findViewById(R.id.lsvEventTaskList).setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupViews() {
        if (mlngEventID != -1){
            Cursor cursor = DatabaseAccess.getRecordsFromTable("tblEvent", "flngEventID", mlngEventID);
            cursor.moveToFirst();
            setEventTitle(cursor.getString(cursor.getColumnIndex("fstrTitle")));
            setEventDescription(cursor.getString(cursor.getColumnIndex("fstrDescription")));
        }
    }

    public void confirmEventCreation(View view){
        try{
            boolean blnInitial = mlngEventID == -1;
            DatabaseAccess.mDatabase.beginTransaction();
            mlngEventID = DatabaseAccess.addRecordToTable("tblEvent",
                    new String[] {"fstrTitle","fstrDescription"},
                    new String[] {getEventTitle(), getEventDescription()},
                    "flngEventID",
                    mlngEventID);
            setupInitialVisibility();

            if(!blnInitial) {
                setResult(RESULT_OK);
                finish();
            }
            DatabaseAccess.mDatabase.setTransactionSuccessful();
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            DatabaseAccess.mDatabase.endTransaction();
        }
    }

    public  void BeginAddTaskToEvent() {
        Intent intent = new Intent(this, Details_Task.class);
        intent.putExtra("EXTRA_EVENT_ID", mlngEventID);
        startActivity(intent);
    }

    public String getEventTitle() {
        return ((TextView) findViewById(R.id.txbEventTitle)).getText().toString();
    }

    public void setEventTitle(String pstrTitle) {
        ((TextView) findViewById(R.id.txbEventTitle)).setText(pstrTitle);
    }

    public String getEventDescription() {
        return ((TextView) findViewById(R.id.txbEventDesc)).getText().toString();
    }

    public void setEventDescription(String pstrTitle) {
        ((TextView) findViewById(R.id.txbEventDesc)).setText(pstrTitle);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mlngEventID != -1){
            getMenuInflater().inflate(R.menu.event_edit_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Bundle bundle;
        DialogFragment newFragment;

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()){
            case R.id.action_delete_event:
                bundle = new Bundle();
                bundle.putLong("EventID", mlngEventID);
                newFragment = new Details_Event.EventDeleteConfirmationFragment();
                newFragment.setArguments(bundle);
                newFragment.show(getSupportFragmentManager(), "Delete Task");
                break;
            case R.id.action_clear_event:
                bundle = new Bundle();
                bundle.putLong("EventID", mlngEventID);
                newFragment = new Details_Event.EventClearConfirmationFragment();
                newFragment.setArguments(bundle);
                newFragment.show(getSupportFragmentManager(), "Clear Task");
            /*case R.id.action_session:
                viewSessions();
                break;
            case R.id.action_task:
                viewTasks();
                break;
            case R.id.action_event  :
                viewEvents();
                break;*/
        }

        return super.onOptionsItemSelected(item);
    }

    public static class EventDeleteConfirmationFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpEventId = getArguments().getLong("EventID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete Event? This will also delete all tasks associated with event.")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((Details_Event)getActivity()).deleteEvent(tmpEventId);
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

    public static class EventClearConfirmationFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpEventId = getArguments().getLong("EventID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Clear Event")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((Details_Event)getActivity()).clearEvent(tmpEventId);
                            ((Details_Event)getActivity()).retrieveEventTasks();
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

    public void deleteEvent(Long plngEventId){
        DatabaseAccess.deleteRecordFromTable("tblEvent",
                "flngEventID",
                plngEventId);

        clearEvent(plngEventId);
    }

    public void clearEvent(Long plngEventId){
        Cursor cursor = DatabaseAccess.retrieveEventTasksFromEvent(plngEventId);
        while(cursor.moveToNext()){
            Task tempTask = new Task(cursor.getLong(cursor.getColumnIndex("flngTaskID")));
            tempTask.deleteTask();
        }
    }
}
