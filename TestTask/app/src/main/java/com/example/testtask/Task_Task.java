package com.example.testtask;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;


public class Task_Task  extends AppCompatActivity {

    //Declare local variables
    Spinner mSession;
    Spinner mGroup;
    Long mlngTaskId = (long)-1;
    Long mlngEventId = (long)-1;
    Long mlngLongTermId = (long)-1;
    Long mlngGroupId = (long)-1;

    ArrayListContainer mSessionList;
    ArrayListContainer mGroupList;
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

    public Long getGroupID(){return mlngGroupId;}

    public Long getEventID() {return mlngEventId;}

    public Long getLongTermID() {return mlngLongTermId;}

    public Boolean getIsOneOffFromSession() {return ((CheckBox) findViewById(R.id.chkSessOneOff)).isChecked();}

    public void setIsOneOff(Boolean pblnOneOff){
        ((CheckBox) findViewById(R.id.chkSessOneOff)).setChecked(pblnOneOff);
    }
    //endregion

    //region Overridden Functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_task);
        mTitle = (TextView) findViewById(R.id.txbTaskTitle);
        mDescription = (TextView) findViewById(R.id.txbTaskDescription);
        timeKeeper = (TimeKeeper) findViewById(R.id.timeKeeper);
        timeKeeper.setUpForRegular();

        mGroup = (Spinner) findViewById(R.id.spnTaskGroupSel);
        mGroupList = new ArrayListContainer();
        mGroupList.LinkArrayToSpinner(mGroup, this);

        mSession = (Spinner) findViewById(R.id.spnTaskSessSel);
        mSessionList = new ArrayListContainer();
        mSessionList.LinkArrayToSpinner(mSession, this);
        mSessionList.mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                if(mSessionList.GetID(position) != -1){
                    (findViewById(R.id.timeKeeper)).setVisibility(View.GONE);
                    (findViewById(R.id.chkSessOneOff)).setVisibility(View.VISIBLE);
                    setIsOneOff(true);
                } else {
                    (findViewById(R.id.timeKeeper)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.chkSessOneOff)).setVisibility(View.INVISIBLE);
                    setIsOneOff(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){
            }
        });

        retrieveExtras();
        retrieveNecessaryData();
        setupInitialVisibility();
        setupViews();
    }

    @Override
    protected void onResume(){
        super.onResume();
        setSessionSpinner();
        setGroupSpinner();
    }
    //endregion

    private void setupViews() {
        if (mlngEventId == -1){
            setSessionSpinner();
            setGroupSpinner();
        }

        if (mlngTaskId != -1){
            LoadTask(mlngTaskId);
        }
    }

    private void setupInitialVisibility() {
        if (mlngEventId != -1){
            (findViewById(R.id.spnTaskSessSel)).setVisibility(View.GONE);
            (findViewById(R.id.btnTaskAddSess)).setVisibility(View.GONE);
            (findViewById(R.id.spnTaskGroupSel)).setVisibility(View.GONE);
            (findViewById(R.id.timeKeeper)).setVisibility(View.GONE);
        } else if (mlngLongTermId != -1){
            (findViewById(R.id.spnTaskSessSel)).setVisibility(View.GONE);
            (findViewById(R.id.btnTaskAddSess)).setVisibility(View.GONE);
            (findViewById(R.id.spnTaskGroupSel)).setVisibility(View.GONE);
            timeKeeper.hideRepetition();
        }
        (findViewById(R.id.chkSessOneOff)).setVisibility(View.GONE);
    }

    private void retrieveExtras() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null){
            mlngTaskId = getIntent().getLongExtra("EXTRA_TASK_ID",-1);
            mlngEventId = getIntent().getLongExtra("EXTRA_EVENT_ID",-1);
            mlngLongTermId = getIntent().getLongExtra("EXTRA_LONGTERM_ID",-1);
            mlngGroupId = getIntent().getLongExtra("EXTRA_GROUP_ID",-1);
        }
    }

    private void retrieveNecessaryData() {
        if (mlngTaskId != -1){
            Cursor tmpCursor = DatabaseAccess.getRecordsFromTable("tblTask","flngTaskID", mlngTaskId);
            tmpCursor.moveToNext();
            mlngEventId = tmpCursor.getLong(tmpCursor.getColumnIndex("flngEventID"));
            mlngGroupId = tmpCursor.getLong(tmpCursor.getColumnIndex("flngGroupID"));
            mlngLongTermId = tmpCursor.getLong(tmpCursor.getColumnIndex("flngLongTermID"));
        }
    }

    //region Initialization Functions
    public void setSessionSpinner(){
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblSession");

        mSessionList.Clear();
        //Add pre-set session so that a value can be grabbed.
        mSessionList.Add("No Session",(long)-1);
        while(cursor.moveToNext()){
            mSessionList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngSessionID")));
        }
        mSessionList.mAdapter.notifyDataSetChanged();
    }

    public void setGroupSpinner(){
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblGroup");

        mGroupList.Clear();
        //Add pre-set session so that a value can be grabbed.
        mGroupList.Add("No Group",(long)-1);
        while(cursor.moveToNext()){
            mGroupList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngGroupID")));
        }
        if(mlngGroupId != -1){
            mGroupList.setIDSpinner(mlngGroupId);
        }
        mGroupList.mAdapter.notifyDataSetChanged();
    }

    private void LoadTask(Long plngTaskId) {
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblTask", "flngTaskID", plngTaskId);

        while(cursor.moveToNext()){
            Long lngTimeID = cursor.getLong(cursor.getColumnIndex("flngTimeID"));
            Long lngSessionID = cursor.getLong(cursor.getColumnIndex("flngSessionID"));
            setIsOneOff(cursor.getLong(cursor.getColumnIndex("fblnOneOff")) == 1 ? true:false);
            setTaskTitle(cursor.getString(cursor.getColumnIndex("fstrTitle")));
            setTaskDesc(cursor.getString(cursor.getColumnIndex("fstrDescription")));
            if (mlngEventId == -1){
                if (mlngGroupId != -1){
                    mGroupList.setIDSpinner(mlngGroupId);
                }
                if (lngSessionID != -1){
                    mSessionList.setIDSpinner(lngSessionID);
                } else if (lngTimeID != -1){
                    timeKeeper.loadTimeDetails(lngTimeID,
                            false);
                }
            }
        }
    }
    //endregion

    //todo: Create process to remove existing instance in case where date is pushed to the future.

    public void confirmActivity(View view){
        if (mlngEventId != -1) {
            if (mlngTaskId != -1){
                updateTaskRecord(); //Updating event task
            } else {
                EventTaskCreation(); //New event task creation
            }
        } else if (mlngTaskId == -1){ //New Task Creation
            InitializeTaskCreation();
        } else{
            //TODO: Allow for going from time to session (need to inactivate time id formally associated with task)
            if(timeKeeper.getTimeID() != -1 && (getSessionID() == -1 || timeKeeper.blnTimeDetailsExist())){
                timeKeeper.createTimeDetails();
            }
            updateTaskRecord();
        }
        setResult(RESULT_OK);
        finish();
    }

    private void EventTaskCreation() {
        //Create time if no session provided
        timeKeeper.createTimeDetails();

        //Create task
        mlngTaskId = createTask(getTaskTitle(),
                getTaskDesc(),
                getSessionID(),
                timeKeeper.getTimeID(),
                getEventID(),
                getGroupID(),
                getLongTermID());

        setResult(RESULT_OK);
        finish();

    }

    private void LongTermTaskCreation() {
        //Create time if no session provided
        timeKeeper.createTimeDetails();

        //Create task
        mlngTaskId = createTask(getTaskTitle(),
                getTaskDesc(),
                getSessionID(),
                timeKeeper.getTimeID(),
                getEventID(),
                getGroupID(),
                getLongTermID());

        setResult(RESULT_OK);
        finish();

    }

    public void cancelActivity(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    private void updateTaskRecord() {
        Long lngNewTimeId = getSessionID() != -1 ? -1:timeKeeper.getTimeID();
         
        DatabaseAccess.updateRecordFromTable("tblTask",
                "flngTaskID",
                mlngTaskId,
                new String[] {"fstrTitle", "fstrDescription", "flngSessionID", "flngTimeID", "flngGroupID"},
                new Object[] {getTaskTitle(), getTaskDesc(), getSessionID(), lngNewTimeId, getGroupID()});
    }

    public void InitializeTaskCreation() {
        //todo: add validation to session before it attempts to be created

        Long lngSessionID = getSessionID();
        //Create time if no session provided
        if (getSessionID() == -1) {
            timeKeeper.createTimeDetails();
        }

        //Create task
        mlngTaskId = createTask(getTaskTitle(),
                getTaskDesc(),
                lngSessionID,
                timeKeeper.getTimeID(),
                getEventID(),
                getGroupID(),
                getLongTermID());

        //Evaluate if task needs an instance of it created
        if (Task_Display.evaluateTaskInstanceCreation(getSessionID(),timeKeeper.getTimeID(), mlngTaskId, mlngLongTermId, this)){
            DatabaseAccess.addRecordToTable("tblTaskInstance",
                    new String[]{"flngTaskID","fblnComplete","fblnSystemComplete", "fdtmCreated"},
                    new Object[]{mlngTaskId, false, false, Task_Display.getCurrentCalendar(this).getTimeInMillis()});
        }

        setResult(RESULT_OK);
        finish();
    }



    private long createTask(String pstrTitle, String pstrDescription, Long plngSessionId, Long plngTimeId, Long plngEventId, Long plngGroupID, Long plngLongTermID){
        ContentValues values = new ContentValues();
        values.put("fstrTitle",pstrTitle);
        values.put("fstrDescription", pstrDescription);
        values.put("flngSessionID", plngSessionId);
        values.put("flngTimeID",plngTimeId);
        values.put("flngEventID",plngEventId);
        values.put("flngGroupID",plngGroupID);
        values.put("flngLongTermID",plngLongTermID);
        values.put("fblnOneOff",getIsOneOffFromSession());
        values.put("fblnActive",1);
        return DatabaseAccess.mDatabase.insertOrThrow("tblTask",null,values);
    }

    public void StartNewSession(View view) {
        Intent intent = new Intent(this, Task_Session.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /*if (mlngTaskId != -1){
            getMenuInflater().inflate(R.menu.event_edit_menu, menu);
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()){
            /*case R.id.action_delete_task:
                Bundle bundle = new Bundle();
                bundle.putLong("TaskID", mlngTaskId);
                DialogFragment newFragment = new Task_Task.TaskDeleteConfirmationFragment();
                newFragment.setArguments(bundle);
                newFragment.show(getSupportFragmentManager(), "Delete Task");*/
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
