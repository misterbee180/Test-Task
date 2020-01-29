package com.deviousindustries.testtask.file_import

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.deviousindustries.testtask.R
import com.deviousindustries.testtask.databinding.ImportFragmentBinding

class ImportFragment : Fragment() {

    companion object {
        fun newInstance() = ImportFragment()
        const val READ_REQUEST_CODE = 100
    }

    private lateinit var viewModel: ImportViewModel
    private lateinit var binding: ImportFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.import_fragment, container, false)
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ImportViewModel::class.java)

        setEvents()
        // TODO: Use the ViewModel
    }

    private fun setEvents(){
        binding.SelectFilePathButton.setOnClickListener{fireFileSelecter()}
        binding.ImportButton.setOnClickListener{viewModel.import()}
    }

    private fun fireFileSelecter(){
        val intent = Intent(ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            when (requestCode){
                100 -> storeImportLocation(data)
            }
        }
    }

    private fun storeImportLocation(data: Intent?) {
        viewModel.setFileLocation(data?.data!!)
    }
}
