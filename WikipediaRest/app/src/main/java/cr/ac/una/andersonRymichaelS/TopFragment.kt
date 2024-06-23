// TopFragment.kt
package cr.ac.una.andersonRymichaelS

import android.content.Context
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

class TopFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TopPlacesAdapter
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_top, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewTop)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TopPlacesAdapter(emptyList())
        recyclerView.adapter = adapter
        database = AppDatabase.getDatabase(requireContext())

        loadTopPlaces()

        return view
    }

    private fun loadTopPlaces() {
        lifecycleScope.launch {
            // Obtener el número de lugares configurados en ParametersFragment
            val sharedPreferences = requireActivity().getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
            val numberOfPlaces = sharedPreferences.getInt(
                getString(R.string.pref_key_number_of_places),
                5 // Valor por defecto
            )

            // Cargar los lugares más visitados desde la base de datos
            val topPlaces = withContext(Dispatchers.IO) {
                database.wikiDao().getTopMarkedPlaces(numberOfPlaces)
            }
            adapter.updateData(topPlaces)
        }
    }
}

