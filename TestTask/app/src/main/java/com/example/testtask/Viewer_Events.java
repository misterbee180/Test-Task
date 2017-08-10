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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        //mEventList.mListView.setOnItemClickListener(itemClickListener);
        mEventList.mListView.setOnItemLongClickListener(itemLongClickListener);
    }

    protected void onResume(){
        super.onResume();
        setSessionList();
    }

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("EventID", mEventList.GetID(position));
            DialogFragment newFragment = new Viewer_Session.ConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Edit Session");

            return true;
        }
    };

    public static class ConfirmationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpEventID = getArguments().getLong("EventID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Edit Session")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getActivity(), Task_Session.class);
                            intent.putExtra("EXTRA_SESSION_ID", tmpEventID);
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

    public  void createNewEvent() {
        Intent intent = new Intent(this, Task_Event.class);
        startActivity(intent);
    }

    public void setSessionList(){
        Cursor cursor;
        String rawGetEvents = "SELECT * \n" +
                "FROM tblEvent e \n";
        cursor = Task_Display.mDataBase.rawQuery(rawGetEvents,null);

        mEventList.Clear();
        while (cursor.moveToNext()){
            mEventList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngID")));
        }
        mEventList.mAdapter.notifyDataSetChanged();
    }

}
