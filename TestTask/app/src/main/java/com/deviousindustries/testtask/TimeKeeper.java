package com.deviousindustries.testtask;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.app.AlertDialog.Builder;

import com.deviousindustries.testtask.classes.Month;
import com.deviousindustries.testtask.classes.Time;
import com.deviousindustries.testtask.classes.Week;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import static com.deviousindustries.testtask.constants.ConstantsKt.*;

//TODO: Design view for visiblity of currently active and future mTime instances associated w/ mTime
//TODO: Pull session selection into timekeeper (remove from task_detail)

public class TimeKeeper extends ConstraintLayout implements View.OnClickListener {

    int mSetIndicator = NULL_POSITION; //Used to determine which mTime button was selected
    int[] arrSpecificDays;
    int intArrayCounter;
    long fdtmFrom = NULL_DATE;
    long fdtmTo = NULL_DATE;
    boolean fblnFromTime;
    boolean fblnToTime;
    boolean fblnToDate;
    boolean fblnLoaded = false;
    int fintMode = 1; //Used for visability 1: Normal 2: Session 3: Task Instance
    Month mMonth;
    Week mWeek;

    //View objects
    Spinner mTimeframeSpinner;
    ArrayListContainer mRepetitionSpinner;
    Spinner mStartingSpinner;
    ConstraintLayout cLayMonthly;
    ConstraintLayout cLayWeekly;
    ConstraintLayout cLayNoRep;
    Button btnFromDate;
    Button btnToDate;
    Button btnFromTime;
    Button btnToTime;
    EditText txtMonthlyDays;
    CheckBox chkThru;

    public TimeKeeper(Context context){
        super(context);
    }

    public TimeKeeper(Context context, AttributeSet attrs) {
        super(context, attrs);

        View.inflate(context, R.layout.activity_timekeeper, this); //This is super important. Inflate loads the screen from XML.
        //Set member access
        arrSpecificDays = new int[31];
        intArrayCounter = 0;
        mRepetitionSpinner = new ArrayListContainer();
        mStartingSpinner = findViewById(R.id.Starting_Spinner);
        mTimeframeSpinner = findViewById(R.id.Timeframe_Spinner);
        mTimeframeSpinner.setOnItemSelectedListener(timeframeListener);

        //Target View Groups
        cLayNoRep = findViewById(R.id.CLayNoFrequency);
        cLayWeekly = findViewById(R.id.CLayWeekly);
        cLayMonthly = findViewById(R.id.CLayMonthly);

        //Target Generic Fields
        mRepetitionSpinner.LinkArrayToSpinner((Spinner) findViewById(R.id.Repetition_Spinner), getContext());
        mRepetitionSpinner.mSpinner.setOnItemSelectedListener(repetitionListener);
        btnFromTime = findViewById(R.id.FromTime_Add_Button);
        btnToTime = findViewById(R.id.ToTime_Add_Button);
        chkThru = findViewById(R.id.Thru_CheckBox);

        //Target No Frequency and Year Fields
        btnFromDate = findViewById(R.id.FromDate_Add_Button);
        btnToDate = findViewById(R.id.ToDate_Add_Button);

        //Target Month Fields
        txtMonthlyDays = findViewById(R.id.Specific_Text);

        //Set Listeners
        btnFromTime.setOnClickListener(this);
        btnToTime.setOnClickListener(this);
        btnFromDate.setOnClickListener(this);
        btnToDate.setOnClickListener(this);
        findViewById(R.id.General_Monthly_Radio).setOnClickListener(this);
        findViewById(R.id.Specific_Montly_Radio).setOnClickListener(this);
        findViewById(R.id.Specific_Add_Button).setOnClickListener(this);

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

    //region CLASSES
//    public class Month{
//        boolean fblnFirst;
//        boolean fblnMiddle;
//        boolean fblnLast;
//        boolean fblnAfterWkn;
//        String fstrSpecific;
//    }
//
//    public class Week{
//        boolean fblnMonday;
//        boolean fblnTuesday;
//        boolean fblnWednesday;
//        boolean fblnThursday;
//        boolean fblnFriday;
//        boolean fblnSaturday;
//        boolean fblnSunday;
//    }
    //endregion

    //region Getters And Setters
    public boolean getThru(){
        return chkThru.isChecked();
    }

    public void setThru(boolean pblnThru){
        chkThru.setChecked(pblnThru);
    }

    public int getRepetition(){
        return Integer.parseInt(mRepetitionSpinner.getID(mRepetitionSpinner.mSpinner.getSelectedItemPosition()).toString());
    }

    public int getStarting(){
        return mStartingSpinner.getSelectedItemPosition();
    }

    public boolean isTimeSet(){
        return (fdtmFrom != NULL_DATE);
    }

    public Week getWeekDetails(){
        Week tempWeek = new Week();
        tempWeek.setFblnSunday(getDayOfWeek("Sunday"));
        tempWeek.setFblnMonday(getDayOfWeek("Monday"));
        tempWeek.setFblnTuesday(getDayOfWeek("Tuesday"));
        tempWeek.setFblnWednesday(getDayOfWeek("Wednesday"));
        tempWeek.setFblnThursday(getDayOfWeek("Thursday"));
        tempWeek.setFblnFriday(getDayOfWeek("Friday"));
        tempWeek.setFblnSaturday(getDayOfWeek("Saturday"));

        return tempWeek;
    }

    public Month getMonthDetails(){
        Month tempMonth = new Month();
        tempMonth.setFblnFirst(((CheckBox)findViewById(R.id.First_Month_Checkbox)).isChecked());
        tempMonth.setFblnMiddle(((CheckBox)findViewById(R.id.Middle_Month_Checkbox)).isChecked());
        tempMonth.setFblnLast(((CheckBox)findViewById(R.id.Last_Month_Checkbox)).isChecked());
        tempMonth.setFblnAfterWkn(((CheckBox)findViewById(R.id.After_Week_Checkbox)).isChecked());
        tempMonth.setFstrSpecific(((EditText)findViewById(R.id.Specific_Text)).getText().toString());

        return tempMonth;
    }

    public long getFromDate(){
        return (fdtmFrom == NULL_DATE) ? Utilities.Companion.getCurrentCalendar().getTimeInMillis():fdtmFrom;
    }

    public long getToDate(){
        return (fdtmTo == NULL_DATE) ? Utilities.Companion.getCurrentCalendar().getTimeInMillis():fdtmTo;
    }


    public void setFromDate(long pdtmFrom) {
        fdtmFrom = pdtmFrom;
        if(pdtmFrom != NULL_DATE) {
            Calendar pcalFrom = Utilities.Companion.getCalendar(pdtmFrom);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
            //java.text.DateFormat dateFormat = SimpleDateFormat.getDateInstance();
            String strFromDate = "From Date: " + dateFormat.format(pcalFrom.getTime()); 
            btnFromDate.setText(strFromDate);
        }
    }

    public void setToDate(long pdtmTo) {
        fdtmTo = pdtmTo;
        if(pdtmTo != NULL_DATE){
            fblnToDate = true;
            Calendar pcalTo = Utilities.Companion.getCalendar(pdtmTo);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
            String strToDate = "To Date: " + dateFormat.format(pcalTo.getTime()); 
            btnToDate.setText(strToDate);
        }
    }

    public void setFromTime(long pdtmFrom) {
        fdtmFrom = pdtmFrom;
        if(pdtmFrom != NULL_DATE){
            fblnFromTime = true;
            Calendar pcalFrom = Utilities.Companion.getCalendar(pdtmFrom);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
            String strFromTime = "From Time: " + dateFormat.format(pcalFrom.getTime()); 
            btnFromTime.setText(strFromTime);
        }
    }

    public void setToTime(long pdtmTo) {
        fdtmTo = pdtmTo;
        if(pdtmTo != NULL_DATE){
            fblnToTime = true;
            Calendar pcalTo = Utilities.Companion.getCalendar(pdtmTo);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
            String strToTime = "To Time: " + dateFormat.format(pcalTo.getTime());
            btnToTime.setText(strToTime);
        }
    }

    public Boolean getDayOfWeek(String pstrDow){
        CheckBox chbDow;
        switch (pstrDow){
            case "Monday":
                chbDow = findViewById(R.id.Monday_Checkbox);
                break;
            case "Tuesday":
                chbDow = findViewById(R.id.Tuesday_Checkbox);
                break;
            case "Wednesday":
                chbDow = findViewById(R.id.Wednesday_Checkbox);
                break;
            case "Thursday":
                chbDow = findViewById(R.id.Thursday_Checkbox);
                break;
            case "Friday":
                chbDow = findViewById(R.id.Friday_Checkbox);
                break;
            case "Saturday":
                chbDow = findViewById(R.id.Saturday_Checkbox);
                break;
            default:
                chbDow = findViewById(R.id.Sunday_Checkbox);
                break;
        }
        return chbDow.isChecked();
    }

    public void setDayOfWeek(String pstrDow, Long pblnOn){
        CheckBox chbDow;
        switch (pstrDow){
            case "Monday":
                chbDow = findViewById(R.id.Monday_Checkbox);
                break;
            case "Tuesday":
                chbDow = findViewById(R.id.Tuesday_Checkbox);
                break;
            case "Wednesday":
                chbDow = findViewById(R.id.Wednesday_Checkbox);
                break;
            case "Thursday":
                chbDow = findViewById(R.id.Thursday_Checkbox);
                break;
            case "Friday":
                chbDow = findViewById(R.id.Friday_Checkbox);
                break;
            case "Saturday":
                chbDow = findViewById(R.id.Saturday_Checkbox);
                break;
            default:
                chbDow = findViewById(R.id.Sunday_Checkbox);
                break;
        }
        chbDow.setChecked(pblnOn == 1);
    }

    public int getTimeframe(){
        if (!mRepetitionSpinner.mSpinner.getSelectedItem().equals("No Repetition")){
            return mTimeframeSpinner.getSelectedItemPosition();
        }
        return NULL_POSITION;
    }
    //endregion

    //region HANDLERS
    Spinner.OnItemSelectedListener repetitionListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            evaluateRepetitionView(mRepetitionSpinner.getID(position));
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
            case R.id.FromTime_Add_Button:
                mSetIndicator = 1;
                instantiateTimeFragment();
                break;
            case R.id.ToTime_Add_Button:
                mSetIndicator = 2;
                instantiateTimeFragment();
                break;
            case R.id.FromDate_Add_Button:
                mSetIndicator = 3;
                instantiateDateFragment();
                break;
            case R.id.ToDate_Add_Button:
                mSetIndicator = 4;
                instantiateDateFragment();
                break;
            case R.id.General_Monthly_Radio:
                findViewById(R.id.First_Month_Checkbox).setVisibility(VISIBLE);
                findViewById(R.id.Middle_Month_Checkbox).setVisibility(VISIBLE);
                findViewById(R.id.Last_Month_Checkbox).setVisibility(VISIBLE);
                findViewById(R.id.After_Week_Checkbox).setVisibility(VISIBLE);
                findViewById(R.id.Specific_Add_Button).setVisibility(GONE);
                findViewById(R.id.Specific_Text).setVisibility(GONE);
                break;
            case R.id.Specific_Montly_Radio:
                findViewById(R.id.First_Month_Checkbox).setVisibility(GONE);
                findViewById(R.id.Middle_Month_Checkbox).setVisibility(GONE);
                findViewById(R.id.Last_Month_Checkbox).setVisibility(GONE);
                findViewById(R.id.After_Week_Checkbox).setVisibility(GONE);
                findViewById(R.id.Specific_Add_Button).setVisibility(VISIBLE);
                findViewById(R.id.Specific_Text).setVisibility(VISIBLE);
                break;
            case R.id.Specific_Add_Button:
                AppCompatActivity context = (AppCompatActivity)getContext();
                DialogFragment newFragment = new TimeKeeper.NumberPickerDialog();
                newFragment.show(context.getSupportFragmentManager(), "numberPicker");
                break;
        }
    }

    public void evaluateRepetitionView(long plngSelection) {
        if (plngSelection == BASE_POSITION){
            cLayNoRep.setVisibility(View.VISIBLE);
            cLayWeekly.setVisibility(View.GONE);
            cLayMonthly.setVisibility(View.GONE);
            mTimeframeSpinner.setVisibility(View.GONE);
            mStartingSpinner.setVisibility(View.GONE);
            findViewById(R.id.Thru_CheckBox).setVisibility(View.GONE);
            findViewById(R.id.ToDate_Add_Button).setVisibility(View.VISIBLE);
        } else {
            mTimeframeSpinner.setVisibility(View.VISIBLE);
            mStartingSpinner.setVisibility(View.VISIBLE);
        }
    }

    public void evaluateTimeframeView(Integer plngSelection) {
        switch (plngSelection){
            case 0: //Day
                cLayNoRep.setVisibility(View.GONE);
                cLayWeekly.setVisibility(View.GONE);
                cLayMonthly.setVisibility(View.GONE);
                findViewById(R.id.ToDate_Add_Button).setVisibility(View.GONE);
                findViewById(R.id.Thru_CheckBox).setVisibility(View.GONE);
                break;
            case 1: //Week
                cLayNoRep.setVisibility(View.GONE);
                cLayWeekly.setVisibility(View.VISIBLE);
                cLayMonthly.setVisibility(View.GONE);
                findViewById(R.id.ToDate_Add_Button).setVisibility(View.GONE);
                findViewById(R.id.Thru_CheckBox).setVisibility(View.VISIBLE);
                break;
            case 2: //Month
                cLayNoRep.setVisibility(View.GONE);
                cLayWeekly.setVisibility(View.GONE);
                cLayMonthly.setVisibility(View.VISIBLE);
                findViewById(R.id.Thru_CheckBox).setVisibility(View.VISIBLE);
                break;
            case 3: //Year
                //Using No Rep View for now until Year View becomes important
                cLayNoRep.setVisibility(View.VISIBLE);
                cLayWeekly.setVisibility(View.GONE);
                cLayMonthly.setVisibility(View.GONE);
                findViewById(R.id.ToDate_Add_Button).setVisibility(View.GONE);
                findViewById(R.id.Thru_CheckBox).setVisibility(View.GONE);
                //Todo: Fix design to allow yearly (and non repeating specific) to date population
                break;
        }
    }

    //endregion

    //region INITIALIZATION
    public void loadTimeKeeper(long plngTimeID){
        Time tempTime = Time.getInstance(plngTimeID);
        loadTimeKeeper(tempTime);
    }

    public void loadTimeKeeper(Time pTime){
        fblnLoaded = true;
        loadTimeDetails(pTime.fdtmFrom,
                pTime.fdtmTo,
                pTime.fblnFromTime,
                pTime.fblnToTime,
                pTime.fblnToDate,
                pTime.fblnThru);
        loadRepetitionDetails(pTime.fintRepetition,
                pTime.fintTimeframe,
                pTime.flngTimeframeID,
                pTime.fintStarting);
    }

    public void loadTimeDetails(long pdtmFrom,
                                long pdtmTo,
                                boolean pblnFromTime,
                                boolean pblnToTime,
                                boolean pblnToDate,
                                boolean pblnThru){
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
        setThru(pblnThru);
    }

    public void loadRepetitionDetails(long plngRepetition,
                                      int pintTimeframe,
                                      long plngTimeframeID,
                                      int pintStarting) {
        //Set repetition spinner
        if (plngRepetition != BASE_POSITION) {
            mRepetitionSpinner.setIDSpinner(plngRepetition);
            mTimeframeSpinner.setSelection(pintTimeframe);
            mStartingSpinner.setSelection(pintStarting);
        }

        switch (pintTimeframe) {
            case 1:
                try(Cursor curWeek = DatabaseAccess.getRecordsFromTable("tblWeek", "flngWeekID", plngTimeframeID)) {
                    curWeek.moveToNext();
                    loadWeekDetails(curWeek.getLong(curWeek.getColumnIndex("fblnMonday")),
                            curWeek.getLong(curWeek.getColumnIndex("fblnTuesday")),
                            curWeek.getLong(curWeek.getColumnIndex("fblnWednesday")),
                            curWeek.getLong(curWeek.getColumnIndex("fblnThursday")),
                            curWeek.getLong(curWeek.getColumnIndex("fblnFriday")),
                            curWeek.getLong(curWeek.getColumnIndex("fblnSaturday")),
                            curWeek.getLong(curWeek.getColumnIndex("fblnSunday")));
                }
                break;
            case 2:
                try(Cursor curMonth = DatabaseAccess.getRecordsFromTable("tblMonth", "flngMonthID", plngTimeframeID)){
                    curMonth.moveToNext();
                    loadMonthDetails(curMonth.getLong(curMonth.getColumnIndex("fblnFirst"))==1,
                            curMonth.getLong(curMonth.getColumnIndex("fblnMiddle"))==1,
                            curMonth.getLong(curMonth.getColumnIndex("fblnLast"))==1,
                            curMonth.getLong(curMonth.getColumnIndex("fblnAfterWkn"))==1,
                            curMonth.getString(curMonth.getColumnIndex("fstrSpecific")));
                }
                break;
        }
    }

    public void loadWeekDetails(long plngMonday,
                                long plngTuesday,
                                long plngWednesday,
                                long plngThursday,
                                long plngFriday,
                                long plngSaturday,
                                long plngSunday){
        setDayOfWeek("Monday",plngMonday);
        setDayOfWeek("Tuesday",plngTuesday);
        setDayOfWeek("Wednesday",plngWednesday);
        setDayOfWeek("Thursday",plngThursday);
        setDayOfWeek("Friday",plngFriday);
        setDayOfWeek("Saturday",plngSaturday);
        setDayOfWeek("Sunday",plngSunday);
    }

    public void loadMonthDetails(boolean pblnFirst,
                                 boolean pblnMiddle,
                                 boolean pblnLast,
                                 boolean pblnAfter,
                                 String pstrSpecific){
        if(pstrSpecific.equals("")){
            ((CheckBox)findViewById(R.id.First_Month_Checkbox)).setChecked(pblnFirst);
            ((CheckBox)findViewById(R.id.Middle_Month_Checkbox)).setChecked(pblnMiddle);
            ((CheckBox)findViewById(R.id.Last_Month_Checkbox)).setChecked(pblnLast);
            ((CheckBox)findViewById(R.id.After_Week_Checkbox)).setChecked(pblnAfter);
            ((RadioButton)findViewById(R.id.General_Monthly_Radio)).setChecked(true);
            (findViewById(R.id.General_Monthly_Radio)).callOnClick();
            intArrayCounter = 0;
        } else {
            String[] tmpArray = pstrSpecific.split(",");
            StringBuilder value = new StringBuilder();
            intArrayCounter = tmpArray.length;
            for (int i = 0; i<tmpArray.length; i++){
                if (i != 0){
                    value.append(", ");
                }
                value.append(tmpArray[i].trim());
                arrSpecificDays[i] = Integer.parseInt(tmpArray[i].trim());
            }
            ((EditText)findViewById(R.id.Specific_Text)).setText(value);

            ((RadioButton)findViewById(R.id.Specific_Montly_Radio)).setChecked(true);
            (findViewById(R.id.Specific_Montly_Radio)).callOnClick();
        }
    }
    //endregion

    //region VIEW INITIALIZATION
    public void setActiveTimekeeper(Boolean pblnActive){
        setViewAndChildrenEnabled(this, pblnActive);
    }

    private void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    public void resetTimeKeeper(){
        fblnFromTime = false;
        fblnToTime = false;
        fdtmFrom = (long)NULL_DATE;
        fdtmTo = (long)NULL_DATE;

        loadWeekDetails(0,0,0,0,0,0,0);
        loadMonthDetails(false, false,false, false, "");

        //set visibility;
        mRepetitionSpinner.setIDSpinner(0);
        mTimeframeSpinner.setSelection(0);
    }

    public void setMode(int pintMode){
        //1 - standard
        //2 - session
        //3 - instance
        //4 - long term
        fintMode = pintMode;
        if (fintMode == 1){
            setUpRepetitionForRegular();
        } else if(fintMode == 2){
            setUpRepititionForSession();
        } else if(fintMode == 3){
            findViewById(R.id.Repetition_Spinner).setVisibility(View.GONE);
            findViewById(R.id.Timeframe_Spinner).setVisibility(View.GONE);
            evaluateRepetitionView(0);
        } else if(fintMode == 4){
            findViewById(R.id.Repetition_Spinner).setVisibility(View.GONE);
        }
    }

    private void setUpRepititionForSession(){
        mRepetitionSpinner.Clear();
        mRepetitionSpinner.Add("Repeat Every", (long)1);
        mRepetitionSpinner.Add("Repeat Every Other", (long)2);
        mRepetitionSpinner.Add("Repeat Every Thrid", (long)3);
        mRepetitionSpinner.Add("Repeat Every Forth", (long)4);
        mRepetitionSpinner.mAdapter.notifyDataSetChanged();
        mRepetitionSpinner.setIDSpinner((long)1);

        evaluateRepetitionView((long)1);
    }

    private void setUpRepetitionForRegular(){
        mRepetitionSpinner.Clear();
        mRepetitionSpinner.Add("No Repetition", (long)0);
        mRepetitionSpinner.Add("Repeat Every", (long)1);
        mRepetitionSpinner.Add("Repeat Every Other", (long)2);
        mRepetitionSpinner.Add("Repeat Every Thrid", (long)3);
        mRepetitionSpinner.Add("Repeat Every Forth", (long)4);
        mRepetitionSpinner.mAdapter.notifyDataSetChanged();
        mRepetitionSpinner.setIDSpinner((long)0);

        evaluateRepetitionView((long)0);
    }
    //endregion

    //region COMPLETION
    public boolean validateTimeDetails(){
        //if session, do not allow no timeframe

        //if no timeframe, only allow from date -- This can be handled by visibility rules

        //Do not allow to date without from date

        return true;
    }

    public Time createTimeDetails(long plngTimeID,
                                  int pintOrigTimeframe,
                                  long plngOrigTimeframeID,
                                  boolean pblnSession,
                                  String pstrTitle){
        long lngTimeframeId = NULL_DATE;

        //Determine and create appropriate data element for repetition type
        if(plngTimeID != NULL_OBJECT){ //Time was loaded
            if(pintOrigTimeframe == getTimeframe()){ //Timeframe type matches (should work even if -1)
                lngTimeframeId = plngOrigTimeframeID;
            } else //noinspection StatementWithEmptyBody
                if(pintOrigTimeframe == NULL_POSITION){} //Repeating mTime newly added
            else{ //Different timeframe was present during mTime load
                deleteTimeframe(pintOrigTimeframe, plngOrigTimeframeID);
            }
        }

        Time returnTime = new Time(plngTimeID,
                getFromDate(),
                getToDate(),
                Utilities.Companion.getCurrentCalendar().getTimeInMillis(),
                fblnFromTime,
                fblnToTime,
                fblnToDate,
                getTimeframe(),
                createTimeframe(lngTimeframeId),
                getRepetition(),
                getStarting(),
                false,
                NULL_OBJECT,
                getThru());

        if(pblnSession) returnTime.setAsSession(pstrTitle);

        return returnTime;
    }

    public long createTimeframe(long plntTimeframeID){
        String[] arrColumns;
        Object[] arrValues;

        switch (getTimeframe()){
            case 0: //Day
                arrColumns = new String[]{};
                arrValues = new Object[]{};
                plntTimeframeID = DatabaseAccess.addRecordToTable("tblDay",
                        arrColumns,
                        arrValues,
                        "flngDayID",
                        plntTimeframeID);
                break;
            case 1: //Week
                mWeek = getWeekDetails();
                arrColumns = new String[]{"fblnMonday","fblnTuesday","fblnWednesday","fblnThursday","fblnFriday","fblnSaturday","fblnSunday"};
                arrValues = new Object[]{mWeek.getFblnMonday(),
                        mWeek.getFblnTuesday(),
                        mWeek.getFblnWednesday(),
                        mWeek.getFblnThursday(),
                        mWeek.getFblnFriday(),
                        mWeek.getFblnSaturday(),
                        mWeek.getFblnSunday()};
                plntTimeframeID = (int)DatabaseAccess.addRecordToTable("tblWeek",
                        arrColumns,
                        arrValues,
                        "flngWeekID",
                        plntTimeframeID);
                break;
            case 2: //Month
                mMonth = getMonthDetails();
                arrColumns = new String[]{"fblnFirst","fblnMiddle","fblnLast","fblnAfterWkn","fstrSpecific"};
                arrValues = new Object[]{mMonth.getFblnFirst(),
                        mMonth.getFblnMiddle(),
                        mMonth.getFblnLast(),
                        mMonth.getFblnAfterWkn(),
                        mMonth.getFstrSpecific()};
                plntTimeframeID = (int)DatabaseAccess.addRecordToTable("tblMonth",
                        arrColumns,
                        arrValues,
                        "flngMonthID",
                        plntTimeframeID);
                break;
            case 3: //Year
                arrColumns = new String[]{};
                arrValues = new Object[]{};
                plntTimeframeID = (int)DatabaseAccess.addRecordToTable("tblYear",
                        arrColumns,
                        arrValues,
                        "flngYearID",
                        plntTimeframeID);
                break;
        }

        return plntTimeframeID;
    }

    public void deleteTimeframe(int pintTimeframe,
                                long plngID){
        String strTable = "";
        String strColumn = "";
        switch (pintTimeframe){
            case 0: //Day
                strTable = "tblDay";
                strColumn = "flngDayID";
                break;
            case 1: //Week
                strTable = "tblWeek";
                strColumn = "flngWeekID";
                break;
            case 2: //Month
                strTable = "tblMonth";
                strColumn = "flngMonthID";
                break;
            case 3: //Year
                strTable = "tblYear";
                strColumn = "flngYearID";
                break;
        }
        DatabaseAccess.deleteRecordFromTable(strTable,strColumn,plngID);
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

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {



        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current mTime as the default values for the picker
            TimeKeeper tempTime = getActivity().findViewById(R.id.timeKeeper);

            int hour = Utilities.Companion.getCurrentCalendar().get(Calendar.HOUR_OF_DAY);
            int minute = Utilities.Companion.getCurrentCalendar().get(Calendar.MINUTE);
            if(tempTime.mSetIndicator == 1 && tempTime.fblnFromTime){
                Calendar fromCal = Utilities.Companion.getCalendar(tempTime.fdtmFrom);
                hour = fromCal.get(Calendar.HOUR_OF_DAY);
                minute = fromCal.get(Calendar.MINUTE);
            }
            else if (tempTime.mSetIndicator == 2){
                if(tempTime.fblnToTime){
                    Calendar ToCal = Utilities.Companion.getCalendar(tempTime.fdtmTo);
                    hour = ToCal.get(Calendar.HOUR_OF_DAY);
                    minute = ToCal.get(Calendar.MINUTE);
                }
                else if(tempTime.fblnFromTime){
                    Calendar fromCal = Utilities.Companion.getCalendar(tempTime.fdtmFrom);
                    hour = fromCal.get(Calendar.HOUR_OF_DAY);
                    minute = fromCal.get(Calendar.MINUTE);
                }
            }

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the mTime chosen by the user
            TimeKeeper tempTime = getActivity().findViewById(R.id.timeKeeper);
            if (tempTime.mSetIndicator == 1) {
                tempTime.fblnFromTime = true;
                if(tempTime.fdtmFrom == NULL_DATE){
                    tempTime.setFromDate(Utilities.Companion.getCurrentCalendar().getTimeInMillis());
                }
                Calendar fromCal = Utilities.Companion.getCalendar(tempTime.fdtmFrom);
                fromCal.set(Calendar.HOUR_OF_DAY,hourOfDay);
                fromCal.set(Calendar.MINUTE,minute);
                tempTime.setFromTime(fromCal.getTimeInMillis());
            } else if (tempTime.mSetIndicator == 2) {
                tempTime.fblnToTime = true;
                //Current logic is that if a to mTime is set a from and to date must also be set.
                if(tempTime.fdtmFrom == NULL_DATE){
                    tempTime.setFromDate(Utilities.Companion.getCurrentCalendar().getTimeInMillis());
                }
                Calendar toCal;
                if(tempTime.fdtmTo != NULL_DATE){
                    toCal = Utilities.Companion.getCalendar(tempTime.fdtmTo);
                } else {
                    toCal = Utilities.Companion.getCurrentCalendar();
                }
                toCal.set(Calendar.HOUR_OF_DAY,hourOfDay);
                toCal.set(Calendar.MINUTE,minute);
                toCal.getTime();
                tempTime.setToTime(toCal.getTimeInMillis());
            }
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener{

        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            TimeKeeper tempTime = getActivity().findViewById(R.id.timeKeeper);

            // Use the current mTime as the default values for the picker
            Calendar temp = Utilities.Companion.getCurrentCalendar();
            int year = temp.get(Calendar.YEAR);
            int month = temp.get(Calendar.MONTH);
            int day = temp.get(Calendar.DAY_OF_MONTH);

            if(tempTime.fdtmFrom != NULL_DATE){
                Calendar tempFrom = Utilities.Companion.getCalendar(tempTime.fdtmFrom);
                year = tempFrom.get(Calendar.YEAR);
                month = tempFrom.get(Calendar.MONTH);
                day = tempFrom.get(Calendar.DAY_OF_MONTH);
            }

            // Create a new instance of TimePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            TimeKeeper tempTime = getActivity().findViewById(R.id.timeKeeper);
            switch (tempTime.mSetIndicator) {
                case 3:
                    Calendar tempFrom = Utilities.Companion.getCalendar(tempTime.fdtmFrom);
                    tempFrom.set(Calendar.YEAR, year);
                    tempFrom.set(Calendar.MONTH, month);
                    tempFrom.set(Calendar.DAY_OF_MONTH, day);
                    tempTime.setFromDate(tempFrom.getTimeInMillis());
                    break;
                case 4:
                    Calendar tempTo = Utilities.Companion.getCalendar(tempTime.fdtmTo);
                    if(tempTime.fdtmFrom == NULL_DATE){
                        tempTime.setFromDate(Utilities.Companion.getCurrentCalendar().getTimeInMillis());
                    }
                    tempTo.set(Calendar.YEAR, year);
                    tempTo.set(Calendar.MONTH, month);
                    tempTo.set(Calendar.DAY_OF_MONTH, day);
                    tempTime.setToDate(tempTo.getTimeInMillis());
                    break;
            }
        }
    }

    public static class NumberPickerDialog extends DialogFragment{
        @NonNull
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
                    TimeKeeper tempTime = getActivity().findViewById(R.id.timeKeeper);
                    tempTime.arrSpecificDays[tempTime.intArrayCounter] = numberPicker.getValue();
                    tempTime.intArrayCounter++;
                    StringBuilder value = new StringBuilder();
                    for(int i=0; i< tempTime.intArrayCounter; i++){
                        if (i != 0){
                            value.append(", ");
                        }
                        value.append(tempTime.arrSpecificDays[i]);
                    }
                    tempTime.txtMonthlyDays.setText(value);
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
