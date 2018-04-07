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

public class Task_Session extends AppCompatActivity{

    static ArrayListContainer mSessionTaskList;
    TimeKeeper timeKeeper;
    Long mlngSessionId = (long)-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_session);
        timeKeeper = (TimeKeeper) findViewById(R.id.timeKeeper);
        timeKeeper.setUpForSession();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null){
            mlngSessionId = getIntent().getLongExtra("EXTRA_SESSION_ID",-1);
        }

        ListView mSessionView = (ListView) findViewById(R.id.lsvSessionTaskList);
        mSessionTaskList = new ArrayListContainer();
        mSessionTaskList.LinkArrayToListView(mSessionView, this);
        mSessionTaskList.mListView.setOnItemClickListener(itemClickListener);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putLong("TaskID", mSessionTaskList.GetID(position));
            DialogFragment newFragment = new Viewer_Task.TaskEditConfirmationFragment();
            newFragment.setArguments(bundle);
            newFragment.show(getSupportFragmentManager(), "Edit Task");
        }
    };

    @Override
    protected void onResume(){
        super.onResume();
        if (mlngSessionId != -1){
            LoadSession();
        }
    }

    private void LoadSession() {
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblSession", "flngSessionID", mlngSessionId);

        while(cursor.moveToNext()){
            setSessionTitle(cursor.getString(cursor.getColumnIndex("fstrTitle")));
            timeKeeper.loadTimeDetails(cursor.getLong(cursor.getColumnIndex("flngTimeID")),
                    false);
        }

        String rawSessionTaskSelect = "SELECT * FROM tblTask t WHERE t.flngSessionID = " + Long.toString(mlngSessionId) + " AND t.fblnOneOff = 0 AND fblnActive = 1\n";
        cursor = DatabaseAccess.mDatabase.rawQuery(rawSessionTaskSelect,null);
        mSessionTaskList.Clear();
        while (cursor.moveToNext()){
            mSessionTaskList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngTaskID")));
        }
        mSessionTaskList.mAdapter.notifyDataSetChanged();
    }

    public void ceaseSessionCreation (View view) {
        Intent intent = new Intent(this, Task_Display.class);
        if (view.getId() == R.id.btnSessionConfirm) {
            //todo: add validation to session before it attempts to be created
            if (mlngSessionId == -1){
                timeKeeper.createTimeDetails();
                createSession(getSessionTitle(), timeKeeper.getTimeID());
            } else {
                timeKeeper.updateTimeDetails();
                updateSessionRecord();
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

    private void updateSessionRecord() {
            String rawUpdateSessionRecord = "UPDATE tblSession \n" +
                    "SET fstrTitle = '" + getSessionTitle() + "' \n" +
                    "WHERE flngSessionID = " + Long.toString(mlngSessionId);
            Cursor c = DatabaseAccess.mDatabase.rawQuery(rawUpdateSessionRecord,null);
            c.moveToFirst();
            c.close();
    }

    private long createSession(String pstrTitle,
                               Long plngTimeId){
        ContentValues values = new ContentValues();
        values.put("fstrTitle", pstrTitle);
        values.put("flngTimeId", plngTimeId);
        return DatabaseAccess.mDatabase.insertOrThrow("tblSession",null,values);
    }

    public String getSessionTitle() {
        TextView SessionTitle = (TextView) findViewById(R.id.txbSessionTitle);
        return SessionTitle.getText().toString();
    }

    public void setSessionTitle(String pSessionTitle) {
        TextView SessionTitle = (TextView) findViewById(R.id.txbSessionTitle);
        SessionTitle.setText(pSessionTitle);
    }
}
