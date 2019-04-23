package com.example.testtask;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class Task_Group extends AppCompatActivity {

    static ArrayListContainer mGroupTask;
    Long mlngGroupId = (long)-1;

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_group);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BeginAddTaskToGroup();
            }
        });

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null){
            mlngGroupId = getIntent().getLongExtra("EXTRA_GROUP_ID",-1);
        }

        ListView mGroupView = (ListView) findViewById(R.id.lsvGroupTaskList);
        mGroupTask = new ArrayListContainer();
        mGroupTask.LinkArrayToListView(mGroupView, this);
        mGroupTask.mListView.setOnItemClickListener(itemClickListener);
    }

    protected void onResume(){
        super.onResume();
        if (mlngGroupId != -1){
            LoadGroup();
        }
        setupInitialVisibility();

    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getBaseContext(), Details_Task.class);
            intent.putExtra("EXTRA_TASK_ID", mGroupTask.getID(position));
            startActivity(intent);
        }
    };

    private void setupInitialVisibility() {
        //New Event Add - Force adding the event before the ability to add tasks is available
        if (mlngGroupId == -1){
            fab.setVisibility(View.GONE);
            findViewById(R.id.lsvGroupTaskList).setVisibility(View.GONE);
            findViewById(R.id.btnGroupConfirm).setVisibility(View.VISIBLE);
        } else {
            //No Tasks Associated with Event
            fab.setVisibility(View.VISIBLE);
            findViewById(R.id.btnGroupConfirm).setVisibility(View.GONE);
            if (mGroupTask.mArrayList.size() == 0){
                findViewById(R.id.lsvGroupTaskList).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.lsvGroupTaskList).setVisibility(View.VISIBLE);
            }
        }
    }

    public  void BeginAddTaskToGroup() {
        Intent intent = new Intent(this, Details_Task.class);
        intent.putExtra("EXTRA_GROUP_ID", mlngGroupId);
        startActivity(intent);
    }

    private void LoadGroup() {
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblGroup", "flngGroupID", mlngGroupId);

        while(cursor.moveToNext()){
            setGroupTitle(cursor.getString(cursor.getColumnIndex("fstrTitle")));
        }

        cursor = DatabaseAccess.getRecordsFromTable("tblTask","flngGroupID", mlngGroupId,"fstrTitle");
        mGroupTask.Clear();
        while (cursor.moveToNext()){
            mGroupTask.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngTaskID")));
        }
        mGroupTask.mAdapter.notifyDataSetChanged();
    }

    public void ceaseGroupCreation(View view) {
        Intent intent = new Intent(this, Task_Display.class);
        if (view.getId() == R.id.btnGroupConfirm) {
            if (mlngGroupId == -1){
                createGroup(getGroupTitle());
            } else {
                updateGroupRecord();
            }

            //use the result to determine if a session was added.
            setResult(RESULT_OK, intent);
            finish();
        } else {
            //use the result to determine if a session was added.
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    private void updateGroupRecord() {
        String rawUpdateGroupRecord = "UPDATE tblGroup \n" +
                "SET fstrTitle = '" + getGroupTitle() + "' \n" +
                "WHERE flngGroupID = " + Long.toString(mlngGroupId);
        Cursor c = DatabaseAccess.mDatabase.rawQuery(rawUpdateGroupRecord,null);
        c.moveToFirst();
        c.close();
    }

    private long createGroup(String pstrTitle){
        ContentValues values = new ContentValues();
        values.put("fstrTitle", pstrTitle);
        return DatabaseAccess.mDatabase.insertOrThrow("tblGroup",null,values);
    }

    public String getGroupTitle() {
        return ((TextView) findViewById(R.id.txbGroupTitle)).getText().toString();
    }

    public void setGroupTitle(String pGroupTitle) {
        ((TextView) findViewById(R.id.txbGroupTitle)).setText(pGroupTitle);
    }
}
