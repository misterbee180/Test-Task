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

public class Details_Session extends AppCompatActivity{

    static ArrayListContainer mSessionTaskList;
    TimeKeeper timeKeeper;
    Long mlngSessionId = (long)-1;
    Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_session);
        timeKeeper = (TimeKeeper) findViewById(R.id.timeKeeper);
        timeKeeper.setMode(2);

        mIntent = getIntent();
        Bundle extras = mIntent.getExtras();
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
            Intent intent = new Intent(getBaseContext(), Details_Task.class);
            intent.putExtra("EXTRA_TASK_ID", mSessionTaskList.getID(position));
            startActivity(intent);
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
            timeKeeper.loadTimeDetails(cursor.getLong(cursor.getColumnIndex("flngTimeID")));
        }

        Cursor tblTask = DatabaseAccess.getRecordsFromTable("tblTask",
                "flngTimeID",
                timeKeeper.mTime.mlngTimeID);

        mSessionTaskList.Clear();
        while (tblTask.moveToNext()){
            mSessionTaskList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngTaskID")));
        }
        mSessionTaskList.mAdapter.notifyDataSetChanged();
    }

    public void createSession (View view) {
        DatabaseAccess.mDatabase.beginTransaction();
        try {
            //todo: add validation to session before it attempts to be created
            if (mlngSessionId == -1){ //Initial creation
                timeKeeper.createTimeDetails();
                mlngSessionId = createSession(getSessionTitle(), timeKeeper.mTime.mlngTimeID);
            } else { //Updating
                timeKeeper.updateTimeDetails();
                updateSessionRecord();
                //Todo: Reevaluate tasks associated to this session (NOT ONE OFF TASKS)
            }
            DatabaseAccess.mDatabase.setTransactionSuccessful();

            mIntent.putExtra("EXTRA_SESSION_ID", mlngSessionId);
            setResult(RESULT_OK, mIntent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseAccess.mDatabase.endTransaction();
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
