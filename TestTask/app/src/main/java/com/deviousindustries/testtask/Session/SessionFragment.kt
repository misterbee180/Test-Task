package com.deviousindustries.testtask.Session

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deviousindustries.testtask.R

class SessionFragment : Fragment() {

    companion object {
        fun newInstance() = SessionFragment()
    }

    private lateinit var viewModel: SessionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.session_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SessionViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
