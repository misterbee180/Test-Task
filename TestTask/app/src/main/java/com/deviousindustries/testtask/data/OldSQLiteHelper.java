package com.deviousindustries.testtask.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.deviousindustries.testtask.Utilities;
import com.deviousindustries.testtask.classes.Time;

import java.util.Calendar;

public class OldSQLiteHelper extends SQLiteOpenHelper {

    public static SQLiteDatabase oldDatabase;
    
    public OldSQLiteHelper(Context context) {
        super(context, "TaskDatabase.db", null, 25);
        oldDatabase = this.getWritableDatabase();
    }

    private static final String CREATE_TASK_TABLE = "CREATE TABLE tblTask (flngTaskID INTEGER PRIMARY KEY , flngTaskDetailID INTEGER NOT NULL DEFAULT -1, " +
            "flngTimeID INTEGER NOT NULL DEFAULT -1, fintTaskType INTEGER NOT NULL DEFAULT 0, flngTaskTypeID INTEGER NOT NULL DEFAULT -1," +
            "fdtmCreated INTEGER NOT NULL DEFAULT (strftime('%s','now')*1000), fdtmDeleted INTEGER NOT NULL DEFAULT -1, flngOneOff INTEGER NOT NULL DEFAULT -1)";

    final String CREATE_TASKDETAIL_TABLE = "CREATE TABLE tblTaskDetail (flngTaskDetailID INTEGER PRIMARY KEY, fstrTitle NOT NULL DEFAULT '', fstrDescription NOT NULL DEFAULT '')";

    private static final String CREATE_TASKINSTANCE_TABLE = "CREATE TABLE tblTaskInstance (flngInstanceID INTEGER PRIMARY KEY, flngTaskID INTEGER NOT NULL DEFAULT -1," +
            "flngTaskDetailID INTEGER NOT NULL DEFAULT -1, fdtmFrom INTEGER NOT NULL DEFAULT -1, fdtmTo INTEGER NOT NULL DEFAULT -1, " +
            "fblnFromTime INTEGER NOT NULL DEFAULT 0, fblnToTime INTEGER NOT NULL DEFAULT 0, fblnToDate INTEGER NOT NULL DEFAULT 0, fdtmCreated INTEGER NOT NULL DEFAULT (strftime('%s','now')*1000), " +
            "fdtmCompleted INTEGER NOT NULL DEFAULT -1, fdtmSystemCompleted INTEGER NOT NULL DEFAULT -1, fdtmDeleted INTEGER NOT NULL DEFAULT -1, fdtmEdited INTEGER NOT NULL DEFAULT -1," +
            "flngSessionID INTEGER NOT NULL DEFAULT -1)";

    private static final String CREATE_TIME_TABLE = "CREATE TABLE tblTime (flngTimeID INTEGER PRIMARY KEY, fdtmFrom INTEGER NOT NULL DEFAULT -1, fdtmTo INTEGER NOT NULL DEFAULT -1, " +
            "fblnFromTime INTEGER NOT NULL DEFAULT 0, fblnToTime INTEGER NOT NULL DEFAULT 0, fblnToDate INTEGER NOT NULL DEFAULT 0, fintTimeframe INTEGER NOT NULL DEFAULT -1, " +
            "flngTimeframeID INTEGER NOT NULL DEFAULT -1, flngRepetition INTEGER NOT NULL DEFAULT -1, fdtmCreated INTEGER NOT NULL DEFAULT (strftime('%s','now')*1000), " +
            "fintStarting INTEGER NOT NULL DEFAULT 0, fblnComplete INTEGER NOT NULL DEFAULT 0, flngGenerationID INTEGER NOT NULL DEFAULT -1, fblnThru INTEGER NOT NULL DEFAULT 0, " +
            "fblnSession INTEGER NOT NULL DEFAULT 0, fstrTitle TEXT NOT NULL DEFAULT '')";

    private static final String CREATE_TIME_INSTANCE_TABLE = "CREATE TABLE tblTimeInstance (flngGenerationID INTEGER PRIMARY KEY, flngTimeID INTEGER NOT NULL DEFAULT -1, " +
            "fdtmUpcoming INTEGER NOT NULL DEFAULT -1, fdtmPriority INTEGER NOT NULL DEFAULT -1, fintThru INTEGER NOT NULL DEFAULT 0)";

    private static final String CREATE_EVENT_TABLE = "CREATE TABLE tblEvent (flngEventID INTEGER PRIMARY KEY , fstrTitle TEXT NOT NULL , fstrDescription TEXT NOT NULL )";

    private static final String CREATE_WEEK_TABLE = "CREATE TABLE tblWeek (flngWeekID INTEGER PRIMARY KEY , fblnMonday INTEGER , fblnTuesday INTEGER , fblnWednesday INTEGER , " +
            "fblnThursday INTEGER , fblnFriday INTEGER , fblnSaturday INTEGER , fblnSunday INTEGER )";

    private static final String CREATE_DAY_TABLE = "CREATE TABLE `tblDay` ( `flngDayID` INTEGER NOT NULL DEFAULT 0, `fdtmFromDate` INTEGER, `fdtmToDate` INTEGER, PRIMARY KEY(`flngDayID`))";

    private static final String CREATE_MONTH_TABLE = "CREATE TABLE tblMonth (flngMonthID INTEGER NOT NULL DEFAULT 0 PRIMARY KEY , fblnFirst INTEGER , fblnMiddle INTEGER , fblnLast INTEGER , " +
            "fblnAfterWkn INTEGER NOT NULL DEFAULT 0, fstrSpecific TEXT )";

    private static final String CREATE_YEAR_TABLE = "CREATE TABLE `tblYear` ( `flngYearID` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`flngYearID`))";

    private static final String CREATE_GROUP_TABLE = "CREATE TABLE tblGroup (flngGroupID INTEGER PRIMARY KEY , fstrTitle TEXT )";

    private static final String CREATE_LONGTERM_TABLE = "CREATE TABLE tblLongTerm (flngLongTermID INTEGER PRIMARY KEY, fstrTitle TEXT NOT NULL , fstrDescription TEXT NOT NULL )";
    //endregion

    //region TABLE DROP SCRIPTS
    private static final String DROP_TASK_TABLE =
            "DROP TABLE IF EXISTS tblTask";

    private static final String DROP_TASKDETAIL_TABLE =
            "DROP TABLE IF EXISTS tblTaskDetail";

    private static final String DROP_TASKINSTANCE_TABLE =
            "DROP TABLE IF EXISTS tblTaskInstance";

    private static final String DROP_SESSION_TABLE =
            "DROP TABLE IF EXISTS tblSession";

    private static final String DROP_TIME_TABLE =
            "DROP TABLE IF EXISTS tblTime";

    private static final String DROP_WEEK_TABLE =
            "DROP TABLE IF EXISTS tblWeek";

    private static final String DROP_EVENT_TABLE =
            "DROP TABLE IF EXISTS tblEvent";

    private static final String DROP_GROUP_TABLE =
            "DROP TABLE IF EXISTS tblGroup";

    private static final String DROP_LONGTERM_TABLE =
            "DROP TABLE IF EXISTS tblLongTerm";

    private static final String DROP_TIME_INSTANCE_TABLE =
            "DROP TABLE IF EXISTS tblTimeInstance";
    //endregion

    //region TABLE TRUNCATE SCRIPTS
    private static final String TRUNCATE_TASK_TABLE =
            "DELETE FROM tblTask";

    private static final String TRUNCATE_TASKINSTANCE_TABLE =
            "DELETE FROM tblTaskInstance";

    private static final String TRUNCATE_SESSION_TABLE =
            "DELETE FROM tblSession";

    private static final String TRUNCATE_TIME_TABLE =
            "DELETE FROM tblTime";

    private static final String TRUNCATE_DAY_TABLE =
            "DELETE FROM tblDay";

    private static final String TRUNCATE_WEEK_TABLE =
            "DELETE FROM tblWeek";

    private static final String TRUNCATE_MONTH_TABLE =
            "DELETE FROM tblMonth";

    private static final String TRUNCATE_YEAR_TABLE =
            "DELETE FROM tblYear";

    private static final String TRUNCATE_EVENT_TABLE =
            "DELETE FROM tblEvent";

    private static final String TRUNCATE_GROUP_TABLE =
            "DELETE FROM tblGroup";

    private static final String TRUNCATE_LONGTERM_TABLE =
            "DELETE FROM tblLongTerm";

    private static final String TRUNCATE_TIME_INSTANCE_TABLE =
            "DELETE FROM tblTimeInstance";
    //endregion

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TASK_TABLE);
        db.execSQL(CREATE_TASKDETAIL_TABLE);
        db.execSQL(CREATE_TASKINSTANCE_TABLE);
        db.execSQL(CREATE_TIME_TABLE);
        db.execSQL(CREATE_WEEK_TABLE);
        db.execSQL(CREATE_EVENT_TABLE);
        db.execSQL(CREATE_DAY_TABLE);
        db.execSQL(CREATE_MONTH_TABLE);
        db.execSQL(CREATE_YEAR_TABLE);
        db.execSQL(CREATE_GROUP_TABLE);
        db.execSQL(CREATE_LONGTERM_TABLE);
        db.execSQL(CREATE_TIME_INSTANCE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {

        oldDatabase = db;
        if (newVersion == 999) {
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
            db.execSQL(TRUNCATE_LONGTERM_TABLE);
            //onCreate(db);
        } else {
            db.beginTransaction();
            try {
                if (oldVersion < 2) {
                    upgradeToV2(db);
                }
                if (oldVersion < 3) {
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
                if (oldVersion < 9) {
                    upgradeToV9(db);
                }
                if (oldVersion < 10) {
                    upgradeToV10(db);
                }
                if (oldVersion < 11) {
                    upgradeToV11(db);
                }
                if (oldVersion < 12) {
                    upgradeToV12(db);
                }
                if (oldVersion < 13) {
                    upgradeToV13(db);
                }
                if (oldVersion < 14) {
                    upgradeToV14(db);
                }
                if (oldVersion < 15) {
                    upgradeToV15(db);
                }
                if (oldVersion < 16) {
                    upgradeToV16(db);
                }
                if (oldVersion < 17) {
                    upgradeToV17(db);
                }
                if (oldVersion < 18) {
                    upgradeToV18(db);
                }
                if (oldVersion < 19) {
                    //upgradeToV19(db);
                }
                if (oldVersion < 20) {
                    upgradeToV20(db);
                }
                if (oldVersion < 21) {
                    upgradeToV21(db);
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
                            int newVersion) {

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
            db.execSQL(TRUNCATE_LONGTERM_TABLE);
            //onCreate(db);
        }
    }

    private void upgradeToV2(SQLiteDatabase db) {
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
        Cursor cursor = db.query("tblTask", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            ContentValues values = populateCurrentTableValues(cursor);
            values.put("flngEventId", -1);
            db.insertOrThrow("tblTaskTmp",
                    null,
                    values);
        }
        db.execSQL("DROP TABLE TblTask;");
        db.execSQL("ALTER TABLE TblTaskTmp RENAME TO TblTask");
    }

    private void upgradeToV3(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE tblTask \n" +
                "ADD fblnActive INTEGER;");

        ContentValues values = new ContentValues();
        values.put("fblnActive", "1");
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
                retrieveTableLength(db, "tblTask"),
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

    private void upgradeToV9(SQLiteDatabase db) throws Exception {

        db.execSQL(CREATE_LONGTERM_TABLE);
        addColumn(db,
                "tblTask",
                "flngLongTermID",
                7,
                true,
                "-1");
    }

    private void upgradeToV10(SQLiteDatabase db) throws Exception {
        addColumn(db,
                "tblTime",
                "fblnToTimeSet",
                3,
                false,
                "0");
        addColumn(db,
                "tblTime",
                "fblnFromTimeSet",
                3,
                false,
                "0");

        Cursor cursor = db.query("tblTime",
                null,
                null,
                null,
                null,
                null,
                null);
        while (cursor.moveToNext()) {
            Long dtmFromTime = cursor.getLong(cursor.getColumnIndex("fdtmFrom"));
            Long dtmFromDate = cursor.getLong(cursor.getColumnIndex("fdtmFromDate"));
            Long dtmToTime = cursor.getLong(cursor.getColumnIndex("fdtmTo"));
            Long dtmToDate = cursor.getLong(cursor.getColumnIndex("fdtmToDate"));
            Long fdtmCreated = cursor.getLong(cursor.getColumnIndex("fdtmCreated"));
            Boolean blnFromTime = false;
            Boolean blnToTime = false;
            Boolean blnFromDate = false;
            Boolean blnToDate = false;
            Calendar calFromDate = Calendar.getInstance();
            Calendar calToDate = Calendar.getInstance();

            //Merging dates into single date field
            if (dtmFromDate != -1) {
                blnFromDate = true;
                calFromDate.setTimeInMillis(dtmFromDate);
            }

            if (dtmFromTime != -1) {
                blnFromTime = true;
                Calendar calTime = Calendar.getInstance();
                calTime.setTimeInMillis(dtmFromTime);
                //Use create date if time but no date provided
                if (!blnFromDate) {
                    blnFromDate = true;
                    calFromDate.setTimeInMillis(fdtmCreated);
                }
                calFromDate.set(Calendar.HOUR, calTime.get(Calendar.HOUR));
                calFromDate.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
                calFromDate.set(Calendar.AM_PM, calTime.get(Calendar.AM_PM));
            }

            if (dtmToDate != -1) {
                blnToDate = true;
                calToDate.setTimeInMillis(dtmToDate);
            }

            if (dtmToTime != -1) {
                blnToTime = true;
                Calendar calTime = Calendar.getInstance();
                calTime.setTimeInMillis(dtmToTime);
                if (!blnToDate) {
                    blnToDate = true;
                    //If no to date exists, use from date if one exists, otherwise, use create date
                    if (blnFromDate) {
                        calToDate = (Calendar) calFromDate.clone();
                    } else {
                        calToDate.setTimeInMillis(fdtmCreated);
                    }
                }
                calToDate.set(Calendar.HOUR, calTime.get(Calendar.HOUR));
                calToDate.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
                calToDate.set(Calendar.AM_PM, calTime.get(Calendar.AM_PM));
            }

            Long lngDateFrom = (long) -1;
            Long lngDateTo = (long) -1;
            if (blnFromDate) {
                lngDateFrom = calFromDate.getTimeInMillis();
            }
            if (blnToDate) {
                lngDateTo = calToDate.getTimeInMillis();
            }
            updateRecordFromTable("tblTime", "flngTimeID", cursor.getInt(cursor.getColumnIndex("flngTimeID"))
                    , new String[]{"fdtmFrom",
                            "fdtmTo",
                            "fblnFromTimeSet",
                            "fblnToTimeSet"}
                    , new Object[]{lngDateFrom,
                            lngDateTo,
                            blnFromTime,
                            blnToTime});
        }
    }

    private void upgradeToV11(SQLiteDatabase db) throws Exception {
        deleteColumn(oldDatabase
                , "tblTime"
                , returnColumnPosition(db
                        , "tblTime"
                        , "fdtmFromDate"));
        deleteColumn(oldDatabase
                , "tblTime"
                , returnColumnPosition(db
                        , "tblTime"
                        , "fdtmToDate"));
    }

    private void upgradeToV12(SQLiteDatabase db) throws Exception {
        //Create new table structures
        final String CREATE_NEW_TASK_TABLE = "CREATE TABLE tblTask (flngTaskID INTEGER PRIMARY KEY , flngTaskDetailID INTEGER NOT NULL DEFAULT -1, " +
                "flngTimeID INTEGER NOT NULL DEFAULT -1, fintTaskType INTEGER NOT NULL DEFAULT 0, flngTaskTypeID INTEGER NOT NULL DEFAULT -1," +
                "fdtmCreated INTEGER NOT NULL DEFAULT (strftime('%s','now')*1000), fdtmDeleted INTEGER NOT NULL DEFAULT -1, fblnOneOff INTEGER NOT NULL DEFAULT 0)";
//
        final String CREATE_NEW_TASK_DETAIL_TABLE = "CREATE TABLE tblTaskDetail (flngTaskDetailID INTEGER PRIMARY KEY, fstrTitle NOT NULL DEFAULT '', fstrDescription NOT NULL DEFAULT '')";
//
//            //Apply foreign key after tables are swapped back. FOREIGN KEY(flngTaskID) REFERENCES tblTask_NEW
        final String CREATE_NEW_TIME_TABLE = "CREATE TABLE tblTime (flngTimeID INTEGER PRIMARY KEY, fdtmFrom INTEGER NOT NULL DEFAULT -1, fdtmTo INTEGER NOT NULL DEFAULT -1, " +
                "fblnFromTime INTEGER NOT NULL DEFAULT 0, fblnToTime INTEGER NOT NULL DEFAULT 0, fblnToDate INTEGER NOT NULL DEFAULT 0, fintTimeframe INTEGER NOT NULL DEFAULT -1, " +
                "flngTimeframeID INTEGER NOT NULL DEFAULT -1, flngRepetition INTEGER NOT NULL DEFAULT -1, fdtmCreated INTEGER NOT NULL DEFAULT (strftime('%s','now')*1000), " +
                "fintStarting INTEGER NOT NULL DEFAULT 0, fdtmUpcoming INTEGER NOT NULL DEFAULT -1, fdtmPriority INTEGER NOT NULL DEFAULT -1, fblnComplete INTEGER NOT NULL DEFAULT 0)";


        final String CREATE_NEW_TASK_INSTANCE_TABLE = "CREATE TABLE tblTaskInstance(flngInstanceID INTEGER PRIMARY KEY, flngTaskID INTEGER NOT NULL DEFAULT -1," +
                "flngTaskDetailID INTEGER NOT NULL DEFAULT -1, fdtmFrom INTEGER NOT NULL DEFAULT -1, fdtmTo INTEGER NOT NULL DEFAULT -1, " +
                "fblnFromTime INTEGER NOT NULL DEFAULT 0, fblnToTime INTEGER NOT NULL DEFAULT 0, fblnToDate INTEGER NOT NULL DEFAULT 0, fdtmCreated INTEGER NOT NULL DEFAULT (strftime('%s','now')*1000), " +
                "fdtmCompleted INTEGER NOT NULL DEFAULT -1, fdtmSystemCompleted INTEGER NOT NULL DEFAULT -1, fdtmDeleted INTEGER NOT NULL DEFAULT -1, fdtmEdited INTEGER NOT NULL DEFAULT -1)";

        //Alter table names
        db.execSQL("ALTER TABLE tblTime RENAME TO tblTime_OLD");
        db.execSQL("ALTER TABLE tblTask RENAME TO tblTask_OLD");
        db.execSQL("ALTER TABLE tblTaskInstance RENAME TO tblTaskInstance_OLD");

        //Create new tables
        db.execSQL(CREATE_NEW_TASK_TABLE);
        db.execSQL(CREATE_NEW_TASK_DETAIL_TABLE);
        db.execSQL(CREATE_NEW_TASK_INSTANCE_TABLE);
        db.execSQL(CREATE_NEW_TIME_TABLE);

        //Load data into new time table structure
        Calendar calNow = Calendar.getInstance();
        Cursor tblTime_OLD = db.query("tblTime_OLD", null, null, null, null, null, null);
        while (tblTime_OLD.moveToNext()) {
            long flngDayID, flngWeekID, flngMonthID, flngYearID;
            int fintTimeframe = -1;
            long flngTimeframeID = -1;

            flngDayID = tblTime_OLD.getInt(tblTime_OLD.getColumnIndex("flngDayID"));
            flngWeekID = tblTime_OLD.getInt(tblTime_OLD.getColumnIndex("flngWeekID"));
            flngMonthID = tblTime_OLD.getInt(tblTime_OLD.getColumnIndex("flngMonthID"));
            flngYearID = tblTime_OLD.getInt(tblTime_OLD.getColumnIndex("flngYearID"));

            if (flngDayID != -1) {
                fintTimeframe = 0;
                flngTimeframeID = flngDayID;
            } else if (flngWeekID != -1) {
                fintTimeframe = 1;
                flngTimeframeID = flngWeekID;
            } else if (flngMonthID != -1) {
                fintTimeframe = 2;
                flngTimeframeID = flngMonthID;
            } else if (flngYearID != -1) {
                fintTimeframe = 3;
                flngTimeframeID = flngYearID;
            }

            db.insert("tblTime",
                    null,
                    generateContentValues(new String[]{"flngTimeID", "fdtmFrom", "fdtmTo", "fblnToDate", "fblnFromTime", "fblnToTime", "fintTimeframe", "flngTimeframeID",
                                    "flngRepetition", "fdtmCreated", "fintStarting", "fdtmUpcoming", "fdtmPriority", "fblnComplete"},
                            new Object[]{tblTime_OLD.getLong(tblTime_OLD.getColumnIndex("flngTimeID")),
                                    tblTime_OLD.getLong(tblTime_OLD.getColumnIndex("fdtmFrom")),
                                    tblTime_OLD.getLong(tblTime_OLD.getColumnIndex("fdtmTo")),
                                    false,
                                    tblTime_OLD.getLong(tblTime_OLD.getColumnIndex("fblnFromTimeSet")) == 1,
                                    tblTime_OLD.getLong(tblTime_OLD.getColumnIndex("fblnToTimeSet")) == 1,
                                    fintTimeframe,
                                    flngTimeframeID,
                                    tblTime_OLD.getLong(tblTime_OLD.getColumnIndex("flngRepetition")) == -1 ? 0 : tblTime_OLD.getLong(tblTime_OLD.getColumnIndex("flngRepetition")),
                                    tblTime_OLD.getLong(tblTime_OLD.getColumnIndex("fdtmCreated")),
                                    0,
                                    (long) -1,
                                    (long) -1,
                                    false}));
        }
        tblTime_OLD.close();

        //Load data into new task instance structure
        Cursor tblInstance = db.query("tblTaskInstance_OLD",
                null,
                null,
                null,
                null,
                null,
                null);
        Cursor tblTask_OLD;
        while (tblInstance.moveToNext()) {
            tblTask_OLD = db.query("tblTask_OLD",
                    null,
                    "flngTaskID = ?",
                    objectArrayToStringArray(new Object[]{tblInstance.getLong(tblInstance.getColumnIndex("flngTaskID"))}),
                    null,
                    null,
                    null);

            tblTask_OLD.moveToFirst();

            tblTime_OLD = db.query("tblTime_OLD",
                    null,
                    "flngTimeID = ?",
                    objectArrayToStringArray(new Object[]{tblTask_OLD.getLong(tblTask_OLD.getColumnIndex("flngTimeID"))}),
                    null,
                    null,
                    null);

            long dtmFrom = -1, dtmTo = -1;
            boolean blnFromTime = false, blntToTime = false;
            if (tblTime_OLD.moveToFirst()) {
                dtmFrom = tblTime_OLD.getLong(tblTime_OLD.getColumnIndex("fdtmFrom"));
                dtmTo = tblTime_OLD.getLong(tblTime_OLD.getColumnIndex("fdtmTo"));
                blnFromTime = tblTime_OLD.getLong(tblTime_OLD.getColumnIndex("fblnFromTimeSet")) == 1;
                blntToTime = tblTime_OLD.getLong(tblTime_OLD.getColumnIndex("fblnToTimeSet")) == 1;
            }

            Long complete = (long) -1;
            Long sysComplete = (long) -1;
            if (tblInstance.getLong(tblInstance.getColumnIndex("fblnComplete")) == 1) {
                complete = calNow.getTimeInMillis();
            } else if (tblInstance.getLong(tblInstance.getColumnIndex("fblnSystemComplete")) == 1) {
                sysComplete = calNow.getTimeInMillis();
            }

            db.insert("tblTaskInstance",
                    null,
                    generateContentValues(new String[]{"flngInstanceID", "flngTaskID", "flngTaskDetailID", "fdtmFrom", "fdtmTo", "fblnFromTime", "fblnToTime", "fblnToDate", "fdtmCreated",
                                    "fdtmCompleted", "fdtmSystemCompleted", "fdtmDeleted", "fdtmEdited"},
                            new Object[]{tblInstance.getLong(tblInstance.getColumnIndex("flngInstanceID")),
                                    tblInstance.getLong(tblInstance.getColumnIndex("flngTaskID")),
                                    addRecordToTable("tblTaskDetail",
                                            new String[]{"fstrTitle", "fstrDescription"},
                                            new Object[]{tblTask_OLD.getString(tblTask_OLD.getColumnIndex("fstrTitle")),
                                                    tblTask_OLD.getString(tblTask_OLD.getColumnIndex("fstrDescription"))}),
                                    dtmFrom,
                                    dtmTo,
                                    blnFromTime,
                                    blntToTime,
                                    false,
                                    tblInstance.getLong(tblInstance.getColumnIndex("fdtmCreated")),
                                    complete,
                                    sysComplete,
                                    (long) -1,
                                    (long) -1}));
            tblTask_OLD.close();
            tblTime_OLD.close();
        }
        tblInstance.close();

        //Update Task table
        tblTask_OLD = db.query("tblTask_OLD", null, null, null, null, null, null);
        Cursor tblSession;
        while (tblTask_OLD.moveToNext()) {
            //Create task details
            String strTitle = tblTask_OLD.getString(tblTask_OLD.getColumnIndex("fstrTitle"));
            String strDescription = tblTask_OLD.getString(tblTask_OLD.getColumnIndex("fstrDescription"));
            long lngTaskID = tblTask_OLD.getLong(tblTask_OLD.getColumnIndex("flngTaskID"));
            long lngDetailID = addRecordToTable("tblTaskDetail",
                    new String[]{"fstrTitle", "fstrDescription"},
                    new Object[]{strTitle, strDescription});

            //Change tasks associated w/ sessions to being associated w/ times
            long lngSession = tblTask_OLD.getLong((tblTask_OLD.getColumnIndex("flngSessionID")));
            tblSession = db.query("tblSession",
                    null,
                    "flngSessionID = ?",
                    objectArrayToStringArray(new Object[]{lngSession}),
                    null,
                    null,
                    null);
            long lngTimeID = tblTask_OLD.getLong((tblTask_OLD.getColumnIndex("flngTimeID")));
            if (tblSession.moveToFirst()) {
                //create new time record matching session time record
                lngTimeID = tblSession.getLong(tblSession.getColumnIndex("flngTimeID"));
                tblSession.close();
            }

            //add all data to new table design
            int fintTaskType = 0;
            long flngTaskTypeID = -1;
            if (tblTask_OLD.getLong(tblTask_OLD.getColumnIndex("flngEventID")) != -1) {
                fintTaskType = 1;
                flngTaskTypeID = tblTask_OLD.getLong(tblTask_OLD.getColumnIndex("flngEventID"));
            } else if (tblTask_OLD.getLong(tblTask_OLD.getColumnIndex("flngLongTermID")) != -1) {
                fintTaskType = 2;
                flngTaskTypeID = tblTask_OLD.getLong(tblTask_OLD.getColumnIndex("flngLongTermID"));
            } else if (tblTask_OLD.getLong(tblTask_OLD.getColumnIndex("flngGroupID")) != -1) {
                fintTaskType = 3;
                flngTaskTypeID = tblTask_OLD.getLong(tblTask_OLD.getColumnIndex("flngGroupID"));
            }
            if (fintTaskType == 1 || fintTaskType == 2) {
                tblTime_OLD = db.query("tblTime_OLD",
                        null,
                        "flngTimeID = ?",
                        objectArrayToStringArray(new Object[]{lngTimeID}),
                        null,
                        null,
                        null);
                //Remove Time ID if time is not used and part of event or longterm
                if (tblTime_OLD.moveToFirst()) {
                    //Neither date nor time
                    if (tblTime_OLD.getLong(tblTime_OLD.getColumnIndex("fdtmFromDate")) == -1 &&
                            tblTime_OLD.getLong(tblTime_OLD.getColumnIndex("fdtmFrom")) == -1) {
                        lngTimeID = -1;
                    }
                }
                tblTime_OLD.close();
            }

            Calendar dtmCreated = Calendar.getInstance();
            dtmCreated.add(Calendar.DAY_OF_YEAR, -2);
            long dtmDelete = -1;
            if (tblTask_OLD.getLong(tblTask_OLD.getColumnIndex("fblnActive")) == 0) {
                dtmDelete = dtmCreated.getTimeInMillis();
            }

            db.insert("tblTask",
                    null,
                    generateContentValues(new String[]{"flngTaskID", "flngTaskDetailID", "flngTimeID", "fintTaskType", "flngTaskTypeID", "fdtmCreated", "fdtmDeleted", "fblnOneOff"},
                            new Object[]{tblTask_OLD.getLong(tblTask_OLD.getColumnIndex("flngTaskID")),
                                    lngDetailID,
                                    lngTimeID,
                                    fintTaskType,
                                    flngTaskTypeID,
                                    dtmCreated.getTimeInMillis(),
                                    dtmDelete,
                                    tblTask_OLD.getLong(tblTask_OLD.getColumnIndex("fblnOneOff")) == 1}));
        }

        Cursor timeCursor = db.query("tblTime",
                null,
                null,
                null,
                null,
                null,
                null);

        Cursor tblTask;
        long rowNum = 0;
        while (timeCursor.moveToNext()) {
            //if there is no timeframe ID then its some sort of one time deal
            rowNum++;
            if (timeCursor.getLong(timeCursor.getColumnIndex("flngTimeframeID")) == -1) {
                tblTask = db.query("tblTask",
                        null,
                        "flngTimeID = ?",
                        objectArrayToStringArray(new Object[]{timeCursor.getLong(timeCursor.getColumnIndex("flngTimeID"))}),
                        null,
                        null,
                        null);
                //if task exists associated to time
                if (tblTask.moveToFirst()) {
                    tblInstance = db.query("tblTaskInstance_OLD",
                            null,
                            "flngTaskID = ?",
                            new String[]{Long.toString(tblTask.getLong(tblTask.getColumnIndex("flngTaskID")))},
                            null,
                            null,
                            null);
                    //if time exists associated to task, then we know that the instance has already been created. Complete the time.
                    if (tblInstance.moveToFirst()) {
                        db.update("tblTime",
                                generateContentValues(new String[]{"fblnComplete"},
                                        new Object[]{true}),
                                "flngTimeID = ?",
                                objectArrayToStringArray(new Object[]{timeCursor.getLong(timeCursor.getColumnIndex("flngTimeID"))}));
                    } else {
                        //Utilities.Companion.ReEvaluateTimeDetails(timeCursor.getLong(timeCursor.getColumnIndex("flngTimeID")));
                    }
                    tblInstance.close();
                } else {
                    //Not sure what to do here. Don't think this should occur. But if there's not task associated to the time then I guess we'll have bigger problems so mark it to compelte.
                    db.update("tblTime",
                            generateContentValues(new String[]{"fblnComplete"},
                                    new Object[]{true}),
                            "flngTimeID = ?",
                            objectArrayToStringArray(new Object[]{timeCursor.getLong(timeCursor.getColumnIndex("flngTimeID"))}));
                }
                tblTask.close();
            } else { //Some sort of repeating time element
                //Utilities.Companion.ReEvaluateTimeDetails(timeCursor.getLong(timeCursor.getColumnIndex("flngTimeID")));
            }
        }
        timeCursor.close();

        db.execSQL("DROP TABLE tblTime_OLD;");

        db.execSQL("DROP TABLE tblTaskInstance_OLD;");

        db.execSQL("DROP TABLE tblTask_OLD;");
    }

    private void upgradeToV13(SQLiteDatabase db) throws Exception {
        Cursor tblTime = db.query("tblTime",
                null,
                null,
                null,
                null,
                null,
                null);
        while (tblTime.moveToNext()) {
            boolean blnComplete = false;
            boolean blnRemove = false;
            long lngTimeID = tblTime.getLong(tblTime.getColumnIndex("flngTimeID"));
            Cursor tblSession = db.query("tblSession",
                    null,
                    "flngTimeID = ?",
                    objectArrayToStringArray(new Object[]{lngTimeID}),
                    null,
                    null,
                    null);
            //If not a session, then there's a chance it needs to be completed.
            if (!tblSession.moveToNext()) {
                blnComplete = true;
            }
            tblSession.close();

            Cursor tblTask = db.query("tblTask",
                    null,
                    "flngTimeID = ?",
                    objectArrayToStringArray(new Object[]{lngTimeID}),
                    null,
                    null,
                    null);
            while (tblTask.moveToNext()) {
                //if there's a task that's not yet deleted then don't remove the time.
                if (tblTask.getLong(tblTask.getColumnIndex("fdtmDeleted")) == -1) {
                    blnComplete = false;
                }
            }
            //Times that just seem to exist for no reason
            if (blnComplete = false && tblTask.getCount() == 0) {
                blnRemove = true;
            }
            tblTask.close();

            if (blnRemove) {
                if (lngTimeID >= 1000) {
                    throw new Exception("Jumped out of debug");
                }
                db.delete("tblTime",
                        "flngTimeID = ?",
                        objectArrayToStringArray(new Object[]{lngTimeID}));
            } else if (blnComplete) {
                if (lngTimeID >= 1000) {
                    throw new Exception("Jumped out of debug");
                }
                db.update("tblTime",
                        generateContentValues(new String[]{"fblnComplete"},
                                new Object[]{true}),
                        "flngTimeID = ?",
                        objectArrayToStringArray(new Object[]{lngTimeID}));
            }
        }
    }

    private void upgradeToV14(SQLiteDatabase db) throws Exception {
        Cursor tblTime = db.query("tblTime",
                null,
                null,
                null,
                null,
                null,
                null);
        while (tblTime.moveToNext()) {
            boolean blnComplete = false;
            boolean blnRemove = false;
            long lngTimeID = tblTime.getLong(tblTime.getColumnIndex("flngTimeID"));
            Cursor tblSession = db.query("tblSession",
                    null,
                    "flngTimeID = ?",
                    objectArrayToStringArray(new Object[]{lngTimeID}),
                    null,
                    null,
                    null);
            //If not a session, then there's a chance it needs to be completed.
            if (!tblSession.moveToNext()) {
                blnComplete = true;
            }
            tblSession.close();

            Cursor tblTask = db.query("tblTask",
                    null,
                    "flngTimeID = ?",
                    objectArrayToStringArray(new Object[]{lngTimeID}),
                    null,
                    null,
                    null);
            while (tblTask.moveToNext()) {
                //if there's a task that's not yet deleted then don't remove the time.
                if (tblTask.getLong(tblTask.getColumnIndex("fdtmDeleted")) == -1) {
                    blnComplete = false;
                }
            }
            //Times that just seem to exist for no reason
            if (blnComplete == false && tblTask.getCount() == 0) {
                blnRemove = true;
            }
            tblTask.close();

            if (tblTime.getLong(tblTime.getColumnIndex("fdtmPriority")) == -1) {
                blnComplete = true;
            }

            if (blnRemove) {
                if (lngTimeID >= 1000) {
                    throw new Exception("Jumped out of debug");
                }
                db.delete("tblTime",
                        "flngTimeID = ?",
                        objectArrayToStringArray(new Object[]{lngTimeID}));
            } else if (blnComplete) {
                if (lngTimeID >= 1000) {
                    throw new Exception("Jumped out of debug");
                }
                db.update("tblTime",
                        generateContentValues(new String[]{"fblnComplete"},
                                new Object[]{true}),
                        "flngTimeID = ?",
                        objectArrayToStringArray(new Object[]{lngTimeID}));
            }
        }
    }

    private void upgradeToV15(SQLiteDatabase db) throws Exception {

        int i = 1;
        addColumn(db, "tblTask",
                "flngOneOff",
                returnColumnPosition(db, "tblTask", "fblnOneOff"), true, "-1");

        //Creates new one off field w/ new time ids where one offs previously had old time IDs
        Cursor tblTime = db.query("tblTime",
                null,
                null,
                null,
                null,
                null,
                null);
        while (tblTime.moveToNext()) {
            long lngTimeID = tblTime.getLong(tblTime.getColumnIndex("flngTimeID"));
            Cursor tblTask = db.query("tblTask",
                    null,
                    "flngTimeID = ?",
                    objectArrayToStringArray(new Object[]{lngTimeID}),
                    null,
                    null,
                    null);
            while (tblTask.moveToNext()) {
                if (tblTask.getLong(tblTask.getColumnIndex("fblnOneOff")) == 1) {
                    Cursor tblInstance = db.query("tblTaskInstance",
                            null,
                            "flngTaskID = ?",
                            objectArrayToStringArray(new Object[]{tblTask.getLong(tblTask.getColumnIndex("flngTaskID"))}),
                            null,
                            null,
                            null);
                    long lngNewTimeID = -1;
                    if (tblInstance.moveToFirst()) {
                        lngNewTimeID = db.insert("tblTime",
                                null,
                                generateContentValues(new String[]{"fdtmFrom", "fdtmTo", "fblnToDate", "fblnFromTime", "fblnToTime", "fintTimeframe", "flngTimeframeID",
                                                "flngRepetition", "fdtmCreated", "fintStarting", "fdtmUpcoming", "fdtmPriority", "fblnComplete"},
                                        new Object[]{tblTime.getLong(tblTime.getColumnIndex("fdtmFrom")),
                                                tblTime.getLong(tblTime.getColumnIndex("fdtmTo")),
                                                false,
                                                tblTime.getLong(tblTime.getColumnIndex("fblnFromTime")) == 1,
                                                tblTime.getLong(tblTime.getColumnIndex("fblnToTime")) == 1,
                                                -1,
                                                (long) -1,
                                                (long) 0,
                                                tblTime.getLong(tblTime.getColumnIndex("fdtmCreated")),
                                                0,
                                                (long) -1,
                                                (long) -1,
                                                true}));
                    } else {
                        lngNewTimeID = db.insert("tblTime",
                                null,
                                generateContentValues(new String[]{"fdtmFrom", "fdtmTo", "fblnToDate", "fblnFromTime", "fblnToTime", "fintTimeframe", "flngTimeframeID",
                                                "flngRepetition", "fdtmCreated", "fintStarting", "fdtmUpcoming", "fdtmPriority", "fblnComplete"},
                                        new Object[]{tblTime.getLong(tblTime.getColumnIndex("fdtmPriority")),
                                                tblTime.getLong(tblTime.getColumnIndex("fdtmPriority")),
                                                false,
                                                tblTime.getLong(tblTime.getColumnIndex("fblnFromTimeSet")) == 1,
                                                tblTime.getLong(tblTime.getColumnIndex("fblnToTimeSet")) == 1,
                                                -1,
                                                (long) -1,
                                                (long) 0,
                                                tblTime.getLong(tblTime.getColumnIndex("fdtmCreated")),
                                                0,
                                                (long) -1,
                                                (long) -1,
                                                false}));
                    }

                    if (lngTimeID >= 1000) {
                        throw new Exception("test");
                    }

                    updateRecordFromTable("tblTask", "flngTaskID", tblTask.getLong(tblTask.getColumnIndex("flngTaskID"))
                            , new String[]{"flngTimeID", "flngOneOff"}
                            , new Object[]{lngNewTimeID, lngTimeID});

                    tblInstance.close();
                }

            }
            tblTask.close();
        }
        tblTime.close();

        //Removes time details where time ID doesn't exist
//                tblTask = db.query("tblTask",
//                        null,
//                        null,
//                        null,
//                        null,
//                        null,
//                        null);
//                while(tblTask.moveToNext()){
//                    tblTime = db.query("tblTime",
//                            null,
//                            "flngTimeID = ?",
//                            objectArrayToStringArray(new Object[] {tblTask.getLong(tblTask.getColumnIndex("flngTimeID"))}),
//                            null,
//                            null,
//                            null);
//                    if(!tblTime.moveToFirst()){
//                        db.update("tblTask",
//                                generateContentValues(new String[]{"flngTimeID"},
//                                        new Object[]{(long)-1}),
//                                "flngTaskID = ?",
//                                objectArrayToStringArray(new Object[] {tblTask.getLong(tblTask.getColumnIndex("flngTaskID"))}));
//                    }
//                    tblTime.close();
//                }
//                tblTask.close();

        //Removes times not tied to anything
        tblTime = db.query("tblTime",
                null,
                null,
                null,
                null,
                null,
                null);
        while (tblTime.moveToNext()) {
            long lngTimeID = tblTime.getLong(tblTime.getColumnIndex("flngTimeID"));
            Cursor tblTask = db.query("tblTask",
                    null,
                    "flngTimeID = ?",
                    objectArrayToStringArray(new Object[]{lngTimeID}),
                    null,
                    null,
                    null);
            if (!tblTask.moveToFirst()) {
                Cursor tblSession = db.query("tblSession",
                        null,
                        "flngTimeID = ?",
                        objectArrayToStringArray(new Object[]{lngTimeID}),
                        null,
                        null,
                        null);
                if (!tblSession.moveToFirst()) {
                    if (lngTimeID >= 1000) {
                        throw new Exception("test");
                    }
                    db.delete("tblTime",
                            "flngTimeID = ?",
                            objectArrayToStringArray(new Object[]{lngTimeID}));
                }
                tblSession.close();
            }
            tblTask.close();
        }
        tblTime.close();

        deleteColumn(db, "tblTask", returnColumnPosition(db, "tblTask", "fblnOneOff"));

    }

    private void upgradeToV16(SQLiteDatabase db) throws Exception {
        db.execSQL(CREATE_TIME_INSTANCE_TABLE);

        Cursor tblTime = getRecordsFromTable("tblTime");
        while (tblTime.moveToNext()) {
            addRecordToTable("tblTimeInstance",
                    new String[]{"flngTimeID", "fdtmUpcoming", "fdtmPriority"},
                    new Object[]{tblTime.getLong(tblTime.getColumnIndex("flngTimeID")),
                            tblTime.getLong(tblTime.getColumnIndex("fdtmUpcoming")),
                            tblTime.getLong(tblTime.getColumnIndex("fdtmPriority"))});
        }
        tblTime.close();

        deleteColumn(db, "tblTime", "fdtmUpcoming");
        deleteColumn(db, "tblTime", "fdtmPriority");
    }

    private void upgradeToV17(SQLiteDatabase db) throws Exception {
        addColumn(db,
                "tblTime",
                "flngGenerationID",
                99,
                true,
                "-1");


    }

    private void upgradeToV18(SQLiteDatabase db) throws Exception {
        db.update("tblTaskInstance",
                generateContentValues(new String[]{"fdtmCompleted"}, new Object[]{Utilities.Companion.getCurrentCalendar().getTimeInMillis()}),
                "fdtmSystemCompleted = 1554830241995",
                null);
    }

    private void upgradeToV19(SQLiteDatabase db) throws Exception {
        addColumn(db, "tblTime", "fblnThru", 99, true, "0");
        addColumn(db, "tblTimeGeneration", "fintThru", 99, true, "0");
        db.execSQL("ALTER TABLE tblTimeGeneration RENAME TO tblTimeInstance");
    }

    private void upgradeToV20(SQLiteDatabase db) throws Exception {
        addColumn(db, "tblTime", "fblnSession", 99, true, "0");
        addColumn(db, "tblTime", "flngSessionDetailID", 99, true, "-1");

        Cursor session = getRecordsFromTable("tblSession");

        while (session.moveToNext()) {
            Time tblTime = Time.getInstance(session.getLong(session.getColumnIndex("flngTimeID")));
            tblTime.setAsSession(session.getString(session.getColumnIndex("fstrTitle")));
        }

        db.execSQL(DROP_SESSION_TABLE);

        addColumn(db, "tblTaskInstance", "flngSessionDetailID", 99, true, "-1");
    }

    private void upgradeToV21(SQLiteDatabase db) throws Exception {
        boolean blnFail = false;

        addColumn(db, "tblTime", "fstrTitle", 99, true, "''");
        addColumn(db, "tblTaskInstance", "flngSessionID", 99, true, "-1");
        Cursor tblTime = getRecordsFromTable("tblTime", "fblnSession = 1", null);

        while (tblTime.moveToNext()) {
            Cursor td = getRecordsFromTable("tblTaskDetail", "flngTaskDetailID"
                    , tblTime.getLong(tblTime.getColumnIndex("flngSessionDetailID")));

            td.moveToFirst();

            //Replace time session IDs w/ Titles
            updateRecordFromTable("tblTime", "flngTimeID",
                    tblTime.getLong(tblTime.getColumnIndex("flngTimeID")),
                    new String[]{"fstrTitle"}, new Object[]{td.getString(td.getColumnIndex("fstrTitle"))});

            //Replace instance session ID's w/ Time ID's
            updateRecordFromTable("tblTaskInstance", "flngSessionDetailID",
                    tblTime.getLong(tblTime.getColumnIndex("flngSessionDetailID")),
                    new String[]{"flngSessionID"}, new Object[]{tblTime.getLong(tblTime.getColumnIndex("flngTimeID"))});

            //Delete the unnecessary session detail ID
            deleteRecordFromTable("tblTaskDetail", "flngTaskDetailID",
                    tblTime.getLong(tblTime.getColumnIndex("flngSessionDetailID")));
        }
        tblTime.close();

        deleteColumn(db, "tblTime", "flngSessionDetailID");
        deleteColumn(db, "tblTaskInstance", "flngSessionDetailID");

        if (blnFail) {
            throw new Exception("Exit and Rollback Debug");
        }
    }

    private void addColumn(SQLiteDatabase db,
                           String pstrTableName,
                           String pstrColumnName,
                           long pintPosition,
                           boolean pblnNotNull,
                           String pstrDefault) throws Exception {
        String strTempTable = pstrTableName + "_Temp";

        //Gets Create Statement for the Specific Table, updates it to be _Temp, then executes the updated create script
        Cursor cursor = db.rawQuery("SELECT sql FROM sqlite_master WHERE name='" + pstrTableName + "';", new String[]{});
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
        cursor = db.rawQuery("PRAGMA table_info(" + strTempTable + ")", new String[]{});
        long intCounter = 0;
        while (cursor.moveToNext()) {
            if (!strCreateStatement.equals("")) {
                strCreateStatement += ", ";
            }
            if (pintPosition == intCounter) {
                strCreateStatement += pstrColumnName + " ";
                String strColumnType = "";
                switch (pstrColumnName.substring(1, 4)) {
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
                    case "long":
                        strColumnType = "INTEGER";
                        break;
                }
                strCreateStatement += strColumnType + " ";
                if (pblnNotNull == true) {
                    strCreateStatement += "NOT NULL ";
                }
                if (pstrDefault == "") {
                    throw new Exception("Default Must Be Provided");
                }
                strCreateStatement += "DEFAULT " + pstrDefault + ", ";
            }
            strCreateStatement += cursor.getString(cursor.getColumnIndex("name")) + " ";
            strCreateStatement += cursor.getString(cursor.getColumnIndex("type")) + " ";
            if (cursor.getInt(cursor.getColumnIndex("notnull")) == 1) {
                strCreateStatement += "NOT NULL ";
            }
            if (cursor.getString(cursor.getColumnIndex("dflt_value")) != null) {
                //Hard coded because for some reason it doesn't keep the required parenthesis
                if (cursor.getString(cursor.getColumnIndex("dflt_value")).equals("strftime('%s','now')*1000")) {
                    strCreateStatement += "DEFAULT (strftime('%s','now')*1000) ";
                } else {
                    strCreateStatement += "DEFAULT " + cursor.getString(cursor.getColumnIndex("dflt_value")) + " ";
                }
            }
            if (cursor.getInt(cursor.getColumnIndex("pk")) == 1) {
                strCreateStatement += "PRIMARY KEY ";
            }
            intCounter += 1;
        }
        //if the counter was higher than the table columns, add to the end.
        if (pintPosition >= intCounter) {
            strCreateStatement += ", ";
            strCreateStatement += pstrColumnName + " ";
            if (pblnNotNull == true) {
                strCreateStatement += "NOT NULL ";
            }
            if (pstrDefault != "") {
                strCreateStatement += "DEFAULT " + pstrDefault;
            }
        }
        db.execSQL("CREATE TABLE " + pstrTableName + " (" + strCreateStatement + ")");

        //Generates a list of the temp tables fields then creates and executes the insert into new table statement
        cursor = db.rawQuery("PRAGMA table_info(" + strTempTable + ")", new String[]{});
        String strOrigColumns = "";
        while (cursor.moveToNext()) {
            if (strOrigColumns != "") {
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
                              String pstrColumnName) throws Exception {
        deleteColumn(db, pstrTableName, returnColumnPosition(db, pstrTableName, pstrColumnName));
    }

    private void deleteColumn(SQLiteDatabase db,
                              String pstrTableName,
                              long pintPosition) throws Exception {

        String strTempTable = pstrTableName + "_Temp";

        //Gets Create Statement for the Specific Table, updates it to be _Temp, then executes the updated create script
        Cursor cursor = db.rawQuery("SELECT sql FROM sqlite_master WHERE name='" + pstrTableName + "';", new String[]{});
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
        cursor = db.rawQuery("PRAGMA table_info(" + strTempTable + ")", new String[]{});
        long intCounter = 0;
        while (cursor.moveToNext()) {
            if (pintPosition != intCounter) {
                if (strCreateStatement != "") {
                    strCreateStatement += ", ";
                }
                strCreateStatement += cursor.getString(cursor.getColumnIndex("name")) + " ";
                strCreateStatement += cursor.getString(cursor.getColumnIndex("type")) + " ";
                if (cursor.getInt(cursor.getColumnIndex("notnull")) == 1) {
                    strCreateStatement += "NOT NULL ";
                }
                if (cursor.getString(cursor.getColumnIndex("dflt_value")) != null) {
                    if (cursor.getString(cursor.getColumnIndex("dflt_value")).equals("strftime('%s','now')*1000")) {
                        strCreateStatement += "DEFAULT (strftime('%s','now')*1000) ";
                    } else {
                        strCreateStatement += "DEFAULT " + cursor.getString(cursor.getColumnIndex("dflt_value")) + " ";
                    }
                }
                if (cursor.getInt(cursor.getColumnIndex("pk")) == 1) {
                    strCreateStatement += "PRIMARY KEY ";
                }
            }
            intCounter += 1;
        }
        db.execSQL("CREATE TABLE " + pstrTableName + " (" + strCreateStatement + ")");

        //Generates a list of the temp tables fields then creates and executes the insert into new table statement
        cursor = db.rawQuery("PRAGMA table_info(" + strTempTable + ")", new String[]{});
        String strOrigColumns = "";
        intCounter = 0;
        while (cursor.moveToNext()) {
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
                              boolean pblnPrimary) throws Exception {

        String strTempTable = pstrTableName + "_Temp";

        //Gets Create Statement for the Specific Table, updates it to be _Temp, then executes the updated create script
        Cursor cursor = db.rawQuery("SELECT sql FROM sqlite_master WHERE name='" + pstrTableName + "';", new String[]{});
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
        cursor = db.rawQuery("PRAGMA table_info(" + strTempTable + ")", new String[]{});
        while (cursor.moveToNext()) {
            String strCurrentColName = cursor.getString(cursor.getColumnIndex("name"));
            if (strCreateStatement != "") {
                strCreateStatement += ", ";
            }
            if (strCurrentColName.equals(pstrOrigColumnName)) {
                if (!pblnPrimary) {
                    strCreateStatement += pstrNewColumnName + " ";
                    strCreateStatement += cursor.getString(cursor.getColumnIndex("type")) + " ";
                    if (pblnNotNull == true) {
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
                    if (cursor.getString(cursor.getColumnIndex("dflt_value")).equals("strftime('%s','now')*1000")) {
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

    private long returnColumnPosition(SQLiteDatabase db,
                                      String pstrTableName,
                                      String pstrColumnName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + pstrTableName + ")", new String[]{});
        long position = 0;
        while (cursor.moveToNext()) {
            String strCurrentColName = cursor.getString(cursor.getColumnIndex("name"));
            if (strCurrentColName.equals(pstrColumnName)) return position;
            position++;
        }
        return -1;
    }

    private long retrieveTableLength(SQLiteDatabase db,
                                     String pstrTable) {
        long length = 0;
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

    private static ContentValues populateCurrentTableValues(Cursor pCursor) {
        ContentValues values = new ContentValues();
        for (int i = 0; i < pCursor.getColumnCount(); i++) {
            String columnName = pCursor.getColumnName(i);
            switch (columnName.substring(1, 4)) {
                case "lng":
                    values.put(columnName, pCursor.getLong(i));
                    break;
                case "dtm":
                    values.put(columnName, pCursor.getLong(i));
                    break;
                case "bln":
                    values.put(columnName, pCursor.getLong(i));
                    break;
                case "str":
                    values.put(columnName, pCursor.getString(i));
                    break;
                case "long":
                    values.put(columnName, pCursor.getLong(i));
                    break;
            }
        }
        return values;
    }

    //region GENERIC FUNCTIONS
    public static Cursor getRecordsFromTable(String pstrTableName,
                                             String pstrIDColumn,
                                             long plngID) {
        String selection = pstrIDColumn + " = ?";
        String[] selectionArgs = {Long.toString(plngID)};

        return oldDatabase.query(pstrTableName,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);
    }

    public static Cursor getRecordsFromTable(String pstrTableName,
                                             String pstrIDColumn,
                                             Long plngID,
                                             String pstrOrderBy) {
        String selection = pstrIDColumn + " = ?";
        String[] selectionArgs = {Long.toString(plngID)};

        return oldDatabase.query(pstrTableName,
                null,
                selection,
                selectionArgs,
                null,
                null,
                pstrOrderBy);
    }

    public static Cursor getRecordsFromTable(String pstrTableName) {
        return oldDatabase.query(pstrTableName,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public static Cursor getRecordsFromTable(String pstrTableName,
                                             String pstrSelection,
                                             Object[] pobjArguemnts) {

        return oldDatabase.query(pstrTableName,
                null,
                pstrSelection,
                objectArrayToStringArray(pobjArguemnts),
                null,
                null,
                null);
    }

    //wound up not working as intended
    public static String[] objectArrayToStringArray(Object[] pobjArray) {
        if (pobjArray != null) {
            String[] strArray = new String[pobjArray.length];
            for (int i = 0; i < pobjArray.length; i++) {
                strArray[i] = pobjArray[i].toString();
            }
            return strArray;
        }
        return new String[]{};
    }

    public static ContentValues generateContentValues(String[] pstrColumns,
                                                      Object[] pstrValues) {
        ContentValues values = new ContentValues();
        for (int i = 0; i < pstrColumns.length; i++) {
            if (pstrValues[i] != null) {
                switch (pstrColumns[i].substring(1, 4)) {
                    case "lng":
                        values.put(pstrColumns[i], (long) pstrValues[i]);
                        break;
                    case "dtm":
                        values.put(pstrColumns[i], (long) pstrValues[i]);
                        break;
                    case "bln":
                        values.put(pstrColumns[i], ((boolean) pstrValues[i]) ? 1 : 0);
                        break;
                    case "str":
                        values.put(pstrColumns[i], (String) pstrValues[i]);
                        break;
                    case "int":
                        values.put(pstrColumns[i], ((Integer) pstrValues[i]).longValue());
                        break;
                }
            }
        }
        return values;
    }

    public static long addRecordToTable(String pstrTableName,
                                        String[] pstrAddColumns,
                                        Object[] pobjAddValues) {

        return addRecordToTable(pstrTableName, pstrAddColumns, pobjAddValues, "", -1);
    }

    public static long addRecordToTable(String pstrTableName,
                                        String[] pstrAddColumns,
                                        Object[] pobjAddValues,
                                        String pstrColumnID,
                                        long plngID) {

        boolean blnUpdate = pstrColumnID != "" && plngID != -1;

        ContentValues values = generateContentValues(pstrAddColumns, pobjAddValues);

        if (blnUpdate) {
            values.put(pstrColumnID, plngID);
            deleteRecordFromTable(pstrTableName, pstrColumnID, plngID);
        }

        if (values.size() >= 1) {
            return oldDatabase.insertOrThrow(pstrTableName,
                    null,
                    values);
        } else {
            return oldDatabase.insertOrThrow(pstrTableName,
                    pstrColumnID,
                    values);
        }
    }

    public static long updateRecordFromTable(String pstrTableName,
                                             String pstrIDColumn,
                                             long plngID,
                                             String[] pstrUpdateColumns,
                                             Object[] pobjUpdateValues) {

        ContentValues values = generateContentValues(pstrUpdateColumns, pobjUpdateValues);

        String selection = pstrIDColumn + " = ?";
        String[] selectionArgs = {Long.toString(plngID)};

        return oldDatabase.update(pstrTableName,
                values,
                selection,
                selectionArgs);
    }

    public static long deleteRecordFromTable(String pstrTableName,
                                             String pstrIDColumn,
                                             Long plngID) {
        String whereClause = pstrIDColumn + " = ?";
        String[] whereArgs = {Long.toString(plngID)};

        return oldDatabase.delete(pstrTableName,
                whereClause,
                whereArgs);
    }

}