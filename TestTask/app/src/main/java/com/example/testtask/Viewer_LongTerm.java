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

public class Viewer_LongTerm extends AppCompatActivity {
    static ArrayListContainer mLongTermListUnc;
    static ArrayListContainer mLongTermListCmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_viewer_longterm);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewLongTermTask();
            }
        });

        ListView mLongTermViewUnc = (ListView) findViewById(R.id.lsvLongTermListUnc);
        mLongTermListUnc = new ArrayListContainer();
        mLongTermListUnc.LinkArrayToListView(mLongTermViewUnc, this);
        mLongTermListUnc.mListView.setOnItemClickListener(itemClickListener);
        mLongTermListUnc.mListView.setOnItemLongClickListener(itemLongClickListener);

        ListView mLongTermViewCmp = (ListView) findViewById(R.id.lsvLongTermListCmp);
        mLongTermListCmp = new ArrayListContainer();
        mLongTermListCmp.LinkArrayToListView(mLongTermViewCmp, this);
        mLongTermListCmp.mListView.setOnItemClickListener(itemClickListener);
        mLongTermListCmp.mListView.setOnItemLongClickListener(itemLongClickListener);
    }

    protected void onResume(){
        super.onResume();
        setLongTermList();
    }

    public  void createNewLongTermTask() {
        Intent intent = new Intent(this, Details_LongTerm.class);
        startActivity(intent);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getBaseContext(), Details_LongTerm.class);
            switch(parent.getId()){
                case R.id.lsvLongTermListUnc:
                    intent.putExtra("EXTRA_LONGTERM_ID", mLongTermListUnc.getID(position));
                    break;
                case R.id.lsvLongTermListCmp:
                    intent.putExtra("EXTRA_LONGTERM_ID", mLongTermListCmp.getID(position));
                    break;
            }
            startActivity(intent);
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            switch(parent.getId()){
                case R.id.lsvLongTermListUnc:
                    bundle.putLong("LongTermID", mLongTermListUnc.getID(position));
                    break;
                case R.id.lsvLongTermListCmp:
                    bundle.putLong("LongTermID", mLongTermListCmp.getID(position));
                    break;
            }
            DialogFragment newFragment = new DeleteLongTermFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Delete LongTerm");

            return true;
        }
    };

    public static class DeleteLongTermFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete Long Term?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DatabaseAccess.deleteRecordFromTable("tblLongTerm",
                                    "flngLongTermID",
                                    getArguments().getLong("LongTermID"));
                            DatabaseAccess.deleteRecordFromTable("tblTask",
                                    "flngLongTermID",
                                    getArguments().getLong("LongTermID"));
                            setLongTermList();
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

    public static void setLongTermList(){
        //Populating complete then populating uncomplete by grabbing all and not adding complete ones
        String rawGetCompleteLongTerms = "SELECT lt.flngLongTermID, lt.fstrTitle \n" +
                "FROM tblLongTerm lt \n" +
                //Where there's at least one task associated to long term
                "WHERE EXISTS (SELECT 1 \n" +
                "FROM tblTask t \n" +
                "WHERE t.flngTaskTypeID = lt.flngLongTermID \n" +
                "AND t.fintTaskType = 2 \n" +
                "AND t.fdtmDeleted = -1) \n " +
                //And none of the tasks do not have a completed task instance associated with them
                "AND NOT EXISTS (SELECT 1 \n" +
                "FROM tblTask t \n" +
                "WHERE t.flngTaskTypeID = lt.flngLongTermID \n" +
                "AND t.fintTaskType = 2 \n" +
                "AND t.fdtmDeleted = -1 \n" +
                "AND NOT EXISTS (SELECT 1 \n" +
                "FROM tblTaskInstance ti \n" +
                "WHERE ti.flngTaskID = t.flngTaskID \n" +
                "AND ti.fdtmCompleted <> -1))\n" +
                "ORDER BY lt.flngLongTermID";

        Cursor cursor = DatabaseAccess.mDatabase.rawQuery(rawGetCompleteLongTerms,null);

        mLongTermListCmp.Clear();
        while (cursor.moveToNext()){
            mLongTermListCmp.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngLongTermID")));
        }
        mLongTermListCmp.mAdapter.notifyDataSetChanged();

        cursor = DatabaseAccess.getRecordsFromTable("tblLongTerm");
        mLongTermListUnc.Clear();
        while (cursor.moveToNext()){
            if(mLongTermListCmp.FindID(cursor.getLong(cursor.getColumnIndex("flngLongTermID"))) == -1){
                mLongTermListUnc.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngLongTermID")));
            }
        }
        mLongTermListUnc.mAdapter.notifyDataSetChanged();
    }

}
