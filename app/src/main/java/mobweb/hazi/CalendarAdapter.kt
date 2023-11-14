package mobweb.hazi

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import mobweb.hazi.CalendarUtils.selectedDates
import mobweb.hazi.data.DayUnit
import java.time.LocalDate

class CalendarAdapter(
    private val daysOfMonth: ArrayList<LocalDate?>,
    private val listener: CellOnItemListener,
) :
    RecyclerView.Adapter<CalendarViewHolder>() {
    private val items = mutableListOf<DayUnit>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view : View = inflater.inflate(R.layout.calendar_cell, parent, false)
        val layoutParams : ViewGroup.LayoutParams? = view.layoutParams
        if (layoutParams != null) {
            layoutParams.height= (parent.height * 0.166666666).toInt()
        }
        return CalendarViewHolder(view, listener, daysOfMonth)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date : LocalDate? = daysOfMonth.get(position)
        if(date !=null) {
            holder.dayOfMonth.setText((date.getDayOfMonth()).toString())
            if (date.equals(CalendarUtils.selectedDate))
                holder.parentView.setBackgroundColor(Color.LTGRAY);
            for (item: LocalDate? in selectedDates) {
                if (date.equals(item)){

                    holder.parentView.setBackgroundColor(Color.parseColor("#f4c2c2"))
                }
            }
            for (item: DayUnit in items) {
                if (date.toString().equals(item.date) && item.isperiod)
                    holder.parentView.setBackgroundColor(Color.parseColor("#e4668f"))
            }
        }
        else {
            holder.dayOfMonth.text = ""
        }
    }

    override fun getItemCount(): Int {
        return daysOfMonth.size
    }

    interface CellOnItemListener{
        fun onItemClick(position : Int, date : LocalDate)
    }

    public fun addItem(item: DayUnit) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    public fun update(dayitems: List<DayUnit>) {
        items.clear()
        items.addAll(dayitems)
        notifyDataSetChanged()
    }

}