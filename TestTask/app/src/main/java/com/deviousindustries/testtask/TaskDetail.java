package com.deviousindustries.testtask;

import android.database.Cursor;

class TaskDetail {
    long mlngTaskDetailId;
    String mstrTitle;
    String mstrDescription;

    TaskDetail(long plngTaskDetailId){
        Cursor taskDetail = DatabaseAccess.getRecordsFromTable("tblTaskDetail", "flngTaskDetailID", plngTaskDetailId);
        if(taskDetail.moveToNext()){
            mstrTitle = taskDetail.getString(taskDetail.getColumnIndex("fstrTitle"));
            mstrDescription = taskDetail.getString(taskDetail.getColumnIndex("fstrDescription"));
        }
    }

    TaskDetail(long plngTaskDetailId,
               String pstrTitle,
               String pstrDescription){
        mlngTaskDetailId = plngTaskDetailId;
        mstrTitle = pstrTitle;
        mstrDescription = pstrDescription;

        mlngTaskDetailId = DatabaseAccess.addRecordToTable("tblTaskDetail",
                new String[]{"fstrTitle","fstrDescription"},
                new Object[]{mstrTitle, mstrDescription},
                "flngTaskDetailID",
                mlngTaskDetailId);
    }
}
