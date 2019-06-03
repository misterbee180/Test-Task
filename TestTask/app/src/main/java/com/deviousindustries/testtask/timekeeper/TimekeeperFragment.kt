package com.deviousindustries.testtask.timekeeper

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.deviousindustries.testtask.constants.*
import com.deviousindustries.testtask.R
import com.deviousindustries.testtask.Utilities
import java.util.*

const val FROM_SOURCE = "FROM"
const val TO_SOURCE = "TO"

class TimekeeperFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, DialogInterface.OnClickListener {
    companion object {
        fun newInstance() = TimekeeperFragment()
    }

    private lateinit var viewModel: TimekeeperViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.timekeeper_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(TimekeeperViewModel::class.java)

        val bundle = arguments
        if (bundle != null) {
            viewModel.loadTime(bundle.getLong("TIME_ID", NULL_OBJECT))
        }

        setObservers()
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

    override fun onClick(dialog: DialogInterface?, which: Int) {
        viewModel.monthSpecificArray[(dialog as AlertDialog).findViewById<NumberPicker>(1).value - 1] = true
        viewModel.updateSpecific()
    }

    private fun setObservers(){
        val activity = requireNotNull(this.activity)

        viewModel.mTime.observe(this, Observer { time ->
            activity.findViewById<Spinner>(R.id.Repetition_Spinner).setSelection(time.fintRepetition)
            activity.findViewById<CheckBox>(R.id.Thru_CheckBox).isChecked = time.fblnThru
        })

        viewModel.timeframe.observe(this, Observer{
            activity.findViewById<Spinner>(R.id.Timeframe_Spinner).setSelection(it)
            determineTimeframeVisibility(it, viewModel.mTime.value!!.fintRepetition)
        })

        viewModel.starting.observe(this, Observer{
            activity.findViewById<Spinner>(R.id.Starting_Spinner).setSelection(it)
        })

        viewModel.week.observe(this, Observer { week ->
            activity.findViewById<CheckBox>(R.id.Monday_Checkbox).isChecked = week.fblnMonday
            activity.findViewById<CheckBox>(R.id.Tuesday_Checkbox).isChecked = week.fblnTuesday
            activity.findViewById<CheckBox>(R.id.Wednesday_Checkbox).isChecked = week.fblnWednesday
            activity.findViewById<CheckBox>(R.id.Thursday_Checkbox).isChecked = week.fblnThursday
            activity.findViewById<CheckBox>(R.id.Friday_Checkbox).isChecked = week.fblnFriday
            activity.findViewById<CheckBox>(R.id.Saturday_Checkbox).isChecked = week.fblnSaturday
            activity.findViewById<CheckBox>(R.id.Sunday_Checkbox).isChecked = week.fblnSunday
        })

        viewModel.month.observe(this, Observer { month ->
            activity.findViewById<CheckBox>(R.id.First_Month_Checkbox).isChecked = month.fblnFirst
            activity.findViewById<CheckBox>(R.id.Middle_Month_Checkbox).isChecked = month.fblnMiddle
            activity.findViewById<CheckBox>(R.id.Last_Month_Checkbox).isChecked = month.fblnLast
            activity.findViewById<CheckBox>(R.id.After_Week_Checkbox).isChecked = month.fblnAfterWkn
            activity.findViewById<EditText>(R.id.Specific_Text).setText(month.fstrSpecific)
        })

        viewModel.monthSpecificRadio.observe(this, Observer{ specific ->
            activity.findViewById<RadioButton>(R.id.Specific_Montly_Radio).isChecked = specific
            activity.findViewById<RadioButton>(R.id.General_Monthly_Radio).isChecked = !specific
        })

        viewModel.isSession.observe(this, Observer { adjust ->
            if(adjust){
                adjustRepetitionForSession()
            }
        })

        viewModel.monthSpecificString.observe(this, Observer { specific ->
            activity.findViewById<EditText>(R.id.Specific_Text).setText(specific)
        })

        viewModel.fromDate.observe(this, Observer{
            setTimeDateDetails(R.id.FromDate_Text, R.id.FromDate_Remove_Button, it)
        })

        viewModel.toDate.observe(this, Observer{
            setTimeDateDetails(R.id.ToDate_Text, R.id.ToDate_Remove_Button, it)
        })

        viewModel.fromTime.observe(this, Observer{
            setTimeDateDetails(R.id.FromTime_Text, R.id.FromTime_Remove_Button, it)
        })

        viewModel.toTime.observe(this, Observer{
            setTimeDateDetails(R.id.ToTime_Text, R.id.ToTime_Remove_Button, it)
        })
    }

    private fun setTimeDateDetails(setText: Int, removeBtn: Int, text: String){
        activity!!.findViewById<TextView>(setText).setText(text)
        activity!!.findViewById<Button>(removeBtn).visibility = if(text.equals("")) View.GONE else View.VISIBLE
    }

    private fun setEvents(){
        val activity = requireNotNull(this.activity)
        activity.findViewById<Spinner>(R.id.Repetition_Spinner).onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                viewModel.setRepetitionID(position)
                displayTimeframeAndStarting(viewModel.mTime.value!!.fintRepetition)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        activity.findViewById<Spinner>(R.id.Timeframe_Spinner).onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                viewModel.setTimeframe(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        activity.findViewById<Spinner>(R.id.Starting_Spinner).onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                viewModel.mTime.value?.fintStarting = position
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        activity.findViewById<CheckBox>(R.id.Thru_CheckBox).setOnCheckedChangeListener { _, isChecked ->
            viewModel.mTime.value?.fblnThru = isChecked
        }

        activity.findViewById<Button>(R.id.FromDate_Add_Button).setOnClickListener{
            viewModel.prepDateAdd(FROM_SOURCE)
            instantiateDateFragment()
        }

        activity.findViewById<Button>(R.id.ToDate_Add_Button).setOnClickListener{
            viewModel.prepDateAdd(TO_SOURCE)
            instantiateDateFragment()
        }

        activity.findViewById<Button>(R.id.FromTime_Add_Button).setOnClickListener{
            viewModel.prepTimeAdd(FROM_SOURCE)
            instantiateTimeFragment()
        }

        activity.findViewById<Button>(R.id.ToTime_Add_Button).setOnClickListener{
            viewModel.prepTimeAdd(TO_SOURCE)
            instantiateTimeFragment()
        }

        activity.findViewById<Button>(R.id.FromDate_Remove_Button).setOnClickListener{
            viewModel.removeDate(FROM_SOURCE)
        }

        activity.findViewById<Button>(R.id.ToDate_Remove_Button).setOnClickListener{
            viewModel.removeDate(TO_SOURCE)
        }

        activity.findViewById<Button>(R.id.FromTime_Remove_Button).setOnClickListener{
            viewModel.removeTime(FROM_SOURCE)
        }

        activity.findViewById<Button>(R.id.ToTime_Remove_Button).setOnClickListener{
            viewModel.removeTime(TO_SOURCE)
        }
    }

    private fun setWeekEvents(){
        val activity = requireNotNull(this.activity)
        activity.findViewById<CheckBox>(R.id.Monday_Checkbox)
                .setOnCheckedChangeListener { _, isChecked ->
            viewModel.week.value?.fblnMonday = isChecked
        }
        activity.findViewById<CheckBox>(R.id.Tuesday_Checkbox)
                .setOnCheckedChangeListener { _, isChecked ->
            viewModel.week.value?.fblnTuesday = isChecked
        }
        activity.findViewById<CheckBox>(R.id.Wednesday_Checkbox)
                .setOnCheckedChangeListener { _, isChecked ->
            viewModel.week.value?.fblnWednesday = isChecked
        }
        activity.findViewById<CheckBox>(R.id.Thursday_Checkbox)
                .setOnCheckedChangeListener { _, isChecked ->
            viewModel.week.value?.fblnThursday = isChecked
        }
        activity.findViewById<CheckBox>(R.id.Friday_Checkbox)
                .setOnCheckedChangeListener { _, isChecked ->
            viewModel.week.value?.fblnFriday = isChecked
        }
        activity.findViewById<CheckBox>(R.id.Saturday_Checkbox)
                .setOnCheckedChangeListener { _, isChecked ->
            viewModel.week.value?.fblnSaturday = isChecked
        }
        activity.findViewById<CheckBox>(R.id.Sunday_Checkbox)
                .setOnCheckedChangeListener { _, isChecked ->
            viewModel.week.value?.fblnSunday = isChecked
        }
    }

    private fun setMonthEvents(){
        val activity = requireNotNull(this.activity)
        activity.findViewById<RadioButton>(R.id.General_Monthly_Radio)
                .setOnClickListener{
                    setupMonthView(false)
                }
        activity.findViewById<RadioButton>(R.id.Specific_Montly_Radio)
                .setOnClickListener{
                    setupMonthView(true)
                }
        activity.findViewById<CheckBox>(R.id.First_Month_Checkbox)
                .setOnCheckedChangeListener { _, isChecked ->
                    viewModel.month.value?.fblnFirst = isChecked
                }
        activity.findViewById<CheckBox>(R.id.Middle_Month_Checkbox)
                .setOnCheckedChangeListener { _, isChecked ->
                    viewModel.month.value?.fblnMiddle = isChecked
                }
        activity.findViewById<CheckBox>(R.id.Last_Month_Checkbox)
                .setOnCheckedChangeListener { _, isChecked ->
                    viewModel.month.value?.fblnLast = isChecked
                }
        activity.findViewById<CheckBox>(R.id.After_Week_Checkbox)
                .setOnCheckedChangeListener { _, isChecked ->
                    viewModel.month.value?.fblnAfterWkn = isChecked
                }
        activity.findViewById<Button>(R.id.Specific_Add_Button)
                .setOnClickListener{
                    NumberPickerFragment().show(childFragmentManager, "numberPicker")
                }
    }

    private fun setupMonthView(isSpecific: Boolean) {
        activity!!.findViewById<LinearLayout>(R.id.General_Monthly_Layout).visibility = if(isSpecific) View.GONE else View.VISIBLE
        activity!!.findViewById<LinearLayout>(R.id.Specific_Monthly_Layout).visibility = if(isSpecific) View.VISIBLE else View.GONE
        viewModel.monthSpecificRadio.value = isSpecific
    }

    private fun determineTimeframeVisibility(timeframePosition: Int,
                                             repetitionPosition: Int) {
        if(repetitionPosition == BASE_POSITION) {
            displayGeneralDetails()
        } else {
            when (timeframePosition) {
                1 -> {
                    displayWeekDetails()
                    activity!!.findViewById<CheckBox>(R.id.Thru_CheckBox).visibility = View.VISIBLE
                }
                2 -> {
                    displayMonthDetails()
                    activity!!.findViewById<CheckBox>(R.id.Thru_CheckBox).visibility = View.VISIBLE
                    setupMonthView(viewModel.monthSpecificRadio.value!!)
                }
                else -> {
                    displayNoDetails()
                    activity!!.findViewById<CheckBox>(R.id.Thru_CheckBox).visibility = View.GONE
                }
            }
        }
    }

    private fun adjustRepetitionForSession(){
        with(activity!!.findViewById<Spinner>(R.id.Repetition_Spinner)){
            adapter = ArrayAdapter<String>(activity!!, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.Session_RepetitionArray))
        }
    }

    //region old Observers and Events and Frags
//    private fun setObservers(){
//        var activity = requireNotNull(this.activity)
//
//        viewModel.repetition.observe(this, Observer { repetition ->
//            activity.findViewById<Spinner>(R.id.Repetition_Spinner).setSelection(repetition)
//        })
//
//        viewModel.timeframe.observe(this, Observer { timeframe ->
//            activity.findViewById<Spinner>(R.id.Timeframe_Spinner).setSelection(timeframe)
//        })
//
//        viewModel.starting.observe(this, Observer { starter ->
//            activity.findViewById<Spinner>(R.id.Starting_Spinner).setSelection(starter)
//        })
//
//        viewModel.isThru.observe(this, Observer { thru ->
//            activity.findViewById<CheckBox>(R.id.Thru_CheckBox).setChecked(thru)
//        })
//
//        viewModel.fromDate.observe(this, Observer {fromMilli ->
//            if(fromMilli != NULL_DATE) activity!!.findViewById<Button>(R.id.FromDate_Button).text = fromMilli.toString()
//        })
//
//        viewModel.toDate.observe(this, Observer { toMilli ->
//            if(toMilli != NULL_DATE) activity!!.findViewById<Button>(R.id.ToDate_Button).text = toMilli.toString()
//        })
//    }

//    private fun setEvents(){
//        var activity = requireNotNull(this.activity)
//        activity.findViewById<Spinner>(R.id.Repetition_Spinner).onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                viewModel.repetition.value = position
//                displayTimeframeAndStarting(position != BASE_POSITION)
//            }
//            override fun onNothingSelected(parent: AdapterView<*>) {
//            }
//        };
//
//        activity.findViewById<Spinner>(R.id.Timeframe_Spinner).onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                viewModel.timeframe.value = position
//                when(position){
//                    1 -> displayWeekDetails()
//                    2 -> displayMonthDetails()
//                    else -> displayGeneralDetails()
//                }
//            }
//            override fun onNothingSelected(parent: AdapterView<*>) {
//            }
//        };
//
//        activity.findViewById<Spinner>(R.id.Starting_Spinner).onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                viewModel.starting.value = position
//            }
//            override fun onNothingSelected(parent: AdapterView<*>) {
//            }
//        };
//
//        activity.findViewById<CheckBox>(R.id.Thru_CheckBox).setOnCheckedChangeListener {view, isChecked ->
//            viewModel.isThru.value = isChecked
//        }
//    }

    //    private fun hideTimeframeFrame(){
//        activity!!.findViewById<FrameLayout>(R.id.Timeframe_Frame).visibility = View.GONE
//    }

//    private fun displayWeekDetails(weekID: Long = NULL_DATE){
//        fragmentManager?.beginTransaction()
//                ?.replace(R.id.container2, WeekFragment.newInstance(weekID))
//                ?.commitNow()
//    }
//
//    private fun displayMonthDetails(monthID: Long = NULL_DATE){
//        fragmentManager?.beginTransaction()
//                ?.replace(R.id.container2, MonthFragment.newInstance(monthID))
//                ?.commitNow()
//
//    }
//
//    private fun displayGeneralDetails(fromDate: Long = NULL_DATE,
//                                   toDate: Long = NULL_DATE){
//        fragmentManager?.beginTransaction()
//                ?.replace(R.id.container2, GeneralFragment.newInstance(fromDate, toDate))
//                ?.commitNow()
//    }

    //endregion

    private fun displayWeekDetails() {
        hideMonthDetails()
        hideGeneralDetails()
        activity!!.findViewById<ConstraintLayout>(R.id.Week_Constraint).visibility = View.VISIBLE
    }

    private fun displayMonthDetails() {
        hideWeekDetails()
        hideGeneralDetails()
        activity!!.findViewById<ConstraintLayout>(R.id.Month_Constraint).visibility = View.VISIBLE
    }

    private fun displayNoDetails() {
        hideWeekDetails()
        hideMonthDetails()
        hideGeneralDetails()
    }

    private fun displayGeneralDetails() {
        hideWeekDetails()
        hideMonthDetails()
        activity!!.findViewById<ConstraintLayout>(R.id.General_Constraint).visibility = View.VISIBLE
        activity!!.findViewById<CheckBox>(R.id.Thru_CheckBox).visibility = View.VISIBLE
    }

    private fun hideWeekDetails(){
        activity!!.findViewById<ConstraintLayout>(R.id.Week_Constraint).visibility = View.GONE
    }

    private fun hideMonthDetails(){
        activity!!.findViewById<ConstraintLayout>(R.id.Month_Constraint).visibility = View.GONE
    }

    private fun hideGeneralDetails() {
        activity!!.findViewById<ConstraintLayout>(R.id.General_Constraint).visibility = View.GONE
    }

    private fun instantiateDateFragment() {
        DatePickerFragment().show(childFragmentManager, "datePicker")
    }

    private fun instantiateTimeFragment() {
        TimePickerFragment().show(childFragmentManager, "timePicker")
    }

    private fun displayTimeframeAndStarting(repetitionPosition: Int){
        val display = repetitionPosition != BASE_POSITION
        if(display) {
            activity!!.findViewById<Spinner>(R.id.Timeframe_Spinner).visibility = View.VISIBLE
            activity!!.findViewById<Spinner>(R.id.Starting_Spinner).visibility = View.VISIBLE
            determineTimeframeVisibility(viewModel.mTime.value!!.fintTimeframe,
                    repetitionPosition)
        } else {
            activity!!.findViewById<Spinner>(R.id.Timeframe_Spinner).visibility = View.GONE
            activity!!.findViewById<Spinner>(R.id.Starting_Spinner).visibility = View.GONE
            hideWeekDetails()
            hideMonthDetails()
            displayGeneralDetails()
        }
    }


}
