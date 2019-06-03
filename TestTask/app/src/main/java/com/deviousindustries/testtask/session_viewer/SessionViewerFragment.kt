package com.deviousindustries.testtask.session_viewer


import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.deviousindustries.testtask.classes.Session

import com.deviousindustries.testtask.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SessionViewerFragment : Fragment() {

    lateinit var sessionList: ListView
    lateinit var viewModel: SessionViewerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.session_viewer_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.i("SessionViewer", "View Model Provider Called")
        viewModel = ViewModelProviders.of(activity!!).get(SessionViewerViewModel::class.java)
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

        sessionList.setOnItemClickListener{ parent, view, position, id ->
            val intent = Intent(context, com.deviousindustries.testtask.session.Session::class.java)
            intent.putExtra("SESSION_ID", id)
            ContextCompat.startActivity(activity!!, intent, null)
        }

        sessionList.setOnItemLongClickListener{ parent, view, position, id ->
            DeleteSessionFragment().apply {
                arguments = Bundle().apply {
                    putLong("SessionID", id)}}
                    .show(childFragmentManager, "Delete Session")
                true
        }
    }

    class DeleteSessionFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val tmpSessionID = arguments!!.getLong("SessionID")
            val builder = AlertDialog.Builder(activity)
            builder.setMessage("Delete Session?")
                    .setPositiveButton("Confirm") { dialog, id ->
                        val viewModel = ViewModelProviders.of(activity!!).get(SessionViewerViewModel::class.java)
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
