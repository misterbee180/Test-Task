package com.deviousindustries.testtask.Classes;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.deviousindustries.testtask.DatabaseAccess;

@Entity(tableName = "tblTimeInstance")
public class TimeInstance {
    @PrimaryKey(autoGenerate = true)
    public long flngGenerationID;

    @ColumnInfo
    public long flngTimeID;

    @ColumnInfo
    public long fdtmUpcoming;

    @ColumnInfo
    public long fdtmPriority;

    @ColumnInfo
    public int fintThru;

    public TimeInstance(){
        flngTimeID = -1;
        fdtmUpcoming = -1;
        fdtmPriority = -1;
        fintThru = 0;
    }

    public TimeInstance(long plngTimeID){
        this();
        flngTimeID = plngTimeID;
    }

    void save(){
        if(fdtmUpcoming != -1){
            DatabaseAccess.addRecordToTable("tblTimeInstance",
                    new String[]{"flngTimeID","fdtmUpcoming","fdtmPriority","fintThru"},
                    new Object[]{flngTimeID, fdtmUpcoming, fdtmPriority, fintThru});
        }
    }
}
