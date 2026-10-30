package fr.ubordeaux.math.paridroid

import android.util.Log

/**
 * Idiomatic Kotlin wrapper for the PariDroid native engine.
 * Handles library loading, lifecycle, and modern Kotlin types (List, Result, Closeable).
 */
public object ParidroidGP {

	private var _initialized = false

	init {
		loadLibrarySafe("gmp")
		loadLibrarySafe("pari")
		loadLibrarySafe("paridroid")
	}

	/**
	 * Initializes the global PARI/GP mathematical engine stack.
	 *
	 * This function is **one-shot only** per application lifecycle. Because the underlying
	 * C library (`libpari`) allocates a single global memory arena, calling this function
	 * more than once will throw an exception or cause unrecoverable native crashes.
	 */
	public fun init(pariSize: Long = 4_000_000L) {
		if (_initialized) {
			return
		}

		PariNative.paridroidInit(pariSize)

		_initialized = true
	}

	/**
	 * Evaluates a GP command and returns the result.
	 */
	public fun eval(cmd: String): String {
		if (!_initialized) {
			error("Engine not initialized")
		}

		return PariNative.paridroidEval(cmd) ?: error("Evaluation returned null for command: $cmd")
	}

	/**
	 * Returns the list of supported GP commands as an idiomatic Kotlin List.
	 */
	public fun getSupportedCommands(): List<String> {
		if (!_initialized) {
			error("Engine not initialized")
		}

		return PariNative.getCommands().orEmpty().toList()
	}

	/**
	 * Closes the global PARI/GP mathematical engine stack.
	 */
	public fun close() {
		PariNative.paridroidClose()
		_initialized = false
	}

	public fun getVersionCode(): Int = PariNative.pariVersionCode()

	public fun getVersionName(): String {
		val code = getVersionCode()
		val major = (code ushr 16) and 0xFF
		val minor = (code ushr 8) and 0xFF
		val patch = code and 0xFF

		return "${major}.${minor}.${patch}"
	}

	private fun loadLibrarySafe(name: String) {
		try {
			Log.i(TAG, "Loading lib$name.so")
			System.loadLibrary(name)
		}
		catch (ex: Throwable) {
			Log.e(TAG, "Error loading lib$name: ${ex.message}")
		}
	}

	private const val TAG = "ParidroidGP"
}
