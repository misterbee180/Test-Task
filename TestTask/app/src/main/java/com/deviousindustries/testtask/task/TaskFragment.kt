package com.deviousindustries.testtask.task

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.core.widget.addTextChangedListener

import com.deviousindustries.testtask.R
import com.deviousindustries.testtask.constants.NULL_OBJECT
import com.deviousindustries.testtask.data.Session
import com.deviousindustries.testtask.session.SessionFragment
import com.deviousindustries.testtask.timekeeper.TimekeeperViewModel

class TaskFragment : Fragment() {

    companion object {
        fun newInstance() = TaskFragment()
        const val TASK_KEY = "TASK_ID"
    }

    private lateinit var viewModel: TaskViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.task_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(TaskViewModel::class.java)

        viewModel.start(
                (ViewModelProviders.of(activity!!).get(TimekeeperViewModel::class.java)),
                arguments?.getLong(TASK_KEY, NULL_OBJECT)
                        ?: NULL_OBJECT
        )
        setObservers()
        setEvents()
        setupSessionSpinner()
    }

    fun setObservers() {

    }

    private fun setEvents() {
        activity!!.findViewById<Button>(R.id.btnTaskAddSess).setOnClickListener {
            fragmentManager!!
                    .beginTransaction()
                    .replace(R.id.contentFrame, SessionFragment(), "Session")
                    .addToBackStack(null)
                    .commit()
        }
    }

    private fun setupSessionSpinner() {
        activity!!.findViewById<Spinner>(R.id.spnTaskSessSel).adapter = ArrayAdapter(
                activity!!,
                android.R.layout.simple_spinner_item,
                viewModel.sessionList)
    }
}
