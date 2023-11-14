package mobweb.hazi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DayUnit::class], version = 1)
abstract class DayUnitDatabase : RoomDatabase() {
    abstract fun dayUnitDao(): DayUnitDao

    companion object {
        fun getDatabase(applicationContext: Context): DayUnitDatabase {
            return Room.databaseBuilder(
                applicationContext,
                DayUnitDatabase::class.java,
                "day-unit"
            ).build();
        }
    }
}