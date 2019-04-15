package com.example.testtask;

import android.database.Cursor;

public class Session {
    Long mlngSessionID;
    Long mlngTimeID;
    String mstrTitle;
    String mstrDescription;

    public Session(){
        mlngSessionID = (long)-1;
        mlngTimeID = (long)-1;
        mstrTitle = "";
        mstrDescription = "";
    }

    public Session(Long plngTimeID){
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblSession", "flngTimeID", plngTimeID);
        if(cursor.moveToFirst()){
            mlngSessionID = cursor.getLong(cursor.getColumnIndex("flngSessionID"));
            mlngTimeID = cursor.getLong(cursor.getColumnIndex("flngTimeID"));
            mstrTitle = cursor.getString(cursor.getColumnIndex("fstrTitle"));
            mstrDescription = cursor.getString(cursor.getColumnIndex("fstrDescription"));
        }
    }

    public void createSession(Long plngTimeID){
        DatabaseAccess.addRecordToTable("tblSession",
                new String[] {"flngTimeID","fstrTitle","fstrDescription"},
                new Object[] {plngTimeID, mstrTitle, mstrDescription});
    }
}


