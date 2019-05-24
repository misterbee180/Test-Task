package com.deviousindustries.testtask.SessionViewer


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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.deviousindustries.testtask.Classes.Session

import com.deviousindustries.testtask.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
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

        sessionList = activity!!.findViewById(R.id.lsvSessionList)
        viewModel.sessionList.observe(this, Observer<List<Session>> { sessions ->
            val sessionAdapter = object : ArrayAdapter<Session>(activity,
                    R.layout.task_item1,
                    sessions) {
                override fun getItemId(position: Int): Long {
                    return getItem(position).timeID
                }

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    var convertView = convertView
                    if (convertView == null) {
                        convertView = (activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.task_item1, null)
                    }

                    (convertView!!.findViewById(R.id.title_text) as TextView).text = getItem(position).title
                    return convertView
                }
            }

            sessionList.setAdapter(sessionAdapter)
        })

        sessionList.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            viewModel.viewSessionDetails(context!!, sessionList!!.getItemIdAtPosition(position))
        })

        sessionList.setOnItemLongClickListener(AdapterView.OnItemLongClickListener { parent, view, position, id ->
            val bundle = Bundle()
            bundle.putLong("SessionID", sessionList.getItemIdAtPosition(position))
            val newFragment = Viewer_Session.DeleteSessionFragment()
            newFragment.setArguments(bundle)
            newFragment.show(getSupportFragmentManager(), "Delete Session")

            true
        })
    }
}
