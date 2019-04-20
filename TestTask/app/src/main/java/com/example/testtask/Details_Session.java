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
    TimeKeeper timeKeeper;;
    Intent mIntent;
    Time mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_session);
        timeKeeper = (TimeKeeper) findViewById(R.id.timeKeeper);
        timeKeeper.setMode(2);
        mTime = new Time();

        mIntent = getIntent();
        Bundle extras = mIntent.getExtras();
        if (extras != null){
            mTime = new Time(getIntent().getLongExtra("EXTRA_TIME_ID",-1));
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
        if (mTime.mlngTimeID != -1){
            LoadSession();
        }
    }

    private void LoadSession() {
        Cursor curSession = mTime.getSession();

        if(curSession.moveToNext()){
            setSessionTitle(curSession.getString(curSession.getColumnIndex("fstrTitle")));
            timeKeeper.loadTimeDetails(mTime);
        }

        Cursor tblTask = DatabaseAccess.getRecordsFromTable("tblTask",
                "flngTimeID",
                mTime.mlngTimeID);

        mSessionTaskList.Clear();
        while (tblTask.moveToNext()){
            Task tempTask = new Task(tblTask.getLong(tblTask.getColumnIndex("flngTaskID")));
            mSessionTaskList.Add(tempTask.mstrTitle,
                    tempTask.mlngTaskID);
        }
        mSessionTaskList.mAdapter.notifyDataSetChanged();
    }

    public void createSession (View view) {
        DatabaseAccess.mDatabase.beginTransaction();
        try {
            //todo: add validation to session before it attempts to be created
            mTime.createSession(getSessionTitle());
            mTime.clearGenerationPoints();
            mTime = timeKeeper.createTimeDetails(mTime.mlngTimeID,
                    mTime.mintTimeframe,
                    mTime.mlngTimeframeID);
            if(mTime.mlngTimeID != -1){
                mTime.refreshInstances();
            }
            DatabaseAccess.mDatabase.setTransactionSuccessful();

            mIntent.putExtra("EXTRA_SESSION_ID", mTime.mlngTimeID);
            setResult(RESULT_OK, mIntent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseAccess.mDatabase.endTransaction();
        }
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
