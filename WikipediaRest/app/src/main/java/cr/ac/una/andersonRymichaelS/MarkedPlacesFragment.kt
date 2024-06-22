package cr.ac.una.andersonRymichaelS

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cr.ac.una.andersonRymichaelS.Entity.MarkedPlace
import cr.ac.una.andersonRymichaelS.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MarkedPlacesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MarkedPlacesAdapter
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_marked_places, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MarkedPlacesAdapter(emptyList())
        recyclerView.adapter = adapter
        database = AppDatabase.getDatabase(requireContext())

        loadMarkedPlaces()

        return view
    }

    private fun loadMarkedPlaces() {
        lifecycleScope.launch {
            val markedPlaces = withContext(Dispatchers.IO) {
                database.wikiDao().getAllMarkedPlaces()
            }
            adapter.updateData(markedPlaces)
        }
    }
}