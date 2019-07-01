package com.deviousindustries.testtask

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class GeneralDialogFragment(private val message: String,
                            private val positive: DialogInterface.OnClickListener,
                            private val negative: DialogInterface.OnClickListener?) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        builder.setMessage(message)
                .setPositiveButton("Confirm", positive)
                .setNegativeButton("Cancel", negative)
        // Create the AlertDialog object and return it
        return builder.create()
    }


}