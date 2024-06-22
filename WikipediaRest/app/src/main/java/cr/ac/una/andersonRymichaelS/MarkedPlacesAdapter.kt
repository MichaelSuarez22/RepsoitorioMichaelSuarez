package cr.ac.una.andersonRymichaelS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import cr.ac.una.andersonRymichaelS.Entity.MarkedPlace

class MarkedPlacesAdapter(private var places: List<MarkedPlace>) : RecyclerView.Adapter<MarkedPlacesAdapter.ViewHolder>() {

    fun updateData(newPlaces: List<MarkedPlace>) {
        places = newPlaces
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_marked_place, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = places[position]
        holder.placeName.text = place.placeName
        holder.placeDescription.text = place.wikipediaArticleTitle
        holder.detectedAt.text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(place.detectedAt)

        // Cargar la imagen del artículo de Wikipedia
        val imageUrl = "https://en.wikipedia.org/wiki/Special:FilePath/${place.wikipediaArticleTitle.replace(" ", "_")}"
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(android.R.drawable.ic_menu_report_image)
            .into(holder.articleImage)
    }

    override fun getItemCount(): Int = places.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val articleImage: ImageView = itemView.findViewById(R.id.articleImage)
        val placeName: TextView = itemView.findViewById(R.id.placeName)
        val placeDescription: TextView = itemView.findViewById(R.id.placeDescription)
        val detectedAt: TextView = itemView.findViewById(R.id.detectedAt)
    }
}
