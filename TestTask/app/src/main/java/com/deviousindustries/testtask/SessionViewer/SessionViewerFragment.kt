package com.deviousindustries.testtask.SessionViewer


import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.deviousindustries.testtask.Classes.Session

import com.deviousindustries.testtask.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SessionViewerFragment : Fragment() {

    lateinit var sessionList: ListView
    lateinit var viewModel: SessionViewerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_session_viewer, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.i("SessionViewer", "View Model Provider Called")
        viewModel = ViewModelProviders.of(this).get(SessionViewerViewModel::class.java)
        setupFab()
        setupSessionList()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSessionList()
    }

    private fun setupFab() {
        val fab = activity!!.findViewById(R.id.AddSession_FAB) as FloatingActionButton
        fab.setOnClickListener { viewModel.createSession(context!!) }
    }

    private fun setupSessionList(){
        val application = requireNotNull(this.activity).application
        sessionList = activity!!.findViewById(R.id.lsvSessionList)
        viewModel.sessionList.observe(this, Observer<List<Session>> { sessions ->
            val sessionAdapter = object : ArrayAdapter<Session>(application,
                    R.layout.task_item1,
                    sessions) {
                override fun getItemId(position: Int): Long {
                    return getItem(position).timeID
                }

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    var convertView = convertView
                    if (convertView == null) {
                        convertView = (application.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.task_item1, null)
                    }

                    (convertView!!.findViewById(R.id.title_text) as TextView).text = getItem(position).title
                    return convertView
                }
            }

            sessionList.setAdapter(sessionAdapter)
        })

        sessionList.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            viewModel.viewSessionDetails(application, sessionList.getItemIdAtPosition(position))
        })

        sessionList.setOnItemLongClickListener(AdapterView.OnItemLongClickListener { parent, view, position, id ->
            val bundle = Bundle()
            bundle.putLong("SessionID", sessionList.getItemIdAtPosition(position))
            val newFragment = DeleteSessionFragment()
            newFragment.setArguments(bundle)
            newFragment.show(activity!!.getSupportFragmentManager(), "Delete Session")
            true
        })
    }

    inner class DeleteSessionFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val tmpSessionID = arguments!!.getLong("SessionID")
            val builder = AlertDialog.Builder(activity)
            builder.setMessage("Delete Session?")
                    .setPositiveButton("Confirm") { dialog, id ->
                        viewModel.deleteSession(tmpSessionID)
                        viewModel.loadSessionList()
                    }
                    .setNegativeButton("Cancel") { dialog, id ->
                        // User cancelled the dialog
                    }
            // Create the AlertDialog object and return it
            return builder.create()
        }
    }

    companion object {
        fun newInstance(): SessionViewerFragment {
            return SessionViewerFragment()
        }
    }

}
