package com.deviousindustries.testtask.timekeeper

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.deviousindustries.testtask.constants.*
import com.deviousindustries.testtask.R
import com.deviousindustries.testtask.databinding.TimekeeperFragmentBinding

const val FROM_SOURCE = "FROM"
const val TO_SOURCE = "TO"

class TimekeeperFragment : Fragment(),
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        DialogInterface.OnClickListener {

    companion object {
        fun newInstance() = TimekeeperFragment()
    }

    lateinit var viewModel: TimekeeperViewModel
    private lateinit var binding: TimekeeperFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.timekeeper_fragment, container, false)
        binding.setLifecycleOwner(this)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(TimekeeperViewModel::class.java)
        binding.viewmodel = viewModel

        val bundle = arguments
        viewModel.loadTimekeeper(bundle?.getLong("TIME_ID", NULL_OBJECT)?: NULL_OBJECT)
//        if (bundle != null) {
//            viewModel.loadTimekeeper(bundle.getLong("TIME_ID", NULL_OBJECT))
//        }

        setEvents()
        setWeekEvents()
        setMonthEvents()
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        viewModel.setTime(hourOfDay, minute)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        viewModel.setDate(month, day)
    }

    @SuppressLint("ResourceType")
    override fun onClick(dialog: DialogInterface?, which: Int) {
        viewModel.monthSpecificArray[(dialog as AlertDialog).findViewById<NumberPicker>(1).value - 1] = true
        viewModel.updateSpecific()
    }

    private fun setEvents(){
        binding.RepetitionSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                viewModel.setRepetition(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        binding.TimeframeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                viewModel.setTimeframe(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        binding.StartingSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                viewModel.mTime.value?.fintStarting = position
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        binding.ThruCheckBox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.mTime.value?.fblnThru = isChecked
        }

        binding.FromDateAddButton.setOnClickListener{
            viewModel.prepDateAdd(FROM_SOURCE)
            instantiateDateFragment()
        }

        binding.ToDateAddButton.setOnClickListener{
            viewModel.prepDateAdd(TO_SOURCE)
            instantiateDateFragment()
        }

        binding.FromTimeAddButton.setOnClickListener{
            viewModel.prepTimeAdd(FROM_SOURCE)
            instantiateTimeFragment()
        }

        binding.ToTimeAddButton.setOnClickListener{
            viewModel.prepTimeAdd(TO_SOURCE)
            instantiateTimeFragment()
        }

        binding.FromDateRemoveButton.setOnClickListener{
            viewModel.removeDate(FROM_SOURCE)
        }

        binding.ToDateRemoveButton.setOnClickListener{
            viewModel.removeDate(TO_SOURCE)
        }

        binding.FromTimeRemoveButton.setOnClickListener{
            viewModel.removeTime(FROM_SOURCE)
        }

        binding.ToTimeRemoveButton.setOnClickListener{
            viewModel.removeTime(TO_SOURCE)
        }
    }

    private fun setWeekEvents(){
        binding.MondayCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.mTime.value!!.week.fblnMonday = isChecked
        }
        binding.TuesdayCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.mTime.value!!.week.fblnTuesday = isChecked
        }
        binding.WednesdayCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.mTime.value!!.week.fblnWednesday = isChecked
        }
        binding.ThursdayCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.mTime.value!!.week.fblnThursday = isChecked
        }
        binding.FridayCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.mTime.value!!.week.fblnFriday = isChecked
        }
        binding.SaturdayCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.mTime.value!!.week.fblnSaturday = isChecked
        }
        binding.SundayCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.mTime.value!!.week.fblnSunday = isChecked
        }
    }

    private fun setMonthEvents(){
        binding.GeneralMonthlyRadio.setOnClickListener{
                    viewModel.setupMonthView(false)
                }
        binding.SpecificMontlyRadio.setOnClickListener{
                    viewModel.setupMonthView(true)
                }
        binding.FirstMonthCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.mTime.value!!.month.fblnFirst = isChecked
                }
        binding.MiddleMonthCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.mTime.value!!.month.fblnMiddle = isChecked
                }
        binding.LastMonthCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.mTime.value!!.month.fblnLast = isChecked
                }
        binding.AfterWeekCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.mTime.value!!.month.fblnAfterWkn = isChecked
                }
        binding.SpecificAddButton.setOnClickListener{
                    NumberPickerFragment().show(childFragmentManager, "numberPicker")
                }
    }

    private fun instantiateDateFragment() {
        DatePickerFragment().show(childFragmentManager, "datePicker")
    }

    private fun instantiateTimeFragment() {
        TimePickerFragment().show(childFragmentManager, "timePicker")
    }




}
