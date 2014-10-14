package fr.ubordeaux.math.paridroid;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

public class ParidroidUtils {
   public static void makeToast(Context context, CharSequence message) {
      Toast.makeText(context, message, Toast.LENGTH_LONG).show();
   }

   public static void showOkAlertDialog(Context context, CharSequence message) {
      new AlertDialog.Builder(context)
             .setMessage(message)
             .setPositiveButton("OK", null)
             .show();
   }
}
