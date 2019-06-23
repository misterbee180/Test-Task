package com.deviousindustries.testtask.task_list

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.Observer

import com.deviousindustries.testtask.R
import com.deviousindustries.testtask.classes.TaskListRecord

class TaskListFragment : Fragment() {

    companion object {
        fun newInstance() = TaskListFragment()
    }

    private lateinit var viewModel: TaskListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tasklist_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(TaskListViewModel::class.java)

        setupTaskList()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRecordList()
    }

    private fun setupTaskList(){
        viewModel.recordList.observe(this, Observer{ records ->
            val adapter = object: ArrayAdapter<TaskListRecord>(activity!!, R.layout.task_item1, records){
                override fun getItemId(position: Int): Long {
                    return getItem(position).getRecordID()
                }

                override fun getItemViewType(position: Int): Int {
                    return getItem(position).getElementType()
                }

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    var convView = convertView

                    if (convView == null) {
                        when(ElementType.fromInt(getItemViewType(position))){
                            ElementType.Instance ->{ convView = (activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.task_item1, null)}
                            ElementType.Group ->{ convView = (activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.task_group1, null)}
                            ElementType.Priority ->{ convView = (activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.seperator_item1, null)}
                        }
                    }

                    (convView!!.findViewById(R.id.title_text) as TextView).text = getItem(position).getItemTitle()
                    return convView
                }
            }
            activity!!.findViewById<ListView>(R.id.lsvTaskList).adapter = adapter
        })
    }

}
