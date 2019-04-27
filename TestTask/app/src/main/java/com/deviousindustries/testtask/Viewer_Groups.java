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

public class Viewer_Groups extends AppCompatActivity {

    ArrayListContainer mGroupList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_viewer_groups);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewGroup();
            }
        });

        ListView mGroupView = findViewById(R.id.lsvGroupList);
        mGroupList = new ArrayListContainer();
        mGroupList.LinkArrayToListView(mGroupView, this);
        mGroupList.mListView.setOnItemClickListener(itemClickListener);
        mGroupList.mListView.setOnItemLongClickListener(itemLongClickListener);
    }

    @Override
    protected void onResume(){
        super.onResume();
        setGroupList();
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getBaseContext(), Details_Group.class);
            intent.putExtra("EXTRA_GROUP_ID", mGroupList.getID(position));
            startActivity(intent);
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("GroupID", mGroupList.getID(position));
            DialogFragment newFragment = new DeleteGroupFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Delete Group");

            return true;
        }
    };

    public static class DeleteGroupFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete Group?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DatabaseAccess.deleteRecordFromTable("tblGroup",
                                    "flngGroupID",
                                    getArguments().getLong("GroupID"));
                            ((Viewer_Groups)getActivity()).setGroupList();
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

    public  void createNewGroup() {
        Intent intent = new Intent(this, Details_Group.class);
        startActivity(intent);
    }

    public void setGroupList(){
        mGroupList.Clear();
        try(Cursor curGroup = DatabaseAccess.getRecordsFromTable("tblGroup")) {
            while (curGroup.moveToNext()) {
                mGroupList.Add(curGroup.getString(curGroup.getColumnIndex("fstrTitle")), curGroup.getLong(curGroup.getColumnIndex("flngGroupID")));
            }
        }
        mGroupList.mAdapter.notifyDataSetChanged();
    }
}
