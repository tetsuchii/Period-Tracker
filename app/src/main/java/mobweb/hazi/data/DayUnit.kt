package mobweb.hazi.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "dayunit")
data class DayUnit(
    @ColumnInfo(name = "note") var note: String,
    @ColumnInfo(name = "date")@PrimaryKey var date: String ,
    @ColumnInfo(name = "isperiod") var isperiod: Boolean,
    @ColumnInfo(name = "isfirstday") var isfirstday: Boolean
) {

}