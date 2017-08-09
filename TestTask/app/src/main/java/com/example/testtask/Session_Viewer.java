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

public class Session_Viewer extends AppCompatActivity {

    static ArrayListContainer mSessionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_viewer);
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
        //mSessionList.mListView.setOnItemClickListener(itemClickListener);
        mSessionList.mListView.setOnItemLongClickListener(itemLongClickListener);
    }

    protected void onResume(){
        super.onResume();
        setSessionList();
    }

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("SessionID", mSessionList.GetID(position));
            DialogFragment newFragment = new Session_Viewer.ConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Edit Session");

            return true;
        }
    };

    public static class ConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpSessionID = getArguments().getLong("SessionID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Edit Session")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getActivity(), Task_Session.class);
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
        Intent intent = new Intent(this, Task_Session.class);
        startActivity(intent);
    }

    public void setSessionList(){
        Cursor cursor;
        String rawGetSessions = "SELECT * \n" +
                "FROM tblSession s \n";
        cursor = Task_Display.mDataBase.rawQuery(rawGetSessions,null);

        mSessionList.Clear();
        while (cursor.moveToNext()){
            mSessionList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngID")));
        }
        mSessionList.mAdapter.notifyDataSetChanged();
    }

}
