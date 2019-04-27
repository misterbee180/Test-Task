package com.deviousindustries.testtask;

class TimeInstance {
    private long mlngTimeID;
    long mdtmUpcoming;
    long mdtmPriority;
    int mintThru;

    private TimeInstance(){
        mlngTimeID = -1;
        mdtmUpcoming = -1;
        mdtmPriority = -1;
        mintThru = 0;
    }
    TimeInstance(long plngTimeID){
        this();
        mlngTimeID = plngTimeID;
    }

    void save(){
        if(mdtmUpcoming != -1){
            DatabaseAccess.addRecordToTable("tblTimeInstance",
                    new String[]{"flngTimeID","fdtmUpcoming","fdtmPriority","fintThru"},
                    new Object[]{mlngTimeID, mdtmUpcoming, mdtmPriority, mintThru});
        }
    }
}
