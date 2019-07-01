package com.deviousindustries.testtask.PriorityGroupInstanceTools

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PriorityGroupInstanceListAdapter(private val onInstanceClick: OnElementItemClick,
                                       private val priorityLayout : Int,
                                       private val groupLayout : Int,
                                       private val instanceLayout : Int,
                                       private val titleViewId: Int) : RecyclerView.Adapter<PriorityGroupInstanceListAdapter.ElementViewHolder>() {
    enum class ElementType {
        Instance, Group, Priority;

        companion object {
            private val map = values().associateBy { it.ordinal }
            fun fromInt(type: Int) = map[type]
        }
    }

    var data = listOf<PriorityGroupInstanceElement>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return ElementType.valueOf(data[position].element).ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElementViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        var view = when (ElementType.fromInt(viewType)) {
            ElementType.Instance -> layoutInflater.inflate(instanceLayout, parent, false)
            ElementType.Group -> layoutInflater.inflate(groupLayout, parent, false)
            ElementType.Priority -> layoutInflater.inflate(priorityLayout, parent, false)
            null -> layoutInflater.inflate(instanceLayout, parent, false)
        }

        return ElementViewHolder(view, onInstanceClick)
    }

    override fun onBindViewHolder(holder: ElementViewHolder, position: Int) {
        holder.view.findViewById<TextView>(titleViewId).text = data[position].title
    }

    interface OnElementItemClick {
        open fun onClick(position: Int)
        open fun onLongClick(position: Int)
    }

    class ElementViewHolder(
            val view: View,
            private val onInstanceClick: OnElementItemClick
    ) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {
        init {
            view.setOnClickListener(this)
            view.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            onInstanceClick.onClick(adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            onInstanceClick.onLongClick(adapterPosition)
            return true
        }
    }
}