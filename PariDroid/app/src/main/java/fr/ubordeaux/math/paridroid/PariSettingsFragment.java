/**
 * Copyright (C) 2019, 2021 Andreas Enge <andreas.enge@inria.fr>
 *
 * This file is part of PariDroid.
 *
 * PariDroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PariDroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ubordeaux.math.paridroid;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import java.util.Set;

public class PariSettingsFragment extends PreferenceFragment {
   /* Call-back class to set the summary of a preference to its value
      when it is changed, and to change the corresponding pari default. */

   private static Preference.OnPreferenceChangeListener change = new Preference.OnPreferenceChangeListener() {

      public boolean onPreferenceChange (Preference pref, Object val) {
         String string = val.toString ();
         pref.setSummary (string);
         PariUtil.setDefault (pref.getKey (), string);
         return true;
      }
   };

   public void onCreate (Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.settings);
      Set<String> keys = PreferenceManager
              .getDefaultSharedPreferences(getActivity().getApplicationContext())
              .getAll().keySet();
      for (String key : keys) {
         EditTextPreference pref = (EditTextPreference) findPreference(key);
         pref.setOnPreferenceChangeListener(change);
         change.onPreferenceChange(pref, pref.getText());
      }
   }
}
