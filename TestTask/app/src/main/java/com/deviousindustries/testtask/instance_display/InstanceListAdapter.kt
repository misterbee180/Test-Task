package com.deviousindustries.testtask.instance_display

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.deviousindustries.testtask.R

class InstanceListAdapter(private val onInstanceClick: OnInstanceItemClick) : RecyclerView.Adapter<InstanceListAdapter.InstanceItemViewHolder>() {
    var data = listOf<TaskListViewModel.GeneralListItem>()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstanceItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        var view = when (ElementType.fromInt(viewType)) {
            ElementType.Instance -> layoutInflater.inflate(R.layout.task_item1, parent, false)
            ElementType.Group -> layoutInflater.inflate(R.layout.task_group1, parent, false)
            ElementType.Priority -> layoutInflater.inflate(R.layout.seperator_item1, parent, false)
            null -> layoutInflater.inflate(R.layout.task_item1, parent, false)
        }

        return InstanceItemViewHolder(view, onInstanceClick)
    }

    override fun onBindViewHolder(holder: InstanceItemViewHolder, position: Int) {
        holder.view.findViewById<TextView>(R.id.title_text).text = data[position].title
    }

    interface OnInstanceItemClick {
        open fun onInstanceClick(position: Int)
        open fun onInstanceLongClick(position: Int)
    }

    class InstanceItemViewHolder(
            val view: View,
            private val onInstanceClick: OnInstanceItemClick
    ) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {
        init {
            view.setOnClickListener(this)
            view.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            onInstanceClick.onInstanceClick(adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            onInstanceClick.onInstanceLongClick(adapterPosition)
            return true
        }
    }
}