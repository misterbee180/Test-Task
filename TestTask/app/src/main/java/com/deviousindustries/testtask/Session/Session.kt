package com.deviousindustries.testtask.Session

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.deviousindustries.testtask.R

class Session : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.session_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, SessionFragment.newInstance())
                    .commitNow()
        }
    }

}
