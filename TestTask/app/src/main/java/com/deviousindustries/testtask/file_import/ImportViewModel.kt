package com.deviousindustries.testtask.file_import

import android.content.ContentResolver
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.deviousindustries.testtask.DatabaseAccess
import com.deviousindustries.testtask.constants.NULL_OBJECT
import com.deviousindustries.testtask.constants.NULL_POSITION
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception

class ImportViewModel : ViewModel() {
    var fileLocation = MutableLiveData<Uri>()
    var enableImport = MutableLiveData<Boolean>()
    var resultText = MutableLiveData<String>()
    var activeSessions = DatabaseAccess.taskDatabaseDao.loadActiveSessions()
    var activeGroups = DatabaseAccess.taskDatabaseDao.loadActiveGroups()

    init{
        fileLocation.value = Uri.EMPTY
        enableImport.value = false
        resultText.value = ""
    }

    fun setFileLocation(location: Uri){
        if(fileLocation.value!! != location){
            fileLocation.value = location
            enableImport.value = true
        }
    }

    fun import(context: Context){
        val result = StringBuilder()
        val contentResolver = object:ContentResolver(context){}
        var lineNumber = 0
        DatabaseAccess.mDatabase.beginTransaction()
        contentResolver.openInputStream(fileLocation.value)?.use{inputSream ->
            BufferedReader(InputStreamReader(inputSream)).use{reader ->
                var line = reader.readLine()
                while(line != null){
                    try{
                        importLine(line, ++lineNumber)
                    } catch (e: Exception){
                        result.append("Error:\n")
                        result.append(e.message)
                        result.append("\n\nIMPORT HAS BEEN ROLLED BACK")
                    } finally {
                    }
                    line = reader.readLine()
                }
                DatabaseAccess.mDatabase.setTransactionSuccessful()
            }
        }
        DatabaseAccess.mDatabase.endTransaction()
        resultText.value = result.toString()
    }

    private fun importLine(line: String, lineNumber: Int) {
        val REQUIRED_TASK_IMPORT_SIZE = 12
        val taskArray = line.split(',')
        if(taskArray.size != REQUIRED_TASK_IMPORT_SIZE) throw Exception("Line does not contain enough details to load. Required $REQUIRED_TASK_IMPORT_SIZE FOUND ${taskArray.size} on line $lineNumber")
        val taskImport = TaskImport(taskArray)
    }

    inner class TaskImport(taskArray: List<String>){
        var title: String = taskArray[0]
        var description: String = taskArray[1]
        var sessionID = NULL_OBJECT
        var groupID = NULL_OBJECT
        var fromDateTime = NULL_OBJECT
        var fromTimeSet = false
        var toDateTime = NULL_OBJECT
        var toDateSet =  false
        var toTimeSet = false
        var repetition = NULL_POSITION
        var timeframe = NULL_POSITION
        var starting = NULL_POSITION

        init{
            sessionID = findSessionID(taskArray[2])
            groupID = findGroupID(taskArray[3])
            if(sessionID == NULL_OBJECT){
                fromDateTime = setupDateTime(taskArray[4], taskArray[5], true)
                fromDateTime = setupDateTime(taskArray[6], taskArray[7], false)
            }
        }

        private fun setupDateTime(pDate: String, pTime: String, destinationFrom: Boolean): Long {
            var dateTime: Long
            val dateExists = pDate.isNotBlank()
            val timeExists = pDate.isNotBlank()

            if(destinationFrom){
                if(!dateExists) throw Exception("Must set from date")
                if(timeExists) fromTimeSet = true
            } else {
                if(dateExists) toDateSet = true
                if(timeExists) toTimeSet = true
            }

            var format = ""
            if (dateExists) format += "MMM/dd"
            if (timeExists) format += "HH:mm"

            try{
                dateTime = SimpleDateFormat(format).parse(pDate+pTime).time
            } catch (e: Exception){
                throw Exception("${if(destinationFrom)"From" else "To"} date could not parse. Please make sure it's in the format ${if(destinationFrom)"mm\\dd" else "hh:mm"}")
            }

            return dateTime
        }

        private fun findSessionID(sessionTitle: String): Long{
            var sessionID = NULL_OBJECT
            if(sessionTitle.isNotBlank()) {
                activeSessions.forEach {
                    if (it.title == sessionTitle) {
                        sessionID = it.timeID
                        return@forEach
                    }
                }
                if(sessionID == NULL_OBJECT) throw Exception("Could not find active session: $sessionTitle")
            }
            return sessionID
        }

        private fun findGroupID(groupTitle: String): Long{
            var groupID = NULL_OBJECT
            if(groupTitle.isNotBlank()){
                activeGroups.forEach{
                    if(it.fstrTitle == groupTitle){
                        groupID = it.flngGroupID
                        return@forEach
                    }
                }
                if(groupID == NULL_OBJECT) throw Exception("Could not find active group: $groupTitle")
            }
            return groupID
        }
    }

}
