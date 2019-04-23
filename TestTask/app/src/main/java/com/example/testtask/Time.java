package com.example.testtask;

import android.database.Cursor;

import java.util.Calendar;

public class Time {
    long mlngTimeID = -1;
    long mdtmFrom;
    long mdtmTo;
    boolean mblnFromTime;
    boolean mblnToTime;
    boolean mblnToDate;
    int mintTimeframe;
    long mlngTimeframeID;
    long mlngRepetition;
    long mdtmCreated;
    int mintStarting;
    boolean mblnComplete;
    long mlngGenerationID;
    boolean mblnThru;

    public Time(){
        mlngTimeID = -1;
        mdtmFrom = -1;
        mdtmTo = -1;
        mblnFromTime = false;
        mblnToTime = false;
        mblnToDate = false;
        mintTimeframe = -1;
        mlngTimeframeID = -1;
        mlngRepetition = -1;
        mdtmCreated = -1;
        mintStarting = 0;
        mblnComplete = false;
        mlngGenerationID = -1;
        mblnThru = false;
    }

    public Time(long plngTimeId){
        this();
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblTime", "flngTimeID", plngTimeId);
        if(cursor.moveToFirst()){
            mlngTimeID = cursor.getLong(cursor.getColumnIndex("flngTimeID"));
            mdtmFrom = cursor.getLong(cursor.getColumnIndex("fdtmFrom"));
            mdtmTo = cursor.getLong(cursor.getColumnIndex("fdtmTo"));
            mblnFromTime = cursor.getLong(cursor.getColumnIndex("fblnFromTime")) == 1;
            mblnToTime = cursor.getLong(cursor.getColumnIndex("fblnToTime")) == 1;
            mblnToDate = cursor.getLong(cursor.getColumnIndex("fblnToDate")) == 1;
            mintTimeframe = cursor.getInt(cursor.getColumnIndex("fintTimeframe"));
            mlngTimeframeID = cursor.getLong(cursor.getColumnIndex("flngTimeframeID"));
            mlngRepetition = cursor.getLong(cursor.getColumnIndex("flngRepetition"));
            mdtmCreated = cursor.getLong(cursor.getColumnIndex("fdtmCreated"));
            mintStarting = cursor.getInt(cursor.getColumnIndex("fintStarting"));
            mblnComplete = cursor.getLong(cursor.getColumnIndex("fblnComplete")) == 1;
            mlngGenerationID = cursor.getInt(cursor.getColumnIndex("flngGenerationID"));
            mblnThru = cursor.getInt(cursor.getColumnIndex("fblnThru")) == 1;
        }
        cursor.close();
    }
    public Time(long plngTimeID,
                Long pdtmFrom,
                Long pdtmTo,
                Long pdtmCreated,
                boolean pblnFromTime,
                boolean pblnToTime,
                boolean pblnToDate,
                int plngTimeframeType,
                long plngTimeframeID,
                long plngRepetition,
                int plngStarting,
                boolean pblnComplete,
                long plngGenerationID,
                boolean pblnThru){

        mlngTimeID = plngTimeID;
        mdtmFrom = pdtmFrom;
        mdtmTo = pdtmTo;
        mblnToDate = pblnToDate;
        mblnFromTime = pblnFromTime;
        mblnToTime = pblnToTime;
        mintTimeframe = plngTimeframeType;
        mlngTimeframeID = plngTimeframeID;
        mlngRepetition = plngRepetition;
        mdtmCreated = pdtmCreated;
        mintStarting = plngStarting;
        mblnComplete = pblnComplete;
        mlngGenerationID = plngGenerationID;
        mblnThru = pblnThru;

        saveTime();
    }

    public void saveTime(){
        mlngTimeID = DatabaseAccess.addRecordToTable("tblTime",
                new String[] {"fdtmFrom", "fdtmTo", "fblnFromTime", "fblnToTime", "fblnToDate","fintTimeframe", "flngTimeframeID", "flngRepetition",
                        "fdtmCreated", "fintStarting", "fblnComplete", "flngGenerationID", "fblnThru"},
                new Object[] {mdtmFrom,
                        mdtmTo,
                        mblnFromTime,
                        mblnToTime,
                        mblnToDate,
                        mintTimeframe,
                        mlngTimeframeID,
                        mlngRepetition,
                        mdtmCreated,
                        mintStarting,
                        mblnComplete,
                        mlngGenerationID,
                        mblnThru},
                "flngTimeID",
                mlngTimeID);

        if(!mblnComplete) buildTimeInstances();
    }

    //region Session
    public boolean isSession(){
        Cursor curSession = DatabaseAccess.getRecordsFromTable("tblSession",
                "flngTimeID",
                mlngTimeID);
        if(curSession.moveToNext()){
            return true;
        }
        return false;
    }

    public void createSession(String pstrTitle){
        DatabaseAccess.addRecordToTable("tblSession",
                new String[] {"flngTimeID","fstrTitle"},
                new Object[] {mlngTimeID, pstrTitle},
                "flngTimeID",
                mlngTimeID);
    }

    public Cursor getSession(){
        return DatabaseAccess.getRecordsFromTable("tblSession", "flngTimeID", mlngTimeID);
    }
    //endregion

    //region TimeGeneration Class
    public boolean timeInstanceExist(){
        return DatabaseAccess.getRecordsFromTable("tblTimeInstance", "flngTimeID", mlngTimeID).getCount() > 0? true:false;
    }

    public long getLatestPriorityAndThru(){
        //Returns the latest time instance associated w/ a time w/ the inclusing on the thru value.
        //This is important as you don't want to be evaluating dates already handled by a thru value.
        Cursor latestTimeInstance = DatabaseAccess.mDatabase.query("tblTimeInstance",
                null,
                "flngTimeID = ?",
                new String[]{Long.toString(mlngTimeID)},
                null,
                null,
                "flngGenerationID desc LIMIT 1");

        if(latestTimeInstance.moveToFirst()){
            Calendar temp = Task_Display.getCalendar(latestTimeInstance.getLong(latestTimeInstance.getColumnIndex("fdtmPriority")));
            temp.add(Calendar.DAY_OF_YEAR, latestTimeInstance.getInt(latestTimeInstance.getColumnIndex("fintThru")));
            return temp.getTimeInMillis();
        }
        return -1;
    }

    public long getNextPriority(boolean pblnTo){
        Cursor tblGeneration = DatabaseAccess.mDatabase.query("tblTimeInstance",
                null,
                "flngTimeID = ? AND fdtmPriority > ?",
                new String[]{Long.toString(mlngTimeID), Long.toString(Task_Display.getCurrentCalendar().getTimeInMillis())},
                null,
                null,
                "fdtmPriority asc LIMIT 1");

        if(tblGeneration.moveToFirst()){
            Calendar calPri = Task_Display.getCalendar(tblGeneration.getLong(tblGeneration.getColumnIndex("fdtmPriority")));
            if(pblnTo){
                calPri.add(Calendar.DAY_OF_YEAR,tblGeneration.getInt(tblGeneration.getColumnIndex("fintThru")));
                if(mblnToTime){
                    Calendar time = Task_Display.getCalendar(mdtmTo);
                    calPri.set(Calendar.HOUR,time.get(Calendar.HOUR));
                    calPri.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
                    calPri.set(Calendar.SECOND, time.get(Calendar.SECOND));
                    calPri.set(Calendar.MILLISECOND, time.get(Calendar.MILLISECOND));
                }
            }
            return calPri.getTimeInMillis();
        }
        return -1;
    }

    public long getMaxUpcoming(){
        Cursor tblGeneration = DatabaseAccess.mDatabase.query("tblTimeInstance",
                new String[] {"MAX(fdtmUpcoming) as fdtmUpcomingMax"},
                "flngTimeID = ?",
                new String[]{Long.toString(mlngTimeID)},
                null,
                null,
                null);

        tblGeneration.moveToFirst();
        long rtn = tblGeneration.getLong(tblGeneration.getColumnIndex("fdtmUpcomingMax"));
        return rtn == 0 ? -1 : rtn;
    }

    public void clearGenerationPoints(){
        DatabaseAccess.deleteRecordFromTable("tblTimeInstance",
                "flngTimeID",
                mlngTimeID);
        DatabaseAccess.updateRecordFromTable("tblTime",
                "flngTimeID",
                mlngTimeID,
                new String[]{"flngGenerationID"},
                new Object[]{(long)-1});
    }

    public void buildTimeInstances(){
        while(true){
            //While we can still attempt to generate an upcoming task that should be generated before today and while the time isn't already exempt (complete)
            TimeInstance tGen = new TimeInstance(mlngTimeID);
            Boolean blnSaveGen = true;
            if(getMaxUpcoming() <= Task_Display.getEndCurrentDay().getTimeInMillis() && !mblnComplete){
                if(mlngRepetition != (long)0){
                    //establish what repetition tasks associated w/ and whether current date fits
                    switch(mintTimeframe){
                        case 0: //Day
                            evaluateDayGeneration(1, getLatestPriorityAndThru(), tGen);
                            break;
                        case 1: //Week
                            evaluateWeekGeneration(1, getLatestPriorityAndThru(), tGen);
                            break;
                        case 2: //Month
                            evaluateMonthGeneration(1, getLatestPriorityAndThru(), tGen);
                            break;
                        case 3: //Year
                            evaluateYearGeneration(1, getLatestPriorityAndThru(), tGen);
                            break;
                    }
                } else {
                    if(!timeInstanceExist()){ //If not previously evaluated, evaluate for the first and only time
                        evaluateDate(1,tGen);
                    } else{
                        completeTime();
                        blnSaveGen = false; //Don't want to save a new time generation if all we did was complete the time.
                    }
                }
                if(blnSaveGen) tGen.save();
            }else{
                break;
            }
        }
    }

    private void evaluateDate(int upcomingRange, TimeInstance pGen){
        pGen.mdtmPriority = mdtmFrom;
        Calendar tempUp = Task_Display.getCalendar(mdtmFrom);
        tempUp.add(Calendar.DAY_OF_YEAR, -1 * upcomingRange);
        pGen.mdtmUpcoming = tempUp.getTimeInMillis();
    }

    private void evaluateDayGeneration(int upcomingRange,
                                       long pdtmOrigPriority,
                                       TimeInstance pGen) {
        Calendar calEvaluate;
        Calendar calNow = Task_Display.getCurrentCalendar();

        //Establishing starting date (either create + starting or prior priority)
        Calendar calBOD;
        Boolean blnSet = false;
        if(pdtmOrigPriority != -1){
            calBOD = Task_Display.getCalendar(pdtmOrigPriority);
            blnSet = true;
        } else {
            calBOD = Task_Display.getCalendar(mdtmCreated);
            //As we don't know when it was last generated the only way to know the starting week is to get on the proper frequency starting point is this way
            calBOD.add(Calendar.DAY_OF_YEAR,mintStarting);
        }
        Calendar calEOD = (Calendar) calBOD.clone();

        //Establishing bounding dates / times - In day case the day bounds are a single day so selecting day step is not needed.
        calBOD.set(Calendar.HOUR_OF_DAY,0);
        calBOD.set(Calendar.MINUTE,0);
        calBOD.set(Calendar.SECOND,0);
        calBOD.set(Calendar.MILLISECOND,0);

        calEOD.set(Calendar.HOUR_OF_DAY,23);
        calEOD.set(Calendar.MINUTE,59);
        calEOD.set(Calendar.SECOND,59);
        calEOD.set(Calendar.MILLISECOND,999);

        //Move the bounds by the occurs metric until the ending bound occurs after the current day/time.
        while(true){
            if(calEOD.before(calNow)){
                calBOD.add(Calendar.DAY_OF_YEAR,(int)mlngRepetition);
                calEOD.add(Calendar.DAY_OF_YEAR,(int)mlngRepetition);
                blnSet = false;
            } else {
                //Finally move the beginning bound to the now date (or now + 1 if priority) if now happens to be w/i the bounds, otherwise select begging of bound
                if(blnSet){
                    calBOD.add(Calendar.DAY_OF_YEAR,1);
                }
                calEvaluate = calBOD;
                break;
            }
        }

        //Get from date cal and provide time details from it to calEvaluate
        Calendar calFrom = Task_Display.getCalendar(mdtmFrom);
        calEvaluate.set(Calendar.HOUR_OF_DAY,calFrom.get(Calendar.HOUR_OF_DAY));
        calEvaluate.set(Calendar.MINUTE,calFrom.get(Calendar.MINUTE));
        calEvaluate.set(Calendar.SECOND,calFrom.get(Calendar.SECOND));
        calEvaluate.set(Calendar.MILLISECOND,calFrom.get(Calendar.MILLISECOND));

        pGen.mdtmPriority = calEvaluate.getTimeInMillis();
        Calendar calUpcoming = (Calendar) calEvaluate.clone();
        calUpcoming.add(Calendar.DAY_OF_YEAR, -1 * upcomingRange);
        pGen.mdtmUpcoming = calUpcoming.getTimeInMillis();
    }

    private void evaluateWeekGeneration(int upcomingRange,
                                        long pdtmOrigPriority,
                                        TimeInstance pGen){
        Calendar calEvaluate;
        Calendar calNow = Task_Display.getCurrentCalendar();

        //determine if calPriority is = the current day. If not (has to be before) use establish what a better starting week would be using starting and created details
        Calendar calBOW;
        Boolean blnSet = false;
        if(pdtmOrigPriority != -1){
            calBOW = Task_Display.getCalendar(pdtmOrigPriority);
            blnSet = true;
        } else {
            calBOW = Task_Display.getCalendar(mdtmCreated);
            //As we don't know when it was last generated the only way to know the starting week is to get on the proper frequency starting point is this way
            calBOW.add(Calendar.WEEK_OF_YEAR,mintStarting);
            calBOW.setWeekDate(calBOW.getWeekYear(), calBOW.get(Calendar.WEEK_OF_YEAR), Calendar.SUNDAY);
        }
        Calendar calEOW = (Calendar) calBOW.clone();
        calEOW.setWeekDate(calEOW.getWeekYear(), calEOW.get(Calendar.WEEK_OF_YEAR), Calendar.SATURDAY);

        calBOW.set(Calendar.HOUR_OF_DAY,0);
        calBOW.set(Calendar.MINUTE,0);
        calBOW.set(Calendar.SECOND,0);
        calBOW.set(Calendar.MILLISECOND,0);

        calEOW.set(Calendar.HOUR_OF_DAY,23);
        calEOW.set(Calendar.MINUTE,59);
        calEOW.set(Calendar.SECOND,59);
        calEOW.set(Calendar.MILLISECOND,999);

        while(true){
            if(calEOW.before(calNow)){
                if(blnSet) calBOW.setWeekDate(calBOW.getWeekYear(), calBOW.get(Calendar.WEEK_OF_YEAR), Calendar.SUNDAY);
                calBOW.add(Calendar.WEEK_OF_YEAR,(int)mlngRepetition);
                calEOW.add(Calendar.WEEK_OF_YEAR,(int)mlngRepetition);
                blnSet = false;
            } else {
                //If we set the date to today via priority, make sure we don't reevaluate today.
                if(blnSet)
                {
                    //As going past the end of the week means that we need to reevaluate the repetition quantity, we need to see if day is saturday before simply moving up a day.
                    if(calBOW.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
                        calBOW.setWeekDate(calBOW.getWeekYear(), calBOW.get(Calendar.WEEK_OF_YEAR), Calendar.SUNDAY);
                        calBOW.add(Calendar.WEEK_OF_YEAR, (int)mlngRepetition);
                    } else {
                        calBOW.add(Calendar.DAY_OF_YEAR,1);
                    }
                } else
                    //if today occurs w/i the week associated w/ the occurrence pattern, start it on the current day (saves a few iterations later), otherwise start it on the sunday
                    if(calBOW.getWeekYear() == calNow.getWeekYear() && calBOW.get(Calendar.WEEK_OF_YEAR) == calNow.get(Calendar.WEEK_OF_YEAR)){
                        calBOW.setWeekDate(calBOW.getWeekYear(), calBOW.get(Calendar.WEEK_OF_YEAR), calNow.get(Calendar.DAY_OF_WEEK));
                    }
                calEvaluate = calBOW;
                break;
            }
        }

        //Set evaluate to end of the day
        calEvaluate.set(Calendar.HOUR_OF_DAY,23);
        calEvaluate.set(Calendar.MINUTE,59);
        calEvaluate.set(Calendar.SECOND,59);
        calEvaluate.set(Calendar.MILLISECOND,999);

        recursiveWeekEval(calNow, calEvaluate, upcomingRange, pGen, true);
    }

    private void recursiveWeekEval(Calendar calNow,
                                   Calendar calEvaluate,
                                   int upcomingRange,
                                   TimeInstance pGen,
                                   boolean pblnFirst){
        Boolean blnComplete = false;
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblWeek", "flngWeekID", mlngTimeframeID);
        cursor.moveToFirst();

        Calendar calWeekday = (Calendar) calEvaluate.clone();
        String columnName = "";

        int i = calWeekday.get(Calendar.DAY_OF_WEEK);
        while(i <= 7){
            switch (calWeekday.get(Calendar.DAY_OF_WEEK)){
                case 1: columnName = "Sunday"; break;
                case 2: columnName = "Monday"; break;
                case 3: columnName = "Tuesday"; break;
                case 4: columnName = "Wednesday"; break;
                case 5: columnName = "Thursday"; break;
                case 6: columnName = "Friday"; break;
                case 7: columnName = "Saturday"; break;
            }
            //Confirm selected day is part of repetition detail
            if(cursor.getInt(cursor.getColumnIndex("fbln" + columnName)) == 1){
                //Don't need to evaluate for days past.
                if(calWeekday.after(calNow)){
                    //Make sure time details are represented in passed out dates.
                    Calendar calFrom = Task_Display.getCalendar(mdtmFrom);
                    calWeekday.set(Calendar.HOUR_OF_DAY,calFrom.get(Calendar.HOUR_OF_DAY));
                    calWeekday.set(Calendar.MINUTE,calFrom.get(Calendar.MINUTE));
                    calWeekday.set(Calendar.SECOND,calFrom.get(Calendar.SECOND));
                    calWeekday.set(Calendar.MILLISECOND,calFrom.get(Calendar.MILLISECOND));

                    if(pblnFirst){
                        pGen.mdtmPriority = calWeekday.getTimeInMillis();
                        Calendar calUpcoming = (Calendar) calWeekday.clone();
                        calUpcoming.add(Calendar.DAY_OF_YEAR, -1 * upcomingRange);
                        pGen.mdtmUpcoming = calUpcoming.getTimeInMillis();
                    }
                    if(mblnThru){
                        if(!pblnFirst){
                            if(Task_Display.getCalendar(calWeekday.getTimeInMillis(),true, false)
                                    .equals(Task_Display.getCalendar(calEvaluate.getTimeInMillis(),true,false))){
                                pGen.mintThru ++;
                            } else {
                                blnComplete = true;
                                break;
                            }
                        } else {
                            calEvaluate = (Calendar) calWeekday.clone();
                        }
                        //Establish next day
                        if(calEvaluate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
                            calEvaluate.setWeekDate(calEvaluate.getWeekYear(), calEvaluate.get(Calendar.WEEK_OF_YEAR), Calendar.SUNDAY);
                            calEvaluate.add(Calendar.WEEK_OF_YEAR, (int)mlngRepetition);
                        } else calEvaluate.add(Calendar.DAY_OF_YEAR,1);

                        //Get the thru date
                        recursiveWeekEval(calNow,
                                calEvaluate,
                                upcomingRange,
                                pGen,
                                false);
                    }
                    blnComplete = true;
                    break;
                }
            }
            //Move to the next day
            if(i<7) calWeekday.add(Calendar.DAY_OF_YEAR,1);
            i++;
        }
        cursor.close();

        if(blnComplete) return;
        //Othewise, establish what next evaluated week will be and call itself
        calEvaluate.setWeekDate(calEvaluate.getWeekYear(), calEvaluate.get(Calendar.WEEK_OF_YEAR), Calendar.SUNDAY);
        calEvaluate.add(Calendar.WEEK_OF_YEAR, (int)mlngRepetition);
        recursiveWeekEval(calNow, calEvaluate, upcomingRange, pGen, pblnFirst);
    }

    private void evaluateMonthGeneration(int upcomingRange,
                                         long pdtmOrigPriority,
                                         TimeInstance pGen) {

        Calendar calEvaluate;
        Calendar calNow = Task_Display.getCurrentCalendar();

        //Establishing starting date (either create + starting or prior priority)
        Calendar calBOM;
        Boolean blnSet = false;
        if(pdtmOrigPriority != -1){
            calBOM = Task_Display.getCalendar(pdtmOrigPriority);
            blnSet = true;
        } else {
            calBOM = Task_Display.getCalendar(mdtmCreated);
            //As we don't know when it was last generated the only way to know the starting week is to get on the proper frequency starting point is this way
            calBOM.add(Calendar.DAY_OF_YEAR, mintStarting);
            calBOM.set(Calendar.DAY_OF_MONTH, 1);
        }
        Calendar calEOM = (Calendar) calBOM.clone();

        //Establishing bounding dates / times - In day case the day bounds are a single day so selecting day step is not needed.
        calEOM.set(Calendar.DAY_OF_MONTH, 1);
        calEOM.add(Calendar.MONTH, 1);
        calEOM.add(Calendar.DAY_OF_YEAR, -1);

        calBOM.set(Calendar.HOUR_OF_DAY, 0);
        calBOM.set(Calendar.MINUTE, 0);
        calBOM.set(Calendar.SECOND, 0);
        calBOM.set(Calendar.MILLISECOND, 0);

        calEOM.set(Calendar.HOUR_OF_DAY, 23);
        calEOM.set(Calendar.MINUTE, 59);
        calEOM.set(Calendar.SECOND, 59);
        calEOM.set(Calendar.MILLISECOND, 999);

        //Move the bounds by the occurs metric until the ending bound occurs after the current day/time.
        while (true) {
            if (calEOM.before(calNow)) {
                if(blnSet) calBOM.set(Calendar.DAY_OF_MONTH,1);//resets DOM to first so that adding month will continue from first
                calBOM.add(Calendar.MONTH, (int)mlngRepetition);
                //Use more complex logic for months as they don't have consistent end days.
                calEOM = (Calendar) calBOM.clone();
                calEOM.add(Calendar.MONTH, 1);
                calEOM.add(Calendar.DAY_OF_YEAR, -1);
                calEOM.set(Calendar.HOUR_OF_DAY, 23);
                calEOM.set(Calendar.MINUTE, 59);
                calEOM.set(Calendar.SECOND, 59);
                calEOM.set(Calendar.MILLISECOND, 999);
                blnSet = false;
            } else {
                //If we set the date to today via priority, make sure we don't reevaluate today.
                if(blnSet){
                    if(calBOM.get(Calendar.DAY_OF_MONTH) == calEOM.get(Calendar.DAY_OF_MONTH)){
                        calBOM.set(Calendar.DAY_OF_MONTH,1);//resets DOM to first so that adding month will continue from first
                        calBOM.add(Calendar.MONTH, (int)mlngRepetition);
                    } else calBOM.add(Calendar.DAY_OF_YEAR,1);
                } else
                    //if today occurs w/i the week associated w/ the occurrence pattern, start it on the current day (saves a few iterations later), otherwise start it on the sunday
                    if(calBOM.before(calNow) && calEOM.after(calNow)){
                        calBOM.set(Calendar.DAY_OF_MONTH, calNow.get(Calendar.DAY_OF_MONTH));
                    }
                calEvaluate = calBOM;
                break;
            }
        }

        //Set to end of the day
        calEvaluate.set(Calendar.HOUR_OF_DAY, 23);
        calEvaluate.set(Calendar.MINUTE, 59);
        calEvaluate.set(Calendar.SECOND, 59);
        calEvaluate.set(Calendar.MILLISECOND, 999);

        recursiveMonthEval(calNow, calEvaluate, calEOM, upcomingRange,pGen, true);
    }

    private void recursiveMonthEval(Calendar calNow,
                                    Calendar calEvaluate,
                                    Calendar calEOM,
                                    int upcomingRange,
                                    TimeInstance pGen,
                                    Boolean pblnFirst) {

        Boolean blnComplete = false;
        Calendar calMonth = (Calendar) calEvaluate.clone();
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblMonth", "flngMonthID", mlngTimeframeID);
        cursor.moveToFirst();
        int month = calEvaluate.get(Calendar.MONTH);
        while (calMonth.get(Calendar.MONTH) == month) {
            if (calMonth.after(calNow)){
                if (cursor.getString(cursor.getColumnIndex("fstrSpecific")).equals("")) {
                    //General Dates (First Last Middle)
                    //First
                    if (    //First
                            (calMonth.get(Calendar.DAY_OF_MONTH) == 1 &&
                                    cursor.getLong(cursor.getColumnIndex("fblnFirst")) == 1) ||
                                    //Last
                                    (calMonth.get(Calendar.DAY_OF_MONTH) == calEOM.get(Calendar.DAY_OF_MONTH) &&
                                            cursor.getLong(cursor.getColumnIndex("fblnLast")) == 1) ||
                                    //Middle
                                    // todo: make fblnMiddle system value
                                    (calMonth.get(Calendar.DAY_OF_MONTH) == 15 &&
                                            cursor.getLong(cursor.getColumnIndex("fblnMiddle")) == 1)) {
                        if (cursor.getLong(cursor.getColumnIndex("fblnAfterWkn")) == 1) {
                            switch (calMonth.get(Calendar.DAY_OF_WEEK)) {
                                case 1:
                                    calMonth.add(Calendar.DAY_OF_MONTH, 1);
                                    break;
                                case 7:
                                    calMonth.add(Calendar.DAY_OF_MONTH, 2);
                                    break;
                                default:
                                    break;
                            }
                        }

                        //Make sure time details are represented in passed out dates.
                        Calendar calFrom = Task_Display.getCalendar(mdtmFrom);
                        calMonth.set(Calendar.HOUR_OF_DAY,calFrom.get(Calendar.HOUR_OF_DAY));
                        calMonth.set(Calendar.MINUTE,calFrom.get(Calendar.MINUTE));
                        calMonth.set(Calendar.SECOND,calFrom.get(Calendar.SECOND));
                        calMonth.set(Calendar.MILLISECOND,calFrom.get(Calendar.MILLISECOND));

                        pGen.mdtmPriority = calMonth.getTimeInMillis();
                        Calendar tempUp = (Calendar) calMonth.clone();
                        tempUp.add(Calendar.DAY_OF_MONTH, upcomingRange * -1);
                        pGen.mdtmUpcoming = tempUp.getTimeInMillis();
                        blnComplete = true;
                        break;
                    }
                } else {
                    String strSpecificDays[] = cursor.getString(cursor.getColumnIndex("fstrSpecific")).split(",");
                    for (int i = 0; i < strSpecificDays.length; i++) {
                        if (calMonth.get(Calendar.DAY_OF_MONTH) == Long.parseLong(strSpecificDays[i].trim())) {
                            //Make sure time details are represented in passed out dates.
                            Calendar calFrom = Task_Display.getCalendar(mdtmFrom);
                            calMonth.set(Calendar.HOUR_OF_DAY,calFrom.get(Calendar.HOUR_OF_DAY));
                            calMonth.set(Calendar.MINUTE,calFrom.get(Calendar.MINUTE));
                            calMonth.set(Calendar.SECOND,calFrom.get(Calendar.SECOND));
                            calMonth.set(Calendar.MILLISECOND,calFrom.get(Calendar.MILLISECOND));

                            if(pblnFirst) {
                                pGen.mdtmPriority = calMonth.getTimeInMillis();
                                Calendar tempUp = (Calendar) calMonth.clone();
                                tempUp.add(Calendar.DAY_OF_MONTH, upcomingRange * -1);
                                pGen.mdtmUpcoming = tempUp.getTimeInMillis();
                            }
                            if(mblnThru){
                                if(!pblnFirst){
                                    if(Task_Display.getCalendar(calMonth.getTimeInMillis(),true, false)
                                            .equals(Task_Display.getCalendar(calEvaluate.getTimeInMillis(),true,false))){
                                        pGen.mintThru ++;
                                    } else {
                                        blnComplete = true;
                                        break;
                                    }
                                } else {
                                    calEvaluate = (Calendar)calMonth.clone();
                                }
                                //Establish next day
                                if(calEvaluate.get(Calendar.DAY_OF_MONTH) == calEOM.get(Calendar.DAY_OF_MONTH)){
                                    calEvaluate.set(Calendar.DAY_OF_MONTH,1);
                                    calEvaluate.add(Calendar.MONTH, (int)mlngRepetition);
                                    calEOM = (Calendar) calEvaluate.clone();
                                    calEOM.add(Calendar.MONTH, 1);
                                    calEOM.add(Calendar.DAY_OF_YEAR, -1);
                                    calEOM.set(Calendar.HOUR_OF_DAY, 23);
                                    calEOM.set(Calendar.MINUTE, 59);
                                    calEOM.set(Calendar.SECOND, 59);
                                    calEOM.set(Calendar.MILLISECOND, 999);
                                } else calEvaluate.add(Calendar.DAY_OF_YEAR,1);
                                //Get the thru date
                                recursiveMonthEval(calNow, calEvaluate, calEOM, upcomingRange, pGen, false);
                            }
                            blnComplete = true;
                            break;
                        }
                    }
                    if(blnComplete) break;
                }

            }
            calMonth.add(Calendar.DAY_OF_MONTH, 1);
        }
        cursor.close();

        if(blnComplete) return;

        //Othewise, establish what next evaluated month will be and call itself
        calEvaluate.set(Calendar.DAY_OF_MONTH,1);
        calEvaluate.add(Calendar.MONTH, (int)mlngRepetition);
        calEOM = (Calendar) calEvaluate.clone();
        calEOM.add(Calendar.MONTH, 1);
        calEOM.add(Calendar.DAY_OF_YEAR, -1);
        calEOM.set(Calendar.HOUR_OF_DAY, 23);
        calEOM.set(Calendar.MINUTE, 59);
        calEOM.set(Calendar.SECOND, 59);
        calEOM.set(Calendar.MILLISECOND, 999);

        recursiveMonthEval(calNow, calEvaluate, calEOM, upcomingRange, pGen, pblnFirst);
    }

    private Calendar evaluateYearGeneration(int upcomingRange,
                                            long pdtmOrigPriority,
                                            TimeInstance pGen) {
        Calendar calEvaluate;
        Calendar calNow = Task_Display.getCurrentCalendar();

        //determine if calPriority is = the current day. If not (has to be before) use establish what a better starting week would be using starting and created details
        Calendar calBOY;
        Boolean blnSet = false;
        if(pdtmOrigPriority != -1){
            blnSet = true;
            calBOY = Task_Display.getCalendar(pdtmOrigPriority);
        } else {
            calBOY = Task_Display.getCalendar(mdtmCreated);
            //As we don't know when it was last generated the only way to know the starting week is to get on the proper frequency starting point is this way
            calBOY.set(Calendar.DAY_OF_YEAR, 1);
            calBOY.add(Calendar.YEAR,mintStarting);
        }

        Calendar calEOY = (Calendar) calBOY.clone();
        calEOY.set(Calendar.DAY_OF_YEAR, 1);
        calEOY.add(Calendar.YEAR, 1);
        calEOY.add(Calendar.DAY_OF_YEAR, -1);

        calBOY.set(Calendar.HOUR_OF_DAY,0);
        calBOY.set(Calendar.MINUTE,0);
        calBOY.set(Calendar.SECOND,0);
        calBOY.set(Calendar.MILLISECOND,0);

        calEOY.set(Calendar.HOUR_OF_DAY,23);
        calEOY.set(Calendar.MINUTE,59);
        calEOY.set(Calendar.SECOND,59);
        calEOY.set(Calendar.MILLISECOND,999);

        while(true){
            //I know this can be simplified but because it matches the established design I will leave it as it is
            if(calEOY.before(calNow)){
                blnSet = false;
                calBOY.add(Calendar.YEAR,(int)mlngRepetition);
                calEOY.add(Calendar.YEAR,(int)mlngRepetition);
            } else {
                if(blnSet){
                    if(calBOY.get(Calendar.DAY_OF_YEAR) == calEOY.get(Calendar.DAY_OF_YEAR)){
                        calBOY.set(Calendar.DAY_OF_YEAR, 1);
                        calBOY.add(Calendar.YEAR,(int)mlngRepetition);
                    } else calBOY.add(Calendar.DAY_OF_YEAR,1);
                }

                //If the repetition point has already passed go to the next year
                Calendar calTempFrom = Task_Display.getCalendar(mdtmFrom);
                calTempFrom.set(Calendar.YEAR, calBOY.get(Calendar.YEAR));
                if(calBOY.after(calTempFrom)){
                    calTempFrom.add(Calendar.YEAR,(int)mlngRepetition);
                }

                //Make sure time details are represented in passed out dates.
                Calendar calFrom = Task_Display.getCalendar(mdtmFrom);
                calTempFrom.set(Calendar.HOUR_OF_DAY,calFrom.get(Calendar.HOUR_OF_DAY));
                calTempFrom.set(Calendar.MINUTE,calFrom.get(Calendar.MINUTE));
                calTempFrom.set(Calendar.SECOND,calFrom.get(Calendar.SECOND));
                calTempFrom.set(Calendar.MILLISECOND,calFrom.get(Calendar.MILLISECOND));

                //Set dates
                pGen.mdtmPriority = calTempFrom.getTimeInMillis();
                calTempFrom.add(Calendar.DAY_OF_YEAR, upcomingRange * -1);
                pGen.mdtmUpcoming = calTempFrom.getTimeInMillis();
                return calTempFrom;
            }
        }
    }

    public void generateInstances(Boolean pblnInitial,
                                  long plngTaskId){
        long lngGenID = mlngGenerationID;
        Cursor tblTimeInstance = getValidGenerationPoints();

        Task tempTask = new Task(plngTaskId);
        while(tblTimeInstance.moveToNext()){
            if(pblnInitial || //for initial generation, we want the instance to generate for all possible generation points
                    tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngGenerationID")) > mlngGenerationID){ //after initial, we only want the instance generated when it hasn't already been generated
                Calendar tempTo;
                if(mblnThru){
                    tempTo = Task_Display.getCalendar(tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("fdtmPriority")));
                    tempTo.add(Calendar.DAY_OF_YEAR,tblTimeInstance.getInt(tblTimeInstance.getColumnIndex("fintThru")));
                    if(mblnToTime){
                        Calendar time = Task_Display.getCalendar(mdtmTo);
                        tempTo.set(Calendar.HOUR,time.get(Calendar.HOUR));
                        tempTo.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
                        tempTo.set(Calendar.SECOND, time.get(Calendar.SECOND));
                        tempTo.set(Calendar.MILLISECOND, time.get(Calendar.MILLISECOND));
                    }
                } else {
                    tempTo = Task_Display.getCalendar(mdtmTo);
                }

                if(plngTaskId != -1){
                    new TaskInstance(tempTask.mlngTaskID,
                            tempTask.mlngTaskDetailID,
                            tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("fdtmPriority")),
                            tempTo.getTimeInMillis(),
                            mblnFromTime,
                            mblnToTime,
                            mblnToDate,
                            Task_Display.getCurrentCalendar().getTimeInMillis());
                }else{
                    Cursor tblTaskCursor = DatabaseAccess.getRecordsFromTable("tblTask", "flngTimeID", mlngTimeID);
                    while (tblTaskCursor.moveToNext()) {
                        if(tblTaskCursor.getLong(tblTaskCursor.getColumnIndex("fdtmDeleted")) == -1){ //Add instance for all non deleted tasks
                            new TaskInstance(tblTaskCursor.getLong(tblTaskCursor.getColumnIndex("flngTaskID")),
                                    tblTaskCursor.getLong(tblTaskCursor.getColumnIndex("flngTaskDetailID")),
                                    tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("fdtmPriority")),
                                    tempTo.getTimeInMillis(),
                                    mblnFromTime,
                                    mblnToTime,
                                    mblnToDate,
                                    Task_Display.getCurrentCalendar().getTimeInMillis());
                        }
                    }
                }
                if(lngGenID < tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngGenerationID"))){ //Updates
                    lngGenID = tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngGenerationID"));
                }
            }
        }

        if(lngGenID > mlngGenerationID) updateGenerationID(lngGenID);
    }

    public Cursor getValidGenerationPoints(){
        String[] strColumns = null;
        String strSelection = "flngTimeID = ? and fdtmUpcoming <= ? and fdtmPriority >= ?";
        String[] strParms = new String[]{Long.toString(mlngTimeID),
                Long.toString(Task_Display.getEndCurrentDay().getTimeInMillis()),
                Long.toString(Task_Display.getBeginningCurentDay().getTimeInMillis())};

        return DatabaseAccess.mDatabase.query("tblTimeInstance",
                strColumns,
                strSelection,
                strParms,
                null,
                null,
                null);
    }

    //endregion

    //region Specific Updates
    public void updateGenerationID(long plngGenID){
        mlngGenerationID = plngGenID;
        saveTime();
    }

    public void completeTime(){
        mblnComplete = true;
        saveTime();
    }

    public void refreshInstances(){
        Cursor curTask = DatabaseAccess.mDatabase.query("tblTask",
                new String[] {"flngTaskID"},
                "fdtmDeleted = -1 and flngTimeID = ?",
                new String[] {Long.toString(mlngTimeID)},
                null,
                null,
                null);

        while (curTask.moveToNext()){
            Task tempTask = new Task(curTask.getLong(curTask.getColumnIndex("flngTaskID")));
            tempTask.clearActiveInstances();
        }

        generateInstances(true, -1);
    }
    //endregion
}

