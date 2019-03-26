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

public class Viewer_Groups extends AppCompatActivity {

    static ArrayListContainer mGroupList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer_groups);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewGroup();
            }
        });

        ListView mGroupView = (ListView) findViewById(R.id.lsvGroupList);
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
            Bundle bundle = new Bundle();
            bundle.putLong("GroupID", mGroupList.GetID(position));
            DialogFragment newFragment = new EditGroupFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Edit Group");
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("GroupID", mGroupList.GetID(position));
            DialogFragment newFragment = new DeleteGroupFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Delete Group");

            return true;
        }
    };

    public static class DeleteGroupFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete Group?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DatabaseAccess.deleteRecordFromTable("tblGroup",
                                    "flngGroupID",
                                    getArguments().getLong("GroupID"));
                            setGroupList();
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

    public static class EditGroupFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Long tmpGroupID = getArguments().getLong("GroupID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Edit Group")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getActivity(), Task_Group.class);
                            intent.putExtra("EXTRA_GROUP_ID", tmpGroupID);
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

    public  void createNewGroup() {
        Intent intent = new Intent(this, Task_Group.class);
        startActivity(intent);
    }

    public static void setGroupList(){
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblGroup");

        mGroupList.Clear();
        while (cursor.moveToNext()){
            mGroupList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngGroupID")));
        }
        mGroupList.mAdapter.notifyDataSetChanged();
    }
}
