package cr.ac.una.andersonRymichaelS.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cr.ac.una.andersonRymichaelS.Entity.MarkedPlace
import cr.ac.una.andersonRymichaelS.dao.WikiDao

@Database(entities = [MarkedPlace::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wikiDao(): WikiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
