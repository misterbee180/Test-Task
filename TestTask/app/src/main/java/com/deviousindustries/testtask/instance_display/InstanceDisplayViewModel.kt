package com.deviousindustries.testtask.instance_display

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.deviousindustries.testtask.AlarmReceiver
import com.deviousindustries.testtask.DatabaseAccess
import com.deviousindustries.testtask.PriorityGroupInstanceTools.PriorityGroupInstanceElement
import com.deviousindustries.testtask.PriorityGroupInstanceTools.PriorityGroupInstanceListAdapter
import com.deviousindustries.testtask.Utilities
import com.deviousindustries.testtask.Utilities.Companion.getCalendar
import com.deviousindustries.testtask.Utilities.Companion.getCurrentCalendar
import com.deviousindustries.testtask.classes.*
import com.deviousindustries.testtask.constants.*
import java.util.*
import kotlin.collections.HashMap

class TaskListViewModel : ViewModel() {

    enum class RecordTypes {
        Priority, Session, TodoInstance
    }

    enum class PriorityQueues {
        Priority, Today, Standard, Upcoming;
    }

    val recordList = MutableLiveData<MutableList<PriorityGroupInstanceElement>>()
    val createPriorityCompleteFragment = MutableLiveData<Boolean>()
    val createSessionCompleteFragment = MutableLiveData<Boolean>()
    val createInstanceCompleteFragment = MutableLiveData<Boolean>()
    val setupInitialAlert = MutableLiveData<Boolean>()
    val viewSession = MutableLiveData<Boolean>()
    val viewInstance = MutableLiveData<Boolean>()
    var activeRecord: PriorityGroupInstanceElement? = null
    var activePosition: Int = NULL_INT

    private val groupMap = HashMap<Triple<Int, Long, String>, Int>()

    init {
        recordList.value = mutableListOf()
        createPriorityCompleteFragment.value = false
        createInstanceCompleteFragment.value = false
        createSessionCompleteFragment.value = false
        viewSession.value = false
        viewInstance.value = false
        setupInitialAlert.value = false
    }

    fun start() {
        var debug_redoSync = false

        generateTaskInstances()
        setupInitialAlert.value = (Utilities.preferences.getLong("general_last_sync", NULL_DATE) < Utilities.getBeginningCurentDay().timeInMillis || debug_redoSync)
        loadRecordList()
    }

    fun resume() {
        loadRecordList()
    }

    fun setupAlert(applicationContext: Context) {
        //Cancel any alarms which may already be set up to run
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        intent.action = "com.deviousindustries.testtask.SYNC"

        //first cancel in case multiple have ben set up
        AlarmReceiver.cancelAlert(applicationContext, intent)

        //re set up the alarm and anything else needing to be done.
        AlarmReceiver.generateAlert(
                applicationContext,
                intent,
                Calendar.getInstance().getTimeInMillis(),
                0,
                AlarmManager.RTC_WAKEUP
        )
    }

    private fun determineListForTask(fromDateTime: Long,
                                     toDateTime: Long,
                                     isFromTimeSet: Boolean,
                                     isToTimeSet: Boolean,
                                     isToDateSet: Boolean,
                                     createdDateTime: Long): Int {

        //Todo: redesign to not use as many booleans. Make considerations for thru tasks.
        val result: Int
        val calNow = getCurrentCalendar() //represents the mTime now
        var calFromWithTime: Calendar? = null
        var calToWithTime: Calendar? = null
        val calFrom: Calendar //represents the from mTime of a task
        val calTo: Calendar //represents the to mTime of a task
        val calCreate = getCurrentCalendar() //represents when the task was created
        calCreate.timeInMillis = createdDateTime

        //start: Set General From Details
        if (fromDateTime != NULL_OBJECT) {
            calFrom = getCalendar(fromDateTime)
        } else {
            calFrom = calCreate.clone() as Calendar
        }

        //start: Set General To Details
        if (toDateTime != NULL_DATE && isToDateSet)
            calTo = getCalendar(toDateTime)
        else {
            calTo = calFrom.clone() as Calendar
            if (isToTimeSet) {
                val temp = getCalendar(toDateTime)
                calTo.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                calTo.set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                calTo.set(Calendar.SECOND, temp.get(Calendar.SECOND))
                calTo.set(Calendar.MILLISECOND, temp.get(Calendar.MILLISECOND))
            }
        }

        //if mTime details exists we need to make sure from and to w/ mTime details are populated
        if (isFromTimeSet || isToTimeSet) {
            calFromWithTime = calFrom.clone() as Calendar
            if (isToTimeSet) {
                calToWithTime = calTo.clone() as Calendar
            } else {
                calToWithTime = calFrom.clone() as Calendar
                //Must have been from mTime that was set so assume till end of day
                calToWithTime.add(Calendar.DAY_OF_YEAR, 1)
                calToWithTime.set(Calendar.HOUR_OF_DAY, 0)
                calToWithTime.set(Calendar.MINUTE, 0)
            }
        }

        //End: Set General From Details
        calFrom.set(Calendar.HOUR_OF_DAY, 0)
        calFrom.set(Calendar.MINUTE, 0)
        calFrom.set(Calendar.SECOND, 0)
        calFrom.set(Calendar.MILLISECOND, 0)

        //End: Set General To Details
        calTo.add(Calendar.DAY_OF_YEAR, 1)
        calTo.set(Calendar.HOUR_OF_DAY, 0)
        calTo.set(Calendar.MINUTE, 0)
        calTo.set(Calendar.MILLISECOND, 0)

        //if either of the mTime settings is set and the from and the to dates surround now then it's a priority
        //this will handle cases both are set and where only one or the other is set

        //Evaluate Time Details
        if (!isFromTimeSet && isToTimeSet //Just to mTime set
                && calNow.after(calFrom) && calNow.before(calToWithTime)) {
            result = PriorityQueues.Priority.ordinal
        } else if (isFromTimeSet && !isToTimeSet && //Just from mTime set
                calNow.after(calFromWithTime) && calNow.before(calTo)) { //Exists w/i mTime bounds
            result = PriorityQueues.Priority.ordinal
        } else if (isFromTimeSet && isToTimeSet && //Time details exist
                calNow.after(calFromWithTime) && calNow.before(calToWithTime)) { //Exists w/i mTime bounds
            result = PriorityQueues.Priority.ordinal
        } else if (calNow.after(calFrom) && calNow.before(calTo) || calNow == calFrom) {
            result = PriorityQueues.Today.ordinal
        } else //At this point it will either be past happening (S) or not yet ready (U)
            if (calNow.after(calTo) || calNow == calTo) {
                result = PriorityQueues.Standard.ordinal
            } else {
                result = PriorityQueues.Upcoming.ordinal
            }
        return result
    }

    private fun generateTaskInstances() {
        DatabaseAccess.mDatabase.beginTransaction()

        try {
            for (time in DatabaseAccess.taskDatabaseDao.getUncompleteTimes()) {
                time.buildTimeInstances()
            }

            for (timeInstance in DatabaseAccess.taskDatabaseDao.getValidGenerationPoints(Utilities.getBeginningCurentDay().timeInMillis, Utilities.getEndCurrentDay().timeInMillis)) {
                with(Time.getInstance(timeInstance.flngTimeID)) {
                    if (this != null && timeInstance.flngGenerationID > flngGenerationID) {
                        generateInstance(timeInstance.fdtmPriority,
                                Utilities.getCalendar(timeInstance.fdtmPriority).also {
                                    if (fblnThru) it.add(Calendar.DAY_OF_YEAR, timeInstance.fintThru)
                                }.timeInMillis)

                        updateGenerationID(timeInstance.flngGenerationID)
                    }
                }
            }
            DatabaseAccess.mDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        DatabaseAccess.mDatabase.endTransaction()
    }

    fun setupForDialogFragment(position: Int) {
        activeRecord = recordList.value!![position]
        activePosition = position
    }

    private fun loadRecordList() {

        val loadedRecordList = mutableListOf<PriorityGroupInstanceElement>()
        groupMap.clear()

        //Add all priorities to the list
        PriorityQueues.values().forEach { priority ->
            //loadedRecordList.add(PrioritySeparator(it.ordinal, it.name, functions))
            loadedRecordList.add(PriorityGroupInstanceElement(
                    priority.ordinal,
                    NULL_INT,
                    NULL_OBJECT,
                    priority.name,
                    PriorityGroupInstanceListAdapter.ElementType.Priority.name,
                    RecordTypes.Priority.name).also { element ->
                element.onClick = {
                    setupForDialogFragment(loadedRecordList.indexOf(element))
                    createPriorityCompleteFragment.value = true
                }
            })
        }

        for (instance in DatabaseAccess.taskDatabaseDao.loadInstancesForTasklist()) {
            val priorityGroup = determineListForTask(instance.fdtmFrom,
                    instance.fdtmTo,
                    instance.hasFromTime,
                    instance.hasToTime,
                    instance.hasToDate,
                    instance.fdtmCreated)

            val instance: InstanceListItemBuilder<PriorityGroupInstanceElement> = TodoItemBuilder(instance.flngInstanceID,
                    instance.fstrTitle,
                    instance.flngGroupKey,
                    instance.fstrGroupTitle,
                    priorityGroup)

            loadedRecordList.addAll(instance.getDisplayRecords())
        }

        loadedRecordList.sortWith(compareBy({ it.priorityId }, { it.groupId }))
        recordList.value = loadedRecordList
    }

    fun completeInstance(position: Int) {
        val recordId = recordList.value!![position].recordId
        DatabaseAccess.taskDatabaseDao.CompleteTaskInstance(recordId)
        loadRecordList()
    }

    fun completeGroup(position: Int) {
        val groupID = recordList.value!![position].groupId
        for (instanceRecord in recordList.value!!.filter { it.groupId == groupID && it.type == RecordTypes.TodoInstance.name }) {
            DatabaseAccess.taskDatabaseDao.CompleteTaskInstance(instanceRecord.recordId)
        }
        loadRecordList()
    }

    fun completePriority(position: Int) {
        val priorityId = recordList.value!![position].priorityId
        for (instanceRecord in recordList.value!!.filter { it.priorityId == priorityId && it.type == RecordTypes.TodoInstance.name }) {
            DatabaseAccess.taskDatabaseDao.CompleteTaskInstance(instanceRecord.recordId)
        }
        loadRecordList()
    }

    private interface InstanceListItemBuilder<T> {
        fun getDisplayRecords(): List<T>
    }

    inner class TodoItemBuilder(private val instanceID: Long,
                                private val title: String,
                                private val sessionID: Long,
                                private val sessionTitle: String,
                                private val priorityID: Int) : InstanceListItemBuilder<PriorityGroupInstanceElement> {

        private val displayRecords = mutableListOf<PriorityGroupInstanceElement>()

        override fun getDisplayRecords(): List<PriorityGroupInstanceElement> {
            val groupID = if (sessionID == NULL_OBJECT) NULL_INT else getSessionGroup()
            addTodoItem(groupID)

            return displayRecords
        }

        private fun addTodoItem(groupID: Int) {
            displayRecords.add(PriorityGroupInstanceElement(
                    priorityID,
                    groupID,
                    instanceID,
                    title,
                    PriorityGroupInstanceListAdapter.ElementType.Instance.name,
                    RecordTypes.TodoInstance.name).also {
                it.onClick = {
                    setupForDialogFragment(recordList.value!!.indexOf(it))
                    createInstanceCompleteFragment.value = true
                }
                it.onClickLong = {
                    setupForDialogFragment(recordList.value!!.indexOf(it))
                    viewInstance.value = true
                }
            })
        }

        private fun getSessionGroup(): Int {
            val sectionTriple = Triple(priorityID, sessionID, "GroupSeparator")
            return groupMap[sectionTriple] ?: putSessionGroup(sectionTriple)
        }

        private fun putSessionGroup(groupTriple: Triple<Int, Long, String>): Int {
            groupMap[groupTriple] = groupMap.size
            val sectionID = groupMap[groupTriple]!!
            addSessionItem(sectionID, groupTriple.second)

            return sectionID
        }

        private fun addSessionItem(sectionID: Int, sessionID: Long) {
            displayRecords.add(
                    PriorityGroupInstanceElement(
                            priorityID,
                            sectionID,
                            sessionID,
                            "--Session: $sessionTitle--",
                            PriorityGroupInstanceListAdapter.ElementType.Group.name,
                            RecordTypes.Session.name).also{
                        it.onClick = {
                            setupForDialogFragment(recordList.value!!.indexOf(it))
                            createSessionCompleteFragment.value = true
                        }
                        it.onClickLong = {
                            setupForDialogFragment(recordList.value!!.indexOf(it))
                            viewSession.value = true
                        }
                    })
        }
    }
}
