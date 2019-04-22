package com.example.testtask;

public class TimeInstance {
    long mlngGenerationID;
    long mlngTimeID;
    long mdtmUpcoming;
    long mdtmPriority;
    int mintThru;

    public TimeInstance(){
        mlngGenerationID = -1;
        mlngTimeID = -1;
        mdtmUpcoming = -1;
        mdtmPriority = -1;
        mintThru = 0;
    }
    public TimeInstance(long plngTimeID){
        this();
        mlngTimeID = plngTimeID;
    }

    public void save(){
        if(mdtmUpcoming != -1){
            mlngGenerationID = DatabaseAccess.addRecordToTable("tblTimeInstance",
                    new String[]{"flngTimeID","fdtmUpcoming","fdtmPriority","fintThru"},
                    new Object[]{mlngTimeID, mdtmUpcoming, mdtmPriority, mintThru});
        }
    }
}
