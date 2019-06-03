package com.deviousindustries.testtask.timekeeper

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment

class NumberPickerFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var numberPicker = NumberPicker(activity).apply{
            minValue = 1
            maxValue = 31
            id = 1
        }

        val builder = AlertDialog.Builder(activity)
                .setTitle("Choose Value")
                .setMessage("Choose a number :")
                .setPositiveButton("OK", parentFragment as DialogInterface.OnClickListener)
                .setNegativeButton("CANCEL") { dialog, which -> }
                .setView(numberPicker)
        return builder.create()
    }
}