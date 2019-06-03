package com.deviousindustries.testtask.session

import android.app.Activity.RESULT_OK
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.deviousindustries.testtask.constants.NULL_OBJECT

import com.deviousindustries.testtask.R
import com.deviousindustries.testtask.timekeeper.TimekeeperFragment
import com.deviousindustries.testtask.timekeeper.TimekeeperViewModel

class SessionFragment() : Fragment() {

    companion object {
        fun newInstance() = SessionFragment()
    }

    private lateinit var viewModel: SessionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.session_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(SessionViewModel::class.java)
        viewModel.setTimekeeper(ViewModelProviders.of(activity!!).get(TimekeeperViewModel::class.java))

        val bundle = arguments
        if (bundle != null) {
            viewModel.Start(bundle.getLong("SESSION_ID",NULL_OBJECT))
        }

        setObservables()
        setEvents()

        //Add timekeeper fragment
        childFragmentManager
                .beginTransaction()
                .replace(R.id.container, TimekeeperFragment.newInstance().apply {
                    arguments = Bundle().apply {
                        putLong("TIME_ID",viewModel.sessionID)
                    }
                })
                .commitNow()
    }

    fun setObservables(){
        viewModel.title.observe(this, Observer{title ->
            setTitle(title)
        })

        viewModel.eventComplete.observe(this, Observer { complete ->
            if(complete){
                completeSession()
                viewModel.eventComplete.value = false
            }
        })
    }

    fun completeSession(){
        with(activity!!) {
            intent.putExtra("EXTRA_SESSION_ID", viewModel.sessionID);
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    fun setEvents(){
        activity!!.findViewById<EditText>(R.id.Session_Title_EditText)
                .addTextChangedListener(onTextChanged = { s, start, before, count ->
            viewModel.title.value = s.toString()
        })

        activity!!.findViewById<Button>(R.id.Confirm_Button).setOnClickListener{_ ->
            viewModel.saveSession()
        }
    }

    //REQUIRED for 2 way setting
    private fun setTitle(title: String) {
        Log.i("Test", activity!!.findViewById<EditText>(R.id.Session_Title_EditText).getText().toString())
        if(!activity!!.findViewById<EditText>(R.id.Session_Title_EditText).getText().toString().equals(title)) {
            activity!!.findViewById<EditText>(R.id.Session_Title_EditText).setText(title)
        }
    }
}
