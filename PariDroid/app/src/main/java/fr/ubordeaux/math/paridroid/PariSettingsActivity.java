/**
 * Copyright (C) 2021 Andreas Enge <andreas.enge@inria.fr>
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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class PariSettingsActivity extends Activity {
   public void onCreate (Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      /* Start preference fragment. */
      FragmentManager fm = getFragmentManager();
      FragmentTransaction ft = fm.beginTransaction();
      ft.replace(android.R.id.content, new PariSettingsFragment());
      ft.commit();
   }
}
