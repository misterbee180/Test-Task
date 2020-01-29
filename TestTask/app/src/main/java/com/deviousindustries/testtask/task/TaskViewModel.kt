package com.deviousindustries.testtask.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import com.deviousindustries.testtask.DatabaseAccess
import com.deviousindustries.testtask.Utilities
import com.deviousindustries.testtask.classes.Task
import com.deviousindustries.testtask.classes.Time
import com.deviousindustries.testtask.constants.NULL_OBJECT

class TaskViewModel : ViewModel() {
    var title = MutableLiveData<String>()
    var description = MutableLiveData<String>()
    var timeID = NULL_OBJECT
    var eventID = NULL_OBJECT
    var longtermID = NULL_OBJECT
    var groupID = NULL_OBJECT
    lateinit var task : Task

    fun loadTask(taskID: Long = NULL_OBJECT){
        with(Task(taskID)){
            task = this
            title.value = fstrTitle
            description.value = fstrDescription
            timeID = flngTimeID
        }
    }

//    fun confirm(){
//        if(task.flngTaskID != NULL_OBJECT){
//            if (wasDetailsEdited()) {
//                task.updateTaskDetails(title.value, description.value)
//            }
//
//            if (eventID == NULL_OBJECT) {
//                if (wasSessionSessionReplaced()) {
//                    mTime = Time.getInstance(getSession())
//                    if (getOneOff() != NULL_OBJECT) {
//                        mTime = mTime.createOneOff(NULL_OBJECT)
//                    }
//                    //replace mTime id
//                    mTask.replaceTimeId(mTime.flngTimeID)
//                } else if (wasSessionTimeReplaced()) {
//                    //create new mTime id and replace.
//                    mTime = timeKeeper.createTimeDetails(NULL_OBJECT,
//                            NULL_POSITION,
//                            NULL_OBJECT,
//                            false,
//                            "")
//                    mTask.replaceTimeId(mTime.flngTimeID)
//                } else if (wasTimeSessionReplaced()) {
//                    //complete mTime and replace id
//                    mTime.completeTime()
//                    mTime = Time.getInstance(getSession())
//                    if (getOneOff() != NULL_OBJECT) {
//                        mTask.updateOneOff(getOneOff())
//                        mTime = mTime.createOneOff(NULL_OBJECT)
//                    }
//                    mTask.replaceTimeId(mTime.flngTimeID)
//                } else {
//                    mTime.clearGenerationPoints()
//                    mTime = timeKeeper.createTimeDetails(mTime.flngTimeID,
//                            mTime.fintTimeframe,
//                            mTime.flngTimeframeID,
//                            false,
//                            "")
//                }
//                //Remove instances associated w/ original mTime
//                mTask.finishActiveInstances(3)
//            }
//        } else {
//            if (getOneOff() != NULL_OBJECT) {
//                //TODO: Create button to allow adding to next mTime instance istead of currently active (adding to next weekend during this weekend instead of this weekend)
//                mTime = Time.getInstance(getSession())
//                mTime = mTime.createOneOff(NULL_OBJECT)
//            } else if (getSession() != NULL_OBJECT) {
//                mTime = Time.getInstance(getSession())
//            } else if (mlngEventID == NULL_OBJECT && //Not event task
//                    (mlngLongTermID == NULL_OBJECT || timeKeeper.isTimeSet())) { //Not long term w/o mTime set
//                mTime = timeKeeper.createTimeDetails(NULL_OBJECT,
//                        NULL_POSITION,
//                        NULL_OBJECT,
//                        false,
//                        "")
//            }
//            mTask = Task(mTask.flngTaskID, //need this because effectively calling new object function
//                    mTime.flngTimeID,
//                    Utilities.getCurrentCalendar().timeInMillis,
//                    getTaskTitle(),
//                    getTaskDesc(),
//                    NULL_DATE,
//                    getTaskType(),
//                    getTaskTypeID(),
//                    getOneOff())
//        }
//        DatabaseAccess.mDatabase.beginTransaction()
//        try {
//            if (timeKeeper.validateTimeDetails()) {
//                //updating and regular creation can probably be joined together but as it's just as simple to keep
//                //them seperated it will for now be.
//                if (mTask.flngTaskID != NULL_OBJECT) {
//                    if (wasDetailsEdited()) {
//                        mTask.updateTaskDetails(mTitle.getText().toString(),
//                                mDescription.getText().toString())
//                    }
//
//                    if (mlngEventID == NULL_OBJECT) {
//                        if (wasSessionSessionReplaced()) {
//                            mTime = Time.getInstance(getSession())
//                            if (getOneOff() != NULL_OBJECT) {
//                                mTime = mTime.createOneOff(NULL_OBJECT)
//                            }
//                            //replace mTime id
//                            mTask.replaceTimeId(mTime.flngTimeID)
//                        } else if (wasSessionTimeReplaced()) {
//                            //create new mTime id and replace.
//                            mTime = timeKeeper.createTimeDetails(NULL_OBJECT,
//                                    NULL_POSITION,
//                                    NULL_OBJECT,
//                                    false,
//                                    "")
//                            mTask.replaceTimeId(mTime.flngTimeID)
//                        } else if (wasTimeSessionReplaced()) {
//                            //complete mTime and replace id
//                            mTime.completeTime()
//                            mTime = Time.getInstance(getSession())
//                            if (getOneOff() != NULL_OBJECT) {
//                                mTask.updateOneOff(getOneOff())
//                                mTime = mTime.createOneOff(NULL_OBJECT)
//                            }
//                            mTask.replaceTimeId(mTime.flngTimeID)
//                        } else {
//                            mTime.clearGenerationPoints()
//                            mTime = timeKeeper.createTimeDetails(mTime.flngTimeID,
//                                    mTime.fintTimeframe,
//                                    mTime.flngTimeframeID,
//                                    false,
//                                    "")
//                        }
//                        //Remove instances associated w/ original mTime
//                        mTask.finishActiveInstances(3)
//                    }
//                } else {
//                    if (getOneOff() != NULL_OBJECT) {
//                        //TODO: Create button to allow adding to next mTime instance istead of currently active (adding to next weekend during this weekend instead of this weekend)
//                        mTime = Time.getInstance(getSession())
//                        mTime = mTime.createOneOff(NULL_OBJECT)
//                    } else if (getSession() != NULL_OBJECT) {
//                        mTime = Time.getInstance(getSession())
//                    } else if (mlngEventID == NULL_OBJECT && //Not event task
//                            (mlngLongTermID == NULL_OBJECT || timeKeeper.isTimeSet())) { //Not long term w/o mTime set
//                        mTime = timeKeeper.createTimeDetails(NULL_OBJECT,
//                                NULL_POSITION,
//                                NULL_OBJECT,
//                                false,
//                                "")
//                    }
//                    mTask = Task(mTask.flngTaskID, //need this because effectively calling new object function
//                            mTime.flngTimeID,
//                            Utilities.getCurrentCalendar().timeInMillis,
//                            getTaskTitle(),
//                            getTaskDesc(),
//                            NULL_DATE,
//                            getTaskType(),
//                            getTaskTypeID(),
//                            getOneOff())
//                }
//                mTime.generateInstances(true, mTask.flngTaskID)
//                setResult(Activity.RESULT_OK)
//                finish()
//            }
//            DatabaseAccess.mDatabase.setTransactionSuccessful()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        DatabaseAccess.mDatabase.endTransaction()
//    }
}
