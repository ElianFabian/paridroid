package com.math.paridroid;

import android.util.Log;

class NativeTask {

    public static final String MSG_TAG = "Paridroid:NativeActivity";

    static {
	try {
	    System.loadLibrary("paridroid-helper");
	} catch (Exception ex) {
	    Log.e(MSG_TAG, "Failed to load native library.");
	}
    }

    public static native int install();
}