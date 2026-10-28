package fr.ubordeaux.math.paridroid

object PariUtil {

    @JvmStatic
    fun setDefault(name: String, value: String) {
        val cmd = "default($name,$value);"
        PariNative.paridroidEval(cmd)
    }
}
