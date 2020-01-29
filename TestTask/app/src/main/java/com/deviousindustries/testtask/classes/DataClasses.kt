package com.deviousindustries.testtask.classes

data class KTask(
        val taskID:Long,
        val taskDetailID:Long,
        val timeID:Long,
        val taskType:Int,
        val taskTypeID:Long,
        val createdDate:Long,
        val deletedDate:Long,
        val isOneOff:Boolean)

data class KTaskDetail(
        val taskDetailID:Long,
        val title:String,
        val description:String)

data class kTaskInstance(
        val instanceID: Long,
        val taskID: Long,
        val taskDetailID: Long,
        val fromDate: Long,
        val fromTime: Long,
        val toDate: Long,
        val toTime: Long,
        val hasToDate: Boolean,
        val hasToTime: Boolean,
        val createdDate: Long,
        val completedDate: Long,
        val systemCompletedDate: Long,
        val deletedDate: Long,
        val editedDate:Long,
        val sessionID: Long)

data class kTime(
        val timeID: Long,
        val fromDate: Long,
        val toDate: Long,
        val fromTime: Long,
        val toTime: Long,
        val hasFromTime: Boolean,
        val hasToTime: Boolean,
        val hasToDate: Boolean,
        val timeframeType: Int,
        val timeframeID: Long,
        val repetitionType: Int,
        val createdDate: Long,
        val startingDate: Long,
        val completeDate: Long,
        val isCompleted: Boolean,
        val generationID: Long,
        val isThru: Boolean,
        val isSession: Boolean,
        val title: String
)

data class kTimeInstance(
        val generationID: Long,
        val timeID: Long,
        val upcomingDate: Long,
        val priorityDate: Long,
        val isThru: Boolean
)