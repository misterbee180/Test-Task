package com.deviousindustries.testtask;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class Details_Session extends AppCompatActivity{

    ArrayListContainer mSessionTaskList;
    TimeKeeper timeKeeper;
    Intent mIntent;
    Time mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_task_session);
        timeKeeper = findViewById(R.id.timeKeeper);
        timeKeeper.setMode(2);
        mTime = new Time();

        mIntent = getIntent();
        Bundle extras = mIntent.getExtras();
        if (extras != null){
            mTime = new Time(getIntent().getLongExtra("EXTRA_TIME_ID",-1));
        }

        ListView mSessionView = findViewById(R.id.lsvSessionTaskList);
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
        setSessionTitle(mTime.mstrTitle);
        timeKeeper.loadTimeKeeper(mTime);

        try(Cursor tblTask = mTime.getTasks()){
            mSessionTaskList.Clear();
            while (tblTask.moveToNext()){
                Task tempTask = new Task(tblTask.getLong(tblTask.getColumnIndex("flngTaskID")));
                mSessionTaskList.Add(tempTask.mstrTitle,
                        tempTask.mlngTaskID);
            }
        }

        mSessionTaskList.mAdapter.notifyDataSetChanged();
    }

    public void createSession (View view) {
        DatabaseAccess.mDatabase.beginTransaction();
        try {
            if(mTime.mlngTimeID != -1){
                mTime.clearGenerationPoints();
            }
            mTime = timeKeeper.createTimeDetails(mTime.mlngTimeID,
                    mTime.mintTimeframe,
                    mTime.mlngTimeframeID,
                    true,
                    getSessionTitle());
            mTime.refreshInstances();

            //Because session changed (possibly) we need to update any oneoffs that haven't yet been completed.
            try(Cursor oneOffs = mTime.findOneOffs()) {
                while (oneOffs.moveToNext()) {
                    Task tempTask = new Task(oneOffs.getLong(oneOffs.getColumnIndex("flngTaskID")));
                    new Time(tempTask.mlngTimeID).clearGenerationPoints();
                    Time tempTime = mTime.createOneOff(tempTask.mlngTimeID);//because we're supplying the time ID we shouldn't need to replace the session id

                    tempTask.finishActiveInstances(3);
                    tempTime.generateInstances(true, tempTask.mlngTaskID);
                }
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
        TextView SessionTitle = findViewById(R.id.txbSessionTitle);
        return SessionTitle.getText().toString();
    }

    public void setSessionTitle(String pSessionTitle) {
        TextView SessionTitle = findViewById(R.id.txbSessionTitle);
        SessionTitle.setText(pSessionTitle);
    }
}
