package cr.ac.una.andersonRymichaelS

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.Fragment

class ParametersFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private val defaultNumberOfPlaces = 5 // Valor por defecto

    @SuppressLint("StringFormatInvalid")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_parameters, container, false)
        sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )

        // Referencias a los elementos de la vista
        val numberPicker = view.findViewById<NumberPicker>(R.id.numberPickerPlaces)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val textView = view.findViewById<TextView>(R.id.textView)

        // Cargar el valor guardado de SharedPreferences
        val numberOfPlaces = sharedPreferences.getInt(
            getString(R.string.pref_key_number_of_places),
            defaultNumberOfPlaces
        )

        // Configurar el NumberPicker
        numberPicker.minValue = 1
        numberPicker.maxValue = 20
        numberPicker.value = numberOfPlaces

        // Mostrar el valor actual
        textView.text = getString(R.string.number_of_visits, numberOfPlaces)

        // Guardar el nuevo valor cuando se cambie
        btnSave.setOnClickListener {
            val newValue = numberPicker.value
            saveNumberOfPlaces(newValue)
            textView.text = getString(R.string.number_of_visits, newValue)
        }

        return view
    }

    private fun saveNumberOfPlaces(value: Int) {
        with(sharedPreferences.edit()) {
            putInt(getString(R.string.pref_key_number_of_places), value)
            apply()
        }
    }
}
