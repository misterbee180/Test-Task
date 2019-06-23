package com.deviousindustries.testtask.task_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import com.deviousindustries.testtask.DatabaseAccess
import com.deviousindustries.testtask.Utilities
import com.deviousindustries.testtask.Utilities.Companion.getCalendar
import com.deviousindustries.testtask.Utilities.Companion.getCurrentCalendar
import com.deviousindustries.testtask.classes.*
import com.deviousindustries.testtask.classes.TaskListRecord.Companion.NULL_ID
import com.deviousindustries.testtask.classes.TaskListRecord.Companion.NULL_SECTION
import com.deviousindustries.testtask.constants.*
import java.util.*
import kotlin.collections.HashMap

enum class PriorityQueues {
    Priortiy, Today, Standard, Upcoming
}

enum class InstanceItemType {
    Todo
}

enum class ElementType{
    Instance, Group, Priority;

    companion object {
        private val map = values().associateBy{it.ordinal}
        fun fromInt(type: Int) = map[type]
    }
}

class TaskListViewModel : ViewModel() {

    val recordList = MutableLiveData<MutableList<TaskListRecord>>()
    val sectionMap = HashMap<Triple<Int, Long, String>, Int>()

    init{
        recordList.value = mutableListOf<TaskListRecord>();
    }

    fun start() {
        var debug_redoSync = false

        if (Utilities.preferences.getLong("general_last_sync", NULL_DATE) < Utilities.getBeginningCurentDay().timeInMillis || debug_redoSync) {
            //This is so that it displays the right things the first mTime the app opens
            generateTaskInstances()

//            //Cancel any alarms which may already be set up to run
//            val intent = Intent(this, AlarmReceiver::class.java)
//            intent.setAction("com.deviousindustries.testtask.SYNC")
//            AlarmReceiver().cancelAlert(getApplicationContext(), intent)
//
//            //re set up the alarm and anything else needing to be done.
//            AlarmReceiver().generateAlert(getApplicationContext(), intent,
//                    Calendar.getInstance().getTimeInMillis(), 0, AlarmManager.RTC_WAKEUP)
        } else {
            generateTaskInstances()
        }
        loadRecordList()
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
        calCreate.setTimeInMillis(pdtmCreated!!)

        //Start: Set General From Details
        if (pdtmFrom != NULL_OBJECT) {
            calFrom = getCalendar(pdtmFrom!!)
        } else {
            calFrom = calCreate.clone() as Calendar
        }

        //Start: Set General To Details
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
            result = PriorityQueues.Priortiy.ordinal
        } else if (pblnFromTimeSet && !pblnToTimeSet && //Just from mTime set
                calNow.after(calFromWithTime) && calNow.before(calTo)) { //Exists w/i mTime bounds
            result = PriorityQueues.Priortiy.ordinal
        } else if (pblnFromTimeSet && pblnToTimeSet && //Time details exist
                calNow.after(calFromWithTime) && calNow.before(calToWithTime)) { //Exists w/i mTime bounds
            result = PriorityQueues.Priortiy.ordinal
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

    fun generateTaskInstances() {
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

    fun loadRecordList() {

        //Add all priorities to the list
        PriorityQueues.values().forEach {
            recordList.value!!.add(Todo_Header(it.ordinal, NULL_ID, it.name));
        }

//        DatabaseAccess.newGetInstancesForTasklist().use { displayInstance ->
//            while (displayInstance.moveToNext()) {
//                val priorityGroup = determineListForTask(displayInstance.getLong(displayInstance.getColumnIndex("fdtmFrom")),
//                        displayInstance.getLong(displayInstance.getColumnIndex("fdtmTo")),
//                        displayInstance.getInt(displayInstance.getColumnIndex("fblnFromTime")) == 1,
//                        displayInstance.getInt(displayInstance.getColumnIndex("fblnToTime")) == 1,
//                        displayInstance.getInt(displayInstance.getColumnIndex("fblnToDate")) == 1,
//                        displayInstance.getLong(displayInstance.getColumnIndex("fdtmCreated")))
//
//                val instance: Instance_Item = Todo_Instance(displayInstance.getLong(displayInstance.getColumnIndex("flngInstanceID")),
//                        displayInstance.getString(displayInstance.getColumnIndex("fstrTitle")),
//                        displayInstance.getLong(displayInstance.getColumnIndex("flngGroupKey")),
//                        displayInstance.getString(displayInstance.getColumnIndex("fstrGroupTitle")),
//                        priorityGroup)
//
//                recordList.value!!.addAll(instance.getDisplayRecords(sectionMap))
//            }
//        }

        recordList.value!!.sortWith(compareBy({it.getPriority()},{it.getGroup()}))
    }

    fun completeInstance() {}

    fun completeGroup() {}

    fun viewInstance() {}

    fun viewGroup() {}
}
