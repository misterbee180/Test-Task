package com.deviousindustries.testtask.session_viewer

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.deviousindustries.testtask.session.Session
import com.deviousindustries.testtask.DatabaseAccess

class SessionViewerViewModel : ViewModel() {

    var sessionList = MutableLiveData<List<com.deviousindustries.testtask.classes.Session>>()

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
                Intent(context, Session::class.java),
                null)
    }

    fun deleteSession(sessionID: Long){
        DatabaseAccess.deleteSession(sessionID)
    }
}