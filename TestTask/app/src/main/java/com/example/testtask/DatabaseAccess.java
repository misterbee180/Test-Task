package com.example.testtask;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Misterbee180 on 8/10/2017.
 */

public class DatabaseAccess {
    public static SQLiteDatabase mDatabase;
    private DatabaseAccess(){}

    public static void setContext(Context pContext){
        TaskDatabaseHelper TaskDatabase = new TaskDatabaseHelper(pContext);
        mDatabase = TaskDatabase.getWritableDatabase();
    }

    private static class TaskDatabaseHelper extends SQLiteOpenHelper {
        //region TABLE CREATE SCRIPTS
        private static final String CREATE_TASK_TABLE =
                "CREATE TABLE `tblTask` (\n" +
                        "\t`flngID`\tINTEGER,\n" +
                        "\t`fstrTitle`\tTEXT,\n" +
                        "\t`fstrDescription`\tTEXT,\n" +
                        "\t`flngSessionID`\tINTEGER,\n" +
                        "\t`flngTimeID`\tINTEGER,\n" +
                        "\t`flngEventID`\tINTEGER,\n" +
                        "\tPRIMARY KEY(`flngID`)\n" +
                        "); ";

        private static final String CREATE_TASKINSTANCE_TABLE =
                "CREATE TABLE `tblTaskInstance` (\n" +
                        "\t`flngID`\tINTEGER,\n" +
                        "\t`flngTaskID`\tINTEGER,\n" +
                        "\t`fblnComplete`\tINTEGER,\n" +
                        "\t`fblnSystemComplete`\tINTEGER,\n" +
                        "\tPRIMARY KEY(`flngID`)\n" +
                        "); ";

        private static final String CREATE_SESSION_TABLE =
                "CREATE TABLE `tblSession` (\n" +
                        "\t`flngID`\tINTEGER,\n" +
                        "\t`fstrTitle`\tTEXT,\n" +
                        "\t`flngTimeID`\tINTEGER,\n" +
                        "\tPRIMARY KEY(`flngID`)\n" +
                        ");";

        private static final String CREATE_TIME_TABLE =
                "CREATE TABLE `tblTime` (\n" +
                        "\t`flngID`\tINTEGER,\n" +
                        "\t`fdtmFrom`\tINTEGER,\n" +
                        "\t`fdtmTo`\tINTEGER,\n" +
                        "\t`flngWeekID`\tINTEGER,\n" +
                        "\t`fdtmEvaluated`\tINTEGER,\n" +
                        "\tPRIMARY KEY(`flngID`)\n" +
                        "); ";

        private static final String CREATE_WEEK_TABLE =
                "CREATE TABLE `tblWeek` (\n" +
                        "\t`flngID`\tINTEGER,\n" +
                        "\t`fblnMonday`\tINTEGER,\n" +
                        "\t`fblnTuesday`\tINTEGER,\n" +
                        "\t`fblnWednesday`\tINTEGER,\n" +
                        "\t`fblnThursday`\tINTEGER,\n" +
                        "\t`fblnFriday`\tINTEGER,\n" +
                        "\t`fblnSaturday`\tINTEGER,\n" +
                        "\t`fblnSunday`\tINTEGER,\n" +
                        "\tPRIMARY KEY(`flngID`)\n" +
                        ");";

        private static final String CREATE_EVENT_TABLE =
                "CREATE TABLE `tblEvent` (\n" +
                        "\t`flngID`\tINTEGER,\n" +
                        "\t`fstrTitle`\tTEXT NOT NULL,\n" +
                        "\t`fstrDescription`\tTEXT NOT NULL,\n" +
                        "\t`fblnEventActive`\tINTEGER,\n" +
                        "\tPRIMARY KEY(`flngID`)\n" +
                        ");";

        //endregion

        //region TABLE DROP SCRIPTS
        private static final String DROP_TASK_TABLE=
                "DROP TABLE IF EXISTS tblTask";

        private static final String DROP_TASKINSTANCE_TABLE=
                "DROP TABLE IF EXISTS tblTaskInstance";

        private static final String DROP_SESSION_TABLE=
                "DROP TABLE IF EXISTS tblSession";

        private static final String DROP_TIME_TABLE=
                "DROP TABLE IF EXISTS tblTime";

        private static final String DROP_WEEK_TABLE=
                "DROP TABLE IF EXISTS tblWeek";

        private static final String DROP_EVENT_TABLE=
                "DROP TABLE IF EXISTS tblEvent";
        //endregion

        TaskDatabaseHelper(Context context) {
            super(context, "TaskDatabase.db", null, 2);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TASK_TABLE);
            db.execSQL(CREATE_TASKINSTANCE_TABLE);
            db.execSQL(CREATE_SESSION_TABLE);
            db.execSQL(CREATE_TIME_TABLE);
            db.execSQL(CREATE_WEEK_TABLE);
            db.execSQL(CREATE_EVENT_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db,
                              int oldVersion,
                              int newVersion){

            if (newVersion == 999){
            db.execSQL(DROP_TASK_TABLE);
            db.execSQL(DROP_TASKINSTANCE_TABLE);
            db.execSQL(DROP_SESSION_TABLE);
            db.execSQL(DROP_TIME_TABLE);
            db.execSQL(DROP_WEEK_TABLE);
            db.execSQL(DROP_EVENT_TABLE);
            onCreate(db);
            } else {
                if (oldVersion < 2){
                    upgradeToV2(db);
                }
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db,
                              int oldVersion,
                              int newVersion){

            db.execSQL(DROP_TASK_TABLE);
            db.execSQL(DROP_TASKINSTANCE_TABLE);
            db.execSQL(DROP_SESSION_TABLE);
            db.execSQL(DROP_TIME_TABLE);
            db.execSQL(DROP_WEEK_TABLE);
            db.execSQL(DROP_EVENT_TABLE);
            onCreate(db);
        }

        private void upgradeToV2(SQLiteDatabase db){
            db.execSQL(CREATE_EVENT_TABLE);
            db.execSQL("CREATE TABLE `tblTaskTmp` (\n" +
                    "\t`flngID`\tINTEGER,\n" +
                    "\t`fstrTitle`\tTEXT,\n" +
                    "\t`fstrDescription`\tTEXT,\n" +
                    "\t`flngSessionID`\tINTEGER,\n" +
                    "\t`flngTimeID`\tINTEGER,\n" +
                    "\t`flngEventID`\tINTEGER,\n" +
                    "\tPRIMARY KEY(`flngID`)\n" +
                    "); ");
            Cursor cursor = returnCurrentTable(db, "tblTask");
            while (cursor.moveToNext()){
                ContentValues values = populateCurrentTableValues(cursor);
                values.put("flngEventId",-1);
                db.insertOrThrow("tblTaskTmp",
                        null,
                        values);
            }
            db.execSQL("DROP TABLE TblTask;");
            db.execSQL("ALTER TABLE TblTaskTmp RENAME TO TblTask");
        }

        private Cursor returnCurrentTable(SQLiteDatabase db,
                                     String pstrTable){
            return db.query(pstrTable,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        }

        private ContentValues populateCurrentTableValues(Cursor pCursor){
            ContentValues values = new ContentValues();
            for (int i = 0; i < pCursor.getColumnCount(); i++){
                String columnName = pCursor.getColumnName(i);
                switch (columnName.substring(1,4)){
                    case "lng":
                        values.put(columnName,pCursor.getLong(i));
                        break;
                    case "dtm":
                        values.put(columnName,pCursor.getLong(i));
                        break;
                    case "bln":
                        values.put(columnName,pCursor.getLong(i));
                        break;
                    case "str":
                        values.put(columnName,pCursor.getString(i));
                        break;
                }
            }
            return values;
        }
    }

    public static Cursor getRepeatingTask(){
        String rawGetRepeatingTasks = "SELECT t.flngID as flngID, IFNULL(tm.flngID,tms.flngID) as flngTimeID, IFNULL(tm.fdtmEvaluated,tms.fdtmEvaluated) as fdtmEvaluated \n" +
                "FROM tblTask t\n" +
                "LEFT JOIN tblTime tm\n" +
                "ON t.flngTimeID = tm.flngID\n" +
                "LEFT JOIN tblSession s\n" +
                "ON s.flngID = t.flngSessionID\n" +
                "LEFT JOIN tblTime tms\n" +
                "ON tms.flngID = s.flngTimeID\n" +
                "WHERE IFNULL(tm.flngWeekID,tms.flngWeekID) <> -1";

        return mDatabase.rawQuery(rawGetRepeatingTasks,null);
    }

    public static Cursor getTaskInstancesWithDetails(){
        String rawQuery = "SELECT i.flngID, t.fstrTitle, IFNULL(tm.fdtmFrom,tms.fdtmFrom) as fdtmFrom, IFNULL(tm.fdtmTo,tms.fdtmTo) as fdtmTo, IFNULL(tm.flngWeekID, tms.flngWeekID) as flngWeekID \n" +
                "FROM tblTask t \n" +
                "JOIN tblTaskInstance i \n" +
                "ON t.flngId = i.flngTaskId \n" +
                "LEFT JOIN tblTime tm \n" +
                "ON tm.flngID = t.flngTimeID \n" +
                "LEFT JOIN tblSession s \n" +
                "ON s.flngID = t.flngSessionID \n" +
                "LEFT JOIN tblTime tms \n" +
                "ON tms.flngID = s.flngTimeID \n" +
                "WHERE i.fblnComplete = 0 \n" +
                "AND i.fblnSystemComplete = 0 \n" +
                "AND t.flngEventId = -1";

        return mDatabase.rawQuery(rawQuery,null);
    }

    public static Cursor retrieveActiveTaskInstanceFromTask(Long plngTaskId){
        String selection = "flngTaskID = ? AND fblnComplete = 0 and fblnSystemComplete = 0";
        String[] selectionArgs = {Long.toString(plngTaskId)};

        return mDatabase.query("tblTaskInstance",
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);
    }

    public static Cursor getRecordFromTable(String pstrTableName,
                                            Long plngID){
        String selection = "flngID = ?";
        String[] selectionArgs = {Long.toString(plngID)};

        return mDatabase.query(pstrTableName,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);
    }

    public static long addRecordToTable(String pstrTableName,
                                        String[] pstrAddColumns,
                                        String[] pstrAddValues){
        ContentValues values = new ContentValues();
        for (int i = 0; i < pstrAddColumns.length; i++){
            values.put(pstrAddColumns[i],pstrAddValues[i]);
        }

        return mDatabase.insertOrThrow(pstrTableName,
                null,
                values);
    }

    public static int updateRecordFromTable(String pstrTableName,
                                             Long plngID,
                                             String[] pstrUpdateColumns,
                                             String[] pstrUpdateValues){
        ContentValues values = new ContentValues();
        for (int i = 0; i < pstrUpdateColumns.length; i++){
            values.put(pstrUpdateColumns[i],pstrUpdateValues[i]);
        }

        String selection = "flngID = ?";
        String[] selectionArgs = {Long.toString(plngID)};

        return mDatabase.update(pstrTableName,
                values,
                selection,
                selectionArgs);
    }

    public static void updateTimeEvaluated(Long fdtmTimeMili, Long flngTimeID) {
        int rowsUpdated = updateRecordFromTable("tblTime",
                flngTimeID,
                new String[]{"fdtmEvaluated"},
                new String[]{Long.toString(fdtmTimeMili)});
    }

    public static long insertTaskInstance(Long plngTaskID) {
        return DatabaseAccess.addRecordToTable("tblTaskInstance",
                new String[]{"flngTaskID","fblnComplete","fblnSystemComplete"},
                new String[]{Long.toString(plngTaskID), "0", "0"});
    }

    public static void updateTaskInstanceComplete(Long plngID){
        updateRecordFromTable("tblTaskInstance",
                plngID,
                new String[]{"fblnComplete"},
                new String[]{"1"});
    }

    public static void updateTaskInstanceSystemComplete(Long plngID){
        updateRecordFromTable("tblTaskInstance",
                plngID,
                new String[]{"fblnSystemComplete"},
                new String[]{"1"});
    }

    public static Cursor retrieveEventTaskInstances(){
        String rawEventTasksSelect = "SELECT e.flngID as flngEventId, ti.flngID as flngTaskInstanceId, e.fstrTitle as fstrEventTitle, t.fstrTitle as fstrTaskTitle \n" +
                "FROM tblTaskInstance ti \n" +
                "JOIN tblTask t \n" +
                "ON t.flngID = ti.flngTaskId \n" +
                "AND ti.fblnComplete <> 1 \n" +
                "AND ti.fblnSystemComplete <> 1 \n" +
                "JOIN tblEvent e \n" +
                "ON e.flngID = t.flngEventId \n" +
                "ORDER BY e.flngID";

        return mDatabase.rawQuery(rawEventTasksSelect,null);
    }

    public static Cursor retrieveEventTaskInstancesFromEvent(Long plngEventId){
        String rawEventTasksSelect = "SELECT ti.flngID as flngTaskInstanceId \n" +
                "FROM tblTaskInstance ti \n" +
                "JOIN tblTask t \n" +
                "ON t.flngID = ti.flngTaskId \n" +
                "AND ti.fblnComplete <> 1 \n" +
                "AND ti.fblnSystemComplete <> 1 \n" +
                "WHERE t.flngEventId = ?";

        String[] parameters = {Long.toString(plngEventId)};

        return mDatabase.rawQuery(rawEventTasksSelect,parameters);
    }

    public static Cursor retrieveEventTasksFromEvent(Long plngEventId){
        String selection = "flngEventId == ?";
        String[] selectionArgs = {Long.toString(plngEventId)};

        return mDatabase.query("tblTask",
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);
    }

    public static long insertEvent(String pstrTitle, String pstrDescription) {
        return addRecordToTable("tblEvent",
                new String[] {"fstrTitle","fstrDescription"},
                new String[] {pstrTitle, pstrDescription});
    }

    public static void updateEvent(Long plngID, String pstrTitle, String pstrDescription) {
        updateRecordFromTable("tblEvent",
                plngID,
                new String[] {"fstrTitle","fstrDescription"},
                new String[] {pstrTitle, pstrDescription});
    }
}
