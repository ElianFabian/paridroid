package fr.ubordeaux.math.paridroid

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class PariSettingsFragment : PreferenceFragmentCompat() {

    private val changeListener = Preference.OnPreferenceChangeListener { pref, valObj ->
        val string = valObj.toString()
        pref.summary = string
        PariUtil.setDefault(pref.key, string)
        true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        val context = requireContext()
        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val keys = sp.all.keys
        for (key in keys) {
            val pref = findPreference<EditTextPreference>(key)
            if (pref != null) {
                pref.onPreferenceChangeListener = changeListener
                changeListener.onPreferenceChange(pref, pref.text)
            } else {
                val editor = sp.edit()
                editor.remove(key)
                editor.apply()
            }
        }
    }
}
