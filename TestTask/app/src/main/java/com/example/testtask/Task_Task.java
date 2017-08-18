package com.example.testtask;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.Calendar;


public class Task_Task  extends AppCompatActivity {

    //Declare local variables
    Spinner mSession;
    Long mlngTaskId = (long)-1;
    Long mlngEventId = (long)-1;

    ArrayListContainer mSessionList;
    TimeKeeper timeKeeper;
    TextView mTitle;
    TextView mDescription;

    //region SETTER/GETTERS
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

    public Long getEventID() {return mlngEventId;}

    public Boolean getIsOneOffFromSession() {return ((CheckBox) findViewById(R.id.chkSessOneOff)).isChecked();}
    //endregion

    //region Overridden Functions
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
        mSessionList.mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                if(mSessionList.GetID(position) != -1){
                    (findViewById(R.id.timeKeeper)).setVisibility(View.GONE);
                    (findViewById(R.id.chkSessOneOff)).setVisibility(View.VISIBLE);
                } else {
                    (findViewById(R.id.timeKeeper)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.chkSessOneOff)).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){
            }
        });

        retrieveExtras();
        setupInitialVisibility();
        setupViews();
    }

    @Override
    protected void onResume(){
        super.onResume();
        setSessionSpinner();
    }
    //endregion

    private void setupViews() {
        if (mlngEventId == -1){
            setSessionSpinner();
        }

        if (mlngTaskId != -1){
            LoadTask(mlngTaskId);
        }
    }

    private void setupInitialVisibility() {
        if (mlngEventId != -1){
            (findViewById(R.id.spnFrequency)).setVisibility(View.GONE);
            (findViewById(R.id.spnTaskSessSel)).setVisibility(View.GONE);
            (findViewById(R.id.btnTaskAddSess)).setVisibility(View.GONE);
            (findViewById(R.id.timeKeeper)).setVisibility(View.GONE);
        }
        (findViewById(R.id.chkSessOneOff)).setVisibility(View.GONE);
    }

    private void retrieveExtras() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null){
            mlngTaskId = getIntent().getLongExtra("EXTRA_TASK_ID",-1);
            mlngEventId = getIntent().getLongExtra("EXTRA_EVENT_ID",-1);
        }
    }

    //region Initialization Functions
    public void setSessionSpinner(){
        String[] projection = {"fstrTitle","flngID"};
        String sortOrder = "fstrTitle DESC";

        Cursor cursor = DatabaseAccess.mDatabase.query("tblSession",
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

    private void LoadTask(Long plngTaskId) {
        Cursor cursor;
        String rawGetTask = "SELECT * \n" +
                "FROM tblTask t \n" +
                "WHERE t.flngID = ?";
        String[] parameters = {Long.toString(plngTaskId)};
        cursor = DatabaseAccess.mDatabase.rawQuery(rawGetTask,parameters);

        while(cursor.moveToNext()){
            Long lngTimeID = cursor.getLong(cursor.getColumnIndex("flngTimeID"));
            Long lngSessionID = cursor.getLong(cursor.getColumnIndex("flngSessionID"));
            setTaskTitle(cursor.getString(cursor.getColumnIndex("fstrTitle")));
            setTaskDesc(cursor.getString(cursor.getColumnIndex("fstrDescription")));
            if (mlngEventId == -1){
                if (lngSessionID != -1){
                    mSessionList.setIDSpinner(lngSessionID);
                } else if (lngTimeID != -1){
                    timeKeeper.loadTimeDetails(lngTimeID);
                }
            }
        }
    }
    //endregion

    public void confirmActivity(View view){
        if (mlngEventId != -1) {
            if (mlngTaskId != -1){
                updateTaskRecord();
            } else {
                EventTaskCreation();
            }
        } else if (mlngTaskId == -1){
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
    }

    private void EventTaskCreation() {
        //Create time if no session provided
        timeKeeper.createTime();

        //Create task
        mlngTaskId = createTask(getTaskTitle(),
                getTaskDesc(),
                getSessionID(),
                timeKeeper.getTimeID(),
                getEventID());

        setResult(RESULT_OK);
        finish();

    }

    public void cancelActivity(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    private void updateTaskRecord() {
        Long lngNewTimeId = getSessionID() != -1 ? -1:timeKeeper.getTimeID();
        String rawUpdateTaskRecord = "UPDATE tblTask \n" +
                "SET fstrTitle = '" + getTaskTitle() + "', \n" +
                "fstrDescription = '" + getTaskDesc() + "', \n" +
                "flngSessionID = " + Long.toString(getSessionID()) + ", \n" +
                "flngTimeID = " + Long.toString(lngNewTimeId) + " \n" +
                "WHERE flngID = " + Long.toString(mlngTaskId);
        Cursor c = DatabaseAccess.mDatabase.rawQuery(rawUpdateTaskRecord,null);
        c.moveToFirst();
        c.close();

        Long lngTaskInstanceID = getOpenTaskInstanceFromTask(mlngTaskId);
        if (lngTaskInstanceID != -1){
            DatabaseAccess.updateTaskInstanceSystemComplete(lngTaskInstanceID);
        }
        if (mlngEventId == -1){
            evaluateTaskInstanceCreation();
        }
    }

    private long getOpenTaskInstanceFromTask(Long plngTaskId){
        String rawGetTaskInstance = "SELECT flngID \n" +
                "FROM tblTaskInstance " +
                "WHERE flngTaskID = " + Long.toString(plngTaskId) + " " +
                "AND fblnSystemComplete = 0 AND fblnComplete = 0";
        Cursor c = DatabaseAccess.mDatabase.rawQuery(rawGetTaskInstance,null);
        c.moveToFirst();
        if (c.getCount() != 1){ return -1; }
        return c.getLong(c.getColumnIndex("flngID"));
    }

    public void InitializeTaskCreation() {
        //todo: add validation to session before it attempts to be created

        Long lngSessionID = getSessionID();
        //Create time if no session provided
        if (getSessionID() == -1) {
            timeKeeper.createTime();
        } else if (getIsOneOffFromSession()){
            timeKeeper.populateTimeFromSession(getSessionID());
            timeKeeper.createTime();
            lngSessionID = (long)-1;
        }

        //Create task
        mlngTaskId = createTask(getTaskTitle(),
                getTaskDesc(),
                lngSessionID,
                timeKeeper.getTimeID(),
                getEventID());

        //Evaluate if task needs an instance of it created
        evaluateTaskInstanceCreation();

        setResult(RESULT_OK);
        finish();


    }

    private void evaluateTaskInstanceCreation(){
        if ((getSessionID() == -1 && timeKeeper.getTimeRange() == "") || getIsOneOffFromSession()) {//no session details or repeating time details to evaluate
            createTaskInstance(mlngTaskId);
        } else if (getSessionID() != -1 && evaluateTime(getSessionID(),(long)-1)){
            createTaskInstance(mlngTaskId);
        } else if (timeKeeper.getTimeRange() != "" && evaluateTime((long)-1, timeKeeper.getTimeID())){
            createTaskInstance(mlngTaskId);
        }
    }

    private long createTask(String pstrTitle, String pstrDescription, Long plngSessionId, Long plngTimeId, Long plngEventId){
        ContentValues values = new ContentValues();
        values.put("fstrTitle",pstrTitle);
        values.put("fstrDescription", pstrDescription);
        values.put("flngSessionID", plngSessionId);
        values.put("flngTimeID",plngTimeId);
        values.put("flngEventID",plngEventId);
        return DatabaseAccess.mDatabase.insertOrThrow("tblTask",null,values);
    }

    private long createTaskInstance(Long plngTaskID) {
        ContentValues values = new ContentValues();
        values.put("flngTaskID",plngTaskID);
        values.put("fblnComplete",0);
        values.put("fblnSystemComplete",0);
        return DatabaseAccess.mDatabase.insertOrThrow("tblTaskInstance",null,values);
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
            cursor = DatabaseAccess.mDatabase.rawQuery(rawGetTimeDetailsFromSession,parameters);
        } else if (plngTimeId != -1){
            String rawGetTimeDetailsFromSession = "SELECT w.* \n" +
                    "FROM tblTime t \n" +
                    "JOIN tblWeek w \n" +
                    "ON w.flngID = t.flngWeekID \n" +
                    "WHERE t.flngID = ?";
            String[] parameters = {Long.toString(plngTimeId)};
            cursor = DatabaseAccess.mDatabase.rawQuery(rawGetTimeDetailsFromSession,parameters);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()){
            case R.id.action_settings:
                return true;
            /*case R.id.action_session:
                viewSessions();
                break;
            case R.id.action_task:
                viewTasks();
                break;
            case R.id.action_event  :
                viewEvents();
                break;*/
        }

        return super.onOptionsItemSelected(item);
    }
}
