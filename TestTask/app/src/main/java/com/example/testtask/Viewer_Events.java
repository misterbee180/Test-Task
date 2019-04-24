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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class Viewer_Events extends AppCompatActivity {

    static ArrayListContainer mEventList;
    static ArrayListContainer mActiveList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_viewer_events);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewEvent();
            }
        });


        ListView mEventView = (ListView) findViewById(R.id.lsvEventList);
        mEventList = new ArrayListContainer();
        mEventList.LinkArrayToListView(mEventView, this);
        mEventList.mListView.setOnItemClickListener(itemClickListener);
        mEventList.mListView.setOnItemLongClickListener(itemLongClickListener);

        ListView mActiveView = (ListView) findViewById(R.id.lsvActiveList);
        mActiveList = new ArrayListContainer();
        mActiveList.LinkArrayToListView(mActiveView, this);
        mActiveList.mListView.setOnItemClickListener(itemClickListener);
        mActiveList.mListView.setOnItemLongClickListener(itemLongClickListener);
    }

    protected void onResume(){
        super.onResume();
        setEventsList();
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Bundle bundle = new Bundle();
            DialogFragment newFragment = null;
            switch(parent.getId()){
                case R.id.lsvEventList:
                    bundle.putLong("EventID", mEventList.getID(position));
                    newFragment = new Viewer_Events.ActivateTaskFragment();
                    break;
                case R.id.lsvActiveList:
                    bundle.putLong("EventID", mActiveList.getID(position));
                    newFragment = new Viewer_Events.CancelActiveTaskFragment();
                    break;
            }
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Event Fragment");
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            switch(parent.getId()){
                case R.id.lsvEventList:
                    bundle.putLong("EventID", mEventList.getID(position));
                    break;
                case R.id.lsvActiveList:
                    bundle.putLong("EventID", mActiveList.getID(position));
                    break;
            }
            DialogFragment newFragment = new Viewer_Events.ConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Edit Event");

            return true;
        }
    };

    public static class ConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpEventID = getArguments().getLong("EventID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Edit Event")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getActivity(), Details_Event.class);
                            intent.putExtra("EXTRA_EVENT_ID", tmpEventID);
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

    public static class ActivateTaskFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpEventID = getArguments().getLong("EventID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Activate Event")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Cursor cursor = DatabaseAccess.retrieveEventTasksFromEvent(tmpEventID);
                            while (cursor.moveToNext()){
                                new TaskInstance(cursor.getLong(cursor.getColumnIndex("flngTaskID")),
                                        cursor.getLong(cursor.getColumnIndex("flngTaskDetailID")),
                                        (long) -1,
                                        (long) -1,
                                        false,
                                        false,
                                        false,
                                        -1);
                            }
                            setEventsList();
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

    public static class CancelActiveTaskFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpEventID = getArguments().getLong("EventID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Cancel Active Event")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try{
                                DatabaseAccess.mDatabase.beginTransaction();
                                Cursor cursor = DatabaseAccess.retrieveEventTaskInstancesFromEvent(tmpEventID);
                                while (cursor.moveToNext()){
                                    TaskInstance ti = new TaskInstance(cursor.getLong(cursor.getColumnIndex("flngInstanceID")));
                                    ti.finishInstance(2);
                                }
                                DatabaseAccess.mDatabase.setTransactionSuccessful();
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            DatabaseAccess.mDatabase.endTransaction();
                            setEventsList();
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

    public  void createNewEvent() {
        Intent intent = new Intent(this, Details_Event.class);
        startActivity(intent);
    }

    public static void setEventsList(){
        Cursor cursor;
        String rawGetEvents = "SELECT *, \n" +
                "CASE WHEN EXISTS(SELECT 1 FROM tblTask t \n" +
                "JOIN tblTaskInstance ti \n" +
                "ON t.flngTaskID = ti.flngTaskID \n" +
                "AND ti.fdtmSystemCompleted = -1 AND ti.fdtmCompleted = -1 \n" +
                "WHERE t.fintTaskType = 1 \n" +
                "AND t.flngTaskTypeID = e.flngEventID) THEN 1 ELSE 0 END as fblnActive \n" +
                "FROM tblEvent e \n";
        cursor = DatabaseAccess.mDatabase.rawQuery(rawGetEvents,null);

        mEventList.Clear();
        mActiveList.Clear();
        while (cursor.moveToNext()){
            if (cursor.getLong(cursor.getColumnIndex("fblnActive"))==1){
                mActiveList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngEventID")));
            } else {
                mEventList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngEventID")));
            }
        }
        mEventList.mAdapter.notifyDataSetChanged();
        mActiveList.mAdapter.notifyDataSetChanged();
    }

}
