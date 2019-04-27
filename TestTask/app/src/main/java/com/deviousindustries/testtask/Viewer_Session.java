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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class Viewer_Session extends AppCompatActivity {

    ArrayListContainer mSessionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_viewer_session);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewSession();
            }
        });

        ListView mSessionView = findViewById(R.id.lsvSessionList);
        mSessionList = new ArrayListContainer();
        mSessionList.LinkArrayToListView(mSessionView, this);
        mSessionList.mListView.setOnItemClickListener(itemClickListener);
        mSessionList.mListView.setOnItemLongClickListener(itemLongClickListener);
    }

    protected void onResume(){
        super.onResume();
        setSessionList();
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getBaseContext(), Details_Session.class);
            intent.putExtra("EXTRA_TIME_ID", mSessionList.getID(position));
            startActivity(intent);
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("SessionID", mSessionList.getID(position));
            DialogFragment newFragment = new Viewer_Session.DeleteSessionFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Delete Session");

            return true;
        }
    };

    public static class DeleteSessionFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpSessionID = getArguments().getLong("SessionID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete Session?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((Viewer_Session)getActivity()).deleteSession(tmpSessionID);
                            ((Viewer_Session)getActivity()).setSessionList();
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

    private void deleteSession(Long plngTimeId){
        try{
            DatabaseAccess.mDatabase.beginTransaction();
            Time tempTime = new Time(plngTimeId);

            try(Cursor cursor = tempTime.getTasks()){
                while(cursor.moveToNext()){
                    Task tempTask = new Task(cursor.getLong(cursor.getColumnIndex("flngTaskID")));
                    //delete current task instances associated w/ session time
                    tempTask.finishActiveInstances(3);
                    //create new time that mimics deleted session and replace on task.
                    tempTask.replaceTimeId(tempTime.getCopy().mlngTimeID);
                    //re-generation of instances will occur during task display logic.
                }
            }

            tempTime.deleteTime();
            DatabaseAccess.mDatabase.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }
        DatabaseAccess.mDatabase.endTransaction();

    }

    public  void createNewSession() {
        Intent intent = new Intent(this, Details_Session.class);
        startActivity(intent);
    }

    public void setSessionList(){
        try(Cursor cursor = DatabaseAccess.getRecordsFromTable("tblTime","fblnSession = 1 and fblnComplete = 0",null)){
            mSessionList.Clear();
            while (cursor.moveToNext()){
                Time tempTime = new Time(cursor.getLong(cursor.getColumnIndex("flngTimeID")));
                mSessionList.Add(tempTime.getSessionTitle(),
                        tempTime.mlngTimeID);
            }
            mSessionList.mAdapter.notifyDataSetChanged();
        }

    }

}
