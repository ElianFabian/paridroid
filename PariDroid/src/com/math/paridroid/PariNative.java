package com.math.paridroid;

import android.util.Log;

class PariNative {

    public static final String MSG_TAG = "PariDroid:PariNative";

      static {
	try {
	    Log.i(MSG_TAG, "Loading libpari-gmp.so");
	    System.loadLibrary("pari-gmp");
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

    /**
     * Gets the next input from history.
     * @return the next input String in history.
     */
    public static native String getNextInput();

    /**
     * Gets the next output from history.
     * @return the next output String in history.
     */
    public static native String getNextOutput();

    /**
     * Gets the previous input from history.
     * @return the previous input String in history.
     */
    public static native String getPreviousInput();

    /**
     * Gets the next output from history.
     * @return the previous output String in history.
     */
    public static native String getPreviousOutput();

    /**
     * Writes the current input/output history to a file.
     * @param name of the file.
     */
    public static native void saveHistory(String fileName);

    /**
     * Declaration of JNI callback to paridroid_close().
     * @see libparidroid.
     */
    public static native void paridroidClose();
}