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
        private static final String CREATE_TASK_TABLE = "CREATE TABLE tblTask (flngTaskID INTEGER PRIMARY KEY , fstrTitle TEXT , fstrDescription TEXT , flngSessionID INTEGER , flngTimeID INTEGER , flngEventID INTEGER , fblnActive INTEGER )";

        private static final String CREATE_TASKINSTANCE_TABLE = "CREATE TABLE tblTaskInstance (flngInstanceID INTEGER PRIMARY KEY , flngTaskID INTEGER , fblnComplete INTEGER , fblnSystemComplete INTEGER , fdtmCreated NOT NULL DEFAULT (strftime('%s','now')*1000))";

        private static final String CREATE_SESSION_TABLE = "CREATE TABLE tblSession (flngSessionID INTEGER PRIMARY KEY , fstrTitle TEXT , flngTimeID INTEGER )";

        private static final String CREATE_TIME_TABLE = "CREATE TABLE tblTime (flngTimeID INTEGER PRIMARY KEY , fdtmFrom INTEGER NOT NULL DEFAULT -1 , fdtmTo INTEGER NOT NULL DEFAULT -1 , fdtmFromDate INTEGER NOT NULL DEFAULT -1 , fdtmToDate INTEGER NOT NULL DEFAULT -1 , flngDayID INTEGER NOT NULL DEFAULT -1 , flngWeekID INTEGER , flngMonthID INTEGER NOT NULL DEFAULT -1 , flngYearID INTEGER NOT NULL DEFAULT -1 , flngRepetition INTEGER NOT NULL DEFAULT -1 , fdtmEvaluated INTEGER , fdtmCreated NOT NULL DEFAULT (strftime('%s','now')*1000) )";

        private static final String CREATE_WEEK_TABLE = "CREATE TABLE tblWeek (flngWeekID INTEGER PRIMARY KEY , fblnMonday INTEGER , fblnTuesday INTEGER , fblnWednesday INTEGER , fblnThursday INTEGER , fblnFriday INTEGER , fblnSaturday INTEGER , fblnSunday INTEGER )";

        private static final String CREATE_EVENT_TABLE = "CREATE TABLE tblEvent (flngEventID INTEGER PRIMARY KEY , fstrTitle TEXT NOT NULL , fstrDescription TEXT NOT NULL )";

        private static final String CREATE_DAY_TABLE = "CREATE TABLE `tblDay` ( `flngDayID` INTEGER NOT NULL DEFAULT 0, `fdtmFromDate` INTEGER, `fdtmToDate` INTEGER, PRIMARY KEY(`flngDayID`))";

        private static final String CREATE_MONTH_TABLE = "CREATE TABLE tblMonth (flngMonthID INTEGER NOT NULL DEFAULT 0 PRIMARY KEY , fblnFirst INTEGER , fblnMiddle INTEGER , fblnLast INTEGER , fblnAfterWkn INTEGER NOT NULL DEFAULT 0, fstrSpecific TEXT )";

        private static final String CREATE_YEAR_TABLE = "CREATE TABLE `tblYear` ( `flngYearID` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`flngYearID`))";

        private static final String CREATE_GROUP_TABLE = "CREATE TABLE tblGroup (flngGroupID INTEGER PRIMARY KEY , fstrTitle TEXT )";
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

        //region TABLE TRUNCATE SCRIPTS
        private static final String TRUNCATE_TASK_TABLE=
                "DELETE FROM tblTask";

        private static final String TRUNCATE_TASKINSTANCE_TABLE=
                "DELETE FROM tblTaskInstance";

        private static final String TRUNCATE_SESSION_TABLE=
                "DELETE FROM tblSession";

        private static final String TRUNCATE_TIME_TABLE=
                "DELETE FROM tblTime";

        private static final String TRUNCATE_DAY_TABLE=
                "DELETE FROM tblDay";

        private static final String TRUNCATE_WEEK_TABLE=
                "DELETE FROM tblWeek";

        private static final String TRUNCATE_MONTH_TABLE=
                "DELETE FROM tblMonth";

        private static final String TRUNCATE_YEAR_TABLE=
                "DELETE FROM tblYear";

        private static final String TRUNCATE_EVENT_TABLE=
                "DELETE FROM tblEvent";

        private static final String TRUNCATE_GROUP_TABLE=
                "DELETE FROM tblGroup";
        //endregion


        TaskDatabaseHelper(Context context) {
            super(context, "TaskDatabase.db", null, 8);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TASK_TABLE);
            db.execSQL(CREATE_TASKINSTANCE_TABLE);
            db.execSQL(CREATE_SESSION_TABLE);
            db.execSQL(CREATE_TIME_TABLE);
            db.execSQL(CREATE_WEEK_TABLE);
            db.execSQL(CREATE_EVENT_TABLE);
            db.execSQL(CREATE_DAY_TABLE);
            db.execSQL(CREATE_MONTH_TABLE);
            db.execSQL(CREATE_YEAR_TABLE);
            db.execSQL(CREATE_GROUP_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db,
                              int oldVersion,
                              int newVersion) {

            if (newVersion == 999){
            db.execSQL(TRUNCATE_TASK_TABLE);
            db.execSQL(TRUNCATE_TASKINSTANCE_TABLE);
            db.execSQL(TRUNCATE_SESSION_TABLE);
            db.execSQL(TRUNCATE_TIME_TABLE);
            db.execSQL(TRUNCATE_WEEK_TABLE);
            db.execSQL(TRUNCATE_EVENT_TABLE);
            db.execSQL(TRUNCATE_DAY_TABLE);
            db.execSQL(TRUNCATE_MONTH_TABLE);
            db.execSQL(TRUNCATE_YEAR_TABLE);
            db.execSQL(TRUNCATE_GROUP_TABLE);
            //onCreate(db);
            } else {
                db.beginTransaction();
                try {
                    if (oldVersion < 2){
                        upgradeToV2(db);
                    }
                    if (oldVersion < 3){
                        upgradeToV3(db);
                    }
                    if (oldVersion < 4) {
                        upgradeToV4(db);
                    }
                    if (oldVersion < 5) {
                        upgradeToV5(db);
                    }
                    if (oldVersion < 6) {
                        upgradeToV6(db);
                    }
                    if (oldVersion < 7) {
                        upgradeToV7(db);
                    }
                    if (oldVersion < 8) {
                        upgradeToV8(db);
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db,
                              int oldVersion,
                              int newVersion){

            if (newVersion == 998) {
                db.execSQL(TRUNCATE_TASK_TABLE);
                db.execSQL(TRUNCATE_TASKINSTANCE_TABLE);
                db.execSQL(TRUNCATE_SESSION_TABLE);
                db.execSQL(TRUNCATE_TIME_TABLE);
                db.execSQL(TRUNCATE_WEEK_TABLE);
                db.execSQL(TRUNCATE_EVENT_TABLE);
                db.execSQL(TRUNCATE_DAY_TABLE);
                db.execSQL(TRUNCATE_MONTH_TABLE);
                db.execSQL(TRUNCATE_YEAR_TABLE);
                db.execSQL(TRUNCATE_GROUP_TABLE);
                //onCreate(db);
            }
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

        private void upgradeToV3(SQLiteDatabase db){
            db.execSQL("ALTER TABLE tblTask \n" +
                    "ADD fblnActive INTEGER;");

            ContentValues values = new ContentValues();
            values.put("fblnActive","1");
            db.update("tblTask",
                    values,
                    null,
                    null);

            //realized I could just do an add column alter statement
            /*db.execSQL("CREATE TABLE `tblTaskTmp` (\n" +
                            "\t`flngID`\tINTEGER,\n" +
                            "\t`fstrTitle`\tTEXT,\n" +
                            "\t`fstrDescription`\tTEXT,\n" +
                            "\t`flngSessionID`\tINTEGER,\n" +
                            "\t`flngTimeID`\tINTEGER,\n" +
                            "\t`flngEventID`\tINTEGER,\n" +
                            "\t`fblnActive`\tINTEGER,\n" +
                            "\tPRIMARY KEY(`flngID`)\n" +
                            "); ");
            Cursor cursor = returnCurrentTable(db, "tblTask");
            while (cursor.moveToNext()){
                ContentValues values = populateCurrentTableValues(cursor);
                values.put("fblnActive",1);
                db.insertOrThrow("tblTaskTmp",
                        null,
                        values);
            }
            db.execSQL("DROP TABLE TblTask;");
            db.execSQL("ALTER TABLE TblTaskTmp RENAME TO TblTask");*/
        }

        private void upgradeToV4(SQLiteDatabase db) throws Exception {
            //Create new time tables
            db.execSQL("CREATE TABLE `tblDay` ( `flngDayID` INTEGER NOT NULL DEFAULT 0, `fdtmFromDate` INTEGER, `fdtmToDate` INTEGER, PRIMARY KEY(`flngDayID`))");
            db.execSQL("CREATE TABLE `tblMonth` ( `flngMonthID` INTEGER NOT NULL DEFAULT 0, `fblnFirst` INTEGER, `fblnMiddle` INTEGER, fblnLast INTEGER, fstrSpecific TEXT, PRIMARY KEY(`flngMonthID`))");
            db.execSQL("CREATE TABLE `tblYear` ( `flngYearID` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`flngYearID`))");
            addColumn(db,
                    "tblTime",
                    "flngDayID",
                    3,
                    true,
                    "-1");
            addColumn(db,
                    "tblTime",
                    "flngMonthID",
                    5,
                    true,
                    "-1");
            addColumn(db,
                    "tblTime",
                    "flngYearID",
                    6,
                    true,
                    "-1");
            addColumn(db,
                    "tblTime",
                    "fdtmFromDate",
                    3,
                    true,
                    "-1");
            addColumn(db,
                    "tblTime",
                    "fdtmToDate",
                    4,
                    true,
                    "-1");
            addColumn(db,
                    "tblTime",
                    "fdtmCreated",
                    11,
                    true,
                    "(strftime('%s','now')*1000)");
            addColumn(db,
                    "tblTime",
                    "fintRepetition",
                    9,
                    true,
                    "-1");
            deleteColumn(db,
                    "tblEvent",
                    3);
            updateColumn(db,
                    "tblEvent",
                    "flngID",
                    "flngEventID",
                    true,
                    "0",
                    true);
            updateColumn(db,
                    "tblSession",
                    "flngID",
                    "flngSessionID",
                    true,
                    "0",
                    true);
            updateColumn(db,
                    "tblTaskInstance",
                    "flngID",
                    "flngInstanceID",
                    true,
                    "0",
                    true);
            addColumn(db,
                    "tblTaskInstance",
                    "fdtmCreated",
                    4,
                    true,
                    "(strftime('%s','now')*1000)");
            updateColumn(db,
                    "tblTask",
                    "flngID",
                    "flngTaskID",
                    true,
                    "0",
                    true);
            updateColumn(db,
                    "tblTime",
                    "flngID",
                    "flngTimeID",
                    true,
                    "0",
                    true);
            updateColumn(db,
                    "tblWeek",
                    "flngID",
                    "flngWeekID",
                    true,
                    "0",
                    true);
            updateColumn(db,
                    "tblTime",
                    "fdtmFrom",
                    "fdtmFrom",
                    true,
                    "-1",
                    false);
            updateColumn(db,
                    "tblTime",
                    "fdtmTo",
                    "fdtmTo",
                    true,
                    "-1",
                    false);
        }

        private void upgradeToV5(SQLiteDatabase db) throws Exception {
            updateColumn(db,
                    "tblTime",
                    "fintRepetition",
                    "flngRepetition",
                    true,
                    "-1",
                    false);
        }

        private void upgradeToV6(SQLiteDatabase db) throws Exception {
            addColumn(db,
                    "tblTask",
                    "fblnOneOff",
                    retrieveTableLength(db,"tblTask"),
                    true,
                    "0");
        }

        private void upgradeToV7(SQLiteDatabase db) throws Exception {
            addColumn(db,
                    "tblMonth",
                    "fblnAfterWkn",
                    4,
                    true,
                    "0");
        }

        private void upgradeToV8(SQLiteDatabase db) throws Exception {

            db.execSQL(CREATE_GROUP_TABLE);
            addColumn(db,
                    "tblTask",
                    "flngGroupID",
                    6,
                    true,
                    "-1");
        }

        /** @param pintPosition starts at 0 */
        private void addColumn(SQLiteDatabase db,
                                String pstrTableName,
                                String pstrColumnName,
                                Integer pintPosition,
                                boolean pblnNotNull,
                                String pstrDefault) throws Exception{
            String strTempTable = pstrTableName + "_Temp";

            //Gets Create Statement for the Specific Table, updates it to be _Temp, then executes the updated create script
            Cursor cursor = db.rawQuery("SELECT sql FROM sqlite_master WHERE name='" + pstrTableName + "';",new String[]{});
            cursor.moveToFirst();
            String strCreate = cursor.getString(cursor.getColumnIndex("sql"));
            strCreate = strCreate.replaceFirst(pstrTableName, strTempTable);
            db.execSQL(strCreate);

            //Insert data from regular tables into new tables
            db.execSQL("INSERT INTO " + strTempTable + " SELECT * FROM " + pstrTableName);

            //Drop original tables
            db.execSQL("DROP TABLE " + pstrTableName);

            //Create new table
            String strCreateStatement = "";
            cursor = db.rawQuery("PRAGMA table_info(" + strTempTable + ")",new String[]{});
            Integer intCounter = 0;
            while(cursor.moveToNext()){
                if (!strCreateStatement.equals("")){
                    strCreateStatement += ", ";
                }
                if (pintPosition == intCounter){
                    strCreateStatement += pstrColumnName + " ";
                    String strColumnType = "";
                    switch (pstrColumnName.substring(1,4)){
                        case "lng":
                            strColumnType = "INTEGER";
                            break;
                        case "dtm":
                            strColumnType = "INTEGER";
                            break;
                        case "bln":
                            strColumnType = "INTEGER";
                            break;
                        case "str":
                            strColumnType = "TEXT";
                            break;
                        case "int":
                            strColumnType = "INTEGER";
                            break;
                    }
                    strCreateStatement += strColumnType + " ";
                    if (pblnNotNull == true){
                        strCreateStatement += "NOT NULL ";
                    }
                    if (pstrDefault == "") {
                        throw new Exception("Default Must Be Provided");
                    }
                    strCreateStatement += "DEFAULT " + pstrDefault + ", ";
                }
                strCreateStatement += cursor.getString(cursor.getColumnIndex("name")) + " ";
                strCreateStatement += cursor.getString(cursor.getColumnIndex("type")) + " ";
                if (cursor.getInt(cursor.getColumnIndex("notnull")) == 1){
                    strCreateStatement += "NOT NULL ";
                }
                if (cursor.getString(cursor.getColumnIndex("dflt_value")) != null){
                    //Hard coded because for some reason it doesn't keep the required parenthesis
                    if (cursor.getString(cursor.getColumnIndex("dflt_value")).equals("strftime('%s','now')*1000")){
                        strCreateStatement += "DEFAULT (strftime('%s','now')*1000) ";
                    } else {
                        strCreateStatement += "DEFAULT " + cursor.getString(cursor.getColumnIndex("dflt_value")) + " ";
                    }
                }
                if (cursor.getInt(cursor.getColumnIndex("pk")) == 1){
                    strCreateStatement += "PRIMARY KEY ";
                }
                intCounter += 1;
            }
            //if the counter was higher than the table columns, add to the end.
            if (pintPosition >= intCounter){
                strCreateStatement += ", ";
                strCreateStatement += pstrColumnName + " ";
                if (pblnNotNull == true){
                    strCreateStatement += "NOT NULL ";
                }
                if (pstrDefault != ""){
                    strCreateStatement += "DEFAULT " + pstrDefault;
                }
            }
            db.execSQL("CREATE TABLE " + pstrTableName + " (" + strCreateStatement + ")");

            //Generates a list of the temp tables fields then creates and executes the insert into new table statement
            cursor = db.rawQuery("PRAGMA table_info(" + strTempTable + ")",new String[]{});
            String strOrigColumns = "";
            while (cursor.moveToNext()){
                if (strOrigColumns != ""){
                    strOrigColumns += ", ";
                }
                strOrigColumns += cursor.getString(cursor.getColumnIndex("name"));
            }
            db.execSQL("INSERT INTO " + pstrTableName + " (" + strOrigColumns + ") SELECT * FROM " + strTempTable);

            //Drop temp table
            db.execSQL("DROP TABLE " + strTempTable);
        }

        private void deleteColumn(SQLiteDatabase db,
                               String pstrTableName,
                               Integer pintPosition) throws Exception{

            String strTempTable = pstrTableName + "_Temp";

            //Gets Create Statement for the Specific Table, updates it to be _Temp, then executes the updated create script
            Cursor cursor = db.rawQuery("SELECT sql FROM sqlite_master WHERE name='" + pstrTableName + "';",new String[]{});
            cursor.moveToFirst();
            String strCreate = cursor.getString(cursor.getColumnIndex("sql"));
            strCreate = strCreate.replaceFirst(pstrTableName, strTempTable);
            db.execSQL(strCreate);

            //Insert data from regular tables into temp table
            db.execSQL("INSERT INTO " + strTempTable + " SELECT * FROM " + pstrTableName);

            //Drop original tables
            db.execSQL("DROP TABLE " + pstrTableName);

            //Create new table
            String strCreateStatement = "";
            cursor = db.rawQuery("PRAGMA table_info(" + strTempTable + ")",new String[]{});
            Integer intCounter = 0;
            while(cursor.moveToNext()){
                if (pintPosition != intCounter){
                    if (strCreateStatement != ""){
                        strCreateStatement += ", ";
                    }
                    strCreateStatement += cursor.getString(cursor.getColumnIndex("name")) + " ";
                    strCreateStatement += cursor.getString(cursor.getColumnIndex("type")) + " ";
                    if (cursor.getInt(cursor.getColumnIndex("notnull")) == 1){
                        strCreateStatement += "NOT NULL ";
                    }
                    if (cursor.getString(cursor.getColumnIndex("dflt_value")) != null){
                        if (cursor.getString(cursor.getColumnIndex("dflt_value")).equals("strftime('%s','now')*1000")){
                            strCreateStatement += "DEFAULT (strftime('%s','now')*1000) ";
                        } else {
                            strCreateStatement += "DEFAULT " + cursor.getString(cursor.getColumnIndex("dflt_value")) + " ";
                        }
                    }
                    if (cursor.getInt(cursor.getColumnIndex("pk")) == 1){
                        strCreateStatement += "PRIMARY KEY ";
                    }
                }
                intCounter += 1;
            }
            db.execSQL("CREATE TABLE " + pstrTableName + " (" + strCreateStatement + ")");

            //Generates a list of the temp tables fields then creates and executes the insert into new table statement
            cursor = db.rawQuery("PRAGMA table_info(" + strTempTable + ")",new String[]{});
            String strOrigColumns = "";
            intCounter = 0;
            while (cursor.moveToNext()){
                if (pintPosition != intCounter) {
                    if (strOrigColumns != "") {
                        strOrigColumns += ", ";
                    }
                    strOrigColumns += cursor.getString(cursor.getColumnIndex("name"));
                }
                intCounter += 1;
            }
            db.execSQL("INSERT INTO " + pstrTableName + " (" + strOrigColumns + ") SELECT " + strOrigColumns + " FROM " + strTempTable);

            //Drop temp table
            db.execSQL("DROP TABLE " + strTempTable);
        }

        private void updateColumn(SQLiteDatabase db,
                               String pstrTableName,
                               String pstrOrigColumnName,
                               String pstrNewColumnName,
                               boolean pblnNotNull,
                               String pstrDefault,
                               boolean pblnPrimary) throws Exception{

            String strTempTable = pstrTableName + "_Temp";

            //Gets Create Statement for the Specific Table, updates it to be _Temp, then executes the updated create script
            Cursor cursor = db.rawQuery("SELECT sql FROM sqlite_master WHERE name='" + pstrTableName + "';",new String[]{});
            cursor.moveToFirst();
            String strCreate = cursor.getString(cursor.getColumnIndex("sql"));
            strCreate = strCreate.replaceFirst(pstrTableName, strTempTable);
            db.execSQL(strCreate);

            //Insert data from regular tables into new tables
            db.execSQL("INSERT INTO " + strTempTable + " SELECT * FROM " + pstrTableName);

            //Drop original tables
            db.execSQL("DROP TABLE " + pstrTableName);

            //Create new table
            String strCreateStatement = "";
            cursor = db.rawQuery("PRAGMA table_info(" + strTempTable + ")",new String[]{});
            while(cursor.moveToNext()){
                String strCurrentColName = cursor.getString(cursor.getColumnIndex("name"));
                if (strCreateStatement != ""){
                    strCreateStatement += ", ";
                }
                if (strCurrentColName.equals(pstrOrigColumnName)){
                    if (!pblnPrimary){
                        strCreateStatement += pstrNewColumnName + " ";
                        strCreateStatement += cursor.getString(cursor.getColumnIndex("type")) + " ";
                        if (pblnNotNull == true){
                            strCreateStatement += "NOT NULL ";
                        }
                        if (pstrDefault == "") {
                            throw new Exception("Default Must Be Provided");
                        }
                        strCreateStatement += "DEFAULT " + pstrDefault + " ";
                    } else {
                        strCreateStatement += pstrNewColumnName + " " + cursor.getString(cursor.getColumnIndex("type")) + " PRIMARY KEY ";
                    }
                } else {
                    strCreateStatement += strCurrentColName + " ";
                    strCreateStatement += cursor.getString(cursor.getColumnIndex("type")) + " ";
                    if (cursor.getInt(cursor.getColumnIndex("notnull")) == 1) {
                        strCreateStatement += "NOT NULL ";
                    }
                    if (cursor.getString(cursor.getColumnIndex("dflt_value")) != null) {
                        if (cursor.getString(cursor.getColumnIndex("dflt_value")).equals("strftime('%s','now')*1000")){
                            strCreateStatement += "DEFAULT (strftime('%s','now')*1000) ";
                        } else {
                            strCreateStatement += "DEFAULT " + cursor.getString(cursor.getColumnIndex("dflt_value")) + " ";
                        }
                    }
                    if (cursor.getInt(cursor.getColumnIndex("pk")) == 1) {
                        strCreateStatement += "PRIMARY KEY ";
                    }
                }
            }
            db.execSQL("CREATE TABLE " + pstrTableName + " (" + strCreateStatement + ")");

            //Generates a list of the temp tables fields then creates and executes the insert into new table statement
//            cursor = db.rawQuery("PRAGMA table_info(" + strTempTable + ")",new String[]{});
//            String strOrigColumns = "";
//            while (cursor.moveToNext()){
//                String strCurrentColName = cursor.getString(cursor.getColumnIndex("name"));
//                if (strOrigColumns != ""){
//                    strOrigColumns += ", ";
//                }
//                if (strCurrentColName == pstrOrigColumnName) {
//                    strOrigColumns += pstrNewColumnName;
//                } else {
//                    strOrigColumns += strCurrentColName;
//                }
//            }
//            db.execSQL("INSERT INTO " + pstrTableName + " VALUES(" + strOrigColumns + ") SELECT * FROM " + strTempTable);
            //SHOULDN'T NEED TO DO THE ABOVE BECAUSE THE COLUMN POSITIONS AREN'T CHANGING AND NOR IS THE TYPE
            db.execSQL("INSERT INTO " + pstrTableName + " SELECT * FROM " + strTempTable);

            //Drop temp table
            db.execSQL("DROP TABLE " + strTempTable);
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

        private int retrieveTableLength(SQLiteDatabase db,
                                        String pstrTable){
            int length = 0;
            Cursor cursor = db.query(pstrTable,
                    null,
                    "1 = 0", //prevents grabbing any rows, just columns
                    null,
                    null,
                    null,
                    null);
            length = cursor.getColumnCount();
            return length;
        }

        private static ContentValues populateCurrentTableValues(Cursor pCursor){
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
                    case "int":
                        values.put(columnName,pCursor.getLong(i));
                        break;
                }
            }
            return values;
        }

    }

    //region GENERIC FUNCTIONS
    public static Cursor getRecordsFromTable(String pstrTableName,
                                             String pstrIDColumn,
                                             Long plngID){
        String selection = pstrIDColumn + " = ?";
        String[] selectionArgs = {Long.toString(plngID)};

        return mDatabase.query(pstrTableName,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);
    }

    public static Cursor getRecordsFromTable(String pstrTableName){
        return mDatabase.query(pstrTableName,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private static ContentValues generateContentValues(String[] pstrColumns,
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

        return addRecordToTable(pstrTableName, pstrAddColumns, pobjAddValues, "", (long)-1);
    }

    public static long addRecordToTable(String pstrTableName,
                                        String[] pstrAddColumns,
                                        Object[] pobjAddValues,
                                        String pstrColumnID,
                                        Long plngID){

        boolean blnUpdate = pstrColumnID != "" && plngID != -1;

        ContentValues values = generateContentValues(pstrAddColumns, pobjAddValues);

        if (blnUpdate){
            values.put(pstrColumnID, plngID);
            deleteRecordFromTable(pstrTableName, pstrColumnID, plngID);
        }

        if (values.size() >= 1){
            return mDatabase.insertOrThrow(pstrTableName,
                    null,
                    values);
        } else {
            return mDatabase.insertOrThrow(pstrTableName,
                    pstrColumnID,
                    values);
        }
    }

    public static int updateRecordFromTable(String pstrTableName,
                                            String pstrIDColumn,
                                            Long plngID,
                                            String[] pstrUpdateColumns,
                                            Object[] pobjUpdateValues){

        ContentValues values = generateContentValues(pstrUpdateColumns, pobjUpdateValues);

        String selection = pstrIDColumn + " = ?";
        String[] selectionArgs = {Long.toString(plngID)};

        return mDatabase.update(pstrTableName,
                values,
                selection,
                selectionArgs);
    }

    public static int deleteRecordFromTable(String pstrTableName,
                                            String pstrIDColumn,
                                            Long plngID){
        String whereClause = pstrIDColumn + " = ?";
        String[] whereArgs = {Long.toString(plngID)};

        return mDatabase.delete(pstrTableName,
                whereClause,
                whereArgs);
    }
    //endregion

    //region SPECIFIC FUNCTIONS
    public static void resetTaskEval(){
        ContentValues updates = new ContentValues();
        updates.put("fdtmEvaluated",1);
        mDatabase.update("tblTime", updates, "", null);
    }

    public static Cursor getTaskInstancesWithDetails(){
        String rawQuery = "SELECT i.flngInstanceID, t.fstrTitle, tm.fdtmFrom, tm.fdtmTo, tm.flngDayID, tm.flngWeekID, tm.flngMonthID, tm.flngYearID, tm.flngRepetition, i.fdtmCreated \n" +
                "FROM tblTask t \n" +
                "JOIN tblTaskInstance i \n" +
                "ON t.flngTaskId = i.flngTaskId \n" +
                "LEFT JOIN tblSession s \n" +
                "ON s.flngSessionID = t.flngSessionID \n" +
                "LEFT JOIN tblTime tm \n" +
                "ON tm.flngTimeID = IFNULL(s.flngTimeID, t.flngTimeID) \n" +
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

    public static Cursor retrieveTasksAssociatedWithSession(Long plngSessionID){
        String selection = "flngSessionID = ?";
        String[] selectionArgs = {Long.toString(plngSessionID)};

        return mDatabase.query("tblTask",
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);
    }

    public static void deleteTaskInstances(Long plngTaskID){
        String selection = "flngTaskID == ?";
        String[] selectionArgs = {Long.toString(plngTaskID)};

        Cursor curInstances= mDatabase.query("tblTaskInstance",
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);

        while (curInstances.moveToNext()){
            deleteRecordFromTable("tblTaskInstance",
                    "flngInstanceID",
                    curInstances.getLong(curInstances.getColumnIndex("flngInstanceID")));
        }
    }

    public static Cursor retrieveEventTaskInstances(){
        String rawEventTasksSelect = "SELECT e.flngEventID, ti.flngInstanceID, e.fstrTitle as fstrEventTitle, t.fstrTitle as fstrTaskTitle \n" +
                "FROM tblTaskInstance ti \n" +
                "JOIN tblTask t \n" +
                "ON t.flngTaskID = ti.flngTaskId \n" +
                "AND ti.fblnComplete <> 1 \n" +
                "AND ti.fblnSystemComplete <> 1 \n" +
                "JOIN tblEvent e \n" +
                "ON e.flngEventID = t.flngEventId \n" +
                "ORDER BY e.flngEventID";

        return mDatabase.rawQuery(rawEventTasksSelect,null);
    }

    public static Cursor retrieveEventTaskInstancesFromEvent(Long plngEventId){
        String rawEventTasksSelect = "SELECT ti.flngInstanceID \n" +
                "FROM tblTaskInstance ti \n" +
                "JOIN tblTask t \n" +
                "ON t.flngTaskID = ti.flngTaskId \n" +
                "AND ti.fblnComplete <> 1 \n" +
                "AND ti.fblnSystemComplete <> 1 \n" +
                "WHERE t.flngEventId = ?";

        String[] parameters = {Long.toString(plngEventId)};

        return mDatabase.rawQuery(rawEventTasksSelect,parameters);
    }

    public static Cursor retrieveEventTasksFromEvent(Long plngEventId){
        String selection = "flngEventId == ? AND fblnActive = 1";
        String[] selectionArgs = {Long.toString(plngEventId)};

        return mDatabase.query("tblTask",
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);
    }

    public static Cursor retrieveTasksAssociatedWithEvent(Long plngEventId){
        String selection = "flngEventID = ?";
        String[] selectionArgs = {Long.toString(plngEventId)};

        return mDatabase.query("tblTask",
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);
    }
    //endregion
}
