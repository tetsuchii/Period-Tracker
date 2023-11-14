package mobweb.hazi

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mobweb.hazi.CalendarUtils.cycleLength
import mobweb.hazi.CalendarUtils.selectedDate
import mobweb.hazi.CalendarUtils.selectedDates
import mobweb.hazi.databinding.ActivityCalendarBinding
import java.time.*
import kotlin.collections.ArrayList

import mobweb.hazi.CalendarUtils.daysInMonthArray
import mobweb.hazi.CalendarUtils.monthYearFromDate
import mobweb.hazi.CalendarUtils.periodLength
import mobweb.hazi.data.DayUnit
import mobweb.hazi.data.DayUnitDatabase
import java.time.temporal.ChronoUnit
import kotlin.concurrent.thread

class CalendarActivity : AppCompatActivity(), CalendarAdapter.CellOnItemListener {
    private lateinit var binding: ActivityCalendarBinding
    private lateinit var monthYearText : TextView
    private lateinit var calendarRecyclerV : RecyclerView
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var popup : Dialog
    private lateinit var note_dialog : Dialog
    private lateinit var show_note_dialog : Dialog
    private lateinit var database: DayUnitDatabase


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        monthYearText = binding.monthYearTextV
        calendarRecyclerV = binding.CalendarRecyclerView
        selectedDate = LocalDate.now();

        popup= Dialog(this)
        note_dialog= Dialog(this)
        show_note_dialog= Dialog(this)
        database = DayUnitDatabase.getDatabase(applicationContext)

        predictPeriod()
        setMonthView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setMonthView() {
        monthYearText.text = selectedDate?.let { monthYearFromDate(it) }
        val daysInMonth : ArrayList<LocalDate?> = daysInMonthArray(selectedDate)
        calendarAdapter = CalendarAdapter(daysInMonth, this)
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 7)
        calendarRecyclerV.layoutManager=layoutManager
        calendarRecyclerV.adapter=calendarAdapter
        loadDayUnitsInBackground(calendarAdapter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(position: Int, date: LocalDate) {
        selectedDate = date
        showPopup();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun previousMonthAction(view: android.view.View) {
        selectedDate = selectedDate?.minusMonths(1)
        selectedDate = selectedDate?.withDayOfMonth(1)
        predictPeriod()
        setMonthView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun nextMonthAction(view: android.view.View) {
        selectedDate = selectedDate?.plusMonths(1)
        selectedDate = selectedDate?.withDayOfMonth(1)
        predictPeriod()
        setMonthView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showPopup(){
        popup.setContentView(R.layout.popupmenu)
        val startperiod : TextView= popup.findViewById(R.id.StartPeriod)
        val stopperiod : TextView= popup.findViewById(R.id.StopPeriod)
        val addnote : TextView= popup.findViewById(R.id.AddNote)
        val shownote : TextView= popup.findViewById(R.id.ShowNote)
        val deletenote : TextView= popup.findViewById(R.id.DeleteNote)

        startperiod.setOnClickListener {
            calculatePeriodandCycleLength()
            thread {
                var newdayunit : DayUnit
                for (i in 0..periodLength-1){
                    if(i == 0)
                        newdayunit = DayUnit("", selectedDate!!.plusDays(i.toLong()).toString(),true,true)
                    else
                        newdayunit = DayUnit("", selectedDate!!.plusDays(i.toLong()).toString(),true,false)
                    //selectedDates.add(selectedDate!!.plusDays(i.toLong()))
                    val tempunit = database.dayUnitDao().findByDate(selectedDate!!.plusDays(i.toLong()).toString())
                    if(tempunit != null && tempunit.date == selectedDate!!.plusDays(i.toLong()).toString()){
                        if(tempunit.note=="")
                        database.dayUnitDao().update(newdayunit)
                        else{
                            val oldnote=tempunit.note
                            database.dayUnitDao().update(newdayunit)
                            database.dayUnitDao().updateNote(oldnote,selectedDate!!.plusDays(i.toLong()).toString())
                        }
                    }
                    else
                        database.dayUnitDao().insert(newdayunit)
                }
            }
            predictPeriod()
            setMonthView()
            popup.dismiss()
        }
        stopperiod.setOnClickListener {
            calculatePeriodandCycleLength()
            thread {
                var list = database.dayUnitDao().getAllWithPeriod()
                for (item: DayUnit in list) {
                    if (selectedDate.toString() == item.date) {
                        var index: Int = list.indexOf(item);
                        for (i in 0..periodLength) {
                            if (index + i < list.size) {
                                if (list[index + i].isperiod) {
                                    if (list[index + i].note == "") {
                                        database.dayUnitDao().deleteItem(list[index + i])
                                    } else {
                                        list[index + i].isperiod = false
                                        list[index + i].isfirstday = false
                                        database.dayUnitDao().update(list[index + i])
                                    }
                                }
                            }
                        }
                    }
                }
            }
            predictPeriod()
            setMonthView()
            popup.dismiss()
        }
        addnote.setOnClickListener {
            popup.dismiss()
            showAddNote()
        }
        shownote.setOnClickListener {
            popup.dismiss()
            thread {
                var unit = database.dayUnitDao().findByDate(selectedDate.toString())
                runOnUiThread {
                    if (unit != null && unit.note != "")
                        showShowNote()
                }
            }
        }
        deletenote.setOnClickListener {
            popup.dismiss()
            thread {
                val dayunit_item = database.dayUnitDao().findByDate(selectedDate.toString())
                if(dayunit_item !=null){
                    if(dayunit_item.note != ""){
                        if(dayunit_item.isperiod){
                            database.dayUnitDao().updateNote("", selectedDate.toString())
                        }
                        else{
                            database.dayUnitDao().deleteById(selectedDate.toString())
                        }
                    }
                }

                }
            setMonthView()
            popup.dismiss()
        }
        popup.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculatePeriodandCycleLength() {
        val shepre=getSharedPreferences("PERIODANDCYCLE", MODE_PRIVATE)
        periodLength = shepre.getInt("PERIOD",5)
        cycleLength =shepre.getInt("CYCLE",28)
    }

    private fun showShowNote() {
        show_note_dialog.setContentView(R.layout.shownotedialog)
        val ok : Button = show_note_dialog.findViewById(R.id.okShow)
        var note : TextView= show_note_dialog.findViewById(R.id.note)
        thread {
            val dayunit_item = database.dayUnitDao().findByDate(selectedDate.toString())
            note.text=dayunit_item.note.toString()
        }
        ok.setOnClickListener {
            show_note_dialog.dismiss()
        }
        show_note_dialog.show()
    }

    private fun showAddNote() {
        note_dialog.setContentView(R.layout.notedialog)
        val note : EditText= note_dialog.findViewById(R.id.note)
        val ok : Button = note_dialog.findViewById(R.id.okNote)
        ok.setOnClickListener {
            thread {
                val dayunit_item = database.dayUnitDao().findByDate(selectedDate.toString())
                if(dayunit_item == null){
                    val newitem = DayUnit(note.text.toString(), selectedDate.toString(),false,false)
                    database.dayUnitDao().insert(newitem)
                }
                database.dayUnitDao().updateNote(note.text.toString(), selectedDate.toString())
            }
            note_dialog.dismiss()
        }
        note_dialog.show()
    }


    private fun loadDayUnitsInBackground(adapter: CalendarAdapter) {
        thread {
            val units = database.dayUnitDao().getAll()
            runOnUiThread {
                adapter.update(units)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun predictPeriod(){
        thread {
            val firstdays = database.dayUnitDao().getAllWithFirstDay()
            var min : Long = 80
            var closestFirstDay = DayUnit("","2001-11-12",false,false)
            for (item in firstdays){
                val year=item.date.subSequence(0,4)
                val month=item.date.subSequence(5,7)
                val day=item.date.subSequence(8,10)
                var date = LocalDate.of(year.toString().toInt(), month.toString().toInt(),day.toString().toInt())
                val maybemin = ChronoUnit.DAYS.between( date , selectedDate )
                if(maybemin<min){
                    min=maybemin;
                    closestFirstDay=item;
                }
            }
            val closestFirstDayToLocalDate = LocalDate.of(closestFirstDay.date.subSequence(0,4).toString().toInt(), closestFirstDay.date.subSequence(5,7).toString().toInt(),closestFirstDay.date.subSequence(8,10).toString().toInt())
            var i = selectedDates.size
            while (i !=0){
                selectedDates.remove(selectedDates[i-1])
                i--
            }
            for (i in 0..periodLength-1){
                selectedDates.add(i,closestFirstDayToLocalDate.plusDays(cycleLength+i.toLong()))
            }
        }
        setMonthView()
    }

}



