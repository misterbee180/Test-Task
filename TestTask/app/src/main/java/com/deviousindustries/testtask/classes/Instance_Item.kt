package com.deviousindustries.testtask.classes

interface Instance_Item {
    fun getDisplayRecords(sectionMap: HashMap<Triple<Int, Long, String>, Int>): List<TaskListRecord>
}