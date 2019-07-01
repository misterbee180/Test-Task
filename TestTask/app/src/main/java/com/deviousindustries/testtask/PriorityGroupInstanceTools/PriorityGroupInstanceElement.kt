package com.deviousindustries.testtask.PriorityGroupInstanceTools

class PriorityGroupInstanceElement(val priorityId: Int,
                                   val groupId: Int,
                                   val recordId: Long,
                                   val title: String,
                                   val element: String,
                                   val type: String) {
    var onClick: () -> Unit = {}
    var onClickLong: () -> Unit = {}

    fun callOnClick() {
        onClick()
    }

    fun callOnLongClick() {
        onClickLong()
    }
}