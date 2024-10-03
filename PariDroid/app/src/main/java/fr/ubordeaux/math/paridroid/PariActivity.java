/*
Copyright (C) 2012, 2013 Charles Boyd
Copyright (C) 2014, 2015, 2018, 2019, 2020, 2021 Andreas Enge <andreas.enge@inria.fr>
Copyright (C) 2016, 2017, 2018, 2019, 2020 Laurent Facq <laurent.facq@math.u-bordeaux.fr>

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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class PariActivity extends Activity {
    
   public static final String MSG_TAG = "PariDroid";

   private MultiAutoCompleteTextView inputText;

   private ListView resultList;
   private ArrayAdapter<String> outputArrayAdapter;
   private final ArrayList<String> outputArrayList = new ArrayList<>(255);
   private final ArrayList<String> inputArrayList = new ArrayList<>(255);
   private int cursor = 0;

   @SuppressLint("ClickableViewAccessibility")
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);

      /* Set up all user interface elements. */
      String version;
      try {
         PackageInfo pi = getPackageManager ()
                 .getPackageInfo (getPackageName (), 0);
         version = pi.versionName;
      }
      catch (NameNotFoundException e) {
         version = "";
      }
      setTitle(getString(R.string.app_name) + " " + version);

      /* Recover parisize from the preferences and initialise PARI, which
         needs to be done early. */
      SharedPreferences prefs = PreferenceManager
              .getDefaultSharedPreferences(this);
      long parisize = Long.parseLong(prefs.getString ("parisize",
              getString (R.string.parisize_default)));
      PariNative.paridroidInit(parisize);
      /* Set all preferences. */
      Map <String, ?> map = prefs.getAll ();
      Set <String> keys = map.keySet ();
      for (String key : keys)
         if (!key.equals ("parisize"))
            PariUtil.setDefault (key, map.get (key).toString ());

      /* Set up the buttons. */
      final ImageButton enterButton = (ImageButton) findViewById(R.id.enterButton);
      enterButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            String in = inputText.getText().toString();
            evaluate(in);
            /* Remove hint from input field. */
            inputText.setHint(null);
            clear();
         }
      });

      final ImageButton clearButton = (ImageButton) findViewById(R.id.clearButton);
      clearButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            clear();
         }
      });

      final ImageButton nextButton = (ImageButton) findViewById(R.id.nextButton);
      nextButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            cursor++;
            if (cursor >= inputArrayList.size())
               cursor = 0; /* cycle through */
            inputText.setText(getFromHistory(cursor));
            inputText.setSelection (inputText.getText ().length ());
         }
      });

      final ImageButton prevButton = (ImageButton) findViewById(R.id.prevButton);
      prevButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            cursor--;
            if (cursor < 0)
               cursor = inputArrayList.size() - 1; /* cycle through */
            inputText.setText(getFromHistory(cursor));
            inputText.setSelection (inputText.getText ().length ());
         }
      });

      /* Set up the input field. */
      inputText = (MultiAutoCompleteTextView) findViewById(R.id.inputText);
      inputText.setOnKeyListener(new OnKeyListener() {
         public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(   event.getAction() == KeyEvent.ACTION_DOWN
               && keyCode == KeyEvent.KEYCODE_ENTER) {
               enterButton.performClick();
               return true;
            }
            else
               return false;
         }
      });
      inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
         @Override
         public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if(i == EditorInfo.IME_ACTION_DONE) {
               enterButton.performClick();
               return true;
            }
            return false;
         }
      });

      /* Add function suggestions to the input field. */
      String[] functions = PariNative.getCommands();
      Arrays.sort(functions);
      ArrayAdapter<String> adapter
              = new ArrayAdapter<>(this, R.layout.suggestion_list_item,
              functions);
      inputText.setAdapter(adapter);

      /* Set up the suggestions through a tokeniser; a token is an
         uninterrupted sequence of letters or digits, or any other single
         character. */
      MultiAutoCompleteTextView.Tokenizer tokenizer = new MultiAutoCompleteTextView.Tokenizer(){
         public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;
            while (i > 0 && Character.isLetterOrDigit(text.charAt(i - 1)))
               i--;
            return i;
         }
         public int findTokenEnd(CharSequence text, int cursor) {
            int len = text.length();
            int i = cursor;
            while (i < len - 1 && Character.isLetterOrDigit(text.charAt(i + 1)))
               i++;
            return i;
         }
         public CharSequence terminateToken(CharSequence text) {
            return text + "(" ;
         }
      };
      inputText.setTokenizer(tokenizer);

      /* Enable suggestions only after four letters are typed; this takes
         into account that many GP functions share a common prefix of three
         letters. */
      inputText.setThreshold(4);

      /* Set up the list of results. */
      resultList = (ListView) findViewById(R.id.resultList);
      outputArrayAdapter = new ArrayAdapter<>(this,
              R.layout.result_list_item,outputArrayList);
      resultList.setAdapter(outputArrayAdapter);

      /* Add a gesture detector. */
      final GestureDetector gd
              = new GestureDetector(this, new ResultListGestureListener(resultList));
      resultList.setOnTouchListener(new View.OnTouchListener() {
         @Override
         public boolean onTouch(View v, MotionEvent event) {
            return gd.onTouchEvent(event);
         }
      });
   }

   private void evaluate(String cmd) {
      /* Check for empty command. */
      if (cmd.replaceAll ("^[:whitespace:]*$", "").length () != 0) {
         inputArrayList.add(cmd);
	      cursor = inputArrayList.size();
	      /* This actually points *beyond* the array, which needs to be
	         taken into account for the next and prev actions. */
         if (cmd.startsWith ("??")) {
            Intent show_url = new Intent(Intent.ACTION_VIEW);
            show_url.setData(
               Uri.parse(getString(R.string.doc_url)
                         + cmd.replaceAll("^\\?*", "")
                              .replaceAll("^ *", "")));
            startActivity(show_url);
         }
         else {
            String result = PariNative.paridroidEval(cmd);
            /* Remove extraneous line breaks to save screen space. */
            String output = result.replaceAll("\n\n", "\n")
                    .replaceAll("\n$", "");

            if (output.length() != 0)
               outputArrayList.add(0, "? " + cmd + "\n" + output);
            else
               outputArrayList.add(0, "? " + cmd);
            resultList.clearChoices();
            outputArrayAdapter.notifyDataSetChanged();
         }
      }
   }

   private void clear() {
      inputText.setText("");
   }


   public String getFromHistory(int i) {
      int n = inputArrayList.size();
      if (n == 0)
         return "";
      else
         return inputArrayList.get (i);
   }

   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu, menu);
      return true;
   }

   static final int OPEN_FILE_REQUEST = 10;

   protected void onActivityResult(int requestCode, int resultCode,
                                   Intent data) {
      if (requestCode == OPEN_FILE_REQUEST) {
         if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.d(MSG_TAG, "open_file " + uri.toString());
            try {
	            int file = getContentResolver().openFileDescriptor(uri, "r").getFd();
	            String output = PariNative.read (file);
	            if (output.length () != 0) {
	               resultList.clearChoices();
	               outputArrayList.add(0, output);
                  outputArrayAdapter.notifyDataSetChanged();
               }
            }
            catch (FileNotFoundException e) {
               e.printStackTrace();
            }
         }
      }
   }

   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.settings:
         Intent settings = new Intent (this, PariSettingsActivity.class);
         startActivity (settings);
         return true;
      case R.id.docs:
         Intent show_url = new Intent(Intent.ACTION_VIEW);
         show_url.setData(Uri.parse(getString(R.string.doc_url)));
         startActivity(show_url);
         return true;
      case R.id.app_license:
         String msg = "This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.\n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n\nSee the GNU General Public License for more details.\nhttp://www.gnu.org/licenses/gpl.txt";
         AlertDialog.Builder alert = new AlertDialog.Builder (this);
         alert.setMessage (msg);
         alert.setPositiveButton ("OK", null);
         alert.show ();
         return true;
      case R.id.read:
         Intent openFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
         openFile.setType("*/*");
         openFile.addCategory(Intent.CATEGORY_OPENABLE);
         startActivityForResult(openFile,OPEN_FILE_REQUEST);
         return true;
      case R.id.shareAll:
         Intent shareAll = new Intent(Intent.ACTION_SEND);
         shareAll.setType("text/plain");
         shareAll.putExtra(Intent.EXTRA_TEXT,
                 TextUtils.join("\n", outputArrayList));
                 /* String.join was added in API level 26 */
         startActivity(shareAll);
         return true;
      case R.id.shareSelected:
         /* Extract all selected items. */
         String content = new String();
         SparseBooleanArray sba = resultList.getCheckedItemPositions();
         for (int i=0; i < resultList.getCount(); i++)
            if (sba.get(i))
               content += outputArrayList.get(i) + "\n";
         if (content.isEmpty())
            Toast.makeText(this, "Nothing selected; tap to select results",
                    Toast.LENGTH_LONG).show();
         else {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, content);
            startActivity(share);
         }
         return true;
      default:
         return super.onOptionsItemSelected(item);
      }
   }
}
