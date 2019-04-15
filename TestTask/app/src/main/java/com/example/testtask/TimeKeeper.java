package com.example.testtask;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.app.AlertDialog.Builder;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeKeeper extends ConstraintLayout implements View.OnClickListener {

    static int mSetIndicator = -1; //Used to determine which time button was selected
    static int[] arrSpecificDays;
    static int intArrayCounter;
    static Time mTime = new Time();
    static long mdtmFrom = -1;
    static long mdtmTo = -1;
    static boolean mblnFromTime;
    static boolean mblnToTime;
    static boolean mblnToDate;
    static int mintMode = 1; //Used for visability 1: Normal 2: Session 3: Task Instance


    //View objects
    Spinner timerangeSpinner;
    ArrayListContainer repetitionSpinner;
    ConstraintLayout cLayMonthly;
    ConstraintLayout cLayWeekly;
    ConstraintLayout cLayNoRep;
    static Button btnFromDate;
    static Button btnToDate;
    static Button btnFromTime;
    static Button btnToTime;
    static EditText txtMonthlyDays;

    public TimeKeeper(Context context){
        super(context);
    }

    public TimeKeeper(Context context, AttributeSet attrs) {
        super(context, attrs);

        View.inflate(context, R.layout.activity_time_keeper, this); //This is super important. Inflate loads the screen from XML.
        //Set member access
        arrSpecificDays = new int[31];
        intArrayCounter = 0;
        repetitionSpinner = new ArrayListContainer();
        timerangeSpinner = (Spinner) findViewById(R.id.spnTimeRange);
        timerangeSpinner.setOnItemSelectedListener(timeframeListener);

        //Target View Groups
        cLayNoRep = (ConstraintLayout) findViewById(R.id.CLayNoFrequency);
        cLayWeekly = (ConstraintLayout) findViewById(R.id.CLayWeekly);
        cLayMonthly = (ConstraintLayout) findViewById(R.id.CLayMonthly);

        //Target Generic Fields
        repetitionSpinner.LinkArrayToSpinner((Spinner) findViewById(R.id.spnRepitition), getContext());
        repetitionSpinner.mSpinner.setOnItemSelectedListener(repetitionListener);
        btnFromTime = (Button) findViewById(R.id.Timekeeper_BtnFromTime);
        btnToTime = (Button) findViewById(R.id.Timekeeper_BtnToTime);

        //Target No Frequency and Year Fields
        btnFromDate = (Button) findViewById(R.id.Timekeeper_NoFreq_BtnFromDate);
        btnToDate = (Button) findViewById(R.id.Timekeeper_NoFreq_BtnToDate);

        //Target Month Fields
        txtMonthlyDays = (EditText) findViewById(R.id.TimeKeeper_Monthly_Txt_Display);

        //Set Listeners
        btnFromTime.setOnClickListener(this);
        btnToTime.setOnClickListener(this);
        btnFromDate.setOnClickListener(this);
        btnToDate.setOnClickListener(this);
        findViewById(R.id.TimeKeeper_Monthly_Gen).setOnClickListener(this);
        findViewById(R.id.TimeKeeper_Monthly_Spec).setOnClickListener(this);
        findViewById(R.id.TimeKeeper_Monthly_Btn_Add).setOnClickListener(this);

        resetTimeKeeper(); //This may not be necessary as right now the id's and such are not static, however they might become necessary in the future...
    }

    public TimeKeeper(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }

    //If you want to pass in a pre-defined function you can either do this or implement the onCallListener and do it the way you're doing w/o this.
    /*View.OnClickListener clickListener = new OnClickListener() {
        public void onClick(View v) {
            // do something here
        }
    };*/

    Spinner.OnItemSelectedListener repetitionListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            evaluateRepetitionView(repetitionSpinner.getID(position));
        }

        public void onNothingSelected(AdapterView parent){

        }
    };

    Spinner.OnItemSelectedListener timeframeListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            evaluateTimeframeView(position);
        }

        public void onNothingSelected(AdapterView parent){

        }
    };

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.Timekeeper_BtnFromTime:
                mSetIndicator = 1;
                instantiateTimeFragment();
                break;
            case R.id.Timekeeper_BtnToTime:
                mSetIndicator = 2;
                instantiateTimeFragment();
                break;
            case R.id.Timekeeper_NoFreq_BtnFromDate:
                mSetIndicator = 3;
                instantiateDateFragment();
                break;
            case R.id.Timekeeper_NoFreq_BtnToDate:
                mSetIndicator = 4;
                instantiateDateFragment();
                break;
            case R.id.TimeKeeper_Monthly_Gen:
                findViewById(R.id.TimeKeeper_Monthly_First).setVisibility(VISIBLE);
                findViewById(R.id.TimeKeeper_Monthly_Middle).setVisibility(VISIBLE);
                findViewById(R.id.TimeKeeper_Monthly_Last).setVisibility(VISIBLE);
                findViewById(R.id.TimeKeeper_Monthly_AfterWkn).setVisibility(VISIBLE);
                findViewById(R.id.TimeKeeper_Monthly_Btn_Add).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Txt_Display).setVisibility(GONE);
                break;
            case R.id.TimeKeeper_Monthly_Spec:
                findViewById(R.id.TimeKeeper_Monthly_First).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Middle).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Last).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_AfterWkn).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Btn_Add).setVisibility(VISIBLE);
                findViewById(R.id.TimeKeeper_Monthly_Txt_Display).setVisibility(VISIBLE);
                break;
            case R.id.TimeKeeper_Monthly_Btn_Add:
                AppCompatActivity context = (AppCompatActivity)getContext();
                DialogFragment newFragment = new TimeKeeper.NumberPickerDialog();
                newFragment.show(context.getSupportFragmentManager(), "numberPicker");
                break;
        }
    }

    public void loadTimeDetails(long pdtmFrom,
                                long pdtmTo,
                                boolean pblnFromTime,
                                boolean pblnToTime,
                                boolean pblnToDate){
        setFromDate(pdtmFrom);
        if(pblnToDate){
            setToDate(pdtmTo);
        }
        if(pblnFromTime){
            setFromTime(pdtmFrom);
        }
        if(pblnToTime){
            setToTime(pdtmTo);
        }
    }

    public void loadTimeDetails(Long plngTimeID) {
        mTime = new Time(plngTimeID);

        setFromDate(mTime.mdtmFrom);
        if(mTime.mblnToDate) setToDate(mTime.mdtmTo);
        if(mTime.mblnFromTime) setFromTime(mTime.mdtmFrom);
        if(mTime.mblnToTime) setToTime(mTime.mdtmTo);
        mblnFromTime = mTime.mblnFromTime;
        mblnToTime = mTime.mblnToTime;
        mblnToDate = mTime.mblnToDate;

        //Set repetition spinner
        if (mTime.mlngRepetition != -1){
            repetitionSpinner.setIDSpinner(mTime.mlngRepetition);
        }

        //Set timeframe spinner
        if (mTime.mlngTimeframeID != -1) {
            Cursor cursor;
            timerangeSpinner.setSelection(mTime.mintTimeframe);
            switch (mTime.mintTimeframe) {
                case 0: //Day
                    //Dont need to do anything
                    break;
                case 1: //Week
                    cursor = DatabaseAccess.getRecordsFromTable("tblWeek", "flngWeekID", mTime.mlngTimeframeID);
                    while(cursor.moveToNext()){
                        setDayOfWeek("Monday",cursor.getLong(cursor.getColumnIndex("fblnMonday")));
                        setDayOfWeek("Tuesday",cursor.getLong(cursor.getColumnIndex("fblnTuesday")));
                        setDayOfWeek("Wednesday",cursor.getLong(cursor.getColumnIndex("fblnWednesday")));
                        setDayOfWeek("Thursday",cursor.getLong(cursor.getColumnIndex("fblnThursday")));
                        setDayOfWeek("Friday",cursor.getLong(cursor.getColumnIndex("fblnFriday")));
                        setDayOfWeek("Saturday",cursor.getLong(cursor.getColumnIndex("fblnSaturday")));
                        setDayOfWeek("Sunday",cursor.getLong(cursor.getColumnIndex("fblnSunday")));
                    }
                    break;
                case 2: //Month
                    cursor = DatabaseAccess.getRecordsFromTable("tblMonth", "flngMonthID", mTime.mlngTimeframeID);

                    while(cursor.moveToNext()){
                        setMonthDetails((cursor.getInt(cursor.getColumnIndex("fblnFirst")) == 1)? true : false,
                                (cursor.getInt(cursor.getColumnIndex("fblnMiddle")) == 1)? true : false,
                                (cursor.getInt(cursor.getColumnIndex("fblnLast")) == 1)? true : false,
                                (cursor.getInt(cursor.getColumnIndex("fblnAfter")) == 1)? true : false,
                                cursor.getString(cursor.getColumnIndex("fstrSpecific")));
                    }
                    break;
                case 3: //Year
                    //Dont need to do anything
                    break;
            }
        }
    }

    public void setMode(int pintMode){
        //1 - standard
        //2 - session
        //3 - instance
        //4 - long term
        mintMode = pintMode;
        if (mintMode == 1){
            setUpRepetitionForRegular();
        } else if(mintMode == 2){
            setUpRepititionForSession();
        } else if(mintMode == 3){
            findViewById(R.id.spnRepitition).setVisibility(View.GONE);
            findViewById(R.id.spnTimeRange).setVisibility(View.GONE);
            evaluateRepetitionView(0);
        } else if(mintMode == 4){
            findViewById(R.id.spnRepitition).setVisibility(View.GONE);
        }
    }

    private void setUpRepititionForSession(){
        repetitionSpinner.Clear();
        repetitionSpinner.Add("Repeat Every", (long)1);
        repetitionSpinner.Add("Repeat Every Other", (long)2);
        repetitionSpinner.Add("Repeat Every Thrid", (long)3);
        repetitionSpinner.Add("Repeat Every Forth", (long)4);
        repetitionSpinner.mAdapter.notifyDataSetChanged();
        repetitionSpinner.setIDSpinner((long)1);

        evaluateRepetitionView((long)1);
    }

    private void setUpRepetitionForRegular(){
        repetitionSpinner.Clear();
        repetitionSpinner.Add("No Repetition", (long)0);
        repetitionSpinner.Add("Repeat Every", (long)1);
        repetitionSpinner.Add("Repeat Every Other", (long)2);
        repetitionSpinner.Add("Repeat Every Thrid", (long)3);
        repetitionSpinner.Add("Repeat Every Forth", (long)4);
        repetitionSpinner.mAdapter.notifyDataSetChanged();
        repetitionSpinner.setIDSpinner((long)0);

        evaluateRepetitionView((long)0);
    }

    public void populateTimeFromSession(Integer plngSessionID){
        Cursor cursor = DatabaseAccess.getRecordsFromTable("tblSession", "flngSessionID", plngSessionID);
        cursor.moveToFirst();
        loadTimeDetails(cursor.getLong(cursor.getColumnIndex("flngTimeID")));
    }

    public void validateTimeDetails(){
        //if session, do not allow no timeframe

        //if no timeframe, only allow from date -- This can be handled by visibility rules

        //Do not allow to date without from date
    }

    public void oneOffTimeCopy(){
        mTime = new Time(mTime.getNextPriority(),
                getToDate(),
                Task_Display.getCurrentCalendar().getTimeInMillis(),
                mblnFromTime,
                mblnToTime,
                mblnToDate,
                -1,
                -1,
                0,
                0,
                false,
                -1);

        mTime.saveTime();
    }

    public void createTimeDetails(){
        long lngTimeframeKey = (long)-1;
        String[] arrColumns;
        Object[] arrValues;

        //Determine and create appropriate data element for repetition type
        switch (getTimeRange()){
            case 0: //Day
                arrColumns = new String[]{};
                arrValues = new Object[]{};
                lngTimeframeKey = DatabaseAccess.addRecordToTable("tblDay",
                        arrColumns,
                        arrValues,
                        "flngDayID",
                        (long)-1);
                break;
            case 1: //Week
                arrColumns = new String[]{"fblnMonday","fblnTuesday","fblnWednesday","fblnThursday","fblnFriday","fblnSaturday","fblnSunday"};
                arrValues = new Object[]{getDayOfWeek("Monday"),
                        getDayOfWeek("Tuesday"),
                        getDayOfWeek("Wednesday"),
                        getDayOfWeek("Thursday"),
                        getDayOfWeek("Friday"),
                        getDayOfWeek("Saturday"),
                        getDayOfWeek("Sunday")};
                lngTimeframeKey = (int)DatabaseAccess.addRecordToTable("tblWeek",
                        arrColumns,
                        arrValues);
                break;
            case 2: //Month
                arrColumns = new String[]{"fblnFirst","fblnMiddle","fblnLast","fblnAfterWkn","fstrSpecific"};
                arrValues = new Object[]{((CheckBox)findViewById(R.id.TimeKeeper_Monthly_First)).isChecked(),
                        ((CheckBox)findViewById(R.id.TimeKeeper_Monthly_Middle)).isChecked(),
                        ((CheckBox)findViewById(R.id.TimeKeeper_Monthly_Last)).isChecked(),
                        ((CheckBox)findViewById(R.id.TimeKeeper_Monthly_AfterWkn)).isChecked(),
                        ((EditText)findViewById(R.id.TimeKeeper_Monthly_Txt_Display)).getText().toString()};
                lngTimeframeKey = (int)DatabaseAccess.addRecordToTable("tblMonth",
                        arrColumns,
                        arrValues);
                break;
            case 3: //Year
                arrColumns = new String[]{};
                arrValues = new Object[]{};
                lngTimeframeKey = (int)DatabaseAccess.addRecordToTable("tblYear",
                        arrColumns,
                        arrValues,
                        "flngYearID",
                        (long)-1);
                break;
        }

        mTime = new Time(getFromDate(),
                getToDate(),
                Task_Display.getCurrentCalendar().getTimeInMillis(),
                mblnFromTime,
                mblnToTime,
                mblnToDate,
                getTimeRange(),
                lngTimeframeKey,
                repetitionSpinner.getID(repetitionSpinner.mSpinner.getSelectedItemPosition()),
                0,
                false,
                -1);

        mTime.saveTime();
    }

    public void updateTimeDetails(){
        //Look to see if the frequency changed and if it has, delete the original record
        if (mTime.mintTimeframe != getTimeRange() && mTime.mintTimeframe != -1){
            //Delete original frequency entry
            String strTable = "";
            String strColumn = "";
            Long lngID = (long)-1;
            switch((int)mTime.mintTimeframe){
                case 1:

                    strTable = "tblDay";
                    strColumn = "flngDayID";
                    //lngID = mlngDayID;
                    break;
                case 2:
                    strTable = "tblMonth";
                    strColumn = "flngMonthID";
                   // lngID = mlngMonthID;
                    break;
                case 3:
                    strTable = "tblWeek";
                    strColumn = "flngWeekID";
                    //lngID = mlngWeekID;
                    break;
                case 4:
                    strTable = "tblYear";
                    strColumn = "flngYearID";
                    //lngID = mlngYearID;
                    break;
            }
        }
        DatabaseAccess.mDatabase.beginTransaction();
        try {
            createTimeDetails();
            DatabaseAccess.mDatabase.setTransactionSuccessful();
        } finally {
            DatabaseAccess.mDatabase.endTransaction();
        }
    }

    public void setActiveTimekeeper(Boolean pblnActive){
        timerangeSpinner.setEnabled(pblnActive);
        repetitionSpinner.mSpinner.setEnabled(pblnActive);
        btnFromDate.setEnabled(pblnActive);
        btnToDate.setEnabled(pblnActive);
        btnFromTime.setEnabled(pblnActive);
        btnToTime.setEnabled(pblnActive);
        txtMonthlyDays.setEnabled(pblnActive);
    }

    public void resetTimeKeeper(){
        mblnFromTime = false;
        mblnToTime = false;
        mdtmFrom = (long)-1;
        mdtmTo = (long)-1;
        //Set visibility
    }

    public void evaluateRepetitionView(long plngSelection) {
        if (plngSelection == 0){
            cLayNoRep.setVisibility(View.VISIBLE);
            cLayWeekly.setVisibility(View.GONE);
            cLayMonthly.setVisibility(View.GONE);
            timerangeSpinner.setVisibility(View.GONE);
        } else {
            timerangeSpinner.setVisibility(View.VISIBLE);
        }
    }

    public void evaluateTimeframeView(Integer plngSelection) {
        switch (plngSelection){
            case 0: //Day
                cLayNoRep.setVisibility(View.GONE);
                cLayWeekly.setVisibility(View.GONE);
                cLayMonthly.setVisibility(View.GONE);
                findViewById(R.id.Timekeeper_NoFreq_BtnToDate).setVisibility(View.GONE);
                break;
            case 1: //Week
                cLayNoRep.setVisibility(View.GONE);
                cLayWeekly.setVisibility(View.VISIBLE);
                cLayMonthly.setVisibility(View.GONE);
                findViewById(R.id.Timekeeper_NoFreq_BtnToDate).setVisibility(View.GONE);
                break;
            case 2: //Month
                cLayNoRep.setVisibility(View.GONE);
                cLayWeekly.setVisibility(View.GONE);
                cLayMonthly.setVisibility(View.VISIBLE);
                findViewById(R.id.TimeKeeper_Monthly_First).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Middle).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Last).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_AfterWkn).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Btn_Add).setVisibility(GONE);
                findViewById(R.id.TimeKeeper_Monthly_Txt_Display).setVisibility(GONE);
                findViewById(R.id.Timekeeper_NoFreq_BtnToDate).setVisibility(View.GONE);
                break;
            case 3: //Year
                //Using No Rep View for now until Year View becomes important
                cLayNoRep.setVisibility(View.VISIBLE);
                cLayWeekly.setVisibility(View.GONE);
                cLayMonthly.setVisibility(View.GONE);
                findViewById(R.id.Timekeeper_NoFreq_BtnToDate).setVisibility(View.GONE); //Todo: Fix design to allow yearly to date repetition
                break;
        }
    }

    //region Getters And Setters
    public Boolean blnTimeDetailsExist(){
        return (mblnFromTime || mblnToTime || getTimeRange() != -1);
    }

    public Boolean getDayOfWeek(String pstrDow){
        CheckBox chbDow = null;
        switch (pstrDow){
            case "Monday":
                chbDow = (CheckBox) findViewById(R.id.chkMonday);
                break;
            case "Tuesday":
                chbDow = (CheckBox) findViewById(R.id.chkTuesday);
                break;
            case "Wednesday":
                chbDow = (CheckBox) findViewById(R.id.chkWednesday);
                break;
            case "Thursday":
                chbDow = (CheckBox) findViewById(R.id.chkThursday);
                break;
            case "Friday":
                chbDow = (CheckBox) findViewById(R.id.chkFriday);
                break;
            case "Saturday":
                chbDow = (CheckBox) findViewById(R.id.chkSaturday);
                break;
            case "Sunday":
                chbDow = (CheckBox) findViewById(R.id.chkSunday);
                break;
        }
        return chbDow.isChecked();
    }

    public void setDayOfWeek(String pstrDow, Long pblnOn){
        CheckBox chbDow = null;
        switch (pstrDow){
            case "Monday":
                chbDow = (CheckBox) findViewById(R.id.chkMonday);
                break;
            case "Tuesday":
                chbDow = (CheckBox) findViewById(R.id.chkTuesday);
                break;
            case "Wednesday":
                chbDow = (CheckBox) findViewById(R.id.chkWednesday);
                break;
            case "Thursday":
                chbDow = (CheckBox) findViewById(R.id.chkThursday);
                break;
            case "Friday":
                chbDow = (CheckBox) findViewById(R.id.chkFriday);
                break;
            case "Saturday":
                chbDow = (CheckBox) findViewById(R.id.chkSaturday);
                break;
            case "Sunday":
                chbDow = (CheckBox) findViewById(R.id.chkSunday);
                break;
        }
        chbDow.setChecked(pblnOn == 1 ? true: false);
    }

    public int getTimeRange(){
        if (!repetitionSpinner.mSpinner.getSelectedItem().equals("No Repetition")){
            return timerangeSpinner.getSelectedItemPosition();
        }
        return -1;
    }

    public void setMonthDetails(boolean pblnFirst,
                                boolean pblnMiddle,
                                boolean pblnLast,
                                boolean pblnAfter,
                                String pstrSpecific){
        ((CheckBox)findViewById(R.id.TimeKeeper_Monthly_First)).setChecked(pblnFirst);
        ((CheckBox)findViewById(R.id.TimeKeeper_Monthly_Middle)).setChecked(pblnMiddle);
        ((CheckBox)findViewById(R.id.TimeKeeper_Monthly_Last)).setChecked(pblnLast);
        ((CheckBox)findViewById(R.id.TimeKeeper_Monthly_AfterWkn)).setChecked(pblnAfter);
        String[] tmpArray = pstrSpecific.split(",");
        String value = "";
        for (int i = 0; i<tmpArray.length; i++){
            if (value != ""){
                value += ", ";
            }
            value += tmpArray[i];
            arrSpecificDays[i] = Integer.parseInt(tmpArray[i]);
        }
        ((EditText)findViewById(R.id.TimeKeeper_Monthly_Txt_Display)).setText(value);
    }
    //endregion

    private void instantiateTimeFragment(){
        AppCompatActivity context = (AppCompatActivity)getContext();
        DialogFragment newFragment = new TimeKeeper.TimePickerFragment();
        newFragment.show(context.getSupportFragmentManager(), "timePicker");
    }

    private void instantiateDateFragment(){
        AppCompatActivity context = (AppCompatActivity)getContext();
        DialogFragment newFragment = new TimeKeeper.DatePickerFragment();
        newFragment.show(context.getSupportFragmentManager(), "datePicker");
    }

    public static long getFromDate(){
        return (mdtmFrom == -1) ? Task_Display.getCurrentCalendar().getTimeInMillis():mdtmFrom;
    }

    public static long getToDate(){
        return (mdtmTo == -1) ? Task_Display.getCurrentCalendar().getTimeInMillis():mdtmTo;
    }


    public static void setFromDate(long pdtmFrom) {
        mdtmFrom = pdtmFrom;
        if(pdtmFrom != -1) {
            Calendar pcalFrom = Task_Display.getCalendar(pdtmFrom);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
            btnFromDate.setText("From Date: " + dateFormat.format(pcalFrom.getTime()));
        }
    }

    public static void setToDate(long pdtmTo) {
        mdtmTo = pdtmTo;
        if(pdtmTo != -1){
            mblnToDate = true;
            Calendar pcalTo = Task_Display.getCalendar(pdtmTo);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
            btnToDate.setText("To Date: " + dateFormat.format(pcalTo.getTime()));
        }
    }

    public static void setFromTime(long pdtmFrom) {
        mdtmFrom = pdtmFrom;
        if(pdtmFrom != -1){
            mblnFromTime = true;
            Calendar pcalFrom = Task_Display.getCalendar(pdtmFrom);
            SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
            btnFromTime.setText("From Time: " + dateFormat.format(pcalFrom.getTime()));
        }
    }

    public static void setToTime(long pdtmTo) {
        mdtmTo = pdtmTo;
        if(pdtmTo != -1){
            mblnToTime = true;
            Calendar pcalTo = Task_Display.getCalendar(pdtmTo);
            SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
            btnToTime.setText("To Time: " + dateFormat.format(pcalTo.getTime()));
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            int hour = Task_Display.getCurrentCalendar().get(Calendar.HOUR_OF_DAY);
            int minute = Task_Display.getCurrentCalendar().get(Calendar.MINUTE);
            if(mSetIndicator == 1 && mblnFromTime){
                Calendar fromCal = Task_Display.getCalendar(mdtmFrom);
                hour = fromCal.get(Calendar.HOUR_OF_DAY);
                minute = fromCal.get(Calendar.MINUTE);
            }
            else if (mSetIndicator == 2){
                if(mblnToTime){
                    Calendar ToCal = Task_Display.getCalendar(mdtmTo);
                    hour = ToCal.get(Calendar.HOUR_OF_DAY);
                    minute = ToCal.get(Calendar.MINUTE);
                }
                else if(mblnFromTime){
                    Calendar fromCal = Task_Display.getCalendar(mdtmFrom);
                    hour = fromCal.get(Calendar.HOUR_OF_DAY);
                    minute = fromCal.get(Calendar.MINUTE);
                }
            }

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            if (mSetIndicator == 1) {
                mblnFromTime = true;
                if(mdtmFrom == -1){
                    setFromDate(Task_Display.getCurrentCalendar().getTimeInMillis());
                }
                Calendar fromCal = Task_Display.getCalendar(mdtmFrom);
                fromCal.set(Calendar.HOUR_OF_DAY,hourOfDay);
                fromCal.set(Calendar.MINUTE,minute);
                setFromTime(fromCal.getTimeInMillis());
            } else if (mSetIndicator == 2) {
                mblnToTime = true;
                //Curent logic is that if a to time is set a from and to date must also be set.
                if(mdtmFrom == -1){
                    setFromDate(Task_Display.getCurrentCalendar().getTimeInMillis());
                }
                Calendar toCal;
                if(mdtmTo != -1){
                    toCal = Task_Display.getCalendar(mdtmTo);
                } else {
                    toCal = Task_Display.getCurrentCalendar();
                }
                toCal.set(Calendar.HOUR_OF_DAY,hourOfDay);
                toCal.set(Calendar.MINUTE,minute);
                toCal.getTime();
                setToTime(toCal.getTimeInMillis());
            }
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener{

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            Calendar temp = Task_Display.getCurrentCalendar();
            int year = temp.get(Calendar.YEAR);
            int month = temp.get(Calendar.MONTH);
            int day = temp.get(Calendar.DAY_OF_MONTH);

            if(mdtmFrom != -1){
                Calendar tempFrom = Task_Display.getCalendar(mdtmFrom);
                year = tempFrom.get(Calendar.YEAR);
                month = tempFrom.get(Calendar.MONTH);
                day = tempFrom.get(Calendar.DAY_OF_MONTH);
            }

            // Create a new instance of TimePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            //Todo: Remove mSetIndicator and use View
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
            switch (mSetIndicator) {
                case 3:
                    Calendar tempFrom = Task_Display.getCalendar(mdtmFrom);
                    tempFrom.set(Calendar.YEAR, year);
                    tempFrom.set(Calendar.MONTH, month);
                    tempFrom.set(Calendar.DAY_OF_MONTH, day);
                    setFromDate(tempFrom.getTimeInMillis());
                    break;
                case 4:
                    Calendar tempTo = Task_Display.getCalendar(mdtmTo);
                    if(mdtmFrom == -1){
                        setFromDate(Task_Display.getCurrentCalendar().getTimeInMillis());
                    }
                    tempTo.set(Calendar.YEAR, year);
                    tempTo.set(Calendar.MONTH, month);
                    tempTo.set(Calendar.DAY_OF_MONTH, day);
                    setToDate(tempTo.getTimeInMillis());
                    break;
            }
        }
    }

    public static class NumberPickerDialog extends DialogFragment{
        private NumberPicker.OnValueChangeListener valueChangeListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final NumberPicker numberPicker = new NumberPicker(getActivity());

            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(31);

            Builder builder = new Builder(getActivity());
            builder.setTitle("Choose Value");
            builder.setMessage("Choose a number :");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    arrSpecificDays[intArrayCounter] = numberPicker.getValue();
                    intArrayCounter++;
                    String value = "";
                    for(int i=0; i< intArrayCounter; i++){
                        if (value != ""){
                            value = value + ", ";
                        }
                        value = value + arrSpecificDays[i];
                    }
                    txtMonthlyDays.setText(value);
                }
            });

            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.setView(numberPicker);
            return builder.create();
        }
    }
}
