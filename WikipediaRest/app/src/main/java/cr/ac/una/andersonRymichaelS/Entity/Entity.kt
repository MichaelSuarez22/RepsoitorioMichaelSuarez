package cr.ac.una.andersonRymichaelS.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marked_places")
data class MarkedPlace(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val detectedAt: Long,
    val wikipediaArticleTitle: String,
    val placeName: String
)
