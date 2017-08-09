package com.example.testtask;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;


public class Task_Task  extends AppCompatActivity {

    //Declare local variables
    Spinner mSession;
    Long mlngTaskId = (long)-1;

    ArrayListContainer mSessionList;
    TimeKeeper timeKeeper;
    TextView mTitle;
    TextView mDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_task);
        mTitle = (TextView) findViewById(R.id.txbTaskTitle);
        mDescription = (TextView) findViewById(R.id.txbTaskDescription);
        timeKeeper = (TimeKeeper) findViewById(R.id.timeKeeper);

        mSession = (Spinner) findViewById(R.id.spnTaskSessSel);
        mSessionList = new ArrayListContainer();
        mSessionList.LinkArrayToSpinner(mSession, this);


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null){
            mlngTaskId = getIntent().getLongExtra("EXTRA_TASK_ID",-1);
        }
        if (mlngTaskId != -1){
            setSessionSpinner();
            LoadTask(mlngTaskId);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        setSessionSpinner();
    }

    public void setSessionSpinner(){
        String[] projection = {"fstrTitle","flngID"};
        String sortOrder = "fstrTitle DESC";

        Cursor cursor = Task_Display.mDataBase.query("tblSession",
                projection,
                null,
                null,
                null,
                null,
                sortOrder);

        mSessionList.Clear();
        //Add pre-set session so that a value can be grabbed.
        mSessionList.Add("No Session",(long)-1);
        while(cursor.moveToNext()){
            mSessionList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngID")));
        }
        mSessionList.mAdapter.notifyDataSetChanged();
    }

    public String getTaskTitle(){
        return mTitle.getText().toString();
    }

    public void setTaskTitle(String pstrTitle){
        mTitle.setText(pstrTitle);
    }

    public String getTaskDesc(){
        return mDescription.getText().toString();
    }

    public void setTaskDesc(String pstrDescription){
        mDescription.setText(pstrDescription);
    }

    //For future reference logic is you don't need to know what session came with the task. If you override it, you override it.
    public Long getSessionID(){
        return mSessionList.GetID(mSession.getSelectedItemPosition());
    }

    private void LoadTask(Long plngTaskId) {
        Cursor cursor;
        String rawGetTask = "SELECT * \n" +
                "FROM tblTask t \n" +
                "WHERE t.flngID = ?";
        String[] parameters = {Long.toString(plngTaskId)};
        cursor = Task_Display.mDataBase.rawQuery(rawGetTask,parameters);

        while(cursor.moveToNext()){
            Long lngTimeID = cursor.getLong(cursor.getColumnIndex("flngTimeID"));
            Long lngSessionID = cursor.getLong(cursor.getColumnIndex("flngSessionID"));
            setTaskTitle(cursor.getString(cursor.getColumnIndex("fstrTitle")));
            setTaskDesc(cursor.getString(cursor.getColumnIndex("fstrDescription")));
            if (lngSessionID != -1){
                mSessionList.setIDSpinner(lngSessionID);
            } else if (lngTimeID != -1){
                timeKeeper.loadTimeDetails(lngTimeID);
            }
        }
    }

    public void ceaseTaskCreation(View view){
        switch(view.getId()){
            case R.id.btnTaskConfirm:
                if (mlngTaskId == -1){
                    InitializeTaskCreation();
                } else{
                    //TODO: Allow for going from time to session (need to inactivate time id formally associated with task)
                    if(timeKeeper.getTimeID() != -1 && getSessionID() == -1){
                        timeKeeper.updateTimeRecord();
                    } else if (timeKeeper.getTimeID() == -1 && timeKeeper.blnTimeDetailsExist()){
                        timeKeeper.createTime();
                    }
                    updateTaskRecord();
                }
                setResult(RESULT_OK);
                finish();
                break;
            default:
                setResult(RESULT_CANCELED);
                finish();
        }
    }

    private void updateTaskRecord() {
        Long lngNewTimeId = getSessionID() != -1 ? -1:timeKeeper.getTimeID();
        String rawUpdateTaskRecord = "UPDATE tblTask \n" +
                "SET fstrTitle = '" + getTaskTitle() + "', \n" +
                "fstrDescription = '" + getTaskDesc() + "', \n" +
                "flngSessionID = " + Long.toString(getSessionID()) + ", \n" +
                "flngTimeID = " + Long.toString(lngNewTimeId) + " \n" +
                "WHERE flngID = " + Long.toString(mlngTaskId);
        Cursor c = Task_Display.mDataBase.rawQuery(rawUpdateTaskRecord,null);
        c.moveToFirst();
        c.close();

        Long lngTaskInstanceID = getOpenTaskInstanceFromTask(mlngTaskId);
        if (lngTaskInstanceID != -1){
            Task_Display.systemCompleteTaskInstance(lngTaskInstanceID);
        }
        evaluateTaskInstanceCreation();
    }

    private long getOpenTaskInstanceFromTask(Long plngTaskId){
        String rawGetTaskInstance = "SELECT flngID \n" +
                "FROM tblTaskInstance " +
                "WHERE flngTaskID = " + Long.toString(plngTaskId) + " " +
                "AND fblnSystemComplete = 0 AND fblnComplete = 0";
        Cursor c = Task_Display.mDataBase.rawQuery(rawGetTaskInstance,null);
        c.moveToFirst();
        if (c.getCount() != 1){ return -1; }
        return c.getLong(c.getColumnIndex("flngID"));
    }

    public void InitializeTaskCreation() {
        //todo: add validation to session before it attempts to be created

        //Create time if no session provided
        if (getSessionID() == -1) {timeKeeper.createTime();}
        //Create task
        mlngTaskId = createTask(getTaskTitle(),
                getTaskDesc(),
                getSessionID(),
                timeKeeper.getTimeID());

        //Evaluate if task needs an instance of it created
        evaluateTaskInstanceCreation();

        setResult(RESULT_OK);
        finish();
    }

    private void evaluateTaskInstanceCreation(){
        if (getSessionID() == -1 && timeKeeper.getTimeRange() == "") {//no session details or repeating time details to evaluate
            createTaskInstance(mlngTaskId);
        } else if (getSessionID() != -1 && evaluateTime(getSessionID(),(long)-1)){
            createTaskInstance(mlngTaskId);
        } else if (timeKeeper.getTimeRange() != "" && evaluateTime((long)-1, timeKeeper.getTimeID())){
            createTaskInstance(mlngTaskId);
        }
    }

    private long createTask(String pstrTitle, String pstrDescription, Long plngSessionId, Long plngTimeId){
        ContentValues values = new ContentValues();
        values.put("fstrTitle",pstrTitle);
        values.put("fstrDescription", pstrDescription);
        values.put("flngSessionID", plngSessionId);
        values.put("flngTimeID",plngTimeId);
        return Task_Display.mDataBase.insertOrThrow("tblTask",null,values);
    }

    private long createTaskInstance(Long plngTaskID) {
        ContentValues values = new ContentValues();
        values.put("flngTaskID",plngTaskID);
        values.put("fblnComplete",0);
        values.put("fblnSystemComplete",0);
        return Task_Display.mDataBase.insertOrThrow("tblTaskInstance",null,values);
    }

    private boolean evaluateTime(Long plngSessionId,
                                 Long plngTimeId) {
        Cursor cursor = null;
        Calendar calendar = Calendar.getInstance();
        Boolean evaluation = false;
        if (plngSessionId != -1) {
            String rawGetTimeDetailsFromSession = "SELECT w.* \n" +
                    "FROM tblTime t \n" +
                    "JOIN tblSession s \n" +
                    "ON s.flngTimeID = t.flngID \n" +
                    "JOIN tblWeek w \n" +
                    "ON w.flngID = t.flngWeekID \n" +
                    "WHERE s.flngID = ?";
            String[] parameters = {Long.toString(plngSessionId)};
            cursor = Task_Display.mDataBase.rawQuery(rawGetTimeDetailsFromSession,parameters);
        } else if (plngTimeId != -1){
            String rawGetTimeDetailsFromSession = "SELECT w.* \n" +
                    "FROM tblTime t \n" +
                    "JOIN tblWeek w \n" +
                    "ON w.flngID = t.flngWeekID \n" +
                    "WHERE t.flngID = ?";
            String[] parameters = {Long.toString(plngTimeId)};
            cursor = Task_Display.mDataBase.rawQuery(rawGetTimeDetailsFromSession,parameters);
        }

        cursor.moveToFirst();
        switch (calendar.get(Calendar.DAY_OF_WEEK)){
            case Calendar.SUNDAY:
                evaluation = cursor.getInt(cursor.getColumnIndex("fblnSunday")) == 1;
                break;
            case Calendar.MONDAY:
                evaluation = cursor.getInt(cursor.getColumnIndex("fblnMonday")) == 1;
                break;
            case Calendar.TUESDAY:
                evaluation = cursor.getInt(cursor.getColumnIndex("fblnTuesday")) == 1;
                break;
            case Calendar.WEDNESDAY:
                evaluation = cursor.getInt(cursor.getColumnIndex("fblnWednesday")) == 1;
                break;
            case Calendar.THURSDAY:
                evaluation = cursor.getInt(cursor.getColumnIndex("fblnThursday")) == 1;
                break;
            case Calendar.FRIDAY:
                evaluation = cursor.getInt(cursor.getColumnIndex("fblnFriday")) == 1;
                break;
            case Calendar.SATURDAY:
                evaluation = cursor.getInt(cursor.getColumnIndex("fblnSaturday")) == 1;
                break;
        }

        return evaluation;
    }

    public void StartNewSession(View view) {
        Intent intent = new Intent(this, Task_Session.class);
        startActivity(intent);
    }
}
