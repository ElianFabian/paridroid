package fr.ubordeaux.math.paridroid

import android.util.Log

internal object PariNative {

    const val MSG_TAG: String = "PariDroid::PariNative"

    init {
        try {
            Log.i(MSG_TAG, "Loading libgmp.so")
            System.loadLibrary("gmp")
        } catch (ex: Exception) {
            Log.e(MSG_TAG, "Error loading libgmp: ${ex.message}")
        }

        try {
            Log.i(MSG_TAG, "Loading libpari.so")
            System.loadLibrary("pari")
        } catch (ex: Exception) {
            Log.e(MSG_TAG, "Error loading libpari: ${ex.message}")
        }

        try {
            Log.i(MSG_TAG, "Loading libparidroid.so")
            System.loadLibrary("paridroid")
        } catch (ex: Exception) {
            Log.e(MSG_TAG, "Error loading libparidroid: ${ex.message}")
        }
    }


    external fun paridroidInit(parisize: Long)

    external fun paridroidEval(cmd: String): String?

    external fun getCommands(): Array<String>?

    external fun read(file: Int): String?

    external fun paridroidClose()
}
