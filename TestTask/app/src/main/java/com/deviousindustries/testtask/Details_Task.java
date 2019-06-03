package com.deviousindustries.testtask;

import android.content.Intent;
import android.database.Cursor;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.deviousindustries.testtask.classes.Task;
import com.deviousindustries.testtask.classes.Time;
import static com.deviousindustries.testtask.constants.ConstantsKt.*;


public class Details_Task extends AppCompatActivity {

    //Declare local variables
    Spinner mSession;
    Spinner mGroup;
    Task mTask;
    Time mTime;
    long mlngEventID;
    long mlngLongTermID;
    long mlngGroupID;
    boolean fblnSessionOnLoad; //DO NOT CONFUSE THIS with an indicator of having been loaded. Simply is used to make sure only certain things
    //happen during a load and not other things. Is set back to false after load. USE mTask.flngTaskID <> NULL_OBJECT

    ArrayListContainer mSessionList;
    ArrayListContainer mGroupList;
    TimeKeeper timeKeeper;
    TextView mTitle;
    TextView mDescription;

    //region Overridden Functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_task_details);
        Utilities.Companion.instantiate(getApplicationContext());
        mTitle = findViewById(R.id.txbTaskTitle);
        mDescription = findViewById(R.id.txbTaskDescription);
        timeKeeper = findViewById(R.id.timeKeeper);
        timeKeeper.setMode(1);
        mTask = new Task();
        mTime = new Time();
        mlngEventID = NULL_OBJECT;
        mlngLongTermID = NULL_OBJECT;
        mlngGroupID = NULL_OBJECT;
        fblnSessionOnLoad = false;

//        Cursor c = DatabaseAccess.getRecordsFromTable("tblLongTerm");
//        c.getLong(15);

        mGroup = findViewById(R.id.spnTaskGroupSel);
        mGroupList = new ArrayListContainer();
        mGroupList.LinkArrayToSpinner(mGroup, this);

        mSession = findViewById(R.id.spnTaskSessSel);
        mSessionList = new ArrayListContainer();
        mSessionList.LinkArrayToSpinner(mSession, this);
        mSessionList.mSpinner.setOnItemSelectedListener(sessionListener);


        retrieveExtras();
        setupInitialVisibility();
        setupViews();
        LoadSessionSpinner();
        LoadGroupSpinner();
    }

    private void retrieveExtras() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null){
            mTask = new Task(extras.getLong("EXTRA_TASK_ID",NULL_OBJECT));
            if(mTask.flngTaskID != NULL_OBJECT){
                fblnSessionOnLoad = true;
                //Get all data from the task and apply it to the control
                setSession(mTask.flngTimeID);
                setOneOff(mTask.flngOneOff);
                setTaskTitle(mTask.fstrTitle);
                setTaskDesc(mTask.fstrDescription);
                if (mTask.fintTaskType == 1) mlngEventID = mTask.flngTaskTypeID;
                if (mTask.fintTaskType == 2) mlngLongTermID = mTask.flngTaskTypeID;
                if (mTask.fintTaskType == 3) {
                    mlngGroupID = mTask.flngTaskTypeID;

                }
                mTime = new Time(mTask.flngTimeID);
                timeKeeper.loadTimeKeeper(mTime);
            } else {
                mlngEventID = extras.getLong("EXTRA_EVENT_ID", NULL_OBJECT);
                mlngLongTermID = extras.getLong("EXTRA_LONGTERM_ID", NULL_OBJECT);
                mlngGroupID = extras.getLong("EXTRA_GROUP_ID", NULL_OBJECT);
            }
        }
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
        return (mSessionList.mSpinner.getSelectedItemPosition() > 0);
    }

    public void setSession(long plngTimeId){
        mSessionList.setIDSpinner(plngTimeId);
    }

    public long getOneOff() {
        if(((CheckBox) findViewById(R.id.chkSessOneOff)).isChecked()){
            return mSessionList.getID(mSessionList.mSpinner.getSelectedItemPosition());
        }
        return NULL_OBJECT;
    }

    public void setOneOff(long plngTimeId){
        if(plngTimeId != NULL_OBJECT){
            ((CheckBox) findViewById(R.id.chkSessOneOff)).setChecked(true);
        } else {
            ((CheckBox) findViewById(R.id.chkSessOneOff)).setChecked(false);
        }
    }

    public int getTaskType(){
        if(mlngEventID != NULL_OBJECT){
            return 1;
        }
        if(mlngLongTermID != NULL_OBJECT){
            return 2;
        }
        if(mlngGroupID != NULL_OBJECT){
            return 3;
        }
        return 0;
    }

    public long getTaskTypeID(){
        if(mlngEventID != NULL_OBJECT){
            return mlngEventID;
        }
        if(mlngLongTermID != NULL_OBJECT){
            return mlngLongTermID;
        }
        if(mlngGroupID != NULL_OBJECT){
            return mlngGroupID;
        }
        return BASE_POSITION;
    }

    public boolean wasDetailsEdited(){
        return (!mTitle.getText().toString().equals(mTask.fstrTitle)
                || !mDescription.getText().toString().equals(mTask.fstrDescription));
    }

    public boolean wasSessionSessionReplaced(){
        return mTask.flngTaskID != NULL_OBJECT &&
                isSessionSet() &&
                mTime.fblnSession; //&&
                //(getSession() != mTime.flngTimeID);
    }

    public boolean wasSessionTimeReplaced(){
        return mTask.flngTaskID != NULL_OBJECT &&
                !isSessionSet() &&
                mTime.fblnSession;
    }

    public boolean wasTimeSessionReplaced(){
        return mTask.flngTaskID != NULL_OBJECT &&
                isSessionSet() &&
                !mTime.fblnSession;
    }
    //endregion

    //region ACTIVITY INITIALIZATION
    private void setupViews() {
        if (mlngGroupID != NULL_OBJECT){//Group
            mGroupList.setIDSpinner(mTask.flngTaskTypeID);
        }

        if (mlngEventID == NULL_OBJECT){
            LoadSessionSpinner();
            LoadGroupSpinner();
            if(mTask.flngTimeID != NULL_OBJECT){
                setOneOff(mTask.flngOneOff);
                //It will only set it to one or the other because only 1 should ever evaluate to a record.
                setSession(mTask.flngOneOff);
                setSession(mTask.flngTimeID);
            }
        }
    }

    private void setupInitialVisibility() {
        if (mlngEventID != NULL_OBJECT){ //Event
            (findViewById(R.id.spnTaskSessSel)).setVisibility(View.GONE);
            (findViewById(R.id.btnTaskAddSess)).setVisibility(View.GONE);
            (findViewById(R.id.spnTaskGroupSel)).setVisibility(View.GONE);
            (findViewById(R.id.timeKeeper)).setVisibility(View.GONE);
        } else if (mlngLongTermID != NULL_OBJECT){ //Longterm
            (findViewById(R.id.spnTaskSessSel)).setVisibility(View.GONE);
            (findViewById(R.id.btnTaskAddSess)).setVisibility(View.GONE);
            (findViewById(R.id.spnTaskGroupSel)).setVisibility(View.GONE);
            timeKeeper.setMode(4);
        } else if (mlngGroupID != NULL_OBJECT && mTask.flngTaskID == NULL_OBJECT){ //Group
            (findViewById(R.id.spnTaskGroupSel)).setEnabled(false);
        }
        (findViewById(R.id.chkSessOneOff)).setVisibility(View.GONE);
    }

    public void LoadSessionSpinner(){
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblTime","fblnSession = 1 and fblnComplete = " + NULL_OBJECT + "",null);

        mSessionList.Clear();
        //Add pre-set session so that a value can be grabbed.
        mSessionList.Add("No Session",NULL_OBJECT);
        while(cursor.moveToNext()){
            Time tempTime = new Time(cursor.getLong(cursor.getColumnIndex("flngTimeID")));
            mSessionList.Add(tempTime.fstrTitle,
                    tempTime.flngTimeID);
        }
        mSessionList.mAdapter.notifyDataSetChanged();
    }

    public void LoadGroupSpinner(){
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblGroup");

        mGroupList.Clear();
        //Add pre-set session so that a value can be grabbed.
        mGroupList.Add("No Group",NULL_OBJECT);
        while(cursor.moveToNext()){
            mGroupList.Add(cursor.getString(cursor.getColumnIndex("fstrTitle")),cursor.getLong(cursor.getColumnIndex("flngGroupID")));
        }
        if(mlngGroupID != NULL_OBJECT){
            mGroupList.setIDSpinner(mlngGroupID);
        }
        mGroupList.mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /*if (mTask.flngTaskID != NULL_OBJECT){
            getMenuInflater().inflate(R.menu.event_edit_menu, menu);
        }*/
        return true;
    }
    //endregion

    //region ACTIVITY COMPLETION
    public void CreateTask(View view){
        DatabaseAccess.mDatabase.beginTransaction();
        try {
            if(timeKeeper.validateTimeDetails()){
                //updating and regular creation can probably be joined together but as it's just as simple to keep
                //them seperated it will for now be.
                if(mTask.flngTaskID != NULL_OBJECT) {
                    if (wasDetailsEdited()) {
                        mTask.updateTaskDetails(mTitle.getText().toString(),
                                mDescription.getText().toString());
                    }

                    if(mlngEventID == NULL_OBJECT){
                        if (wasSessionSessionReplaced()) {
                            mTime = new Time(getSession());
                            if (getOneOff() != NULL_OBJECT) {
                                mTime = mTime.createOneOff(NULL_OBJECT);
                            }
                            //replace mTime id
                            mTask.replaceTimeId(mTime.flngTimeID);
                        } else if (wasSessionTimeReplaced()) {
                            //create new mTime id and replace.
                            mTime = timeKeeper.createTimeDetails(NULL_OBJECT,
                                    NULL_POSITION,
                                    NULL_OBJECT,
                                    false,
                                    "");
                            mTask.replaceTimeId(mTime.flngTimeID);
                        } else if (wasTimeSessionReplaced()) {
                            //complete mTime and replace id
                            mTime.completeTime();
                            mTime = new Time(getSession());
                            if (getOneOff() != NULL_OBJECT){
                                mTask.updateOneOff(getOneOff());
                                mTime = mTime.createOneOff(NULL_OBJECT);
                            }
                            mTask.replaceTimeId(mTime.flngTimeID);
                        } else {
                            mTime.clearGenerationPoints();
                            mTime = timeKeeper.createTimeDetails(mTime.flngTimeID,
                                    mTime.fintTimeframe,
                                    mTime.flngTimeframeID,
                                    false,
                                    "");
                        }
                        //Remove instances associated w/ original mTime
                        mTask.finishActiveInstances(3);
                    }
                } else {
                    if (getOneOff() != NULL_OBJECT) {
                        //TODO: Create button to allow adding to next mTime instance istead of currently active (adding to next weekend during this weekend instead of this weekend)
                        mTime = new Time(getSession());
                        mTime = mTime.createOneOff(NULL_OBJECT);
                    } else if (getSession() != NULL_OBJECT) {
                        mTime = new Time(getSession());
                    } else if (mlngEventID == NULL_OBJECT && //Not event task
                            (mlngLongTermID == NULL_OBJECT || timeKeeper.isTimeSet())) { //Not long term w/o mTime set
                        mTime = timeKeeper.createTimeDetails(NULL_OBJECT,
                                NULL_POSITION,
                                NULL_OBJECT,
                                false,
                                "");
                    }
                    mTask = new Task(mTask.flngTaskID, //need this because effectively calling new object function
                            mTime.flngTimeID,
                            Viewer_Tasklist.getCurrentCalendar().getTimeInMillis(),
                            getTaskTitle(),
                            getTaskDesc(),
                            NULL_DATE,
                            getTaskType(),
                            getTaskTypeID(),
                            getOneOff());
                }
                mTime.generateInstances(true,mTask.flngTaskID);
                setResult(RESULT_OK);
                finish();
            }
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
            if(fblnSessionOnLoad && mSessionList.getID(position) != mTime.flngTimeID){
                fblnSessionOnLoad = false;
            }
            if(mSessionList.getID(position) != NULL_OBJECT){
                timeKeeper.resetTimeKeeper();
                timeKeeper.loadTimeKeeper(mSessionList.getID(position));
                //Deactivate timekeeper for editing
                timeKeeper.setActiveTimekeeper(false);
                //Provide one off opportunity
                (findViewById(R.id.chkSessOneOff)).setVisibility(View.VISIBLE);
                if(!fblnSessionOnLoad)setOneOff(mSessionList.getID(position));
            } else {
                timeKeeper.resetTimeKeeper();
                timeKeeper.loadTimeKeeper(mTime);
                //Reactivate timekeeper for editing
                timeKeeper.setActiveTimekeeper(true);
                //Remove one off opportunity
                (findViewById(R.id.chkSessOneOff)).setVisibility(View.INVISIBLE);
                if(!fblnSessionOnLoad)setOneOff(mSessionList.getID(position));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };

    public void StartNewSession(View view) {
        Intent intent = new Intent(this, Details_Session.class);
        startActivityForResult(intent,1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 &&
                resultCode == RESULT_OK) {
            LoadSessionSpinner();
            setSession(data.getLongExtra("EXTRA_SESSION_ID", NULL_OBJECT));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        /*switch (item.getItemId()){
            case R.id.action_delete_task:
                Bundle bundle = new Bundle();
                bundle.putLong("TaskID", mTask.flngTaskID);
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
                break;
        }*/

        return super.onOptionsItemSelected(item);
    }

    //endregion
}
