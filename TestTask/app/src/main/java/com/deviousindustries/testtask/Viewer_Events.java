package com.deviousindustries.testtask;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.deviousindustries.testtask.Classes.TaskInstance;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class Viewer_Events extends AppCompatActivity {

    ArrayListContainer mEventList;
    ArrayListContainer mActiveList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_viewer_events);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewEvent();
            }
        });


        ListView mEventView = findViewById(R.id.lsvEventList);
        mEventList = new ArrayListContainer();
        mEventList.LinkArrayToListView(mEventView, this);
        mEventList.mListView.setOnItemClickListener(itemClickListener);
        mEventList.mListView.setOnItemLongClickListener(itemLongClickListener);

        ListView mActiveView = findViewById(R.id.lsvActiveList);
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
            DialogFragment newFragment;
            if (parent.getId() == R.id.lsvEventList) {
                bundle.putLong("EventID", mEventList.getID(position));
                newFragment = new ActivateTaskFragment();
            } else { //R.id.lsvActiveList
                bundle.putLong("EventID", mActiveList.getID(position));
                newFragment = new CancelActiveTaskFragment();
            }
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Event Fragment");
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getBaseContext(), Details_Event.class);
            switch(parent.getId()){
                case R.id.lsvEventList:
                    intent.putExtra("EXTRA_EVENT_ID", mEventList.getID(position));
                    break;
                case R.id.lsvActiveList:
                    intent.putExtra("EXTRA_EVENT_ID", mActiveList.getID(position));
                    break;
            }
            startActivity(intent);
            return true;
        }
    };

    public static class ActivateTaskFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpEventID = getArguments().getLong("EventID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Activate Event")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try(Cursor curEventTask = DatabaseAccess.retrieveEventTasksFromEvent(tmpEventID)){
                                while (curEventTask.moveToNext()){
                                    new TaskInstance(-1,
                                            curEventTask.getLong(curEventTask.getColumnIndex("flngTaskID")),
                                            curEventTask.getLong(curEventTask.getColumnIndex("flngTaskDetailID")),
                                            (long) -1,
                                            (long) -1,
                                            false,
                                            false,
                                            false,
                                            -1);
                                }
                            }
                            ((Viewer_Events)getActivity()).setEventsList();
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
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpEventID = getArguments().getLong("EventID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Cancel Active Event")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try{
                                DatabaseAccess.mDatabase.beginTransaction();
                                try(Cursor curEventTask = DatabaseAccess.retrieveEventTaskInstancesFromEvent(tmpEventID)){
                                    while (curEventTask.moveToNext()){
                                        TaskInstance ti = new TaskInstance(curEventTask.getLong(curEventTask.getColumnIndex("flngInstanceID")));
                                        ti.finishInstance(2);
                                    }
                                }
                                DatabaseAccess.mDatabase.setTransactionSuccessful();
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            DatabaseAccess.mDatabase.endTransaction();
                            ((Viewer_Events)getActivity()).setEventsList();
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

    public void setEventsList(){
        mEventList.Clear();
        mActiveList.Clear();

        try(Cursor curActiveEvents = DatabaseAccess.getEvents()){
            while (curActiveEvents.moveToNext()){
                if (curActiveEvents.getLong(curActiveEvents.getColumnIndex("fblnActive"))==1){
                    mActiveList.Add(curActiveEvents.getString(curActiveEvents.getColumnIndex("fstrTitle")),curActiveEvents.getLong(curActiveEvents.getColumnIndex("flngEventID")));
                } else mEventList.Add(curActiveEvents.getString(curActiveEvents.getColumnIndex("fstrTitle")),curActiveEvents.getLong(curActiveEvents.getColumnIndex("flngEventID")));
            }
        }

        mEventList.mAdapter.notifyDataSetChanged();
        mActiveList.mAdapter.notifyDataSetChanged();
    }

}
