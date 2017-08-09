package com.example.testtask;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Misterbee180 on 7/16/2017.
 */

public class DatabaseAccess {
    public void DatabaseAccess(){
    }

    public static class TblTaskOpenHelper extends SQLiteOpenHelper {
        private static final String CREATE_TASK_TABLE =
                "CREATE TABLE `tblTask` (\n" +
                        "\t`flngID`\tINTEGER,\n" +
                        "\t`fstrTitle`\tTEXT,\n" +
                        "\t`fstrDescription`\tTEXT,\n" +
                        "\t`flngSessionID`\tINTEGER,\n" +
                        "\t`flngTimeID`\tINTEGER,\n" +
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

        TblTaskOpenHelper(Context context) {
            super(context, "TaskDatabase.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TASK_TABLE);
            db.execSQL(CREATE_TASKINSTANCE_TABLE);
            db.execSQL(CREATE_SESSION_TABLE);
            db.execSQL(CREATE_TIME_TABLE);
            db.execSQL(CREATE_WEEK_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db,
                              int oldVersion,
                              int newVersion){

        }
    }
}
