package com.deviousindustries.testtask.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.deviousindustries.testtask.classes.*

@Database(entities = arrayOf(Task::class, TaskInstance::class, TaskDetail::class, Time::class, TimeInstance::class, LongTerm::class, Group::class, Event::class, Day::class, Week::class, Month::class, Year::class),
        version = 25,
        exportSchema = false)
abstract class TaskDatabase: RoomDatabase() {
    abstract val taskDatabaseDao: TaskDatabaseDao

    companion object {
        private var INSTANCE: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase{
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            TaskDatabase::class.java,
                            "TaskDatabase.db"
                    )
                            .addMigrations(MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_25)
                            .allowMainThreadQueries()
                            .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

        //region Migrations
        val MIGRATION_21_22 = object : Migration(21,22){
            override fun migrate(database: SupportSQLiteDatabase){
                updateColumn(database, "tblTask", "flngTaskID", "flngTaskID", true, "-1", true)
                updateColumn(database, "tblTimeInstance", "flngGenerationID", "flngGenerationID", true, "-1", true)
                updateColumn(database, "tblTimeInstance", "fintThru", "fintThru", true, "0", false, "INTEGER")
                updateColumn(database, "tblTime", "flngTimeID", "flngTimeID", true, "-1", true)
                updateColumn(database, "tblTime", "flngGenerationID", "flngGenerationID", true, "-1", false, "INTEGER")
                updateColumn(database, "tblTime", "fblnThru", "fblnThru", true, "0", false, "INTEGER")
                updateColumn(database, "tblTime", "fstrTitle", "fstrTitle", true, "''", false, "TEXT")
                updateColumn(database, "tblTime", "fblnSession", "fblnSession", true, "0", false, "INTEGER")
                updateColumn(database, "tblTaskInstance", "flngInstanceID", "flngInstanceID", true, "-1", true)
                updateColumn(database, "tblTaskInstance", "flngSessionID", "flngSessionID", true, "-1", false, "INTEGER")
                remakeTable(database,
                        "CREATE TABLE tblTaskDetail (flngTaskDetailID INTEGER NOT NULL DEFAULT -1 PRIMARY KEY, fstrTitle TEXT NOT NULL DEFAULT '', fstrDescription TEXT NOT NULL DEFAULT '')",
                        "tblTaskDetail")
                updateColumn(database, "tblLongTerm", "flngLongTermID", "flngLongTermID", true, "-1", true)
                updateColumn(database, "tblLongTerm", "fstrTitle", "fstrTitle", true, "''", false)
                updateColumn(database, "tblLongTerm", "fstrDescription", "fstrDescription", true, "''", false)
                updateColumn(database, "tblGroup", "flngGroupID", "flngGroupID", true, "-1", true)
                updateColumn(database, "tblGroup", "fstrTitle", "fstrTitle", true, "''", false)
                updateColumn(database, "tblEvent", "flngEventID", "flngEventID", true, "-1", true)
                updateColumn(database, "tblEvent", "fstrTitle", "fstrTitle", true, "''", false)
                updateColumn(database, "tblEvent", "fstrDescription", "fstrDescription", true, "''", false)
                updateColumn(database, "tblDay", "flngDayID", "flngDayID", true, "-1", true)
                deleteColumn(database, "tblDay", "fdtmFromDate")
                deleteColumn(database, "tblDay", "fdtmToDate")
                updateColumn(database, "tblWeek", "flngWeekID", "flngWeekID", true, "-1", true)
                updateColumn(database, "tblWeek", "fblnMonday", "fblnMonday", true, "0", false)
                updateColumn(database, "tblWeek", "fblnTuesday", "fblnTuesday", true, "0", false)
                updateColumn(database, "tblWeek", "fblnWednesday", "fblnWednesday", true, "0", false)
                updateColumn(database, "tblWeek", "fblnThursday", "fblnThursday", true, "0", false)
                updateColumn(database, "tblWeek", "fblnFriday", "fblnFriday", true, "0", false)
                updateColumn(database, "tblWeek", "fblnSaturday", "fblnSaturday", true, "0", false)
                updateColumn(database, "tblWeek", "fblnSunday", "fblnSunday", true, "0", false)
                updateColumn(database, "tblMonth", "flngMonthID", "flngMonthID", true, "-1", true)
                updateColumn(database, "tblMonth", "fblnFirst", "fblnFirst", true, "0", false)
                updateColumn(database, "tblMonth", "fblnMiddle", "fblnMiddle", true, "0", false)
                updateColumn(database, "tblMonth", "fblnLast", "fblnLast", true, "0", false)
                updateColumn(database, "tblMonth", "fblnAfterWkn", "fblnAfterWkn", true, "0", false)
                updateColumn(database, "tblMonth", "fstrSpecific", "fstrSpecific", true, "0", false)
                updateColumn(database, "tblYear", "flngYearID", "flngYearID", true, "-1", true)
            }
        }
        val MIGRATION_22_23 = object : Migration(22,23){
            override fun migrate(database: SupportSQLiteDatabase) {
                //No changes - Changed column name in class... Hopefully this doesn't suck
            }
        }
        val MIGRATION_23_24 = object : Migration(23,24){
            override fun migrate(database: SupportSQLiteDatabase) {
                //No changes - Changed column name in class... Hopefully this doesn't suck
            }
        }

        val MIGRATION_25 = object : Migration(24,25){
            override fun migrate(database: SupportSQLiteDatabase) {
                val queries = listOf("UPDATE tblTask SET flngTaskDetailID = 0 WHERE flngTaskDetailID = -1",
                        "UPDATE tblTask SET flngTimeID = 0 WHERE flngTimeID = -1",
                        "UPDATE tblTask SET flngTaskTypeID = 0 WHERE flngTaskTypeID = -1",
                        "UPDATE tblTask SET flngOneOff = 0 WHERE flngOneOff = -1",
                        "UPDATE tblTime SET flngTimeframeID = 0 WHERE flngTimeframeID = -1",
                        "UPDATE tblTime SET flngRepetition = 0 WHERE flngRepetition = -1",
                        "UPDATE tblTime SET flngGenerationID = 0 WHERE flngGenerationID = -1",
                        "UPDATE tblTimeInstance SET flngTimeID = 0 WHERE flngTimeID = -1",
                        "UPDATE tblTaskInstance SET flngTaskID = 0 WHERE flngTaskID = -1",
                        "UPDATE tblTaskInstance SET flngTaskDetailID = 0 WHERE flngTaskDetailID = -1",
                        "UPDATE tblTaskInstance SET flngSessionID = 0 WHERE flngSessionID = -1",
                        "DELETE from tblTimeInstance WHERE flngTimeID = 0",
                        "DELETE from tblTime WHERE flngTimeID = 0",
                        "DELETE from tblTimeInstance WHERE flngTimeID NOT IN (SELECT flngTimeID FROM tblTime)")

                for(query in queries){
                    database.execSQL(query)
                }

                updateColumn(database, "tblTask", "flngTaskID", "flngTaskID", true, "0", true)
                updateColumn(database, "tblTask", "flngTaskDetailID", "flngTaskDetailID", true, "0", false)
                updateColumn(database, "tblTask", "flngTimeID", "flngTimeID", true, "0", false)
                updateColumn(database, "tblTask", "flngTaskTypeID", "flngTaskTypeID", true, "0", false)
                updateColumn(database, "tblTask", "flngOneOff", "flngOneOff", true, "0", false)

                updateColumn(database, "tblTime", "flngTimeID", "flngTimeID", true, "0", true)
                updateColumn(database, "tblTime", "flngTimeframeID", "flngTimeframeID", true, "0", false)
                updateColumn(database, "tblTime", "flngRepetition", "flngRepetition", true, "0", false)
                updateColumn(database, "tblTime", "flngGenerationID", "flngGenerationID", true, "0", false)

                updateColumn(database, "tblTimeInstance", "flngGenerationID", "flngGenerationID", true, "0", true)
                updateColumn(database, "tblTimeInstance", "flngTimeID", "flngTimeID", true, "0", false)

                updateColumn(database, "tblTaskInstance", "flngInstanceID", "flngInstanceID", true, "0", true)
                updateColumn(database, "tblTaskInstance", "flngTaskID", "flngTaskID", true, "0", false)
                updateColumn(database, "tblTaskInstance", "flngTaskDetailID", "flngTaskDetailID", true, "0", false)
                updateColumn(database, "tblTaskInstance", "flngSessionID", "flngSessionID", true, "0", false)
            }
        }
        //endregion

        //region Helper_Functions
        @Throws(Exception::class)
        private fun addColumn(db: SupportSQLiteDatabase,
                              pstrTableName: String,
                              pstrColumnName: String,
                              pintPosition: Long,
                              pblnNotNull: Boolean,
                              pstrDefault: String) {
            val strTempTable = pstrTableName + "_Temp"

            //Gets Create Statement for the Specific Table, updates it to be _Temp, then executes the updated create script
            var cursor = db.query("SELECT sql FROM sqlite_master WHERE name='$pstrTableName';")
            cursor.moveToFirst()
            var strCreate = cursor.getString(cursor.getColumnIndex("sql"))
            strCreate = strCreate.replaceFirst(pstrTableName.toRegex(), strTempTable)
            db.execSQL(strCreate)

            //Insert data from regular tables into new tables
            db.execSQL("INSERT INTO $strTempTable SELECT * FROM $pstrTableName")

            //Drop original tables
            db.execSQL("DROP TABLE $pstrTableName")

            //Create new table
            var strCreateStatement = ""
            cursor = db.query("PRAGMA table_info($strTempTable)")
            var intCounter: Long = 0
            while (cursor.moveToNext()) {
                if (strCreateStatement != "") {
                    strCreateStatement += ", "
                }
                if (pintPosition == intCounter) {
                    strCreateStatement += "$pstrColumnName "
                    var strColumnType = ""
                    when (pstrColumnName.substring(1, 4)) {
                        "lng" -> strColumnType = "INTEGER"
                        "dtm" -> strColumnType = "INTEGER"
                        "bln" -> strColumnType = "INTEGER"
                        "str" -> strColumnType = "TEXT"
                        "long" -> strColumnType = "INTEGER"
                    }
                    strCreateStatement += "$strColumnType "
                    if (pblnNotNull == true) {
                        strCreateStatement += "NOT NULL "
                    }
                    if (pstrDefault == "") {
                        //FOR THE MILLIONTH TIME, USE '' as the default!!! NOT ""
                        throw Exception("Default Must Be Provided. Probably meant to use ''")
                    }
                    strCreateStatement += "DEFAULT $pstrDefault, "
                }
                strCreateStatement += cursor.getString(cursor.getColumnIndex("name")) + " "
                strCreateStatement += cursor.getString(cursor.getColumnIndex("type")) + " "
                if (cursor.getInt(cursor.getColumnIndex("notnull")) == 1) {
                    strCreateStatement += "NOT NULL "
                }
                if (cursor.getString(cursor.getColumnIndex("dflt_value")) != null) {
                    //Hard coded because for some reason it doesn't keep the required parenthesis
                    if (cursor.getString(cursor.getColumnIndex("dflt_value")) == "strftime('%s','now')*1000") {
                        strCreateStatement += "DEFAULT (strftime('%s','now')*1000) "
                    } else {
                        strCreateStatement += "DEFAULT " + cursor.getString(cursor.getColumnIndex("dflt_value")) + " "
                    }
                }
                if (cursor.getInt(cursor.getColumnIndex("pk")) == 1) {
                    strCreateStatement += "PRIMARY KEY "
                }
                intCounter += 1
            }
            //if the counter was higher than the table columns, add to the end.
            if (pintPosition >= intCounter) {
                strCreateStatement += ", "
                strCreateStatement += "$pstrColumnName "
                if (pblnNotNull == true) {
                    strCreateStatement += "NOT NULL "
                }
                if (pstrDefault == "") {
                    //FOR THE MILLIONTH TIME, USE '' as the default!!! NOT ""
                    throw Exception("Default Must Be Provided. Probably meant to use ''")
                }
                strCreateStatement += "DEFAULT $pstrDefault "
            }
            db.execSQL("CREATE TABLE $pstrTableName ($strCreateStatement)")

            //Generates a list of the temp tables fields then creates and executes the insert into new table statement
            cursor = db.query("PRAGMA table_info($strTempTable)")
            var strOrigColumns = ""
            while (cursor.moveToNext()) {
                if (strOrigColumns != "") {
                    strOrigColumns += ", "
                }
                strOrigColumns += cursor.getString(cursor.getColumnIndex("name"))
            }
            db.execSQL("INSERT INTO $pstrTableName ($strOrigColumns) SELECT * FROM $strTempTable")

            //Drop temp table
            db.execSQL("DROP TABLE $strTempTable")
        }

        @Throws(Exception::class)
        private fun deleteColumn(db: SupportSQLiteDatabase,
                                 pstrTableName: String,
                                 pstrColumnName: String) {
            deleteColumn(db, pstrTableName, returnColumnPosition(db, pstrTableName, pstrColumnName))
        }

        @Throws(Exception::class)
        private fun deleteColumn(db:SupportSQLiteDatabase,
                                 pstrTableName: String,
                                 pintPosition: Long) {

            val strTempTable = pstrTableName + "_Temp"

            //Gets Create Statement for the Specific Table, updates it to be _Temp, then executes the updated create script
            var cursor = db.query("SELECT sql FROM sqlite_master WHERE name='$pstrTableName';")
            cursor.moveToFirst()
            var strCreate = cursor.getString(cursor.getColumnIndex("sql"))
            strCreate = strCreate.replaceFirst(pstrTableName.toRegex(), strTempTable)
            db.execSQL(strCreate)

            //Insert data from regular tables into temp table
            db.execSQL("INSERT INTO $strTempTable SELECT * FROM $pstrTableName")

            //Drop original tables
            db.execSQL("DROP TABLE $pstrTableName")

            //Create new table
            var strCreateStatement = ""
            cursor = db.query("PRAGMA table_info($strTempTable)")
            var intCounter: Long = 0
            while (cursor.moveToNext()) {
                if (pintPosition != intCounter) {
                    if (strCreateStatement != "") {
                        strCreateStatement += ", "
                    }
                    strCreateStatement += cursor.getString(cursor.getColumnIndex("name")) + " "
                    strCreateStatement += cursor.getString(cursor.getColumnIndex("type")) + " "
                    if (cursor.getInt(cursor.getColumnIndex("notnull")) == 1) {
                        strCreateStatement += "NOT NULL "
                    }
                    if (cursor.getString(cursor.getColumnIndex("dflt_value")) != null) {
                        if (cursor.getString(cursor.getColumnIndex("dflt_value")) == "strftime('%s','now')*1000") {
                            strCreateStatement += "DEFAULT (strftime('%s','now')*1000) "
                        } else {
                            strCreateStatement += "DEFAULT " + cursor.getString(cursor.getColumnIndex("dflt_value")) + " "
                        }
                    }
                    if (cursor.getInt(cursor.getColumnIndex("pk")) == 1) {
                        strCreateStatement += "PRIMARY KEY "
                    }
                }
                intCounter += 1
            }
            db.execSQL("CREATE TABLE $pstrTableName ($strCreateStatement)")

            //Generates a list of the temp tables fields then creates and executes the insert into new table statement
            cursor = db.query("PRAGMA table_info($strTempTable)")
            var strOrigColumns = ""
            intCounter = 0
            while (cursor.moveToNext()) {
                if (pintPosition != intCounter) {
                    if (strOrigColumns != "") {
                        strOrigColumns += ", "
                    }
                    strOrigColumns += cursor.getString(cursor.getColumnIndex("name"))
                }
                intCounter += 1
            }
            db.execSQL("INSERT INTO $pstrTableName ($strOrigColumns) SELECT $strOrigColumns FROM $strTempTable")

            //Drop temp table
            db.execSQL("DROP TABLE $strTempTable")
        }

        @Throws(Exception::class)
        private fun remakeTable(db:SupportSQLiteDatabase,
                                pstrCreateStatement: String,
                                pstrTableName: String){

            //NOTE: Function only works when re-aranging column order
            val strTempTable = pstrTableName + "_old"

            //RENAME old table
            db.execSQL("ALTER TABLE $pstrTableName RENAME TO $strTempTable")

            //Run new create script
            db.execSQL(pstrCreateStatement)

            //Insert from old table into new table
            db.execSQL("INSERT INTO $pstrTableName SELECT * FROM $strTempTable")
        }

        @Throws(Exception::class)
        private fun updateColumn(db:SupportSQLiteDatabase,
                                 pstrTableName: String,
                                 pstrOrigColumnName: String,
                                 pstrNewColumnName: String,
                                 pblnNotNull: Boolean,
                                 pstrDefault: String,
                                 pblnPrimary: Boolean,
                                 pstrColumnType: String = "") {

            val strTempTable = pstrTableName + "_Temp"

            //Gets Create Statement for the Specific Table, updates it to be _Temp, then executes the updated create script
            var cursor = db.query("SELECT sql FROM sqlite_master WHERE name='$pstrTableName';")
            cursor.moveToFirst()
            var strCreate = cursor.getString(cursor.getColumnIndex("sql"))
            strCreate = strCreate.replaceFirst(pstrTableName.toRegex(), strTempTable)
            db.execSQL(strCreate)

            //Insert data from regular tables into new tables
            db.execSQL("INSERT INTO $strTempTable SELECT * FROM $pstrTableName")

            //Drop original tables
            db.execSQL("DROP TABLE $pstrTableName")

            //Create new table
            var strCreateStatement = ""
            cursor = db.query("PRAGMA table_info($strTempTable)")
            while (cursor.moveToNext()) {
                val strCurrentColName = cursor.getString(cursor.getColumnIndex("name"))
                if (strCreateStatement != "") {
                    strCreateStatement += ", "
                }
                if (strCurrentColName == pstrOrigColumnName) {
                    //if (!pblnPrimary) {
                        strCreateStatement += "$pstrNewColumnName "
                        if(pstrColumnType == "") strCreateStatement += cursor.getString(cursor.getColumnIndex("type")) + " "
                        else strCreateStatement += pstrColumnType + " "
                        if (pblnNotNull == true) {
                            strCreateStatement += "NOT NULL "
                        }
                    //FOR THE MILLIONTH TIME, USE '' as the default!!! NOT ""
                        if (pstrDefault == "") {
                            throw Exception("Default Must Be Provided. Probably meant to use ''")
                        }
                        strCreateStatement += "DEFAULT $pstrDefault "
                    if(pblnPrimary){
                        strCreateStatement += " PRIMARY KEY "
                    }
                    //}
//                    else {
//                        strCreateStatement += pstrNewColumnName + " " + cursor.getString(cursor.getColumnIndex("type")) + " PRIMARY KEY "
//                    }
                } else {
                    strCreateStatement += strCurrentColName + " "
                    strCreateStatement += cursor.getString(cursor.getColumnIndex("type")) + " "
                    if (cursor.getInt(cursor.getColumnIndex("notnull")) == 1) {
                        strCreateStatement += "NOT NULL "
                    }
                    if (cursor.getString(cursor.getColumnIndex("dflt_value")) != null) {
                        if (cursor.getString(cursor.getColumnIndex("dflt_value")) == "strftime('%s','now')*1000") {
                            strCreateStatement += "DEFAULT (strftime('%s','now')*1000) "
                        } else {
                            strCreateStatement += "DEFAULT " + cursor.getString(cursor.getColumnIndex("dflt_value")) + " "
                        }
                    }
                    if (cursor.getInt(cursor.getColumnIndex("pk")) == 1) {
                        strCreateStatement += "PRIMARY KEY "
                    }
                }
            }
            db.execSQL("CREATE TABLE $pstrTableName ($strCreateStatement)")

            //Generates a list of the temp tables fields then creates and executes the insert into new table statement
            //            cursor = db.query("PRAGMA table_info(" + strTempTable + ")",new String[]{});
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
            db.execSQL("INSERT INTO $pstrTableName SELECT * FROM $strTempTable")

            //Drop temp table
            db.execSQL("DROP TABLE $strTempTable")
        }

        private fun returnColumnPosition(db:SupportSQLiteDatabase,
                                         pstrTableName: String,
                                         pstrColumnName: String): Long {
            val cursor = db.query("PRAGMA table_info($pstrTableName)")
            var position: Long = 0
            while (cursor.moveToNext()) {
                val strCurrentColName = cursor.getString(cursor.getColumnIndex("name"))
                if (strCurrentColName == pstrColumnName) return position
                position++
            }
            return -1
        }

        private fun retrieveTableLength(db:SupportSQLiteDatabase,
                                        pstrTable: String): Long {
            var length: Long = 0
            val cursor = db.query(
                    SupportSQLiteQueryBuilder.builder(pstrTable)
                            .selection("1 = 0",null)
                            .create())//prevents grabbing any rows, just columns
            length = cursor.getColumnCount().toLong()
            return length
        }

        private fun populateCurrentTableValues(pCursor: Cursor): ContentValues {
            val values = ContentValues()
            for (i in 0 until pCursor.columnCount) {
                val columnName = pCursor.getColumnName(i)
                when (columnName.substring(1, 4)) {
                    "lng" -> values.put(columnName, pCursor.getLong(i))
                    "dtm" -> values.put(columnName, pCursor.getLong(i))
                    "bln" -> values.put(columnName, pCursor.getLong(i))
                    "str" -> values.put(columnName, pCursor.getString(i))
                    "long" -> values.put(columnName, pCursor.getLong(i))
                }
            }
            return values
        }
        //endregion
    }
}