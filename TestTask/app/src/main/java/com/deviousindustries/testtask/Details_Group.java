package com.deviousindustries.testtask;

import android.content.Intent;
import android.database.Cursor;

import com.deviousindustries.testtask.Classes.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class Details_Group extends AppCompatActivity {

    ArrayListContainer mGroupTask;
    Long mlngGroupId = (long)-1;

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_task_group);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.AddSession_FAB);
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

        ListView mGroupView = findViewById(R.id.lsvGroupTaskList);
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
            findViewById(R.id.txtGroupAddReq).setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
            if(mGroupTask.mArrayList.size() == 0){
                findViewById(R.id.txtGroupAddReq).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.txtGroupAddReq).setVisibility(View.GONE);
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

        cursor = DatabaseAccess.getTasksFromGroup(mlngGroupId);
        mGroupTask.Clear();
        while (cursor.moveToNext()){
            Task tempTask = new Task(cursor.getLong(cursor.getColumnIndex("flngTaskID")));
            mGroupTask.Add(tempTask.fstrTitle,
                    tempTask.flngTaskID);
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
