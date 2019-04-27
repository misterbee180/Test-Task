package com.deviousindustries.testtask;

import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class Details_Group extends AppCompatActivity {

    static ArrayListContainer mGroupTask;
    Long mlngGroupId = (long)-1;

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
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
        } else {
            //No Tasks Associated with Event
            fab.setVisibility(View.VISIBLE);
            findViewById(R.id.lsvGroupTaskList).setVisibility(View.VISIBLE);
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

        //Todo: fix to only show if either repeating or there's not a completed task instance
        cursor = DatabaseAccess.getRecordsFromTable("tblTask","fintTaskType = 3 and flngTaskTypeID = ? and fdtmDeleted = -1", new Object[] {mlngGroupId});
        mGroupTask.Clear();
        while (cursor.moveToNext()){
            Task tempTask = new Task(cursor.getLong(cursor.getColumnIndex("flngTaskID")));
            mGroupTask.Add(tempTask.mstrTitle,
                    tempTask.mlngTaskID);
        }
        mGroupTask.mAdapter.notifyDataSetChanged();
    }

    public void ceaseGroupCreation(View view) {
        boolean blnInitial = mlngGroupId == -1;
        try{
            DatabaseAccess.mDatabase.beginTransaction();
            mlngGroupId = DatabaseAccess.addRecordToTable("tblGroup",
                    new String[] {"fstrTitle"},
                    new String[] {getGroupTitle()},
                    "flngGroupID",
                    mlngGroupId);
            setupInitialVisibility();

            if(!blnInitial) {
                setResult(RESULT_OK);
                finish();
            }
            DatabaseAccess.mDatabase.setTransactionSuccessful();
        } catch(Exception e) {
            e.printStackTrace();
        }
        DatabaseAccess.mDatabase.endTransaction();
    }

    public String getGroupTitle() {
        return ((TextView) findViewById(R.id.txbGroupTitle)).getText().toString();
    }

    public void setGroupTitle(String pGroupTitle) {
        ((TextView) findViewById(R.id.txbGroupTitle)).setText(pGroupTitle);
    }
}
