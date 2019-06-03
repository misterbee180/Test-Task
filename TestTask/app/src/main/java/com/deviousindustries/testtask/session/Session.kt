package com.deviousindustries.testtask.session

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.deviousindustries.testtask.constants.NULL_OBJECT
import com.deviousindustries.testtask.R
import com.deviousindustries.testtask.Utilities

class Session : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.session_activity)
        Utilities.instantiate(application)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, SessionFragment.newInstance().apply {
                        arguments = Bundle().apply{
                            putLong("SESSION_ID",intent.extras?.getLong("SESSION_ID",NULL_OBJECT) ?: NULL_OBJECT)}})
                    .commitNow()
        }
    }

}
