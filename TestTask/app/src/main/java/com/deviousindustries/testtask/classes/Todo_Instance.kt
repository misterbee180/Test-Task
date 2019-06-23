package com.deviousindustries.testtask.classes

import com.deviousindustries.testtask.classes.TaskListRecord.Companion.NULL_SECTION
import com.deviousindustries.testtask.constants.NULL_OBJECT
import com.deviousindustries.testtask.task_list.InstanceItemType
import kotlin.collections.HashMap

class Todo_Instance(val instanceID: Long,
                    val title: String,
                    val sessionID: Long,
                    val sessionTitle: String,
                    val priorityID: Int) : Instance_Item{

    val displayRecords = mutableListOf<TaskListRecord>()

    override fun getDisplayRecords(sectionMap: HashMap<Triple<Int, Long, String>, Int>): List<TaskListRecord>{
        displayRecords.add(
                Todo_Item(priorityID,
                        if(sessionID == NULL_OBJECT) NULL_SECTION else getSection(sectionMap),
                        instanceID,
                        InstanceItemType.Todo.ordinal,
                        title))

        return displayRecords
    }

    private fun getSection(sectionMap: HashMap<Triple<Int, Long, String>, Int>): Int {
        val sectionTriple = Triple(priorityID, sessionID, "Todo_Session")
        return sectionMap.get(sectionTriple) ?: putSection(sectionTriple, sectionMap)
    }

    private fun putSection(sectionTriple: Triple<Int, Long, String>,
                           sectionMap: HashMap<Triple<Int, Long, String>, Int>): Int{
        sectionMap.put(sectionTriple,sectionMap.size)
        val subSectionID = sectionMap.get(sectionTriple)!!
        displayRecords.add(
                Todo_Session(priorityID, subSectionID, sessionID, sessionTitle)
        )

        return subSectionID
    }
}