package com.deviousindustries.testtask.instance_display

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.deviousindustries.testtask.AlarmReceiver
import com.deviousindustries.testtask.DatabaseAccess
import com.deviousindustries.testtask.Utilities
import com.deviousindustries.testtask.Utilities.Companion.getCalendar
import com.deviousindustries.testtask.Utilities.Companion.getCurrentCalendar
import com.deviousindustries.testtask.classes.*
import com.deviousindustries.testtask.constants.*
import java.util.*
import kotlin.collections.HashMap

enum class ElementType {
    Instance, Group, Priority;

    companion object {
        private val map = values().associateBy { it.ordinal }
        fun fromInt(type: Int) = map[type]
    }
}

class TaskListViewModel : ViewModel() {

    enum class RecordTypes {
        Priority, Session, TodoInstance
    }

    enum class PriorityQueues {
        Priority, Today, Standard, Upcoming;
    }

    val recordList = MutableLiveData<MutableList<GeneralListItem>>()
    val createShortPriorityFragment = MutableLiveData<Boolean>()
    val createShortSessionFragment = MutableLiveData<Boolean>()
    val createShortInstanceFragment = MutableLiveData<Boolean>()
    val setupInitialAlert = MutableLiveData<Boolean>()
    val viewSession = MutableLiveData<Boolean>()
    val viewInstance = MutableLiveData<Boolean>()
    var activeRecord : GeneralListItem? = null
    var activePosition : Int = NULL_INT

    private val groupMap = HashMap<Triple<Int, Long, String>, Int>()

    init {
        recordList.value = mutableListOf()
        createShortPriorityFragment.value = false
        createShortInstanceFragment.value = false
        createShortSessionFragment.value = false
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

    fun setupAlert(applicationContext : Context) {
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

    private fun determineListForTask(pdtmFrom: Long?,
                                     pdtmTo: Long?,
                                     pblnFromTimeSet: Boolean?,
                                     pblnToTimeSet: Boolean?,
                                     pblnToDateSet: Boolean?,
                                     pdtmCreated: Long?): Int {

        //Todo: redesign to not use as many booleans. Make considerations for thru tasks.
        val result: Int
        val calNow = getCurrentCalendar() //represents the mTime now
        var calFromWithTime: Calendar? = null
        var calToWithTime: Calendar? = null
        val calFrom: Calendar //represents the from mTime of a task
        val calTo: Calendar //represents the to mTime of a task
        val calCreate = getCurrentCalendar() //represents when the task was created
        calCreate.timeInMillis = pdtmCreated!!

        //start: Set General From Details
        if (pdtmFrom != NULL_OBJECT) {
            calFrom = getCalendar(pdtmFrom!!)
        } else {
            calFrom = calCreate.clone() as Calendar
        }

        //start: Set General To Details
        if (pdtmTo != NULL_DATE && pblnToDateSet!!)
            calTo = getCalendar(pdtmTo!!)
        else {
            calTo = calFrom.clone() as Calendar
            if (pblnToTimeSet!!) {
                val temp = getCalendar(pdtmTo!!)
                calTo.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                calTo.set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                calTo.set(Calendar.SECOND, temp.get(Calendar.SECOND))
                calTo.set(Calendar.MILLISECOND, temp.get(Calendar.MILLISECOND))
            }
        }

        //if mTime details exists we need to make sure from and to w/ mTime details are populated
        if (pblnFromTimeSet!! || pblnToTimeSet!!) {
            calFromWithTime = calFrom.clone() as Calendar
            if (pblnToTimeSet!!) {
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
        if (!pblnFromTimeSet && pblnToTimeSet //Just to mTime set
                && calNow.after(calFrom) && calNow.before(calToWithTime)) {
            result = PriorityQueues.Priority.ordinal
        } else if (pblnFromTimeSet && !pblnToTimeSet && //Just from mTime set
                calNow.after(calFromWithTime) && calNow.before(calTo)) { //Exists w/i mTime bounds
            result = PriorityQueues.Priority.ordinal
        } else if (pblnFromTimeSet && pblnToTimeSet && //Time details exist
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

        val loadedRecordList = mutableListOf<GeneralListItem>()
        groupMap.clear()

        //Add all priorities to the list
         val functions = object : ShortLongClick {
            override fun regClick(position: Int) {
                setupForDialogFragment(position)
                createShortPriorityFragment.value = true
                //completePriority(position)
            }

            override fun longClick(position: Int) {
                //Nothing to do
            }

        }
        PriorityQueues.values().forEach {
            //loadedRecordList.add(PrioritySeparator(it.ordinal, it.name, functions))
            loadedRecordList.add(GeneralListItem(
                    it.ordinal,
                    NULL_INT,
                    NULL_OBJECT,
                    it.name,
                    ElementType.Priority.name,
                    RecordTypes.Priority.name,
                    functions
            ))
        }

        for (instance in DatabaseAccess.taskDatabaseDao.loadInstancesForTasklist()) {
            val priorityGroup = determineListForTask(instance.fdtmFrom,
                    instance.fdtmTo,
                    instance.hasFromTime,
                    instance.hasToTime,
                    instance.hasToDate,
                    instance.fdtmCreated)

            val instance: InstanceListItemBuilder<GeneralListItem> = TodoItemBuilder(instance.flngInstanceID,
                    instance.fstrTitle,
                    instance.flngGroupKey,
                    instance.fstrGroupTitle,
                    priorityGroup)

            loadedRecordList.addAll(instance.getDisplayRecords())
        }

        loadedRecordList.sortWith(compareBy({ it.priorityId }, { it.groupId }))
        recordList.value = loadedRecordList
    }

    interface ShortLongClick {
        fun regClick(position: Int)
        fun longClick(position: Int)
    }

    fun recordClicked(position: Int) {
        recordList.value!![position].callOnClick(position)
    }

    fun recordLongClicked(position: Int) {
        recordList.value!![position].callOnLongClick(position)
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

    fun viewInstance(position: Int) {
        //Navigate to instance
    }

    fun viewGroup(position: Int) {
        //Navigate to session (or what ever group element it is)
    }

    class GeneralListItem(val priorityId: Int,
                          val groupId: Int,
                          val recordId: Long,
                          val title: String,
                          val element: String,
                          val type: String,
                          private val onClickMethods: ShortLongClick) : InstanceListAdapter.OnInstanceItemClick{
        override fun onInstanceClick(position: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onInstanceLongClick(position: Int) {

        }

        fun callOnClick(position: Int) {
            onClickMethods.regClick(position)
        }

        fun callOnLongClick(position: Int) {
            onClickMethods.longClick(position)
        }
    }

    private interface InstanceListItemBuilder<T> {
        fun getDisplayRecords(): List<T>
    }

    inner class TodoItemBuilder(private val instanceID: Long,
                                private val title: String,
                                private val sessionID: Long,
                                private val sessionTitle: String,
                                private val priorityID: Int) : InstanceListItemBuilder<GeneralListItem> {

        private val displayRecords = mutableListOf<GeneralListItem>()

        override fun getDisplayRecords(): List<GeneralListItem> {
            val groupID = if (sessionID == NULL_OBJECT) NULL_INT else getSessionGroup()
            addTodoItem(groupID)

            return displayRecords
        }

        private fun addTodoItem(groupID: Int) {
            val functions = object : ShortLongClick {
                override fun regClick(position: Int) {
                    setupForDialogFragment(position)
                    createShortInstanceFragment.value = true
                    //completeInstance(position)
                }

                override fun longClick(position: Int) {
                    setupForDialogFragment(position)
                    viewInstance.value = true
                    //viewInstance(position)
                }
            }

            displayRecords.add(GeneralListItem(
                    priorityID,
                    groupID,
                    instanceID,
                    title,
                    ElementType.Instance.name,
                    RecordTypes.TodoInstance.name,
                    functions
            ))
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
            val functions = object : ShortLongClick {
                override fun regClick(position: Int) {
                    setupForDialogFragment(position)
                    createShortSessionFragment.value = true
                    //completeGroup(position)
                }

                override fun longClick(position: Int) {
                    setupForDialogFragment(position)
                    viewSession.value = true
                    //viewGroup(position)
                }
            }

            displayRecords.add(
                    GeneralListItem(
                            priorityID,
                            sectionID,
                            sessionID,
                            "Session: $sessionTitle",
                            ElementType.Group.name,
                            RecordTypes.Session.name,
                            functions))
        }
    }
}
