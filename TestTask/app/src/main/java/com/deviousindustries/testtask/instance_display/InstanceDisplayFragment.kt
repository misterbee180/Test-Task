package com.deviousindustries.testtask.instance_display


import android.content.DialogInterface
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.deviousindustries.testtask.*
import com.deviousindustries.testtask.PriorityGroupInstanceTools.PriorityGroupInstanceListAdapter
import com.deviousindustries.testtask.session.SessionFragment
import com.deviousindustries.testtask.session_viewer.SessionViewerFragment
import com.deviousindustries.testtask.task.TaskFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class InstanceDisplayFragment : Fragment(), PriorityGroupInstanceListAdapter.OnElementItemClick {
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
        viewModel.createPriorityCompleteFragment.observe(this, Observer { display ->
            if (display) {
                viewModel.createPriorityCompleteFragment.value = false
                GeneralDialogFragment("Complete all instances under ${viewModel.activeRecord!!.title} queue?",
                        positive = DialogInterface.OnClickListener { _, _ ->
                            viewModel.completePriority(viewModel.activePosition)
                        },
                        negative = null).show(fragmentManager!!, "Priority Complete")
            }
        })

        viewModel.createInstanceCompleteFragment.observe(this, Observer { display ->
            if (display){
                viewModel.createInstanceCompleteFragment.value = false
                GeneralDialogFragment("Complete ${viewModel.activeRecord!!.title}?",
                        positive = DialogInterface.OnClickListener { _, _ ->
                            viewModel.completeInstance(viewModel.activePosition)
                        },
                        negative = null).show(fragmentManager!!,"Instance Complete")
            }
        })

        viewModel.createSessionCompleteFragment.observe(this, Observer { display ->
            if (display){
                viewModel.createSessionCompleteFragment.value = false
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

    override fun onLongClick(position: Int) {
        viewModel.recordList.value!![position].callOnLongClick()
    }

    override fun onClick(position: Int) {
        viewModel.recordList.value!![position].callOnClick()
    }

    private fun setupTaskList() {
        activity!!.findViewById<RecyclerView>(R.id.InstanceList_Recycle).adapter = PriorityGroupInstanceListAdapter(
                this,
                R.layout.seperator_item1,
                R.layout.task_group1,
                R.layout.task_item1,
                R.id.title_text
        )
        viewModel.recordList.observe(this, Observer { records ->
            (activity!!.findViewById<RecyclerView>(R.id.InstanceList_Recycle).adapter as PriorityGroupInstanceListAdapter).data = records
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
