package fr.ubordeaux.math.paridroid

import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlin.math.abs

class ResultListGestureListener(private val resultList: ListView) : GestureDetector.SimpleOnGestureListener() {

    @Suppress("UNCHECKED_CAST")
    private val outputArrayAdapter: ArrayAdapter<String>
        get() = resultList.adapter as ArrayAdapter<String>

    private fun getIndex(event: MotionEvent): Int {
        val x = event.x.toInt()
        val y = event.y.toInt()
        return resultList.pointToPosition(x, y)
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1 == null) return false
        val diffX = e2.x - e1.x
        if (diffX > resultList.width / 3.0 && abs(velocityX / velocityY) > 4) {
            val pos1 = getIndex(e1)
            val pos2 = getIndex(e2)
            if (pos1 == pos2 && pos1 >= 0) {
                outputArrayAdapter.getItem(pos1)?.let { outputArrayAdapter.remove(it) }
                resultList.clearChoices()
                outputArrayAdapter.notifyDataSetChanged()
            }
        }
        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        val pos = getIndex(e)
        if (pos >= 0) {
            val checked = resultList.checkedItemPositions.get(pos)
            resultList.setItemChecked(pos, !checked)
            outputArrayAdapter.notifyDataSetChanged()
        }
        return true
    }
}
