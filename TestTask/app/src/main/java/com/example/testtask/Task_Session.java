package com.example.testtask;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

public class Task_Session extends AppCompatActivity{

    TimeKeeper timeKeeper;
    Long mlngSessionId = (long)-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_session);
        timeKeeper = (TimeKeeper) findViewById(R.id.timeKeeper);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null){
            mlngSessionId = getIntent().getLongExtra("EXTRA_SESSION_ID",-1);
        }
        if (mlngSessionId != -1){
            LoadSession(mlngSessionId);
        } else {
            //Sessions should never select no repetition
            //todo: prevent selection of no repetition
            timeKeeper.setTimeRange("W");
        }
    }

    private void LoadSession(Long lngSessionId) {
        Cursor cursor = DatabaseAccess.getRecordFromTable("tblSession",mlngSessionId);

        while(cursor.moveToNext()){
            setSessionTitle(cursor.getString(cursor.getColumnIndex("fstrTitle")));
            timeKeeper.loadTimeDetails(cursor.getLong(cursor.getColumnIndex("flngTimeID")));
        }
    }

    public void ceaseSessionCreation (View view) {
        Intent intent = new Intent(this, Task_Display.class);
        if (view.getId() == R.id.btnSessConfirm) {
            //todo: add validation to session before it attempts to be created
            if (mlngSessionId == -1){
                long tmpTimeId = createTime();
                createSession(getSessionTitle(), tmpTimeId);
            } else {
                timeKeeper.updateTimeRecord();
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
                    "WHERE flngID = " + Long.toString(mlngSessionId);
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

    private long createTime(){
        Long lngWeekKey = (long)-1;
        if (timeKeeper.getTimeRange() != "") {
            ContentValues weekValues = new ContentValues();
            weekValues.put("fblnMonday",timeKeeper.getDayOfWeek("Monday"));
            weekValues.put("fblnTuesday",timeKeeper.getDayOfWeek("Tuesday"));
            weekValues.put("fblnWednesday",timeKeeper.getDayOfWeek("Wednesday"));
            weekValues.put("fblnThursday",timeKeeper.getDayOfWeek("Thursday"));
            weekValues.put("fblnFriday",timeKeeper.getDayOfWeek("Friday"));
            weekValues.put("fblnSaturday",timeKeeper.getDayOfWeek("Saturday"));
            weekValues.put("fblnSunday",timeKeeper.getDayOfWeek("Sunday"));
            lngWeekKey = DatabaseAccess.mDatabase.insertOrThrow("tblWeek",null,weekValues);
        }
        ContentValues timeValues = new ContentValues();
        timeValues.put("fdtmFrom",timeKeeper.getFromTime());
        timeValues.put("fdtmTo",timeKeeper.getToTime());
        timeValues.put("flngWeekID",lngWeekKey);
        timeValues.put("fdtmEvaluated",Calendar.getInstance().getTimeInMillis());
        return DatabaseAccess.mDatabase.insertOrThrow("tblTime",null,timeValues);
    }

    public String getSessionTitle() {
        TextView SessionTitle = (TextView) findViewById(R.id.txbSessTitle);
        return SessionTitle.getText().toString();
    }

    public void setSessionTitle(String pSessionTitle) {
        TextView SessionTitle = (TextView) findViewById(R.id.txbSessTitle);
        SessionTitle.setText(pSessionTitle);
    }
}
