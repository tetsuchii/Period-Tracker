package mobweb.hazi.data

import androidx.room.*
import java.time.chrono.ChronoPeriod

@Dao
interface DayUnitDao {
    @Query("SELECT * FROM dayunit")
    fun getAll(): List<DayUnit>

    @Query("SELECT * FROM dayunit WHERE isperiod =1")
    fun getAllWithPeriod(): List<DayUnit>

    @Query("SELECT * FROM DAYUNIT WHERE isfirstday=1")
    fun getAllWithFirstDay(): List<DayUnit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(shoppingItems: DayUnit): Long

    @Update
    fun update(shoppingItem: DayUnit)

    @Delete
    fun deleteItem(shoppingItem: DayUnit)

    @Query("SELECT * FROM  dayunit  WHERE date =:date ")
    fun findByDate(date : String) : DayUnit

    @Query("UPDATE dayunit SET note = :note WHERE date LIKE :date ")
    fun updateNote(note : String,date : String)

    @Query("DELETE FROM dayunit WHERE date = :date")
    fun deleteById( date : String)
}