package com.example.testtask;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class Task_Group extends AppCompatActivity {

    static ArrayListContainer mGroupTaskList;
    Long mlngGroupId = (long)-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_group);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null){
            mlngGroupId = getIntent().getLongExtra("EXTRA_GROUP_ID",-1);
        }

        ListView mGroupView = (ListView) findViewById(R.id.lsvGroupTaskList);
        mGroupTaskList = new ArrayListContainer();
        mGroupTaskList.LinkArrayToListView(mGroupView, this);
        mGroupTaskList.mListView.setOnItemClickListener(itemClickListener);
    }

    protected void onResume(){
        super.onResume();
        if (mlngGroupId != -1){
            LoadGroup();
        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("TaskID", mGroupTaskList.GetID(position));
            DialogFragment newFragment = new Viewer_Task.TaskEditConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Edit Task");
        }
    };

    private void LoadGroup() {
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblGroup", "flngGroupID", mlngGroupId);

        while(cursor.moveToNext()){
            setGroupTitle(cursor.getString(cursor.getColumnIndex("fstrTitle")));
        }

        cursor = DatabaseAccess.getRecordsFromTable("tblTask","flngGroupID", mlngGroupId);
        mGroupTaskList.Clear();
        while (cursor.moveToNext()){
            mGroupTaskList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngTaskID")));
        }
        mGroupTaskList.mAdapter.notifyDataSetChanged();
    }

    public void ceaseGroupCreation(View view) {
        Intent intent = new Intent(this, Task_Display.class);
        if (view.getId() == R.id.btnGroupConfirm) {
            //todo: add validation to session before it attempts to be created
            if (mlngGroupId == -1){
                createGroup(getGroupTitle());
            } else {
                updateGroupRecord();
                //Todo: Reevaluate tasks associated to this session (NOT ONE OFF TASKS)
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
