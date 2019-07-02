package com.deviousindustries.testtask.task

import android.widget.ArrayAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import com.deviousindustries.testtask.DatabaseAccess
import com.deviousindustries.testtask.Utilities
import com.deviousindustries.testtask.classes.Task
import com.deviousindustries.testtask.classes.Time
import com.deviousindustries.testtask.constants.BASE_POSITION
import com.deviousindustries.testtask.constants.NULL_DATE
import com.deviousindustries.testtask.constants.NULL_OBJECT
import com.deviousindustries.testtask.constants.NULL_POSITION
import com.deviousindustries.testtask.data.Session
import com.deviousindustries.testtask.timekeeper.TimekeeperViewModel
import java.lang.Exception

class TaskViewModel : ViewModel() {
    var title = MutableLiveData<String>()
    var description = MutableLiveData<String>()
    var selectedSession = MutableLiveData<Int>()
    var oneOff = MutableLiveData<Boolean>()
    val sessionList = mutableListOf<Session>()
    var eventID = NULL_OBJECT
    var longTermID = NULL_OBJECT
    var groupID = NULL_OBJECT
    lateinit var task: Task
    lateinit var prevTime: Time
    lateinit var timekeeperViewModel: TimekeeperViewModel

    fun start(timekeeperViewModel: TimekeeperViewModel, 
              taskID: Long = NULL_OBJECT) {
        this.timekeeperViewModel = timekeeperViewModel
        loadTask(taskID)
    }

    private fun loadTask(taskID: Long = NULL_OBJECT) {
        with(Task(taskID)) {
            task = this
            prevTime = Time.getInstance(task.flngTimeID)
            timekeeperViewModel.loadTimekeeper(task.flngTimeID)
            title.value = fstrTitle
            description.value = fstrDescription
            setupSessionList()
            when (fintTaskType) {
                1 -> eventID = flngTaskTypeID
                2 -> longTermID = flngTaskTypeID
                3 -> groupID = flngTaskTypeID
            }
        }
    }

    private fun setupSessionList() {
        sessionList.add(Session("No Session", NULL_OBJECT))
        sessionList.addAll(DatabaseAccess.taskDatabaseDao.loadActiveSessions())
    }

    private fun wasDetailsEdited(): Boolean {
        return title.value != task.fstrTitle || description.value != task.fstrDescription
    }

    private fun isSessionSet(): Boolean {
        return selectedSession.value != NULL_POSITION
    }

    private fun wasSessionSessionReplaced(): Boolean {
        return taskWasLoaded() &&
                isSessionSet() &&
                prevTime.fblnSession
    }

    private fun wasSessionTimeReplaced(): Boolean {
        return taskWasLoaded() &&
                !isSessionSet() &&
                prevTime.fblnSession
    }

    private fun wasTimeSessionReplaced(): Boolean {
        return taskWasLoaded() &&
                isSessionSet() &&
                !prevTime.fblnSession
    }

    private fun getSessionID(): Long {
        return sessionList[selectedSession.value!!].timeID
    }

    private fun getTaskType(): Int {
        if (eventID != NULL_OBJECT) {
            return 1
        }
        if (longTermID != NULL_OBJECT) {
            return 2
        }
        return if (groupID != NULL_OBJECT) {
            3
        } else 0
    }

    private fun getTaskTypeID(): Long {
        return when (getTaskType()) {
            1 -> eventID
            2 -> longTermID
            3 -> groupID
            else -> BASE_POSITION.toLong()
        }
    }

    fun confirm() {
        try {
            DatabaseAccess.mDatabase.beginTransaction()
            if (taskWasLoaded()) {
                if (wasDetailsEdited()) {
                    task.updateTaskDetails(title.value, description.value)
                }
                if (eventID == NULL_OBJECT) {
                    when {
                        wasSessionSessionReplaced() -> {
                            with(Time.getInstance(getSessionID())) {
                                var newTimeID = flngTimeID
                                if (oneOff.value!!) {
                                    newTimeID = createOneOff(NULL_OBJECT).flngTimeID
                                }
                                task.replaceTimeId(newTimeID)
                            }
                        }
                        wasSessionTimeReplaced() -> {
                            timekeeperViewModel.saveTimekeeper().also {
                                task.replaceTimeId(it.flngTimeID)
                            }
                        }
                        wasTimeSessionReplaced() -> {
                            //complete time and replace id
                            prevTime.completeTime()
                            with(Time.getInstance(getSessionID())) {
                                if (oneOff.value!!) {
                                    task.updateOneOff(getSessionID())
                                    createOneOff(NULL_OBJECT).also {
                                        task.replaceTimeId(it.flngTimeID)
                                    }
                                } else {
                                    task.replaceTimeId(flngTimeID)
                                }
                            }
                        }
                        else -> {
                            prevTime.clearGenerationPoints()
                            timekeeperViewModel.saveTimekeeper()
                        }
                    }
                    task.finishActiveInstances(3)
                }
            } else {
                var timeID = NULL_OBJECT
                if (oneOff.value!!) {
                    //TODO: Create button to allow adding to next time instance istead of currently active (adding to next weekend during this weekend instead of this weekend)
                    with(Time.getInstance(getSessionID())) {
                        timeID = createOneOff(NULL_OBJECT).flngTimeID
                    }
                } else if (getSessionID() != NULL_OBJECT) {
                    timeID = getSessionID()
                } else if (eventID == NULL_OBJECT && //Not event task
                        (longTermID == NULL_OBJECT || timekeeperViewModel.isAnyTimeInfoSet())) { //Not long term w/o time set
                    timekeeperViewModel.saveTimekeeper()
                }
                task = Task(task.flngTaskID, //need this because effectively calling new object function
                        timekeeperViewModel.timeID,
                        Utilities.getCurrentCalendar().timeInMillis,
                        title.value,
                        description.value,
                        NULL_DATE,
                        getTaskType(),
                        getTaskTypeID(),
                        if (isSessionSet() && oneOff.value!!) getSessionID() else NULL_OBJECT)
            }
            DatabaseAccess.mDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            DatabaseAccess.mDatabase.endTransaction()
        }
    }

    private fun taskWasLoaded() = task.flngTaskID != NULL_OBJECT
}
