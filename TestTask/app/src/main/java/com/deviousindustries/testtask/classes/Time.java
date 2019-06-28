package com.deviousindustries.testtask.classes;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.deviousindustries.testtask.DatabaseAccess;
import com.deviousindustries.testtask.Utilities;

import java.time.Duration;
import java.util.Calendar;

import static com.deviousindustries.testtask.constants.ConstantsKt.*;

//Todo: remove session detail id and replace w/ title column. No reason to have task detail if record isn't copied over and over to other places

@Entity(tableName = "tblTime")
public class Time {
    @PrimaryKey(autoGenerate = true)
    public long flngTimeID;
    public long fdtmFrom;
    public long fdtmTo;
    public boolean fblnFromTime;
    public boolean fblnToTime;
    public boolean fblnToDate;
    public int fintTimeframe;
    public long flngTimeframeID;
    @ColumnInfo(name = "flngRepetition")
    public int fintRepetition;
    public long fdtmCreated;
    public int fintStarting;
    public boolean fblnComplete;
    public long flngGenerationID;
    public boolean fblnThru;
    @NonNull
    public boolean fblnSession;
    @NonNull
    public String fstrTitle;

    @Ignore
    public Integer originalTimeframe;
    @Ignore
    public Week week;
    @Ignore
    public Month month;

    public static Time getInstance(Long timeID){
        if(timeID != NULL_OBJECT){
            Time temp = DatabaseAccess.taskDatabaseDao.loadTime(timeID);
            temp.week = temp.fintTimeframe == WEEK_POSITION ? Week.Companion.getInstance(temp.flngTimeframeID) : Week.Companion.getInstance(NULL_OBJECT);
            temp.month = temp.fintTimeframe == MONTH_POSITION ? Month.Companion.getInstance(temp.flngTimeframeID) : Month.Companion.getInstance(NULL_OBJECT);
            temp.originalTimeframe = temp.fintTimeframe;
            return temp;
        } else {
            return new Time();
        }
    }

    //region Constructors
    public Time(){
        flngTimeID = NULL_OBJECT;
        fdtmFrom = NULL_DATE;
        fdtmTo = NULL_DATE;
        fblnFromTime = false;
        fblnToTime = false;
        fblnToDate = false;
        fintTimeframe = NULL_POSITION;
        flngTimeframeID = NULL_OBJECT;
        fintRepetition = BASE_POSITION;
        fdtmCreated = Utilities.Companion.getCurrentCalendar().getTimeInMillis();
        fintStarting = NULL_POSITION;
        fblnComplete = false;
        flngGenerationID = NULL_OBJECT;
        fblnThru = false;
        fblnSession = false;
        fstrTitle = "";
        originalTimeframe = NULL_POSITION;
        month = new Month();
        week = new Week();
    }

    public Time(long plngTimeID,
                Long pdtmFrom,
                Long pdtmTo,
                Long pdtmCreated,
                boolean pblnFromTime,
                boolean pblnToTime,
                boolean pblnToDate,
                int pintTimeframe,
                long plngTimeframeID,
                int plngRepetition,
                int pintStarting,
                boolean pblnComplete,
                long plngGenerationID,
                boolean pblnThru){
        this();
        flngTimeID = plngTimeID;
        fdtmFrom = pdtmFrom;
        fdtmTo = pdtmTo;
        fblnToDate = pblnToDate;
        fblnFromTime = pblnFromTime;
        fblnToTime = pblnToTime;
        fintTimeframe = pintTimeframe;
        flngTimeframeID = plngTimeframeID;
        fintRepetition = plngRepetition;
        fdtmCreated = pdtmCreated;
        fintStarting = pintStarting;
        fblnComplete = pblnComplete;
        flngGenerationID = plngGenerationID;
        fblnThru = pblnThru;

        flngTimeID = DatabaseAccess.addRecordToTable("tblTime",
                new String[] {"fdtmFrom", "fdtmTo", "fblnFromTime", "fblnToTime", "fblnToDate","fintTimeframe", "flngTimeframeID", "flngRepetition",
                        "fdtmCreated", "fintStarting", "fblnComplete", "flngGenerationID", "fblnThru","fblnSession","fstrTitle"},
                new Object[] {fdtmFrom,
                        fdtmTo,
                        fblnFromTime,
                        fblnToTime,
                        fblnToDate,
                        fintTimeframe,
                        flngTimeframeID,
                        Long.parseLong(Integer.toString(fintRepetition)),
                        fdtmCreated,
                        fintStarting,
                        fblnComplete,
                        flngGenerationID,
                        fblnThru,
                        fblnSession,
                        fstrTitle},
                "flngTimeID",
                flngTimeID);

        if(!fblnComplete) buildTimeInstances();
    }
    //endregion

    public void saveTime(){
        DatabaseAccess.mDatabase.beginTransaction();
        try{
            flngTimeframeID = saveTimeframe();
            if (flngTimeID != NULL_OBJECT) {
                clearGenerationPoints();
                DatabaseAccess.taskDatabaseDao.updateTime(this);
            }
            else {
                flngTimeID = DatabaseAccess.taskDatabaseDao.insertTime(this);
            }
            if(!fblnComplete) buildTimeInstances();
            refreshTaskInstances();
            DatabaseAccess.mDatabase.setTransactionSuccessful();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseAccess.mDatabase.endTransaction();
        }
    }

    public void updateToSession(String title){
        try{
            DatabaseAccess.mDatabase.beginTransaction();
            fstrTitle = title;
            fblnSession = true;
            DatabaseAccess.taskDatabaseDao.updateTime(this);
            DatabaseAccess.mDatabase.setTransactionSuccessful();
        } catch(Exception e){
            e.printStackTrace();
        } finally{
            DatabaseAccess.mDatabase.endTransaction();
        }
    }

    private Long saveTimeframe() {
        Long returnTimeframeID = flngTimeframeID;

        //Establish what needs to be deleted if anything
        if (originalTimeframe != fintTimeframe && originalTimeframe != NULL_POSITION) {
            //possibly delete
            //do not use original ID
            returnTimeframeID = NULL_OBJECT;
        }

        switch (getTimeframe()) {
            case 0 :  //Day
            {
                if(returnTimeframeID != NULL_OBJECT){} //Don't need to do this becase day currently has nothing to update
                    else returnTimeframeID = DatabaseAccess.taskDatabaseDao.insertDay(new Day());
                    break;
            }
            case 1 : //Week
            {
                if(returnTimeframeID != NULL_OBJECT) DatabaseAccess.taskDatabaseDao.updateWeek(week);
                    else returnTimeframeID = DatabaseAccess.taskDatabaseDao.insertWeek(week);
                    break;
            }
            case 2 : //Month
            {
                if(returnTimeframeID != NULL_OBJECT) DatabaseAccess.taskDatabaseDao.updateMonth(month);
                    else returnTimeframeID = DatabaseAccess.taskDatabaseDao.insertMonth(month);
                    break;
            }
            case 3 : //Year
            {
                if(returnTimeframeID != NULL_OBJECT) {}//Don't need to do this becase year currently has nothing to update
                    else returnTimeframeID = DatabaseAccess.taskDatabaseDao.insertYear(new Year());
                    break;
            }
        }

        return returnTimeframeID;
    }

    private Integer getTimeframe(){
        if(fintRepetition == BASE_POSITION) return NULL_POSITION;
        return fintTimeframe;
    }

    //region TimeGeneration Class
    private boolean timeInstanceExist(){
        return DatabaseAccess.getRecordsFromTable("tblTimeInstance", "flngTimeID", flngTimeID).getCount() > 0;
    }

    private long getLatestPriorityAndThru(){
        //Returns the latest mTime instance associated w/ a mTime w/ the inclusing on the thru value.
        //This is important as you don't want to be evaluating dates already handled by a thru value.
        try(Cursor latestTimeInstance = DatabaseAccess.retrieveMostRecent("tblTimeInstance","flngTimeID", flngTimeID, "flngGenerationID")) {
            if (latestTimeInstance.moveToFirst()) {
                Calendar temp = Utilities.Companion.getCalendar(latestTimeInstance.getLong(latestTimeInstance.getColumnIndex("fdtmPriority")));
                temp.add(Calendar.DAY_OF_YEAR, latestTimeInstance.getInt(latestTimeInstance.getColumnIndex("fintThru")));
                return temp.getTimeInMillis();
            }
        }
        return NULL_DATE;
    }

    private long getNextPriority(boolean pblnTo){
        try(Cursor tblGeneration = DatabaseAccess.getValidGenerationPoints(true, false, flngTimeID)){
            if(tblGeneration.moveToFirst()){
                Calendar calPri = Utilities.Companion.getCalendar(tblGeneration.getLong(tblGeneration.getColumnIndex("fdtmPriority")));
                if(pblnTo){
                    calPri.add(Calendar.DAY_OF_YEAR,tblGeneration.getInt(tblGeneration.getColumnIndex("fintThru")));
                    if(fblnToTime){
                        Calendar time = Utilities.Companion.getCalendar(fdtmTo);
                        calPri.set(Calendar.HOUR_OF_DAY,time.get(Calendar.HOUR_OF_DAY));
                        calPri.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
                        calPri.set(Calendar.SECOND, time.get(Calendar.SECOND));
                        calPri.set(Calendar.MILLISECOND, time.get(Calendar.MILLISECOND));
                    }
                }
                return calPri.getTimeInMillis();
            }
        }
        return NULL_DATE;
    }

    private long getMaxUpcoming(){
        try(Cursor tblGeneration = DatabaseAccess.retrieveMaxUpcoming(flngTimeID)){
            tblGeneration.moveToFirst();
            long rtn = tblGeneration.getLong(tblGeneration.getColumnIndex("fdtmUpcomingMax"));
            return rtn == 0 ? NULL_DATE : rtn;
        }
    }

    public void clearGenerationPoints(){

        //Finds and deletes all time instances
        for(TimeInstance instance : DatabaseAccess.taskDatabaseDao.loadTimeInstancesFromTime(flngTimeID)){
            instance.delete();
        }

        //resets the generation ID
        flngGenerationID = NULL_OBJECT;
        DatabaseAccess.taskDatabaseDao.updateTime(this);
    }

    public void buildTimeInstances(){
        while(true){
            //While we can still attempt to generate an upcoming task that should be generated before today and while the mTime isn't already exempt (complete)
            TimeInstance tGen = new TimeInstance(flngTimeID);
            boolean blnSaveGen = true;
            if(getMaxUpcoming() <= Utilities.Companion.getEndCurrentDay().getTimeInMillis() && !fblnComplete){
                if(fintRepetition != (long)0){
                    //establish what repetition tasks associated w/ and whether current date fits
                    switch(fintTimeframe){
                        case 0: //Day
                            evaluateDayGeneration(Integer.parseInt(Utilities.preferences.getString("upcoming_day","1")), getLatestPriorityAndThru(), tGen);
                            break;
                        case 1: //Week
                            evaluateWeekGeneration(Integer.parseInt(Utilities.preferences.getString("upcoming_week","1")), getLatestPriorityAndThru(), tGen);
                            break;
                        case 2: //Month
                            evaluateMonthGeneration(Integer.parseInt(Utilities.preferences.getString("upcoming_month","1")), getLatestPriorityAndThru(), tGen);
                            break;
                        case 3: //Year
                            evaluateYearGeneration(Integer.parseInt(Utilities.preferences.getString("upcoming_year","1")), getLatestPriorityAndThru(), tGen);
                            break;
                    }
                } else {
                    if(!timeInstanceExist()){ //If not previously evaluated, evaluate for the first and only mTime
                        evaluateDate(Integer.parseInt(Utilities.preferences.getString("upcoming_std","1")),tGen);
                    } else{
                        completeTime();
                        blnSaveGen = false; //Don't want to save a new mTime generation if all we did was complete the mTime.
                    }
                }
                if(blnSaveGen) tGen.save();
            }else{
                break;
            }
        }
    }

    private void evaluateDate(int upcomingRange, TimeInstance pGen){
        pGen.fdtmPriority = fdtmFrom;
        Calendar tempUp = Utilities.Companion.getCalendar(fdtmFrom);
        tempUp.add(Calendar.DAY_OF_YEAR, -upcomingRange);
        pGen.fdtmUpcoming = tempUp.getTimeInMillis();

        Calendar tempFrom = Utilities.Companion.getCalendar(fdtmFrom);
        Calendar tempTo = Utilities.Companion.getCalendar(fdtmTo);
        int diff = (int)Duration.between(tempFrom.toInstant(), tempTo.toInstant()).toDays();
        pGen.fintThru = diff > 0 ? diff : 0;
    }

    private void evaluateDayGeneration(int upcomingRange,
                                       long pdtmOrigPriority,
                                       TimeInstance pGen) {
        Calendar calEvaluate;
        Calendar calNow = Utilities.Companion.getCurrentCalendar();

        //Establishing starting date (either create + starting or prior priority)
        Calendar calBOD;
        boolean blnSet = false;
        if(pdtmOrigPriority != NULL_DATE){
            calBOD = Utilities.Companion.getCalendar(pdtmOrigPriority);
            blnSet = true;
        } else {
            calBOD = Utilities.Companion.getCalendar(fdtmCreated);
            //As we don't know when it was last generated the only way to know the starting week is to get on the proper frequency starting point is this way
            calBOD.add(Calendar.DAY_OF_YEAR, fintStarting);
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

        //Move the bounds by the occurs metric until the ending bound occurs after the current day/mTime.
        while(true){
            if(calEOD.before(calNow)){
                calBOD.add(Calendar.DAY_OF_YEAR,(int) fintRepetition);
                calEOD.add(Calendar.DAY_OF_YEAR,(int) fintRepetition);
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

        //Get from date cal and provide mTime details from it to calEvaluate
        Calendar calFrom = Utilities.Companion.getCalendar(fdtmFrom);
        calEvaluate.set(Calendar.HOUR_OF_DAY,calFrom.get(Calendar.HOUR_OF_DAY));
        calEvaluate.set(Calendar.MINUTE,calFrom.get(Calendar.MINUTE));
        calEvaluate.set(Calendar.SECOND,calFrom.get(Calendar.SECOND));
        calEvaluate.set(Calendar.MILLISECOND,calFrom.get(Calendar.MILLISECOND));

        pGen.fdtmPriority = calEvaluate.getTimeInMillis();
        Calendar calUpcoming = (Calendar) calEvaluate.clone();
        calUpcoming.add(Calendar.DAY_OF_YEAR, -1 * upcomingRange);
        pGen.fdtmUpcoming = calUpcoming.getTimeInMillis();
    }

    private void evaluateWeekGeneration(int upcomingRange,
                                        long pdtmOrigPriority,
                                        TimeInstance pGen){
        Calendar calEvaluate;
        Calendar calNow = Utilities.Companion.getCurrentCalendar();

        //determine if calPriority is = the current day. If not (has to be before) use establish what a better starting week would be using starting and created details
        Calendar calBOW;
        boolean blnSet = false;
        if(pdtmOrigPriority != NULL_DATE){
            calBOW = Utilities.Companion.getCalendar(pdtmOrigPriority);
            blnSet = true;
        } else {
            calBOW = Utilities.Companion.getCalendar(fdtmCreated);
            //As we don't know when it was last generated the only way to know the starting week is to get on the proper frequency starting point is this way
            calBOW.add(Calendar.WEEK_OF_YEAR, fintStarting);
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
                calBOW.add(Calendar.WEEK_OF_YEAR,(int) fintRepetition);
                calEOW.add(Calendar.WEEK_OF_YEAR,(int) fintRepetition);
                blnSet = false;
            } else {
                //If we set the date to today via priority, make sure we don't reevaluate today.
                if(blnSet)
                {
                    //As going past the end of the week means that we need to reevaluate the repetition quantity, we need to see if day is saturday before simply moving up a day.
                    if(calBOW.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
                        calBOW.setWeekDate(calBOW.getWeekYear(), calBOW.get(Calendar.WEEK_OF_YEAR), Calendar.SUNDAY);
                        calBOW.add(Calendar.WEEK_OF_YEAR, (int) fintRepetition);
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
        boolean blnComplete = false;
        try(Cursor tblWeek = DatabaseAccess.getRecordsFromTable("tblWeek", "flngWeekID", flngTimeframeID)){
            tblWeek.moveToFirst();

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
                if(tblWeek.getInt(tblWeek.getColumnIndex("fbln" + columnName)) == 1){
                    //Don't need to evaluate for days past.
                    if(calWeekday.after(calNow)){
                        //Make sure mTime details are represented in passed out dates.
                        Calendar calFrom = Utilities.Companion.getCalendar(fdtmFrom);
                        calWeekday.set(Calendar.HOUR_OF_DAY,calFrom.get(Calendar.HOUR_OF_DAY));
                        calWeekday.set(Calendar.MINUTE,calFrom.get(Calendar.MINUTE));
                        calWeekday.set(Calendar.SECOND,calFrom.get(Calendar.SECOND));
                        calWeekday.set(Calendar.MILLISECOND,calFrom.get(Calendar.MILLISECOND));

                        if(pblnFirst){
                            pGen.fdtmPriority = calWeekday.getTimeInMillis();
                            Calendar calUpcoming = (Calendar) calWeekday.clone();
                            calUpcoming.add(Calendar.DAY_OF_YEAR, -upcomingRange);
                            pGen.fdtmUpcoming = calUpcoming.getTimeInMillis();
                        }
                        if(fblnThru){
                            if(!pblnFirst){
                                if(Utilities.Companion.getCalendar(calWeekday.getTimeInMillis(),true, false)
                                        .equals(Utilities.Companion.getCalendar(calEvaluate.getTimeInMillis(),true,false))){
                                    pGen.fintThru ++;
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
                                calEvaluate.add(Calendar.WEEK_OF_YEAR, (int) fintRepetition);
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
        }

        if(blnComplete) return;
        //Othewise, establish what next evaluated week will be and call itself
        calEvaluate.setWeekDate(calEvaluate.getWeekYear(), calEvaluate.get(Calendar.WEEK_OF_YEAR), Calendar.SUNDAY);
        calEvaluate.add(Calendar.WEEK_OF_YEAR, (int) fintRepetition);
        recursiveWeekEval(calNow, calEvaluate, upcomingRange, pGen, pblnFirst);
    }

    private void evaluateMonthGeneration(int upcomingRange,
                                         long pdtmOrigPriority,
                                         TimeInstance pGen) {

        Calendar calEvaluate;
        Calendar calNow = Utilities.Companion.getCurrentCalendar();

        //Establishing starting date (either create + starting or prior priority)
        Calendar calBOM;
        boolean blnSet = false;
        if(pdtmOrigPriority != NULL_DATE){
            calBOM = Utilities.Companion.getCalendar(pdtmOrigPriority);
            blnSet = true;
        } else {
            calBOM = Utilities.Companion.getCalendar(fdtmCreated);
            //As we don't know when it was last generated the only way to know the starting week is to get on the proper frequency starting point is this way
            calBOM.add(Calendar.DAY_OF_YEAR, fintStarting);
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

        //Move the bounds by the occurs metric until the ending bound occurs after the current day/mTime.
        while (true) {
            if (calEOM.before(calNow)) {
                if(blnSet) calBOM.set(Calendar.DAY_OF_MONTH,1);//resets DOM to first so that adding month will continue from first
                calBOM.add(Calendar.MONTH, (int) fintRepetition);
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
                        calBOM.add(Calendar.MONTH, (int) fintRepetition);
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

        boolean blnComplete = false;
        Calendar calMonth = (Calendar) calEvaluate.clone();
        try(Cursor tblMonth = DatabaseAccess.getRecordsFromTable("tblMonth", "flngMonthID", flngTimeframeID)){
            tblMonth.moveToFirst();
            int month = calEvaluate.get(Calendar.MONTH);
            while (calMonth.get(Calendar.MONTH) == month) {
                if (calMonth.after(calNow)){
                    if (tblMonth.getString(tblMonth.getColumnIndex("fstrSpecific")).equals("")) {
                        //General Dates (First Last Middle)
                        //First
                        if (    //First
                                (calMonth.get(Calendar.DAY_OF_MONTH) == 1 &&
                                        tblMonth.getLong(tblMonth.getColumnIndex("fblnFirst")) == 1) ||
                                        //Last
                                        (calMonth.get(Calendar.DAY_OF_MONTH) == calEOM.get(Calendar.DAY_OF_MONTH) &&
                                                tblMonth.getLong(tblMonth.getColumnIndex("fblnLast")) == 1) ||
                                        //Middle
                                        (calMonth.get(Calendar.DAY_OF_MONTH) == Integer.parseInt(Utilities.preferences.getString("middle_month","15")) &&
                                                tblMonth.getLong(tblMonth.getColumnIndex("fblnMiddle")) == 1)) {
                            if (tblMonth.getLong(tblMonth.getColumnIndex("fblnAfterWkn")) == 1) {
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

                            //Make sure mTime details are represented in passed out dates.
                            Calendar calFrom = Utilities.Companion.getCalendar(fdtmFrom);
                            calMonth.set(Calendar.HOUR_OF_DAY,calFrom.get(Calendar.HOUR_OF_DAY));
                            calMonth.set(Calendar.MINUTE,calFrom.get(Calendar.MINUTE));
                            calMonth.set(Calendar.SECOND,calFrom.get(Calendar.SECOND));
                            calMonth.set(Calendar.MILLISECOND,calFrom.get(Calendar.MILLISECOND));

                            pGen.fdtmPriority = calMonth.getTimeInMillis();
                            Calendar tempUp = (Calendar) calMonth.clone();
                            tempUp.add(Calendar.DAY_OF_MONTH, -upcomingRange);
                            pGen.fdtmUpcoming = tempUp.getTimeInMillis();
                            blnComplete = true;
                            break;
                        }
                    } else {
                        String[] strSpecificDays = tblMonth.getString(tblMonth.getColumnIndex("fstrSpecific")).split(",");
                        for (String strSpecificDay : strSpecificDays) {
                            if (calMonth.get(Calendar.DAY_OF_MONTH) == Long.parseLong(strSpecificDay.trim())) {
                                //Make sure mTime details are represented in passed out dates.
                                Calendar calFrom = Utilities.Companion.getCalendar(fdtmFrom);
                                calMonth.set(Calendar.HOUR_OF_DAY, calFrom.get(Calendar.HOUR_OF_DAY));
                                calMonth.set(Calendar.MINUTE, calFrom.get(Calendar.MINUTE));
                                calMonth.set(Calendar.SECOND, calFrom.get(Calendar.SECOND));
                                calMonth.set(Calendar.MILLISECOND, calFrom.get(Calendar.MILLISECOND));

                                if (pblnFirst) {
                                    pGen.fdtmPriority = calMonth.getTimeInMillis();
                                    Calendar tempUp = (Calendar) calMonth.clone();
                                    tempUp.add(Calendar.DAY_OF_MONTH, -upcomingRange);
                                    pGen.fdtmUpcoming = tempUp.getTimeInMillis();
                                }
                                if (fblnThru) {
                                    if (!pblnFirst) {
                                        if (Utilities.Companion.getCalendar(calMonth.getTimeInMillis(), true, false)
                                                .equals(Utilities.Companion.getCalendar(calEvaluate.getTimeInMillis(), true, false))) {
                                            pGen.fintThru++;
                                        } else {
                                            blnComplete = true;
                                            break;
                                        }
                                    } else {
                                        calEvaluate = (Calendar) calMonth.clone();
                                    }
                                    //Establish next day
                                    if (calEvaluate.get(Calendar.DAY_OF_MONTH) == calEOM.get(Calendar.DAY_OF_MONTH)) {
                                        calEvaluate.set(Calendar.DAY_OF_MONTH, 1);
                                        calEvaluate.add(Calendar.MONTH, (int) fintRepetition);
                                        calEOM = (Calendar) calEvaluate.clone();
                                        calEOM.add(Calendar.MONTH, 1);
                                        calEOM.add(Calendar.DAY_OF_YEAR, -1);
                                        calEOM.set(Calendar.HOUR_OF_DAY, 23);
                                        calEOM.set(Calendar.MINUTE, 59);
                                        calEOM.set(Calendar.SECOND, 59);
                                        calEOM.set(Calendar.MILLISECOND, 999);
                                    } else calEvaluate.add(Calendar.DAY_OF_YEAR, 1);
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
        }

        if(blnComplete) return;

        //Othewise, establish what next evaluated month will be and call itself
        calEvaluate.set(Calendar.DAY_OF_MONTH,1);
        calEvaluate.add(Calendar.MONTH, (int) fintRepetition);
        calEOM = (Calendar) calEvaluate.clone();
        calEOM.add(Calendar.MONTH, 1);
        calEOM.add(Calendar.DAY_OF_YEAR, -1);
        calEOM.set(Calendar.HOUR_OF_DAY, 23);
        calEOM.set(Calendar.MINUTE, 59);
        calEOM.set(Calendar.SECOND, 59);
        calEOM.set(Calendar.MILLISECOND, 999);

        recursiveMonthEval(calNow, calEvaluate, calEOM, upcomingRange, pGen, pblnFirst);
    }

    private void evaluateYearGeneration(int upcomingRange,
                                        long pdtmOrigPriority,
                                        TimeInstance pGen) {
        Calendar calNow = Utilities.Companion.getCurrentCalendar();

        //determine if calPriority is = the current day. If not (has to be before) use establish what a better starting week would be using starting and created details
        Calendar calBOY;
        boolean blnSet = false;
        if(pdtmOrigPriority != NULL_DATE){
            blnSet = true;
            calBOY = Utilities.Companion.getCalendar(pdtmOrigPriority);
        } else {
            calBOY = Utilities.Companion.getCalendar(fdtmCreated);
            //As we don't know when it was last generated the only way to know the starting week is to get on the proper frequency starting point is this way
            calBOY.set(Calendar.DAY_OF_YEAR, 1);
            calBOY.add(Calendar.YEAR, fintStarting);
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
                calBOY.add(Calendar.YEAR,(int) fintRepetition);
                calEOY.add(Calendar.YEAR,(int) fintRepetition);
            } else {
                if(blnSet){
                    if(calBOY.get(Calendar.DAY_OF_YEAR) == calEOY.get(Calendar.DAY_OF_YEAR)){
                        calBOY.set(Calendar.DAY_OF_YEAR, 1);
                        calBOY.add(Calendar.YEAR,(int) fintRepetition);
                    } else calBOY.add(Calendar.DAY_OF_YEAR,1);
                }

                //If the repetition point has already passed go to the next year
                Calendar calTempFrom = Utilities.Companion.getCalendar(fdtmFrom);
                calTempFrom.set(Calendar.YEAR, calBOY.get(Calendar.YEAR));
                if(calBOY.after(calTempFrom)){
                    calTempFrom.add(Calendar.YEAR,(int) fintRepetition);
                }

                //Make sure mTime details are represented in passed out dates.
                Calendar calFrom = Utilities.Companion.getCalendar(fdtmFrom);
                calTempFrom.set(Calendar.HOUR_OF_DAY,calFrom.get(Calendar.HOUR_OF_DAY));
                calTempFrom.set(Calendar.MINUTE,calFrom.get(Calendar.MINUTE));
                calTempFrom.set(Calendar.SECOND,calFrom.get(Calendar.SECOND));
                calTempFrom.set(Calendar.MILLISECOND,calFrom.get(Calendar.MILLISECOND));

                //Set dates
                pGen.fdtmPriority = calTempFrom.getTimeInMillis();
                calTempFrom.add(Calendar.DAY_OF_YEAR, -upcomingRange);
                pGen.fdtmUpcoming = calTempFrom.getTimeInMillis();
                return;
            }
        }
    }

    public Time createOneOff(long plngTimeID){
        return new Time(plngTimeID,
                getNextPriority(false),
                getNextPriority(true),
                Utilities.Companion.getCurrentCalendar().getTimeInMillis(),
                fblnFromTime,
                fblnToTime,
                fblnToDate,
                NULL_POSITION,
                NULL_OBJECT,
                BASE_POSITION,
                NULL_POSITION,
                false,
                NULL_OBJECT,
                false);
    }

    public void generateInstance(long pdtmFrom,
                          long pdtmTo){

        Calendar tempTo = Utilities.Companion.getCalendar(pdtmTo);
        if(fblnToTime){
            Calendar time = Utilities.Companion.getCalendar(fdtmTo);
            tempTo.set(Calendar.HOUR_OF_DAY,time.get(Calendar.HOUR_OF_DAY));
            tempTo.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
            tempTo.set(Calendar.SECOND, time.get(Calendar.SECOND));
            tempTo.set(Calendar.MILLISECOND, time.get(Calendar.MILLISECOND));
        }

        try(Cursor taskList = DatabaseAccess.getTasksFromTime(flngTimeID)){
            while(taskList.moveToNext()){
                Task tempTask = new Task(taskList.getLong(taskList.getColumnIndex("flngTaskID")));
                tempTask.generateInstance(pdtmFrom,
                        tempTo.getTimeInMillis(),
                        fblnFromTime,
                        fblnToTime,
                        fblnToDate,
                        fblnSession ? flngTimeID : NULL_OBJECT);
            }
        }
    }

    public void generateInstances(Boolean pblnInitial,
                           long plngTaskId){
        long lngGenID = flngGenerationID;
        try(Cursor tblTimeInstance = DatabaseAccess.getValidGenerationPoints(true, true, flngTimeID)){
            while(tblTimeInstance.moveToNext()){
                if(pblnInitial || //for initial generation of tasks associated w/ sessions, we want the instance to generate for all possible generation points
                        tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngGenerationID")) > flngGenerationID){ //after initial, we only want the instance generated when it hasn't already been generated
                    Calendar tempTo;
                    if(fblnThru){
                        tempTo = Utilities.Companion.getCalendar(tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("fdtmPriority")));
                        tempTo.add(Calendar.DAY_OF_YEAR,tblTimeInstance.getInt(tblTimeInstance.getColumnIndex("fintThru")));
                        if(fblnToTime){
                            Calendar time = Utilities.Companion.getCalendar(fdtmTo);
                            tempTo.set(Calendar.HOUR_OF_DAY,time.get(Calendar.HOUR_OF_DAY));
                            tempTo.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
                            tempTo.set(Calendar.SECOND, time.get(Calendar.SECOND));
                            tempTo.set(Calendar.MILLISECOND, time.get(Calendar.MILLISECOND));
                        }
                    } else {
                        tempTo = Utilities.Companion.getCalendar(fdtmTo);
                    }

                    Task tempTask;
                    if(plngTaskId != NULL_OBJECT){
                        tempTask = new Task(plngTaskId);
                        tempTask.generateInstance(tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("fdtmPriority")),
                                tempTo.getTimeInMillis(),
                                fblnFromTime,
                                fblnToTime,
                                fblnToDate,
                                fblnSession ? flngTimeID : NULL_OBJECT);
                    }else{
                        try(Cursor tblTask = DatabaseAccess.getRecordsFromTable("tblTask", "flngTimeID", flngTimeID)){
                            while (tblTask.moveToNext()) {
                                tempTask = new Task(tblTask.getLong(tblTask.getColumnIndex("flngTaskID")));
                                if(tempTask.fdtmDeleted == NULL_DATE){
                                    tempTask.generateInstance(tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("fdtmPriority")),
                                            tempTo.getTimeInMillis(),
                                            fblnFromTime,
                                            fblnToTime,
                                            fblnToDate || tblTimeInstance.getInt(tblTimeInstance.getColumnIndex("fintThru")) > 0,
                                            fblnSession ? flngTimeID : NULL_OBJECT);
                                }
                            }
                        }
                    }
                    if(lngGenID < tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngGenerationID"))){ //Updates
                        lngGenID = tblTimeInstance.getLong(tblTimeInstance.getColumnIndex("flngGenerationID"));
                    }
                }
            }
        }

        if(lngGenID > flngGenerationID) updateGenerationID(lngGenID);
    }

    //endregion

    //region Specific Updates
    public void setAsSession(String pstrSessionTitle){
        fblnSession = true;
        fstrTitle = pstrSessionTitle;
        DatabaseAccess.updateRecordFromTable("tblTime","flngTimeID", flngTimeID,
                new String[]{"fblnSession","fstrTitle"},
                new Object[]{fblnSession, fstrTitle});
    }

    public void updateGenerationID(long plngGenID){
        flngGenerationID = plngGenID;
        DatabaseAccess.updateRecordFromTable("tblTime","flngTimeID", flngTimeID,
                new String[]{"flngGenerationID"},
                new Object[]{plngGenID});
    }

    public void completeTime(){
        fblnComplete = true;
        DatabaseAccess.taskDatabaseDao.updateTime(this);
    }

    public void refreshTaskInstances(){
        try(Cursor curTask = DatabaseAccess.getTasksFromTime(flngTimeID)){
            while (curTask.moveToNext()){
                Task tempTask = new Task(curTask.getLong(curTask.getColumnIndex("flngTaskID")));
                tempTask.finishActiveInstances(3);
            }
        }

        generateInstances(true, NULL_OBJECT);
    }



    public void finishTaskInstances(int pintCompleteType){
        try(Cursor curTask = DatabaseAccess.getTasksFromTime(flngTimeID)) {
            while (curTask.moveToNext()) {
                Task tempTask = new Task(curTask.getLong(curTask.getColumnIndex("flngTaskID")));
                tempTask.finishActiveInstances(pintCompleteType);
            }
        }
    }

    public void deleteTime(){
        try{
            DatabaseAccess.mDatabase.beginTransaction();
            //complete mTime
            completeTime();
            //remove timeInstances
            clearGenerationPoints();
            DatabaseAccess.mDatabase.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            DatabaseAccess.mDatabase.endTransaction();
        }
    }

    public Time getCopy(){

        return new Time(NULL_OBJECT,
                fdtmFrom,
                fdtmTo,
                fdtmCreated,
                fblnFromTime,
                fblnToTime,
                fblnToDate,
                fintTimeframe,
                flngTimeframeID,
                fintRepetition,
                fintStarting,
                fblnComplete,
                NULL_OBJECT, //TODO: Once mTime instances generated this should be set to the corresponding mTime instance for the new mTime so that duplicated tasks aren't re-created.
                fblnThru);
    }
    //endregion
}

