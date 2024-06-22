package cr.ac.una.andersonRymichaelS.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cr.ac.una.andersonRymichaelS.Entity.MarkedPlace

@Dao
interface WikiDao {

    @Insert
    suspend fun insertMarkedPlace(markedPlace: MarkedPlace)

    @Query("SELECT * FROM marked_places")
    suspend fun getAllMarkedPlaces(): List<MarkedPlace>
}
