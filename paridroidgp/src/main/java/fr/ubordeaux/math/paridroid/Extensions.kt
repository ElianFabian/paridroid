package fr.ubordeaux.math.paridroid

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private val _pariGpDefault = PariGpDefault()

@Suppress("UnusedReceiverParameter")
public val ParidroidGP.default: PariGpDefault get() = _pariGpDefault

/**
 * Runtime-configurable PARI/GP defaults.
 * These properties dynamically read from and write to the running engine.
 * Primitive types are used for seamless and idiomatic Kotlin access.
 */
public class PariGpDefault internal constructor() {

	private fun eval(cmd: String): String = ParidroidGP.eval(cmd)

	public fun getDefault(name: String): String {
		return eval("default($name)").trim()
	}

	public fun setDefault(name: String, value: String) {
		eval("default($name,$value)")
	}

	private class BooleanDefault(private val key: String) : ReadWriteProperty<PariGpDefault, Boolean> {
		private val trueValues = listOf("1", "on", "true", "yes")
		override fun getValue(thisRef: PariGpDefault, property: KProperty<*>): Boolean {
			return thisRef.getDefault(key).lowercase() in trueValues
		}
		override fun setValue(thisRef: PariGpDefault, property: KProperty<*>, value: Boolean) {
			thisRef.setDefault(key, if (value) "1" else "0")
		}
	}

	private class LongDefault(private val key: String) : ReadWriteProperty<PariGpDefault, Long> {
		override fun getValue(thisRef: PariGpDefault, property: KProperty<*>): Long {
			return thisRef.getDefault(key).toLong()
		}
		override fun setValue(thisRef: PariGpDefault, property: KProperty<*>, value: Long) {
			thisRef.setDefault(key, value.toString())
		}
	}

	private class StringDefault(private val key: String) : ReadWriteProperty<PariGpDefault, String> {
		override fun getValue(thisRef: PariGpDefault, property: KProperty<*>): String {
			return thisRef.getDefault(key)
		}
		override fun setValue(thisRef: PariGpDefault, property: KProperty<*>, value: String) {
			thisRef.setDefault(key, "\"$value\"")
		}
	}

	public var breakLoop: Boolean by BooleanDefault("breakloop")
	public var colors: String by StringDefault("colors")
	public var compatible: Boolean by BooleanDefault("compatible")
	public var dataDir: String by StringDefault("datadir")
	public var debug: Long by LongDefault("debug")
	public var debugFiles: Long by LongDefault("debugfiles")
	public var debugMem: Long by LongDefault("debugmem")
	public var docTest: Boolean by BooleanDefault("doctest")
	public var echo: Boolean by BooleanDefault("echo")
	public var factorAddPrimes: Boolean by BooleanDefault("factor_add_primes")
	public var factorLimit: Long by LongDefault("factorlimit")
	public var factorProven: Boolean by BooleanDefault("factor_proven")
	public var format: String by StringDefault("format")
	public var graphColorMap: String by StringDefault("graphcolormap")
	public var graphColors: String by StringDefault("graphcolors")
	public var help: String by StringDefault("help")
	public var histFile: String by StringDefault("histfile")
	public var histSize: Long by LongDefault("histsize")
	public var lines: Long by LongDefault("lines")
	public var lineWrap: Long by LongDefault("linewrap")
	public var log: Boolean by BooleanDefault("log")
	public var logFile: String by StringDefault("logfile")
	public var nbThreads: Long by LongDefault("nbthreads")
	public var newGaloisFormat: Boolean by BooleanDefault("new_galois_format")
	public var output: Long by LongDefault("output")
	public var pariSize: Long by LongDefault("parisize")
	public var pariSizeMax: Long by LongDefault("parisizemax")
	public var path: String by StringDefault("path")
	public var plotHSizes: String by StringDefault("plothsizes")
	public var prettyPrinter: String by StringDefault("prettyprinter")
	public var primeLimit: Long by LongDefault("primelimit")
	public var prompt: String by StringDefault("prompt")
	public var promptCont: String by StringDefault("prompt_cont")
	public var psFile: String by StringDefault("psfile")
	public var readline: Long by LongDefault("readline")
	public var realBitPrecision: Long by LongDefault("realbitprecision")
	public var realPrecision: Long by LongDefault("realprecision")
	public var recover: Boolean by BooleanDefault("recover")
	public var secure: Boolean by BooleanDefault("secure")
	public var seriesPrecision: Long by LongDefault("seriesprecision")
	public var simplify: Boolean by BooleanDefault("simplify")
	public var soPath: String by StringDefault("sopath")
	public var strictArgs: Boolean by BooleanDefault("strictargs")
	public var strictMatch: Boolean by BooleanDefault("strictmatch")
	public var texStyle: Long by LongDefault("TeXstyle")
	public var threadSize: Long by LongDefault("threadsize")
	public var threadSizeMax: Long by LongDefault("threadsizemax")
	public var timer: Boolean by BooleanDefault("timer")
}
