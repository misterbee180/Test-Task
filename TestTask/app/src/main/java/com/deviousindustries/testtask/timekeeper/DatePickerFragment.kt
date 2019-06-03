package com.deviousindustries.testtask.timekeeper

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment : DialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val timeInMilli = arguments?.getLong(BASE_TIME)
        var year = 0 //Choosing to not use Utility to make this usable elsewhere
        var month = 0
        var day = 0

        with(Calendar.getInstance().apply{
            if(timeInMilli != null) timeInMillis = timeInMilli}){
            year = get(Calendar.YEAR)
            month = get(Calendar.MONTH)
            day = get(Calendar.DAY_OF_MONTH)
        }

        return DatePickerDialog(activity!!, parentFragment as DatePickerDialog.OnDateSetListener, year, month, day)
    }
}