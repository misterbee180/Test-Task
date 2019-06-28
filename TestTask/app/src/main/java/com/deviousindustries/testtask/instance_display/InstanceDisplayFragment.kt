package com.deviousindustries.testtask.instance_display


import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.deviousindustries.testtask.*
import com.deviousindustries.testtask.classes.TaskDetail
import com.deviousindustries.testtask.classes.TaskInstance
import com.deviousindustries.testtask.classes.Time
import com.deviousindustries.testtask.session.SessionFragment
import com.deviousindustries.testtask.session_viewer.SessionViewerFragment
import com.deviousindustries.testtask.task.TaskFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class InstanceDisplayFragment : Fragment(), InstanceListAdapter.OnInstanceItemClick {
    companion object {
        fun newInstance() = InstanceDisplayFragment()
    }

    private lateinit var viewModel: TaskListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tasklist_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this).get(TaskListViewModel::class.java)
        viewModel.start()
        setupFab()
        setupTaskList()
        setupObservables()
    }

    private fun setupObservables() {
        viewModel.createShortPriorityFragment.observe(this, Observer { display ->
            if (display) {
                viewModel.createShortPriorityFragment.value = false
                GeneralDialogFragment("Complete all instances under ${viewModel.activeRecord!!.title} queue?",
                        positive = DialogInterface.OnClickListener { _, _ ->
                            viewModel.completePriority(viewModel.activePosition)
                        },
                        negative = null).show(fragmentManager!!, "Priority Complete")
            }
        })

        viewModel.createShortInstanceFragment.observe(this, Observer { display ->
            if (display){
                viewModel.createShortInstanceFragment.value = false
                GeneralDialogFragment("Complete ${viewModel.activeRecord!!.title}?",
                        positive = DialogInterface.OnClickListener { _, _ ->
                            viewModel.completeInstance(viewModel.activePosition)
                        },
                        negative = null).show(fragmentManager!!,"Instance Complete")
            }
        })

        viewModel.createShortSessionFragment.observe(this, Observer { display ->
            if (display){
                viewModel.createShortSessionFragment.value = false
                GeneralDialogFragment("Complete all instances under ${viewModel.activeRecord!!.title}?",
                        positive = DialogInterface.OnClickListener { _, _ ->
                            viewModel.completeGroup(viewModel.activePosition)
                        },
                        negative = null).show(fragmentManager!!, "Session Complete")
            }
        })

        viewModel.viewInstance.observe(this, Observer { display ->
            if (display){
                viewModel.viewInstance.value = false
                val intent = Intent(activity, Details_Instance::class.java)
                intent.putExtra("EXTRA_INSTANCE_ID", viewModel.activeRecord!!.recordId)
                startActivity(intent)
//                fragmentManager!!
//                        .beginTransaction()
//                        .replace(R.id.contentFrame, SessionFragment().apply{
//                            arguments = Bundle().apply {
//                                putLong("INSTANCE_ID", viewModel.activeRecord!!.recordId)
//                            }
//                        }, "Session")
//                        .addToBackStack(null)
//                        .commit()
            }
        })

        viewModel.viewSession.observe(this, Observer { display ->
            if (display){
                viewModel.viewSession.value = false
                fragmentManager!!
                        .beginTransaction()
                        .replace(R.id.contentFrame, SessionFragment().apply{
                            arguments = Bundle().apply {
                                putLong("SESSION_ID", viewModel.activeRecord!!.recordId)
                            }
                        }, "Session")
                        .addToBackStack(null)
                        .commit()
            }
        })

        viewModel.setupInitialAlert.observe(this, Observer { setUp ->
            if(setUp){
                viewModel.setupInitialAlert.value = false
                viewModel.setupAlert(activity!!.applicationContext)
            }
        })
    }

    private fun setupFab() {
        val fab = activity!!.findViewById(R.id.AddTask_FAB) as FloatingActionButton
        fab.setOnClickListener { createTask() }
    }

    override fun onResume() {
        super.onResume()
        viewModel.resume()
    }

    override fun onInstanceLongClick(position: Int) {
        viewModel.recordLongClicked(position)
    }

    override fun onInstanceClick(position: Int) {
        viewModel.recordClicked(position)
    }

    private fun setupTaskList() {
        activity!!.findViewById<RecyclerView>(R.id.InstanceList_Recycle).adapter = InstanceListAdapter(this)
        viewModel.recordList.observe(this, Observer { records ->
            (activity!!.findViewById<RecyclerView>(R.id.InstanceList_Recycle).adapter as InstanceListAdapter).data = records
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_task_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        when (item.itemId) {
            R.id.action_settings -> viewSettings()
            R.id.action_session -> viewSessions()
            R.id.action_task -> viewTasks()
            R.id.action_event -> viewEvents()
            R.id.action_group -> viewGroups()
            R.id.action_longTerm -> viewLongTerm()
            else -> null
        }

        return super.onOptionsItemSelected(item)
    }

    private fun viewSessions() {
        fragmentManager!!
                .beginTransaction()
                .replace(R.id.contentFrame, SessionViewerFragment(), "SessionViewer")
                .addToBackStack(null)
                .commit()

    }

    private fun createTask() {
        val intent = Intent(activity, Details_Task::class.java)
        startActivity(intent)
//        fragmentManager!!
//                .beginTransaction()
//                .replace(R.id.contentFrame, TaskFragment(), "Task")
//                .addToBackStack(null)
//                .commit()
    }

    private fun viewTasks() {
        val intent = Intent(activity, Viewer_Task::class.java)
        startActivity(intent)
    }

    private fun viewGroups() {
        val intent = Intent(activity, Viewer_Groups::class.java)
        startActivity(intent)
    }

    private fun viewLongTerm() {
        val intent = Intent(activity, Viewer_LongTerm::class.java)
        startActivity(intent)
    }

    private fun viewEvents() {
        val intent = Intent(activity, Viewer_Events::class.java)
        startActivity(intent)
    }

    private fun viewSettings() {
        val intent = Intent(activity, SettingsActivity::class.java)
        startActivity(intent)
    }

}
