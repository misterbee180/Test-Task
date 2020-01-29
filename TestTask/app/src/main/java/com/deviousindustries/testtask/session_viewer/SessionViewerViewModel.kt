package com.deviousindustries.testtask.session_viewer

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.deviousindustries.testtask.DatabaseAccess
import com.deviousindustries.testtask.data.Session

class SessionViewerViewModel : ViewModel() {

    var sessionList = MutableLiveData<List<Session>>()

    init {
        sessionList.value = listOf()
        Log.i("SessionViewerViewModel", "MainActivity View Model Created!")
    }

    override fun onCleared() {
        Log.i("SessionViewerViewModel", "MainActivity View Model Destroyed!")
        super.onCleared()
    }

    fun loadSessionList() {
        sessionList.value = DatabaseAccess.taskDatabaseDao.loadActiveSessions()
    }

    fun deleteSession(sessionID: Long){
        DatabaseAccess.deleteSession(sessionID)
    }
}