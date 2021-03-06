package com.deviousindustries.testtask;

import android.content.Intent;
import android.database.Cursor;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.deviousindustries.testtask.classes.Task;
import com.deviousindustries.testtask.classes.Time;
import static com.deviousindustries.testtask.constants.ConstantsKt.*;

public class Details_Session extends AppCompatActivity{

    ArrayListContainer mSessionTaskList;
    TimeKeeper timeKeeper;
    Intent fintent;
    Time mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_task_session);
        Utilities.Companion.instantiate(getApplicationContext());
        timeKeeper = findViewById(R.id.timeKeeper);
        timeKeeper.setMode(2);
        Long loadID = NULL_OBJECT;

        fintent = getIntent();
        Bundle extras = fintent.getExtras();
        if (extras != null){
            loadID = getIntent().getLongExtra("EXTRA_TIME_ID",NULL_OBJECT);
        }

        mTime = Time.getInstance(loadID);

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
        if (mTime.flngTimeID != NULL_OBJECT){
            LoadSession();
        }
    }

    private void LoadSession() {
        setSessionTitle(mTime.fstrTitle);
        timeKeeper.loadTimeKeeper(mTime);

        try(Cursor tblTask = DatabaseAccess.findOneOffs(mTime.flngTimeID)){
            mSessionTaskList.Clear();
            while (tblTask.moveToNext()){
                Task tempTask = new Task(tblTask.getLong(tblTask.getColumnIndex("flngTaskID")));
                mSessionTaskList.Add(tempTask.fstrTitle,
                        tempTask.flngTaskID);
            }
        }

        mSessionTaskList.mAdapter.notifyDataSetChanged();
    }

    public void createSession (View view) {
        DatabaseAccess.mDatabase.beginTransaction();
        try {
            if(mTime.flngTimeID != NULL_OBJECT){
                mTime.clearGenerationPoints();
            }
            mTime = timeKeeper.createTimeDetails(mTime.flngTimeID,
                    mTime.fintTimeframe,
                    mTime.flngTimeframeID,
                    true,
                    getSessionTitle());
            mTime.refreshTaskInstances();

            //Because session changed (possibly) we need to update any oneoffs that haven't yet been completed.
            try(Cursor oneOffs = DatabaseAccess.findOneOffs(mTime.flngTimeID)) {
                while (oneOffs.moveToNext()) {
                    Task tempTask = new Task(oneOffs.getLong(oneOffs.getColumnIndex("flngTaskID")));
                    Time.getInstance(tempTask.flngTimeID).clearGenerationPoints();
                    Time tempTime = mTime.createOneOff(tempTask.flngTimeID);//because we're supplying the mTime ID we shouldn't need to replace the session id

                    tempTask.finishActiveInstances(3);
                    tempTime.generateInstances(true, tempTask.flngTaskID);
                }
            }

            DatabaseAccess.mDatabase.setTransactionSuccessful();

            fintent.putExtra("EXTRA_SESSION_ID", mTime.flngTimeID);
            setResult(RESULT_OK, fintent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseAccess.mDatabase.endTransaction();
        }
    }

    public String getSessionTitle() {
        TextView SessionTitle = findViewById(R.id.Session_Title_EditText);
        return SessionTitle.getText().toString();
    }

    public void setSessionTitle(String pSessionTitle) {
        TextView SessionTitle = findViewById(R.id.Session_Title_EditText);
        SessionTitle.setText(pSessionTitle);
    }
}
