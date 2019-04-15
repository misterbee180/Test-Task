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

public class Viewer_Session extends AppCompatActivity {

    static ArrayListContainer mSessionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer_session);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewSession();
            }
        });

        ListView mSessionView = (ListView) findViewById(R.id.lsvSessionList);
        mSessionList = new ArrayListContainer();
        mSessionList.LinkArrayToListView(mSessionView, this);
        //mEventList.mListView.setOnItemClickListener(itemClickListener);
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
            intent.putExtra("EXTRA_SESSION_ID", mSessionList.getID(position));
            startActivity(intent);

//            Bundle bundle = new Bundle();
//            bundle.putLong("SessionID", mSessionList.getID(position));
//            DialogFragment newFragment = new Viewer_Session.EditSessionFragment();
//            newFragment.setArguments(bundle);
//            newFragment.show(getSupportFragmentManager(), "Edit Session");
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
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpSessionID = getArguments().getLong("SessionID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete Session? This will delete all tasks associated with this session as well.")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteSession(tmpSessionID);
                            setSessionList();
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

    private static void deleteSession(Long plngSessionId){
        DatabaseAccess.deleteRecordFromTable("tblSession",
                "flngSessionID",
                plngSessionId);

        Cursor cursor = DatabaseAccess.retrieveTasksAssociatedWithSession(plngSessionId);
        while(cursor.moveToNext()){
            DatabaseAccess.deleteRecordFromTable("tblTask",
                    "flngTaskID",
                    cursor.getLong(cursor.getColumnIndex("flngTaskID")));
            DatabaseAccess.deleteTaskInstances(cursor.getLong(cursor.getColumnIndex("flngTaskID")));
        }
    }

    public static class EditSessionFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpSessionID = getArguments().getLong("SessionID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Edit Session")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getActivity(), Details_Session.class);
                            intent.putExtra("EXTRA_SESSION_ID", tmpSessionID);
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

    public  void createNewSession() {
        Intent intent = new Intent(this, Details_Session.class);
        startActivity(intent);
    }

    public static void setSessionList(){
        Cursor cursor;
        String rawGetSessions = "SELECT * \n" +
                "FROM tblSession s \n";
        cursor = DatabaseAccess.mDatabase.rawQuery(rawGetSessions,null);

        mSessionList.Clear();
        while (cursor.moveToNext()){
            mSessionList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngSessionID")));
        }
        mSessionList.mAdapter.notifyDataSetChanged();
    }

}
