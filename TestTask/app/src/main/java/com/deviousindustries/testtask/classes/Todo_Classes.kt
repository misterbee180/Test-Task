package com.deviousindustries.testtask.classes

import com.deviousindustries.testtask.classes.TaskListRecord.Companion.NULL_SECTION
import com.deviousindustries.testtask.task_list.ElementType

class Todo_Item(val priorityId: Int,
                val sectionId: Int,
                val recordId: Long,
                val recordType: Int,
                val title: String): TaskListRecord {

    override fun getPriority(): Int {
        return priorityId;
    }

    override fun getGroup(): Int {
        return sectionId;
    }

    override fun getRecordID(): Long {
        return recordId
    }

    override fun getElementType(): Int{
        return ElementType.Instance.ordinal
    }

    override fun getItemTitle(): String {
        return title;
    }
}

class Todo_Session(val priorityId: Int,
                   val sectionId: Int,
                   val recordId: Long,
                   val title: String): TaskListRecord {

    override fun getPriority(): Int {
        return priorityId;
    }

    override fun getGroup(): Int {
        return sectionId;
    }

    override fun getRecordID(): Long {
        return recordId
    }

    override fun getElementType(): Int{
        return ElementType.Group.ordinal
    }

    override fun getItemTitle(): String {
        return title;
    }

}

class Todo_Header(val priorityId: Int,
                  val recordId: Long,
                  val title: String): TaskListRecord {

    override fun getPriority(): Int {
        return priorityId;
    }

    override fun getGroup(): Int {
        return NULL_SECTION;
    }

    override fun getRecordID(): Long {
        return recordId
    }

    override fun getElementType(): Int{
        return ElementType.Priority.ordinal
    }

    override fun getItemTitle(): String {
        return title;
    }
}