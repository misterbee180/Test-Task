package com.deviousindustries.testtask.SessionViewer

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.deviousindustries.testtask.Classes.Session
import com.deviousindustries.testtask.Classes.Task
import com.deviousindustries.testtask.Classes.Time
import com.deviousindustries.testtask.DatabaseAccess
import com.deviousindustries.testtask.Details_Session
import java.lang.Exception

class SessionViewerViewModel : ViewModel() {

    var sessionList = MutableLiveData<List<Session>>()

    init {
        sessionList.value = listOf()
        Log.i("SessionViewerViewModel", "SessionViewer View Model Created!")
    }

    override fun onCleared() {
        Log.i("SessionViewerViewModel", "SessionViewer View Model Destroyed!")
        super.onCleared()
    }

    fun loadSessionList() {
        sessionList.value = DatabaseAccess.taskDatabaseDao.loadActiveSessions()
    }

    fun createSession(context: Context){
        startActivity(context,
                Intent(context, Details_Session::class.java),
                null)
    }

    fun viewSessionDetails(context: Context, sessionID: Long){
        val intent = Intent(context, Details_Session::class.java)
        intent.putExtra("EXTRA_TIME_ID", sessionID)
        startActivity(context, intent, null)
    }

    fun deleteSession(sessionID: Long){
        DatabaseAccess.deleteSession(sessionID)
    }
}