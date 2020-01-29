package com.deviousindustries.testtask.session

import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import com.deviousindustries.testtask.constants.NULL_OBJECT
import com.deviousindustries.testtask.DatabaseAccess
import com.deviousindustries.testtask.timekeeper.TimekeeperViewModel
import java.lang.Exception

class SessionViewModel : ViewModel() {
    var sessionID: Long = NULL_OBJECT
    var title = MutableLiveData<String>()
    var eventComplete = MutableLiveData<Boolean>()
    lateinit var timekeeperViewModel: TimekeeperViewModel

    init{
        title.value = ""
        eventComplete.value = false
    }

    fun start(sessionID: Long){
        this.sessionID = sessionID
        timekeeperViewModel.isSession.value = true
        if(sessionID != NULL_OBJECT){
            title.value = DatabaseAccess.taskDatabaseDao.loadSession(sessionID)
        }
    }

    fun saveSession(){
        eventComplete.value = true
        SaveSession().execute()
    }

    inner class SaveSession():AsyncTask<Unit, Unit, Unit>(){
        override fun doInBackground(vararg params: Unit?) {
            DatabaseAccess.mDatabase.beginTransaction()
            try{
                timekeeperViewModel.saveTimekeeper()
                        .updateToSession(title.value!!)

                DatabaseAccess.mDatabase.setTransactionSuccessful()
            } catch(e:Exception){
                e.printStackTrace()
            } finally {
                DatabaseAccess.mDatabase.endTransaction()
            }
        }
    }
}
