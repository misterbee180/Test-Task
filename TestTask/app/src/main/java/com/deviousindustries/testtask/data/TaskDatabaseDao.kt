package com.deviousindustries.testtask.data

import android.database.Cursor
import androidx.room.*
import com.deviousindustries.testtask.Utilities
import com.deviousindustries.testtask.constants.*
import com.deviousindustries.testtask.classes.*

@Dao
interface TaskDatabaseDao {

    //region Class Inserts
    @Insert
    fun insertTask(task: Task)

    @Insert
    fun insertTaskInstance(taskInstance: TaskInstance)

    @Insert
    fun insertTaskDetail(taskDetail: TaskDetail)

    @Insert
    fun insertTime(time: Time):Long

    @Insert
    fun insertTimeInstance(timeInstance: TimeInstance)

    @Insert
    fun insertLongTerm(longTerm: LongTerm)

    @Insert
    fun insertGroup(group: Group)

    @Insert
    fun insertEvent(event: Event)

    @Insert
    fun insertDay(day: Day): Long

    @Insert
    fun insertWeek(week: Week): Long

    @Insert
    fun insertMonth(month: Month): Long

    @Insert
    fun insertYear(year: Year): Long
    //endregion
    
    //region Updates
    @Update
    fun updateTask(task: Task)

    @Update
    fun updateTaskInstance(taskInstance: TaskInstance)

    @Update
    fun updateTaskDetail(taskDetail: TaskDetail)

    @Update
    fun updateTime(time: Time)

    @Update
    fun updateTimeInstance(timeInstance: TimeInstance)

    @Update
    fun updateLongTerm(longTerm: LongTerm)

    @Update
    fun updateGroup(group: Group)

    @Update
    fun updateEvent(event: Event)

    @Update
    fun updateDay(day: Day)

    @Update
    fun updateWeek(week: Week)

    @Update
    fun updateMonth(month: Month)

    @Update
    fun updateYear(year: Year)
    //endregion

    //region Class Deletes
    @Delete
    fun deleteTask(task: Task)

    @Delete
    fun deleteTaskInstance(taskInstance: TaskInstance)

    @Delete
    fun deleteTaskDetail(taskDetail: TaskDetail)

    @Delete
    fun deleteTime(time: Time)

    @Delete
    fun deleteTimeInstance(timeInstance: TimeInstance)

    @Delete
    fun deleteLongTerm(longTerm: LongTerm)

    @Delete
    fun deleteGroup(group: Group)

    @Delete
    fun deleteEvent(event: Event)

    @Delete
    fun deleteDay(day: Day)

    @Delete
    fun deleteWeek(week: Week)

    @Delete
    fun deleteMonth(month: Month)

    @Delete
    fun deleteYear(year: Year)
    //endregion

    //region Selects
    @Query("SELECT * FROM tblTask WHERE flngTaskID = :ID")
    fun loadTask(ID: Long): Cursor

    @Query("SELECT * FROM tblTaskDetail WHERE flngTaskDetailID = :ID")
    fun loadTaskDetail(ID: Long): Cursor

    @Query("SELECT * FROM tblTaskInstance WHERE flngInstanceID = :ID")
    fun loadTaskInstance(ID: Long): Cursor

    @Query("SELECT * FROM tblTime WHERE flngTimeID = :ID")
    fun loadTime(ID: Long): Time

    @Query("SELECT * FROM tblTime WHERE fblnComplete = $FALSE_INT")
    fun getUncompleteTimes(): Array<Time>

    @Query("SELECT * FROM tblTimeInstance WHERE flngGenerationID = :ID")
    fun loadTimeInstance(ID: Long): Cursor

    @Query("SELECT * FROM tblTimeInstance WHERE flngTimeID = :timeID")
    fun loadTimeInstancesFromTime(timeID: Long): Array<TimeInstance>

    @Query("SELECT * FROM tblTimeInstance WHERE fdtmUpcoming < :eod AND fdtmPriority + $FULL_DAY_MILLI * fintThru >= :bod")
    fun getValidGenerationPoints(bod: Long, eod: Long) : Array<TimeInstance>

    @Query("SELECT * FROM tblEvent WHERE flngEventID = :ID")
    fun loadEvent(ID: Long): Cursor

    @Query("SELECT * FROM tblGroup WHERE flngGroupID = :ID")
    fun loadGroup(ID: Long): Cursor

    @Query("SELECT * FROM tblLongTerm WHERE flngLongTermID = :ID")
    fun loadLongTerm(ID: Long): Cursor

    @Query("SELECT * FROM tblWeek WHERE flngWeekID = :ID")
    fun loadWeek(ID: Long): Week

    @Query("SELECT * FROM tblMonth WHERE flngMonthID = :ID")
    fun loadMonth(ID: Long): Month

    @Query("SELECT fstrTitle, flngTimeID FROM tblTime WHERE fblnSession = 1 and fblnComplete = $NULL_OBJECT")
    fun loadActiveSessions(): List<Session>

    @Query("SELECT * FROM tblTask WHERE flngTimeID = :ID and fdtmDeleted = $NULL_DATE")
    fun loadActiveTasksFromTime(ID: Long): List<Task>

    @Query("SELECT fstrTitle FROM tblTime WHERE flngTimeID = :ID")
    fun loadSession(ID: Long): String

//    @Query("SELECT * FROM tblTaskInstance where flngTaskID IN (:tasks)")
//    fun loadActiveTaskInstanceFromTask(tasks:List<Task>): List<TaskInstance>



    //endregion

}