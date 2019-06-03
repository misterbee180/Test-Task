package com.deviousindustries.testtask.timekeeper

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

const val BASE_TIME = "BASE_TIME"

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    lateinit internal var callback: OnTimePickerListener

    fun setOnTimePickerListener(callback: OnTimePickerListener){
        this.callback = callback
    }

    interface OnTimePickerListener{
        fun onTimePicked(hourOfDay: Int, minute: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val timeInMilli = arguments?.getLong(BASE_TIME)

        var hour: Int = 0 //Choosing to not use Utility to make this usable elsewhere
        var minute: Int = 0

        with(Calendar.getInstance().apply{
            if(timeInMilli != null) timeInMillis = timeInMilli}){
            hour = get(Calendar.HOUR_OF_DAY)
            minute = get(Calendar.MINUTE)
        }

        return TimePickerDialog(activity, this, hour, minute,
                DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        callback.onTimePicked(hourOfDay, minute)
    }
}

//override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//    source = arguments!!.getString("SOURCE")
//    viewModel = ViewModelProviders.of(activity!!).get(TimekeeperViewModel::class.java)
//
//    // Use the current mTime as the default values for the picker
//    Utilities.getCurrentCalendar().let {
//        var hour = it.get(Calendar.HOUR_OF_DAY)
//        var minute = it.get(Calendar.MINUTE)
//
//        when (source) {
//            "from" -> {
//                if (viewModel.mTime.value!!.fblnFromTime) {
//                    with(Utilities.getCalendar(viewModel.mTime.value!!.fdtmFrom)) {
//                        hour = get(Calendar.HOUR_OF_DAY)
//                        minute = get(Calendar.MINUTE)
//                    }
//                }
//            }
//            "to" -> {
//                if (viewModel.mTime.value!!.fblnToTime) {
//                    with(Utilities.getCalendar(viewModel.mTime.value!!.fdtmTo)) {
//                        hour = get(Calendar.HOUR_OF_DAY)
//                        minute = get(Calendar.MINUTE)
//                    }
//                } else if (viewModel.mTime.value!!.fblnFromTime) {
//                    with(Utilities.getCalendar(viewModel.mTime.value!!.fdtmFrom)) {
//                        hour = get(Calendar.HOUR_OF_DAY)
//                        minute = get(Calendar.MINUTE)
//                    }
//                }
//            }
//        }
//        return TimePickerDialog(activity!!, this, hour, minute,
//                DateFormat.is24HourFormat(activity))
//    }
//}
//
//override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
//    when (source) {
//        "from" -> {
//            viewModel.mTime.value!!.fblnFromTime = true
//            if(viewModel.mTime.value!!.fdtmFrom == NULL_DATE){
//                viewModel.setDate("from",Utilities.getCurrentCalendar().timeInMillis)
//            }
//            with(Utilities.getCalendar(viewModel.mTime.value!!.fdtmFrom)) {
//                set(Calendar.HOUR_OF_DAY, hourOfDay)
//                set(Calendar.MINUTE, minute)
//
//                viewModel.setDate("from",Utilities.getCurrentCalendar().timeInMillis)
//            }
//        }
//        "to" -> {
//            viewModel.mTime.value!!.fblnToTime = true
//            if(viewModel.mTime.value!!.fdtmFrom == NULL_DATE){
//                viewModel.mTime.value!!.fdtmFrom = Utilities.getCurrentCalendar().timeInMillis
//            }
//            if(viewModel.mTime.value!!.fdtmTo == NULL_DATE){
//                viewModel.mTime.value!!.fdtmTo = Utilities.getCurrentCalendar().timeInMillis
//            }
//            with(Utilities.getCalendar(viewModel.mTime.value!!.fdtmTo)) {
//                set(Calendar.HOUR_OF_DAY, hourOfDay)
//                set(Calendar.MINUTE, minute)
//
//                viewModel.mTime.value!!.fdtmTo = getTimeInMillis()
//            }
//        }
//    }
//}