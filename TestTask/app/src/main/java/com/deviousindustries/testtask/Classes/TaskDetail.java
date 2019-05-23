package com.deviousindustries.testtask.Classes;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.deviousindustries.testtask.DatabaseAccess;


@Entity(tableName = "tblTaskDetail")
public class TaskDetail {
    @PrimaryKey(autoGenerate = true)
    public long flngTaskDetailID;

    @ColumnInfo
    @NonNull
    public String fstrTitle;

    @ColumnInfo
    @NonNull
    public String fstrDescription;

    public TaskDetail(){
        flngTaskDetailID = -1;
        fstrTitle = "";
        fstrDescription = "";
    }

    public TaskDetail(long plngTaskDetailId){
        Cursor taskDetail = DatabaseAccess.getRecordsFromTable("tblTaskDetail", "flngTaskDetailID", plngTaskDetailId);
        if(taskDetail.moveToNext()){
            fstrTitle = taskDetail.getString(taskDetail.getColumnIndex("fstrTitle"));
            fstrDescription = taskDetail.getString(taskDetail.getColumnIndex("fstrDescription"));
        }
    }

    public TaskDetail(long plngTaskDetailId,
               String pstrTitle,
               String pstrDescription){
        flngTaskDetailID = plngTaskDetailId;
        fstrTitle = pstrTitle;
        fstrDescription = pstrDescription;

        flngTaskDetailID = DatabaseAccess.addRecordToTable("tblTaskDetail",
                new String[]{"fstrTitle","fstrDescription"},
                new Object[]{fstrTitle, fstrDescription},
                "flngTaskDetailID",
                flngTaskDetailID);
    }
}
