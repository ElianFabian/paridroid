package fr.ubordeaux.math.paridroid

import android.os.ParcelFileDescriptor
import android.util.Log
import java.util.Locale.ROOT

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
			error("Engine already initialized")
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
		return PariNative.getCommands().orEmpty().toList()
	}

	/**
	 * Reads from a ParcelFileDescriptor safely without any reflection.
	 * Note: detachFd() transfers ownership of the file descriptor to native code.
	 */
	public fun read(pfd: ParcelFileDescriptor): String {
		val intFd = pfd.detachFd()

		return PariNative.read(intFd) ?: error("Read returned null or empty output from file descriptor")
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
