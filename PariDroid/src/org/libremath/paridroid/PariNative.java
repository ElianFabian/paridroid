/**
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
 *
 * Written by Charles Boyd
 * UMass Amherst - Dept. of Mathematics
 * (Copyright 2012)
 */

package org.libremath.paridroid;

import android.util.Log;

class PariNative {

    public static final String MSG_TAG = "PariDroid::PariNative";

      static {
	try {
	    Log.i(MSG_TAG, "Loading libpari.so");
	    System.loadLibrary("pari");
	} catch (Exception ex) {
	    Log.e(MSG_TAG, "Error: " + ex.getMessage());
	}
    }

    static {
	try {
	    Log.i(MSG_TAG,"Loading libparidroid.so");
	    System.loadLibrary("paridroid");
	} catch (Exception ex) {
	    Log.e(MSG_TAG, "Error: " + ex.getMessage());
	}
    }

    /* JNI CALLBACK DECLARATIONS */

    /**
     * Declaration of JNI callback to paridroid_init().
     */
    public static native void paridroidInit();

    /**
     * Declaration of JNI callback to paridroid_eval().
     * @param cmd: The GP command to run.
     * @return Output from GP interpreter.
     */
    public static native String paridroidEval(String cmd);

    public static native int getHistSize();
}