package com.example.testtask;

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


public class Details_Task extends AppCompatActivity {

    //Declare local variables
    Spinner mSession;
    Spinner mGroup;
    Task mTask;
    Time mTime;
    long mlngEventID;
    long mlngLongTermID;
    long mlngGroupID;
    boolean mblnLoaded;

    ArrayListContainer mSessionList;
    ArrayListContainer mGroupList;
    static TimeKeeper timeKeeper;
    TextView mTitle;
    TextView mDescription;

    //region Overridden Functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_task_details);
        mTitle = (TextView) findViewById(R.id.txbTaskTitle);
        mDescription = (TextView) findViewById(R.id.txbTaskDescription);
        timeKeeper = (TimeKeeper) findViewById(R.id.timeKeeper);
        timeKeeper.setMode(1);
        mTask = new Task();
        mTime = new Time();
        mlngEventID = -1;
        mlngLongTermID = -1;
        mlngGroupID = -1;
        mblnLoaded = false;

//        Cursor c = DatabaseAccess.getRecordsFromTable("tblLongTerm");
//        c.getLong(15);

        mGroup = (Spinner) findViewById(R.id.spnTaskGroupSel);
        mGroupList = new ArrayListContainer();
        mGroupList.LinkArrayToSpinner(mGroup, this);

        mSession = (Spinner) findViewById(R.id.spnTaskSessSel);
        mSessionList = new ArrayListContainer();
        mSessionList.LinkArrayToSpinner(mSession, this);
        mSessionList.mSpinner.setOnItemSelectedListener(sessionListener);


        retrieveExtras();
        setupInitialVisibility();
        setupViews();
        LoadSessionSpinner();
        LoadGroupSpinner();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }
    //endregion

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

    public Long getSession(){
        return mSessionList.getID(mSession.getSelectedItemPosition());
    }

    public boolean isSessionSet(){
        if(mSessionList.mSpinner.getSelectedItemPosition() != 0) return true;
        return false;
    }

    public void setSession(long plngTimeId){
        mSessionList.setIDSpinner(plngTimeId);
    }

    public long getOneOff() {
        if(((CheckBox) findViewById(R.id.chkSessOneOff)).isChecked()){
            return mSessionList.getID(mSessionList.mSpinner.getSelectedItemPosition());
        }
        return -1;
    }

    public void setOneOff(long plngTimeId){
        if(plngTimeId != -1){
            ((CheckBox) findViewById(R.id.chkSessOneOff)).setChecked(true);
        } else {
            ((CheckBox) findViewById(R.id.chkSessOneOff)).setChecked(false);
        }
    }

    public int getTaskType(){
        if(mlngEventID != -1){
            return 1;
        }
        if(mlngLongTermID != -1){
            return 2;
        }
        if(mlngGroupID != -1){
            return 3;
        }
        return 0;
    }

    public long getTaskTypeID(){
        if(mlngEventID != -1){
            return mlngEventID;
        }
        if(mlngLongTermID != -1){
            return mlngLongTermID;
        }
        if(mlngGroupID != -1){
            return mlngGroupID;
        }
        return -1;
    }

    public boolean wasDetailsEdited(){
        if (!mTitle.getText().toString().equals(mTask.mstrTitle)
        || !mDescription.getText().toString().equals(mTask.mstrDescription))
            return true;
        return false;
    }

    public boolean wasSessionSessionReplaced(){
        if(mTask.mlngTaskID != -1) { //Task was loaded
            if (isSessionSet() && mTime.isSession()
                    && (getSession() != mTime.mlngTimeID)) {
                return true;
            }
        }
        return false;
    }

    public boolean wasSessionTimeReplaced(){
        if(mTask.mlngTaskID != -1){ //Task was loaded
            if(!isSessionSet() && mTime.isSession()){
                return true;
            }
        }
        return false;
    }

    public boolean wasTimeSessionReplaced(){
        if(mTask.mlngTaskID != -1){
            if(isSessionSet() && !mTime.isSession()){
                return true;
            }
        }
        return false;
    }
    //endregion

    //region ACTIVITY INITIALIZAION
    private void setupViews() {
        if (mlngGroupID != -1){//Group
            mGroupList.setIDSpinner(mTask.mlngTaskTypeID);
        }

        if (mlngEventID == -1){
            LoadSessionSpinner();
            LoadGroupSpinner();
            if(mTask.mlngTimeID != -1){
                setOneOff(mTask.mlngOneOff);
                //It will only set it to one or the other because only 1 should ever evaluate to a record.
                setSession(mTask.mlngOneOff);
                setSession(mTask.mlngTimeID);
            }
        }
    }

    private void setupInitialVisibility() {
        if (mlngEventID != -1){ //Event
            (findViewById(R.id.spnTaskSessSel)).setVisibility(View.GONE);
            (findViewById(R.id.btnTaskAddSess)).setVisibility(View.GONE);
            (findViewById(R.id.spnTaskGroupSel)).setVisibility(View.GONE);
            (findViewById(R.id.timeKeeper)).setVisibility(View.GONE);
        } else if (mlngLongTermID != -1){ //Longterm
            (findViewById(R.id.spnTaskSessSel)).setVisibility(View.GONE);
            (findViewById(R.id.btnTaskAddSess)).setVisibility(View.GONE);
            (findViewById(R.id.spnTaskGroupSel)).setVisibility(View.GONE);
            timeKeeper.setMode(4);
        } else if (mlngGroupID != -1 && !mblnLoaded){ //Group
            (findViewById(R.id.spnTaskGroupSel)).setEnabled(false);
        }
        (findViewById(R.id.chkSessOneOff)).setVisibility(View.GONE);
    }

    private void retrieveExtras() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null){
            mTask = new Task(extras.getLong("EXTRA_TASK_ID",-1));
            if(mTask.mlngTaskID != -1){
                mblnLoaded = true;
                //Get all data from the task and apply it to the control
                setSession(mTask.mlngTimeID);
                setOneOff(mTask.mlngOneOff);
                setTaskTitle(mTask.mstrTitle);
                setTaskDesc(mTask.mstrDescription);
                if (mTask.mintTaskType == 1) mlngEventID = mTask.mlngTaskTypeID;
                if (mTask.mintTaskType == 2) mlngLongTermID = mTask.mlngTaskTypeID;
                if (mTask.mintTaskType == 3) {
                    mlngGroupID = mTask.mlngTaskTypeID;

                }
                mTime = new Time(mTask.mlngTimeID);
                timeKeeper.loadTimeDetails(mTime);
            } else {
                mlngEventID = extras.getLong("EXTRA_EVENT_ID", -1);
                mlngLongTermID = extras.getLong("EXTRA_LONGTERM_ID", -1);
                mlngGroupID = extras.getLong("EXTRA_GROUP_ID", -1);
            }
        }
    }

    public void LoadSessionSpinner(){
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblSession");

        mSessionList.Clear();
        //Add pre-set session so that a value can be grabbed.
        mSessionList.Add("No Session",(long)-1);
        while(cursor.moveToNext()){
            mSessionList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngTimeID")));
        }
        mSessionList.mAdapter.notifyDataSetChanged();
    }

    public void LoadGroupSpinner(){
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblGroup");

        mGroupList.Clear();
        //Add pre-set session so that a value can be grabbed.
        mGroupList.Add("No Group",(long)-1);
        while(cursor.moveToNext()){
            mGroupList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngGroupID")));
        }
        if(mlngGroupID != -1){
            mGroupList.setIDSpinner(mlngGroupID);
        }
        mGroupList.mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /*if (mTask.mlngTaskID != -1){
            getMenuInflater().inflate(R.menu.event_edit_menu, menu);
        }*/
        return true;
    }
    //endregion

    //region ACTIVITY COMPLETION
    public void CreateTask(View view){
        DatabaseAccess.mDatabase.beginTransaction();
        try {
            //updating and regular creation can probably be joined together but as it's just as simple to keep
            //them seperated it will for now be.
            if(mTask.mlngTaskID != -1) {
                if (wasDetailsEdited()) {
                    mTask.updateTaskDetails(mTitle.getText().toString(),
                            mDescription.getText().toString());
                }

                if(mlngEventID == -1){
                    if (wasSessionSessionReplaced()) {
                        mTime = new Time(getSession());
                        if (getOneOff() != -1) {
                            mTime = oneOffTimeCopy();
                        }
                        //replace time id
                        mTask.replaceTimeId(mTime.mlngTimeID);
                    } else if (wasSessionTimeReplaced()) {
                        //create new time id and replace.
                        mTime = timeKeeper.createTimeDetails(-1,
                                -1,
                                -1);
                        mTask.replaceTimeId(mTime.mlngTimeID);
                    } else if (wasTimeSessionReplaced()) {
                        //complete time and replace id
                        mTime.completeTime();
                        mTask.updateOneOff(getOneOff());
                        mTask.replaceTimeId(getSession());
                        mTime = new Time(getSession());
                    } else {
                        mTime.clearGenerationPoints();
                        mTime = timeKeeper.createTimeDetails(mTime.mlngTimeID,
                                mTime.mintTimeframe,
                                mTime.mlngTimeframeID);
                    }
                    //Remove instances associated w/ original time
                    mTask.clearActiveInstances();
                }
            } else {
                if (getOneOff() != -1) {
                    //TODO: Create button to allow adding to time instance currently active (adding to this weekend during weekend instead of next weekend)
                    mTime = new Time(getSession());
                    mTime = oneOffTimeCopy();
                } else if (getSession() != -1) {
                    mTime = new Time(getSession());
                } else if (mlngEventID == -1 && //Not event task
                        (mlngLongTermID == -1 || timeKeeper.isTimeSet())) { //Not long term w/o time set
                    mTime = timeKeeper.createTimeDetails(-1,
                            -1,
                            -1);
                }
                mTask = new Task(mTask.mlngTaskID, //need this because effectively calling new object function
                        mTime.mlngTimeID,
                        Task_Display.getCurrentCalendar().getTimeInMillis(),
                        getTaskTitle(),
                        getTaskDesc(),
                        (long) -1,
                        getTaskType(),
                        getTaskTypeID(),
                        getOneOff());
            }
            mTime.generateInstances(true,mTask.mlngTaskID);
            setResult(RESULT_OK);
            finish();

            DatabaseAccess.mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }
        DatabaseAccess.mDatabase.endTransaction();
    }
    //endregion

    //region HANDLERS
    AdapterView.OnItemSelectedListener sessionListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            if(mblnLoaded && mSessionList.getID(position) != mTime.mlngTimeID){
                mblnLoaded = false;
            }
            if(mSessionList.getID(position) != -1){
                timeKeeper.resetTimeKeeper();
                timeKeeper.loadTimeDetails(mSessionList.getID(position));
                //Deactivate timekeeper for editing
                timeKeeper.setActiveTimekeeper(false);
                //Provide one off opportunity
                (findViewById(R.id.chkSessOneOff)).setVisibility(View.VISIBLE);
                if(!mblnLoaded)setOneOff(mSessionList.getID(position));
            } else {
                timeKeeper.resetTimeKeeper();
                timeKeeper.loadTimeDetails(mTime);
                //Reactivate timekeeper for editing
                timeKeeper.setActiveTimekeeper(true);
                //Remove one off opportunity
                (findViewById(R.id.chkSessOneOff)).setVisibility(View.INVISIBLE);
                if(!mblnLoaded)setOneOff(mSessionList.getID(position));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };

    public Time oneOffTimeCopy(){
        return new Time(-1,
                mTime.getNextPriority(false),
                mTime.getNextPriority(true),
                Task_Display.getCurrentCalendar().getTimeInMillis(),
                mTime.mblnFromTime,
                mTime.mblnToTime,
                mTime.mblnToDate,
                -1,
                -1,
                0,
                0,
                false,
                -1,
                false);
    }

    public void StartNewSession(View view) {
        Intent intent = new Intent(this, Details_Session.class);
        startActivityForResult(intent,1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
                if (resultCode == RESULT_OK) {
                    LoadSessionSpinner();
                    setSession(data.getLongExtra("EXTRA_SESSION_ID", -1));
                }
                break;
            }
        }
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
                bundle.putLong("TaskID", mTask.mlngTaskID);
                DialogFragment newFragment = new Details_Task.TaskDeleteConfirmationFragment();
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

    //endregion
}
