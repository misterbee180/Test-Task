package com.deviousindustries.testtask;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteQueryBuilder;

import com.deviousindustries.testtask.classes.Task;
import com.deviousindustries.testtask.classes.Time;
import com.deviousindustries.testtask.data.TaskDatabaseDao;
import static com.deviousindustries.testtask.constants.ConstantsKt.*;

/**
 * Created by Misterbee180 on 8/10/2017.
 */

public class DatabaseAccess {
    public static SupportSQLiteDatabase mDatabase = null;
    public static TaskDatabaseDao taskDatabaseDao = null;

    public static SupportSQLiteDatabase getInstance(@NonNull SupportSQLiteOpenHelper helper, TaskDatabaseDao dao){
        if(mDatabase == null) {
            mDatabase = helper.getWritableDatabase();
            taskDatabaseDao = dao;
        }

        return mDatabase;
    }

    //region NewFunctions
    public static void deleteSession(Long sessionID){
        try{
            DatabaseAccess.mDatabase.beginTransaction();
            Time tempTime = taskDatabaseDao.loadTime(sessionID);
            for(Task task : taskDatabaseDao.loadActiveTasksFromTime(sessionID)){
                task.finishActiveInstances(3);
                task.replaceTimeId(tempTime.getCopy().flngTimeID);
            }
            DatabaseAccess.taskDatabaseDao.deleteTime(tempTime);
            DatabaseAccess.mDatabase.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }
        DatabaseAccess.mDatabase.endTransaction();
    }
    //endregion

    //region GENERIC FUNCTIONS
    public static Cursor getRecordsFromTable(String pstrTableName,
                                             String pstrIDColumn,
                                             long plngID){
        String selection = pstrIDColumn + " = ?";
        String[] selectionArgs = {Long.toString(plngID)};

        return mDatabase.query(
                SupportSQLiteQueryBuilder.builder(pstrTableName)
                .selection(selection, selectionArgs)
                .create());
    }

    public static Cursor getRecordsFromTable(String pstrTableName,
                                             String pstrIDColumn,
                                             Long plngID,
                                             String pstrOrderBy){
        String selection = pstrIDColumn + " = ?";
        String[] selectionArgs = {Long.toString(plngID)};

        return mDatabase.query(
                SupportSQLiteQueryBuilder.builder(pstrTableName)
                        .selection(selection, selectionArgs)
                        .orderBy(pstrOrderBy)
                        .create());
    }

    public static Cursor getRecordsFromTable(String pstrTableName){
        return mDatabase.query(SupportSQLiteQueryBuilder.builder(pstrTableName)
                .create());
    }

    public static Cursor getRecordsFromTable(String pstrTableName,
                                             String pstrSelection,
                                             Object[] pobjArguemnts){

        return mDatabase.query(SupportSQLiteQueryBuilder.builder(pstrTableName)
                .selection(pstrSelection, objectArrayToStringArray(pobjArguemnts))
                .create());
    }

    public static String[] objectArrayToStringArray(Object[] pobjArray){
        if(pobjArray != null){
            String[] strArray = new String[pobjArray.length];
            for (int i = 0; i < pobjArray.length; i++){
                strArray[i] = pobjArray[i].toString();
            }
            return strArray;
        }
        return new String[]{};
    }

    public static ContentValues generateContentValues(String[] pstrColumns,
                                                       Object[] pstrValues){
        ContentValues values = new ContentValues();
        for (int i = 0; i < pstrColumns.length; i++){
            if (pstrValues[i] != null){
                switch (pstrColumns[i].substring(1,4)){
                    case "lng":
                        values.put(pstrColumns[i],(long)pstrValues[i]);
                        break;
                    case "dtm":
                        values.put(pstrColumns[i],(long)pstrValues[i]);
                        break;
                    case "bln":
                        values.put(pstrColumns[i],((boolean)pstrValues[i])?1:0);
                        break;
                    case "str":
                        values.put(pstrColumns[i],(String)pstrValues[i]);
                        break;
                    case "int":
                        values.put(pstrColumns[i], ((Integer)pstrValues[i]).longValue());
                        break;
                }
            }
        }
        return values;
    }

    public static long addRecordToTable(String pstrTableName,
                                        String[] pstrAddColumns,
                                        Object[] pobjAddValues){

        return addRecordToTable(pstrTableName, pstrAddColumns, pobjAddValues, "", NULL_OBJECT);
    }

    public static long addRecordToTable(String pstrTableName,
                                        String[] pstrAddColumns,
                                        Object[] pobjAddValues,
                                        String pstrColumnID,
                                        long plngID){

        boolean blnUpdate = pstrColumnID != "" && plngID != NULL_OBJECT;

        ContentValues values = generateContentValues(pstrAddColumns, pobjAddValues);

        if (blnUpdate){
            values.put(pstrColumnID, plngID);
            deleteRecordFromTable(pstrTableName, pstrColumnID, plngID);
        }

        return mDatabase.insert(pstrTableName,
                SQLiteDatabase.CONFLICT_ROLLBACK,
                values);
    }

    public static  long updateRecordFromTable(String pstrTableName,
                                            String pstrIDColumn,
                                            long plngID,
                                            String[] pstrUpdateColumns,
                                            Object[] pobjUpdateValues){

        ContentValues values = generateContentValues(pstrUpdateColumns, pobjUpdateValues);

        String selection = pstrIDColumn + " = ?";
        String[] selectionArgs = {Long.toString(plngID)};

        return mDatabase.update(pstrTableName,
                SQLiteDatabase.CONFLICT_ROLLBACK,
                values,
                selection,
                selectionArgs);
    }

    public static long deleteRecordFromTable(String pstrTableName,
                                            String pstrIDColumn,
                                            Long plngID){
        String whereClause = pstrIDColumn + " = ?";
        String[] whereArgs = {Long.toString(plngID)};

        return mDatabase.delete(pstrTableName,
                whereClause,
                whereArgs);
    }

    public static  Cursor retrieveMostRecent(String pstrTableName,
                                             String pstrColumnName,
                                             Long plngColumnID,
                                             String pstrOrderBy){
        //Returns the most recent task instance by created date
        String selection = pstrColumnName + " = ?";
        String[] selectionArgs = {Long.toString(plngColumnID)};
        String orderBy = pstrOrderBy + " desc";

        return mDatabase.query(
                SupportSQLiteQueryBuilder.builder(pstrTableName)
                        .selection(selection, selectionArgs)
                        .orderBy(orderBy)
                        .limit("1")
                        .create());
    }
    //endregion

    //region SPECIFIC FUNCTIONS
    public static Cursor retrieveMaxUpcoming(long plngTimeID){
        return mDatabase.query(
                SupportSQLiteQueryBuilder.builder("tblTimeInstance")
                        .columns(new String[] {"MAX(fdtmUpcoming) as fdtmUpcomingMax"})
                        .selection("flngTimeID = ?", new String[]{Long.toString(plngTimeID)})
                        .create());
    }

    public static Cursor findOneOffs(long plngTimeID){
        String rawQuery = "SELECT t.flngTaskID\n" +
                "FROM tblTask t\n" +
                "WHERE t.fdtmDeleted = " + NULL_DATE + "\n" + //Task is not deleted
                "and t.flngOneOff = ?\n" + //Task is associated w/ mTime
                "and NOT EXISTS (\n" + //No completed instances
                "SELECT 1\n" +
                "FROM tblTaskInstance ti\n" +
                "WHERE ti.flngTaskID = t.flngTaskID \n" +
                "AND NOT(ti.fdtmCompleted = " + NULL_DATE + "\n" +
                "AND ti.fdtmSystemCompleted = " + NULL_DATE + "\n" +
                "AND ti.fdtmEdited = " + NULL_DATE + "))";
        String[] parms = new String[]{Long.toString(plngTimeID)};
        return DatabaseAccess.mDatabase.query(rawQuery, parms);
    }

    public static Cursor getCompletedLongTerms(){
        String rawGetCompleteLongTerms = "SELECT lt.flngLongTermID, lt.fstrTitle \n" +
                "FROM tblLongTerm lt \n" +
                //Where there's at least one task associated to long term
                "WHERE EXISTS (SELECT 1 \n" +
                "FROM tblTask t \n" +
                "WHERE t.flngTaskTypeID = lt.flngLongTermID \n" +
                "AND t.fintTaskType = 2 \n" +
                "AND t.fdtmDeleted = " + NULL_DATE + ") \n " +
                //And none of the tasks do not have a completed task instance associated with them
                "AND NOT EXISTS (SELECT 1 \n" +
                "FROM tblTask t \n" +
                "WHERE t.flngTaskTypeID = lt.flngLongTermID \n" +
                "AND t.fintTaskType = 2 \n" +
                "AND t.fdtmDeleted = " + NULL_DATE + " \n" +
                "AND NOT EXISTS (SELECT 1 \n" +
                "FROM tblTaskInstance ti \n" +
                "WHERE ti.flngTaskID = t.flngTaskID \n" +
                "AND NOT(ti.fdtmCompleted = " + NULL_DATE + " AND ti.fdtmSystemCompleted = " + NULL_DATE + "))) \n" +
                "ORDER BY lt.flngLongTermID";

        return mDatabase.query(rawGetCompleteLongTerms,null);
    }

    public static Cursor getEvents(){
        String rawGetEvents = "SELECT *, \n" +
                "CASE WHEN EXISTS(SELECT 1 FROM tblTask t \n" +
                "JOIN tblTaskInstance ti \n" +
                "ON t.flngTaskID = ti.flngTaskID \n" +
                "AND ti.fdtmSystemCompleted = " + NULL_DATE + " AND ti.fdtmCompleted = " + NULL_DATE + " \n" +
                "WHERE t.fintTaskType = 1 \n" +
                "AND t.flngTaskTypeID = e.flngEventID) THEN 1 ELSE 0 END as fblnActive \n" +
                "FROM tblEvent e \n";

        return mDatabase.query(rawGetEvents,null);
    }

    public static Cursor getTasks(int pintSorting){
        String rawGetTasks = "SELECT t.*,td.fstrTitle, td.fstrDescription, tm.fdtmCreated, g.fstrTitle as fstrGroup, " +
                "CASE WHEN tm.fstrTitle = '' THEN 'No Session' ELSE tm.fstrTitle END as fstrSession\n" +
                "FROM tblTask t\n" +
                "JOIN tblTaskDetail td\n" +
                "ON td.flngTaskDetailID = t.flngTaskDetailID\n" +
                "JOIN tblTime tm\n" +
                "ON tm.flngTimeID = t.flngTimeID\n" +
                "LEFT JOIN tblGroup g\n" +
                "ON g.flngGroupID = t.flngTaskTypeID\n" +
                "AND t.fintTaskType = 3\n" +
                "WHERE (tm.fblnComplete = " + NULL_OBJECT + "\n" +
                "OR EXISTS (SELECT 1\n" +
                "FROM tblTaskInstance ti\n" +
                "WHERE ti.flngTaskID = t.flngTaskID\n" +
                "AND (ti.fdtmCompleted = " + NULL_DATE + " AND ti.fdtmSystemCompleted = " + NULL_DATE + " AND ti.fdtmDeleted = " + NULL_DATE + ")))\n" +
                "AND t.fdtmDeleted = " + NULL_DATE + "\n";

        switch(pintSorting) {
            case 0: //Ascending
                rawGetTasks += "ORDER BY td.fstrTitle";
                break;
            case 1: //Created
                rawGetTasks += "ORDER BY tm.fdtmCreated Desc";
                break;
            case 2: //Group
                rawGetTasks += "ORDER BY ifNULL(g.fstrTitle,\"z\"), td.fstrTitle";
                break;
            case 3: //Session
                rawGetTasks += "ORDER BY ifNULL(tm.fstrTitle,\"z\"), td.fstrTitle";
                break;
//            case Group:
//                rawGetTasks += "ORDER BY g.fstrTitle, t.fstrTitle";
//                break;
//            case Session:
//                rawGetTasks += "ORDER BY s.fstrTitle, t.fstrTitle";
//                break;
        }

        return mDatabase.query(rawGetTasks,null);
    }

    public static Cursor getValidGenerationPoints(long pEndCurrentDay, long pBeginCurrentDay){

        //NOTE: I was forced to "inline" all of the arguments because when doing match in android queries sometimes bugs are produced.
        String strSelection = "fdtmUpcoming <= " + pEndCurrentDay;
        strSelection += " and fdtmPriority + 86400000 * fintThru >= " + pBeginCurrentDay;

        return DatabaseAccess.mDatabase.query(
                SupportSQLiteQueryBuilder.builder("tblTimeInstance")
                        .selection(strSelection, null)
                        .create());
    }

    public static Cursor getValidGenerationPoints(boolean pblnIncludeThru,
                                            boolean pblnAll,
                                            long plngTimeID){
        //All is not really the right variable name. It's really a question of valid generation for oneoffs vs valid generation for every day tasks.
        //NOTE: I was forced to "inline" all of the arguments because when doing match in android queries sometimes bugs are produced.
        String strSelection = "flngTimeID = " + plngTimeID;
        String orderBy = null;
        if(pblnAll) {
            strSelection += " and fdtmUpcoming <= " + Viewer_Tasklist.getEndCurrentDay().getTimeInMillis();
        } else {
            orderBy = "fdtmPriority LIMIT 1";
        }
        if(pblnIncludeThru) strSelection += " and fdtmPriority + 86400000 * fintThru >= " + Viewer_Tasklist.getBeginningCurentDay().getTimeInMillis();
        else strSelection += " and fdtmPriority >= " + Viewer_Tasklist.getBeginningCurentDay().getTimeInMillis();

        return DatabaseAccess.mDatabase.query(
                SupportSQLiteQueryBuilder.builder("tblTimeInstance")
                        .selection(strSelection,null)
                        .orderBy(orderBy)
                        .create());
    }

    public static Cursor getTasksFromTime(long plngTimeID){
        return DatabaseAccess.mDatabase.query(
                SupportSQLiteQueryBuilder.builder("tblTask")
                        .columns(new String[] {"flngTaskID"})
                        .selection("fdtmDeleted = " + NULL_DATE + " and flngTimeID = ?",new String[] {Long.toString(plngTimeID)})
                        .create());
    }

    public static Cursor getTasksFromGroup(Long plngGroupID){
        String rawString = "SELECT flngTaskID FROM tblTask t\n" +
                "JOIN tblTime tm ON tm.flngTimeID = t.flngTimeID\n" +
                "WHERE t.fintTaskType = 3 and flngTaskTypeID = ? and fdtmDeleted = " + NULL_DATE + "\n" +
                "AND (tm.flngRepetition > 0 \n" +
                "OR EXISTS (SELECT 1 FROM tblTaskInstance ti\n" +
                "WHERE ti.flngTaskID = t.flngTaskID \n" +
                "and ti.fdtmCompleted = " + NULL_DATE + " and ti.fdtmSystemCompleted = " + NULL_DATE + " and ti.fdtmDeleted = " + NULL_DATE + "))\n";
        return mDatabase.query(rawString, new String[] {plngGroupID.toString()});
    }

    public static Cursor getTasksFromLongTerm(Boolean pblnComplete, long plngLongTermID){
        String rawGetUnCompleteLongTermTasks = "SELECT td.fstrTitle, t.flngTaskID \n" +
                "FROM tblTask t \n" +
                "JOIN tblTaskDetail td \n" +
                "ON td.flngTaskDetailID = t.flngTaskDetailID\n" +
                "JOIN tblLongTerm lt \n" +
                "ON lt.flngLongTermID = t.flngTaskTypeID \n" +
                "AND t.fintTaskType = 2\n" +
                "AND t.fdtmDeleted = " + NULL_DATE + " \n" +
                "WHERE lt.flngLongTermID = ? \n" +
                "AND NOT EXISTS (SELECT 1 \n" +
                "FROM tblTaskInstance i \n" +
                "WHERE i.flngTaskID = t.flngTaskID \n" +
                "AND NOT(i.fdtmCompleted == " + NULL_DATE + " AND i.fdtmSystemCompleted = " + NULL_DATE + ")) \n" +
                "ORDER BY t.flngTaskID";

        String rawGetCompleteLongTermTasks = "SELECT td.fstrTitle, t.flngTaskID \n" +
                "FROM tblTask t \n" +
                "JOIN tblTaskDetail td \n" +
                "ON td.flngTaskDetailID = t.flngTaskDetailID\n" +
                "JOIN tblLongTerm lt \n" +
                "ON lt.flngLongTermID = t.flngTaskTypeID \n" +
                "AND t.fintTaskType = 2\n" +
                "AND t.fdtmDeleted = " + NULL_DATE + " \n" +
                "WHERE lt.flngLongTermID = ? \n" +
                "AND EXISTS (SELECT 1 \n" +
                "FROM tblTaskInstance i \n" +
                "WHERE i.flngTaskID = t.flngTaskID \n" +
                "AND NOT(i.fdtmCompleted == " + NULL_DATE + " AND i.fdtmSystemCompleted = " + NULL_DATE + ")) \n" +
                "ORDER BY t.flngTaskID";

        String[] parameters = {Long.toString(plngLongTermID)};

        if(pblnComplete) return mDatabase.query(rawGetCompleteLongTermTasks,parameters);
        return mDatabase.query(rawGetUnCompleteLongTermTasks,parameters);
    }

    public static  Cursor retrieveActiveTaskInstanceFromTask(Long plngTaskId){
        String selection = "flngTaskID = ? " +
                "AND fdtmCompleted = " + NULL_DATE + " " +
                "and fdtmSystemCompleted = " + NULL_DATE + " " +
                "and fdtmDeleted = " + NULL_DATE + " ";
        String[] selectionArgs = {Long.toString(plngTaskId)};

        return mDatabase.query(
                SupportSQLiteQueryBuilder.builder("tblTaskInstance")
                        .selection(selection, selectionArgs)
                        .create());
    }

    public static  Cursor retrieveEventTaskInstances(){
        String rawEventTasksSelect = "SELECT e.flngEventID, e.fstrTitle as fstrEventTitle, ti.flngInstanceID, td.fstrTitle as fstrTaskTitle \n" +
                "FROM tblTaskInstance ti \n" +
                "JOIN tblTask t \n" +
                "ON t.flngTaskID = ti.flngTaskId \n" +
                "AND ti.fdtmCompleted = " + NULL_DATE + " \n" +
                "AND ti.fdtmSystemCompleted = " + NULL_DATE + " \n" +
                "AND ti.fdtmDeleted = " + NULL_DATE + " \n" +
                "AND t.fintTaskType = 1 \n" +
                "JOIN tblTaskDetail td \n" +
                "ON td.flngTaskDetailID = ti.flngTaskDetailID \n" +
                "JOIN tblEvent e \n" +
                "ON e.flngEventID = t.flngTaskTypeID \n" +
                "ORDER BY e.flngEventID";

        return mDatabase.query(rawEventTasksSelect);
    }

    public static  Cursor retrieveEventTaskInstancesFromEvent(Long plngEventId){
        String rawEventTasksSelect = "SELECT ti.flngInstanceID \n" +
                "FROM tblTaskInstance ti \n" +
                "JOIN tblTask t \n" +
                "ON t.flngTaskID = ti.flngTaskId \n" +
                "AND ti.fdtmCompleted = " + NULL_DATE + " \n" +
                "AND ti.fdtmSystemCompleted = " + NULL_DATE + " \n" +
                "WHERE t.fintTaskType = 1 \n" +
                "AND t.flngTaskTypeID = ?";

        String[] parameters = {Long.toString(plngEventId)};

        return mDatabase.query(rawEventTasksSelect,parameters);
    }

    public static  Cursor retrieveEventTasksFromEvent(Long plngEventId){
        String selection = "fintTaskType = 1 AND flngTaskTypeID == ? AND fdtmDeleted = " + NULL_DATE + "";
        String[] selectionArgs = {Long.toString(plngEventId)};

        return mDatabase.query(
                SupportSQLiteQueryBuilder.builder("tblTask")
                        .selection(selection, selectionArgs)
                        .create());
    }

    public static  Cursor retrieveTasksAssociatedWithLongTerm(Long plngLongTermId){
        String selection = "flngTaskTypeID = ? and fintTaskType = 2 AND fdtmDeleted = " + NULL_DATE + "";
        String[] selectionArgs = {Long.toString(plngLongTermId)};

        return mDatabase.query(
                SupportSQLiteQueryBuilder.builder("tblTask")
                        .selection(selection, selectionArgs)
                        .create());
    }

    public static  Cursor getInstancesForTasklist(){
        String rawQuery = "SELECT i.*" +
                ", ifNULL(lt.fstrTitle||': ','')||td.fstrTitle as fstrTitle, IFNULL(tm.fstrTitle,'') as fstrSessionTitle  \n" +
                "FROM tblTaskInstance i \n" +
                "JOIN tblTaskDetail td \n" +
                "ON td.flngTaskDetailID = i.flngTaskDetailID \n" +
                "JOIN tblTask t \n" +
                "ON t.flngTaskID = i.flngTaskID\n" +
                "AND t.fintTaskType <> 1\n" + //Event
                "LEFT JOIN tblTime tm\n" +
                "ON tm.flngTimeID = i.flngSessionID\n" +
                "LEFT JOIN tblLongTerm lt \n" +
                "ON lt.flngLongTermID = t.flngTaskTypeID \n" +
                "AND t.fintTaskType = 2 \n" +
                "WHERE i.fdtmCompleted = " + NULL_DATE + " \n" +
                "AND i.fdtmSystemCompleted = " + NULL_DATE + " \n" +
                "AND i.fdtmDeleted = " + NULL_DATE + " \n" +
                //"ORDER BY CASE WHEN t.fblnOneOff = 1 THEN -1 ELSE t.flngSessionID END ";
                "ORDER BY i.flngSessionID, i.flngTaskID";

        return DatabaseAccess.mDatabase.query(rawQuery);
    }

    public static void forceWALCheckpoint(){
        mDatabase.query("PRAGMA wal_checkpoint(FULL)").close();
    }
    //endregion
}
