package com.example.testtask;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Task_Event extends AppCompatActivity {

    ArrayListContainer mEventTasks = new ArrayListContainer();
    Long mlngEventID = (long)-1;;

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BeginAddTaskToEvent();
            }
        });

        //set up listviewer for later task insersion
        ListView eventTaskView = (ListView) findViewById(R.id.lsvEventTaskList);
        mEventTasks.LinkArrayToListView(eventTaskView, this);
        mEventTasks.mListView.setOnItemLongClickListener(itemLongClickListener);

    }

    @Override
    protected void onResume(){
        super.onResume();
        retrieveExtras();
        if (mlngEventID != -1){
            retrieveEventTasks();
        }
        setupInitialVisibility();
        setupViews();
    }

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("TaskID", mEventTasks.GetID(position));
            bundle.putLong("EventID",mlngEventID);
            DialogFragment newFragment = new Task_Event.ConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Edit Event");

            return true;
        }
    };

    public static class ConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long lngTaskId = getArguments().getLong("TaskID");
            final Long lngEventId = getArguments().getLong("EventID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Edit Event Task")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getActivity(), Task_Task.class);
                            intent.putExtra("EXTRA_EVENT_ID", lngEventId);
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

    private void retrieveEventTasks() {
        Cursor cursor = DatabaseAccess.retrieveEventTasksFromEvent(mlngEventID);

        mEventTasks.Clear();
        while (cursor.moveToNext()){
            mEventTasks.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngID")));
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
            Cursor cursor = DatabaseAccess.getRecordFromTable("tblEvent",mlngEventID);
            cursor.moveToFirst();
            setEventTitle(cursor.getString(cursor.getColumnIndex("fstrTitle")));
            setEventDescription(cursor.getString(cursor.getColumnIndex("fstrDescription")));
        }
    }

    public void confirmActivity(View view){
        if (mlngEventID == -1){
            mlngEventID = DatabaseAccess.insertEvent(getEventTitle(), getEventDescription());
            setupInitialVisibility();
        } else {
            DatabaseAccess.updateEvent(mlngEventID, getEventTitle(), getEventDescription());
            setResult(RESULT_OK);
            finish();
        }
    }

    public void cancelActivity(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    public  void BeginAddTaskToEvent() {
        Intent intent = new Intent(this, Task_Task.class);
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
}
