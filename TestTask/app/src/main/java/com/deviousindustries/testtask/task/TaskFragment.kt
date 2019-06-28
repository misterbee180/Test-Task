package com.deviousindustries.testtask.task

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager

import com.deviousindustries.testtask.R
import com.deviousindustries.testtask.constants.NULL_OBJECT
import com.deviousindustries.testtask.timekeeper.TimekeeperFragment
import com.deviousindustries.testtask.timekeeper.TimekeeperViewModel

class TaskFragment : Fragment() {

    companion object {
        fun newInstance() = TaskFragment()
        const val TASK = "TASK_KEY"
    }

    private lateinit var viewModel: TaskViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.task_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(TaskViewModel::class.java)

        viewModel.loadTask(arguments?.getLong(TASK, NULL_OBJECT)?: NULL_OBJECT)
        (ViewModelProviders.of(activity!!).get(TimekeeperViewModel::class.java))
                .loadTimekeeper(viewModel.timeID)
    }

}
