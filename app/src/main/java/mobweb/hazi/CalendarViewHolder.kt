package mobweb.hazi

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate

class CalendarViewHolder(itemView: View, private var listener: CalendarAdapter.CellOnItemListener?, days: ArrayList<LocalDate?> ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val dayOfMonth : TextView = itemView.findViewById(R.id.CellDayTextV)
        val parentView : View = itemView.findViewById(R.id.parentView)
        var days : ArrayList<LocalDate?> = ArrayList()
    init {
        itemView.setOnClickListener(this)
        this.days=days
    }

    override fun onClick(p0: View?) {
        days.get(adapterPosition)?.let { listener?.onItemClick(adapterPosition, it) }
    }



}