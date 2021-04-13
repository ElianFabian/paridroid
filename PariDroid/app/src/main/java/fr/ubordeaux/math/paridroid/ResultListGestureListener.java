/*
Copyright (C) 2021 Andreas Enge <andreas.enge@inria.fr>
Copyright (C) 2021 Laurent Facq <laurent.facq@math.u-bordeaux.fr>

This file is part of PariDroid.

PariDroid is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PariDroid is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package fr.ubordeaux.math.paridroid;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ResultListGestureListener extends GestureDetector.SimpleOnGestureListener {

   private final ListView resultList;
   private final ArrayAdapter<String> outputArrayAdapter;

   public ResultListGestureListener(ListView rl) {
      resultList = rl;
      outputArrayAdapter = (ArrayAdapter<String>) resultList.getAdapter();
   }

   private int getIndex (MotionEvent event) {
      /* Return the index in the result list where the event occurred. */
      int x = (int) event.getX();
      int y = (int) event.getY();
      return resultList.pointToPosition(x, y);
   }
   @Override
   public boolean onFling(MotionEvent event1, MotionEvent event2,
                          float velocityX, float velocityY) {
      float diffX = event2.getX() - event1.getX();
      if (diffX > resultList.getWidth() / 3.0
              && Math.abs(velocityX / velocityY) > 4) {
         /* Long right fling, not too wobbly. */
         int pos1 = getIndex(event1);
         int pos2 = getIndex(event2);
         if (pos1 == pos2 && pos1 >= 0) {
            outputArrayAdapter.remove(outputArrayAdapter.getItem(pos1));
            outputArrayAdapter.notifyDataSetChanged();
         }
      }
      return true;
   }
}
