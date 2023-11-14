package mobweb.hazi

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import mobweb.hazi.databinding.ActivitySettingsBinding
import android.text.TextUtils

import android.widget.EditText
import java.text.SimpleDateFormat
import java.util.*
import androidx.annotation.RequiresApi
import mobweb.hazi.CalendarUtils.cycleLength
import mobweb.hazi.CalendarUtils.periodLength
import mobweb.hazi.data.DayUnit
import mobweb.hazi.data.DayUnitDatabase
import java.time.LocalDate
import kotlin.concurrent.thread
import android.preference.PreferenceManager

import android.content.SharedPreferences





class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lastFirstDay.transformIntoDatePicker(this, "MM/dd/yyyy", Date())

        binding.ApplyChanges.setOnClickListener{
            if(checkData()){
                var database = DayUnitDatabase.getDatabase(applicationContext)
                periodLength=binding.periodLength.text.toString().toInt()
                cycleLength=binding.cycleLength.text.toString().toInt()
                val year=binding.lastFirstDay.text.toString().subSequence(6,10)
                val month=binding.lastFirstDay.text.toString().subSequence(0,2)
                val day=binding.lastFirstDay.text.toString().subSequence(3,5)
                var date = LocalDate.of(year.toString().toInt(), month.toString().toInt(),day.toString().toInt())
                thread {
                    database.clearAllTables()
                    var newdayunit : DayUnit
                    for (i in 0..periodLength-1){
                        if(i == 0)
                            newdayunit = DayUnit("", date!!.plusDays(i.toLong()).toString(),true,true)
                        else
                            newdayunit = DayUnit("", date!!.plusDays(i.toLong()).toString(),true,false)
                            database.dayUnitDao().insert(newdayunit)
                    }
                }

                setDefaults("CYCLE", cycleLength)
                setDefaults("PERIOD", periodLength)

                val intent = Intent(this,CalendarActivity::class.java)
                startActivity(intent)
            }
        }
    }

    fun setDefaults(key: String, value: Int) {
        val sharedPref = getSharedPreferences(
            "PERIODANDCYCLE", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt(key, value)
        editor.commit()
    }

    private fun EditText.transformIntoDatePicker(context: Context, format: String, maxDate: Date? = null) {
        isFocusableInTouchMode = false
        isClickable = true
        isFocusable = false

        val myCalendar = Calendar.getInstance()
        val datePickerOnDataSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(format, Locale.UK)
                setText(sdf.format(myCalendar.time))
            }

        setOnClickListener {
            DatePickerDialog(
                context, datePickerOnDataSetListener, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).run {
                maxDate?.time?.also { datePicker.maxDate = it }
                show()
            }
        }
    }


    private fun checkData() :Boolean {
        var datavalid=true
        if(isEmpty(binding.periodLength)){
            binding.periodLength.error = "You must enter cycle length!"
            datavalid=false
        }
        if(isEmpty(binding.cycleLength)){
            binding.cycleLength.error = "You must enter cycle length!"
            datavalid=false
        }
        if(isEmpty(binding.lastFirstDay)){
            binding.lastFirstDay.error = "You must enter cycle length!"
            datavalid=false
        }
        return datavalid
    }

    private fun isEmpty(text: EditText): Boolean {
        val str: CharSequence = text.text.toString()
        return TextUtils.isEmpty(str)
    }
}