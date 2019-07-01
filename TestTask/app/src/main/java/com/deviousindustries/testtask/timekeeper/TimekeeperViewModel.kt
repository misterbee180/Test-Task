package com.deviousindustries.testtask.timekeeper

import android.annotation.SuppressLint
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.deviousindustries.testtask.constants.*
import com.deviousindustries.testtask.Utilities
import com.deviousindustries.testtask.classes.*
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

//Possible improvement: Used savedState details to pass time ID (or call VM factory to provide constructor values)
class TimekeeperViewModel : ViewModel() {
    val displaySpecificMonthly = MutableLiveData<Boolean>()
    val displayGeneralMonthly = MutableLiveData<Boolean>()
    val displayThru = MutableLiveData<Boolean>()
    val displayMonth = MutableLiveData<Boolean>()
    val displayWeek = MutableLiveData<Boolean>()
    val displayGeneral = MutableLiveData<Boolean>()
    val displayTimeframe = MutableLiveData<Boolean>()
    val displayStarting = MutableLiveData<Boolean>()
    val test = MutableLiveData<Boolean>().apply { this.value = false }
    var time = MutableLiveData<Time>()
    lateinit var oldTime : Time
    var timeID : Long = NULL_OBJECT
    var isSession = MutableLiveData<Boolean>()

    //Indicators of what date or time values being set
    var settingDate = ""
    var settingTime = ""

    var fromDate = MutableLiveData<String>()
    var toDate = MutableLiveData<String>()
    fun setDate(month: Int, dayOfMonth: Int){
        with(Utilities.getCurrentCalendar().apply {
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)}) {
            when(settingDate) {
                FROM_SOURCE -> fromDate.value = getDateString(timeInMillis)
                TO_SOURCE -> toDate.value = getDateString(timeInMillis)
            }
            addDateToModel(timeInMillis)
        }
    }

    var fromTime = MutableLiveData<String>()
    var toTime = MutableLiveData<String>()
    fun setTime(hourOfDay: Int, minute: Int) {
        with(Utilities.getCurrentCalendar().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)}){

            when(settingTime) {
                FROM_SOURCE -> fromTime.value = getTimeString(timeInMillis)
                TO_SOURCE -> toTime.value = getTimeString(timeInMillis)
            }
            addTimeToModel(timeInMillis)
        }
    }

    private fun addTimeToModel(timeMilli: Long){
        val calTime = Calendar.getInstance().apply { timeInMillis = timeMilli }
        when(settingTime){
            FROM_SOURCE -> {
                time.value!!.fblnFromTime = true
                time.value!!.fdtmFrom =
                        (if(time.value!!.fdtmFrom == NULL_DATE) Utilities.getCurrentCalendar()
                        else Utilities.getCalendar(time.value!!.fdtmFrom))
                                .apply {
                                    set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY))
                                    set(Calendar.MINUTE, calTime.get(Calendar.MINUTE))
                                }.timeInMillis
            }
            TO_SOURCE -> {
                time.value!!.fblnToTime = true
                if(time.value!!.fdtmFrom == NULL_DATE) time.value!!.fdtmFrom = Utilities.getCurrentCalendar().timeInMillis
                time.value!!.fdtmTo =
                        (if(time.value!!.fdtmTo == NULL_DATE) Utilities.getCurrentCalendar()
                        else Utilities.getCalendar(time.value!!.fdtmTo))
                                .apply {
                                    set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY))
                                    set(Calendar.MINUTE, calTime.get(Calendar.MINUTE))
                                }.timeInMillis
            }
        }
    }

    private fun addDateToModel(timeMilli: Long){
        val calDate = Calendar.getInstance().apply { timeInMillis = timeMilli }
        when (settingDate) {
            FROM_SOURCE -> {
                time.value!!.fdtmFrom = Utilities.getCalendar(time.value!!.fdtmFrom).apply{
                    set(Calendar.YEAR, calDate.get(Calendar.YEAR))
                    set(Calendar.MONTH, calDate.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, calDate.get(Calendar.DAY_OF_MONTH))
                }.timeInMillis
            }
            TO_SOURCE -> {
                time.value!!.fblnToDate = true
                if(time.value!!.fdtmFrom == NULL_DATE) time.value!!.fdtmFrom = Utilities.getCurrentCalendar().timeInMillis
                time.value!!.fdtmTo = Utilities.getCalendar(time.value!!.fdtmTo).apply{
                    set(Calendar.YEAR, calDate.get(Calendar.YEAR))
                    set(Calendar.MONTH, calDate.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, calDate.get(Calendar.DAY_OF_MONTH))
                }.timeInMillis
            }
        }
    }

    private fun getDateString(dateMilli: Long):String{
        var rtn = ""
        if(dateMilli != NULL_DATE){
            with(Utilities.getCalendar(dateMilli)){
                @SuppressLint("SimpleDateFormat") val dateFormat = SimpleDateFormat("MM-dd")
                //java.text.DateFormat dateFormat = SimpleDateFormat.getDateInstance();
                rtn = dateFormat.format(getTime())
            }
        }
        return rtn
    }

    private fun getTimeString(timeMilli: Long):String{
        var timeString = ""
        if(timeMilli != NULL_DATE){
            with(Utilities.getCalendar(timeMilli)){
                @SuppressLint("SimpleDateFormat") val dateFormat = SimpleDateFormat("h:mm a")
                //java.text.DateFormat dateFormat = SimpleDateFormat.getDateInstance();
                timeString = dateFormat.format(getTime())
            }
        }
        return timeString
    }

    private var _repetition = MutableLiveData<Int>()
    var repetition: LiveData<Int> = _repetition
    private fun loadRepetition(repetition: Int){
        if(isSession.value!!) _repetition.value = repetition - 1
        else _repetition.value = repetition
    }

    fun setRepetition(position: Int){
        if(_repetition.value != position) {
            _repetition.value = position

            //set correct time value
            var repValue = position
            if(isSession.value!!) repValue += 1
            time.value?.fintRepetition = repValue
            displayTimeframeAndStarting(repValue)
        }
    }

    private fun displayTimeframeAndStarting(repetitionPosition: Int){
        if(repetitionPosition != BASE_POSITION){
            displayTimeframe.value = true
            displayStarting.value = true
            determineTimeframeVisibility(timeframe.value!!)
        } else {
            displayTimeframe.value = false
            displayStarting.value = false
            displayWeek.value = false
            displayMonth.value = false
            displayGeneral.value = true
        }
    }

    private fun determineTimeframeVisibility(timeframePosition: Int) {
        //reset visibility to start
        displayMonth.value = false
        displayWeek.value = false
        displayGeneral.value = false
        displayThru.value = false

        when (timeframePosition) {
            1 -> {
                displayWeek.value = true
                displayThru.value = true
            }
            2 -> {
                displayMonth.value = true
                displayThru.value = true
                setupMonthView(monthSpecificRadio.value!!)
            }
            else -> {
                displayNoDetails()
                displayThru.value = false
            }
        }
    }

    fun setupMonthView(isSpecific: Boolean) {
        displayGeneralMonthly.value = !isSpecific
        displaySpecificMonthly.value = isSpecific
        monthSpecificRadio.value = isSpecific
    }

    private fun displayNoDetails() {
        displayWeek.value = false
        displayMonth.value = false
        displayGeneral.value = false
    }

    var timeframe = MutableLiveData<Int>()
    fun setTimeframe(position: Int){
        timeframe.value = if(position == NULL_POSITION) BASE_POSITION else position
        time.value!!.fintTimeframe = position
        determineTimeframeVisibility(timeframe.value!!)
    }

    var starting = MutableLiveData<Int>()
    fun setStarting(position: Int){
        starting.value = if(position == NULL_POSITION) BASE_POSITION else position
        time.value?.fintStarting = starting.value
    }
    var monthSpecificRadio = MutableLiveData<Boolean>()
    var monthSpecificArray = arrayOf(false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,
            false, false, false, false)
    var monthSpecificString = MutableLiveData<String>()

    init{
        monthSpecificRadio.value = false
        isSession.value = false
        monthSpecificString.value = ""
        timeframe.value = BASE_POSITION
        starting.value = BASE_POSITION
        fromDate.value = ""
        toDate.value = ""
        fromTime.value = ""
        toTime.value = ""
    }


    fun loadTimekeeper(timeID: Long){
        this.timeID = timeID
        LoadTimekeeper().execute(timeID)
    }

    inner class LoadTimekeeper : AsyncTask<Long, Int, Time>(){

        override fun doInBackground(vararg params: Long?):Time {
            return Time.getInstance(params[0]!!)
        }

        override fun onPostExecute(result: Time) {
            super.onPostExecute(result)

            time.value = result
            oldTime = result.clone() as Time
            if(timeID != NULL_OBJECT){
                if(time.value!!.fblnFromTime) fromTime.value = getTimeString(time.value!!.fdtmFrom)
                if(time.value!!.fblnToTime) toTime.value = getTimeString(time.value!!.fdtmTo)
                loadRepetition(time.value!!.fintRepetition)
                setTimeframe(time.value!!.fintTimeframe)
                setStarting(time.value!!.fintStarting)
                establishMonthRadio()
            }
        }
    }

    private fun establishMonthRadio(){
        monthSpecificRadio.value = time.value!!.month.fstrSpecific != ""
        if(monthSpecificRadio.value!!) loadMonthArray(time.value!!.month.fstrSpecific)
    }

    private fun loadMonthArray(specific: String){
        for(day in specific.split(',')){
            monthSpecificArray[day.trim().toInt()-1] = true
        }
        updateSpecific()
    }

    fun saveTimekeeper(): Time{
        return time.value!!.apply{saveTime()}
    }

    fun updateSpecific(){
        var counter = 0
        var found = false
        val stringBuilder = StringBuilder()
        for(day in monthSpecificArray){
            counter ++
            if(day) {
                if(found) stringBuilder.append(", ")
                stringBuilder.append(counter)
                found = true
            }
        }
        monthSpecificString.value = stringBuilder.toString()
        time.value!!.month.fstrSpecific = stringBuilder.toString()
    }

    fun removeDate(source: String){
        when(source){
            FROM_SOURCE ->{
                if(time.value!!.fblnToDate){
                    if(time.value!!.fblnFromTime){
                        time.value!!.fdtmFrom = Utilities.getCurrentCalendar().let{
                            with(Utilities.getCalendar(time.value!!.fdtmFrom)){
                                it.set(Calendar.HOUR_OF_DAY, this.get(Calendar.HOUR_OF_DAY))
                                it.set(Calendar.MINUTE, this.get(Calendar.MINUTE))
                                it.set(Calendar.SECOND, this.get(Calendar.SECOND))
                                it.set(Calendar.MILLISECOND, this.get(Calendar.MILLISECOND))
                                it.timeInMillis
                            }
                        }
                    } else {
                        time.value!!.fdtmFrom = Utilities.getCurrentCalendar().timeInMillis
                    }
                } else {
                    time.value!!.fdtmFrom = NULL_DATE
                }
            }
            TO_SOURCE ->{
                time.value!!.fblnToDate = false
                if(time.value!!.fblnToTime){
                    time.value!!.fdtmTo = Utilities.getCurrentCalendar().let{
                        with(Utilities.getCalendar(time.value!!.fdtmTo)){
                            it.set(Calendar.HOUR_OF_DAY, this.get(Calendar.HOUR_OF_DAY))
                            it.set(Calendar.MINUTE, this.get(Calendar.MINUTE))
                            it.set(Calendar.SECOND, this.get(Calendar.SECOND))
                            it.set(Calendar.MILLISECOND, this.get(Calendar.MILLISECOND))
                            it.timeInMillis
                        }
                    }
                } else {
                    time.value!!.fdtmTo = NULL_DATE
                }
            }
        }
    }

    fun removeTime(source: String){
        when(source){
            FROM_SOURCE -> {
                time.value!!.fblnFromTime = false
                fromTime.value = getTimeString(NULL_DATE)
            }
            TO_SOURCE -> {
                time.value!!.fblnToTime = false
                toTime.value = getTimeString(NULL_DATE)
            }
        }
    }

    fun prepDateAdd(source: String){
        settingDate = source
    }

    fun prepTimeAdd(source: String){
        settingTime = source
    }

    //region OLD Code
    //lateinit var time: Time
//    lateinit var week: Week
//    lateinit var month: Month

//    var fromDate = MutableLiveData<Long>()
//    var toDate = MutableLiveData<Long>()
//    var hasToDate = false
//    var fromTime = MutableLiveData<Long>()
//    var toTime = MutableLiveData<Long>()
//    var repetition = MutableLiveData<Int>()
//    var timeframe = MutableLiveData<Int>()
//    var starting = MutableLiveData<Int>()
//    var isThru = MutableLiveData<Boolean>()
//    var hasFromTime = false
//    var hasToTime = false
//
//
//    var daysOfWeek = MutableLiveData<Array<Boolean>>()
//
//    var firstOfMonth = MutableLiveData<Boolean>()
//    var middleOfMonth = MutableLiveData<Boolean>()
//    var lastOfMonth = MutableLiveData<Boolean>()
//    var afterWeekend = MutableLiveData<Boolean>()
//    var specificDays = MutableLiveData<Array<Boolean>>()
//    var monthSpecificArray = MutableLiveData<Boolean>()


    //    init{
//        fromDate.value = NULL_DATE
//        toDate.value = NULL_DATE
//        fromTime.value = NULL_DATE
//        toTime.value = NULL_DATE
//        repetition.value = BASE_POSITION
//        timeframe.value = NULL_POSITION
//        starting.value = NULL_POSITION
//        isThru.value = false
//    }
//
//    init{
//        daysOfWeek.value = arrayOf(false, false, false, false, false, false, false)
//    }
//
//    init{
//        firstOfMonth.value = false
//        middleOfMonth.value = false
//        lastOfMonth.value = false
//        afterWeekend.value = false
//        //each boolean represents a day of the month. Prevents duplication of days
//        specificDays.value = arrayOf(false, false, false, false, false, false, false,
//                false, false, false, false, false, false, false,
//                false, false, false, false, false, false, false,
//                false, false, false, false, false, false, false,
//                false, false, false)
//        monthSpecificArray.value = false
//    }

//    private fun loadTimeKeeper(time: Time) {
//        fromDate.value = time.fdtmFrom
//        toDate.value = time.fdtmTo
//        hasFromTime = time.fblnFromTime
//        hasToTime = time.fblnToTime
//        hasToDate = time.fblnToDate
//        isThru.value = time.fblnThru
//
//        loadRepetitionDetails(time.fintRepetition,
//                time.fintTimeframe,
//                time.flngTimeframeID,
//                time.fintStarting)
//    }
//
//    private fun loadRepetitionDetails(pintRepetition: Int,
//                              pintTimeframe: Int,
//                              plngTimeframeID: Long,
//                              pintStarting: Int) {
//        //Set repetition spinner
//        if (pintRepetition != BASE_POSITION) {
//            repetition.value = pintRepetition
//            timeframe.value = pintTimeframe
//            starting.value = pintStarting
//        }
//
//        when(time.fintTimeframe){
//            1 -> {week = DatabaseAccess.taskDatabaseDao.loadWeek(time.flngTimeframeID);
//                loadWeekDetails(week.fblnMonday,
//                    week.fblnTuesday,
//                    week.fblnWednesday,
//                    week.fblnThursday,
//                    week.fblnFriday,
//                    week.fblnSaturday,
//                    week.fblnSunday)}
//            2 -> {month = DatabaseAccess.taskDatabaseDao.loadMonth(time.flngTimeframeID)
//                loadMonthDetails(month.fblnFirst, month.fblnMiddle, month.fblnLast, month.fblnAfterWkn, month.fstrSpecific)
//            }
//        }
//    }
//
//    private fun loadMonthDetails(pblnFirst: Boolean,
//                                 pblnMiddle: Boolean,
//                                 pblnLast: Boolean,
//                                 pblnAfter: Boolean,
//                                 pstrSpecific: String) {
//        if (pstrSpecific == "") {
//            firstOfMonth.value = pblnFirst
//            middleOfMonth.value = pblnMiddle
//            lastOfMonth.value = pblnLast
//            afterWeekend.value = pblnAfter
//            monthSpecificArray.value = false
//        } else {
//            monthSpecificArray.value = true
//            val tmpArray = pstrSpecific.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//            for (i in tmpArray) {
//                specificDays.value?.set(i.toInt()-1,true)
//            }
//        }
//    }
//
//    private fun loadWeekDetails(plngMonday: Boolean,
//                                plngTuesday: Boolean,
//                                plngWednesday: Boolean,
//                                plngThursday: Boolean,
//                                plngFriday: Boolean,
//                                plngSaturday: Boolean,
//                                plngSunday: Boolean) {
//        setDayOfWeek("Monday", plngMonday)
//        setDayOfWeek("Tuesday", plngTuesday)
//        setDayOfWeek("Wednesday", plngWednesday)
//        setDayOfWeek("Thursday", plngThursday)
//        setDayOfWeek("Friday", plngFriday)
//        setDayOfWeek("Saturday", plngSaturday)
//        setDayOfWeek("Sunday", plngSunday)
//    }
//
//    private fun setDayOfWeek(dayOfWeek: String, set: Boolean){
//        when(dayOfWeek.toLowerCase()){
//            "sunday" -> daysOfWeek.value?.set(0, set)
//            "monday" -> daysOfWeek.value?.set(1, set)
//            "tuesday" -> daysOfWeek.value?.set(2, set)
//            "wednesday" -> daysOfWeek.value?.set(3, set)
//            "thrusday" -> daysOfWeek.value?.set(4, set)
//            "friday" -> daysOfWeek.value?.set(5, set)
//            "saturday" -> daysOfWeek.value?.set(6, set)
//        }
//    }
    //endregion

}
