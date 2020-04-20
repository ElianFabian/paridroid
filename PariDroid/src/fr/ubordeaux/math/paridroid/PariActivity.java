/**
 * Copyright (C) 2012, 2013 Charles Boyd
 * Copyright (C) 2014, 2015, 2018, 2019 Andreas Enge <andreas.enge@inria.fr>
 * Copyright (C) 2016, 2017, 2018, 2019 Laurent Facq <laurent.facq@math.u-bordeaux.fr>
 *
 * This file is part of PariDroid.
 *
 * PariDroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PariDroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ubordeaux.math.paridroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.*;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.io.*;

public class PariActivity extends Activity implements View.OnClickListener {
    
   public static final String MSG_TAG = "PariDroid";

   private MultiAutoCompleteTextView inputText;

   private Button enterButton;

   private Button clearButton;

   private Button nextButton;

   private Button prevButton;

   private ListView resultList;

   private ArrayAdapter outputArrayAdapter;

   private ArrayList<String> outputArrayList = new ArrayList<String>(255);

   private ArrayList<String> inputArrayList = new ArrayList<String>(255);

   private int cursor = 0;

   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);

      /* Recover parisize and primelimit from the preferences. */
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
      long parisize = Long.valueOf (prefs.getString ("parisize", getString (R.string.parisize_default)));
      long primelimit = Long.valueOf (prefs.getString ("primelimit", getString (R.string.primelimit_default)));
      PariNative.paridroidInit(parisize, primelimit);
      /* Set all preferences. */
      Map <String, ?> map = prefs.getAll ();
      Set <String> keys = map.keySet ();
      for (String key : keys)
         if (!key.equals ("parisize") && !key.equals ("primelimit"))
            PariUtil.setDefault (key, map.get (key).toString ());

      initialize();
   }

   private void initialize() {

      String version;
      try {
         PackageInfo pi = getPackageManager ().getPackageInfo (getPackageName (), 0);
         version = pi.versionName;
      }
      catch (NameNotFoundException e)
      {
         version = "";
      }
      setTitle(getString(R.string.app_name) + " " + version);

      inputText = (MultiAutoCompleteTextView)findViewById(R.id.inputText);
      inputText.setDropDownHeight (ViewGroup.LayoutParams.MATCH_PARENT);

      enterButton = (Button)findViewById(R.id.enterButton);
      enterButton.setOnClickListener(this);

      clearButton = (Button)findViewById(R.id.clearButton);
      clearButton.setOnClickListener(this);

      nextButton = (Button)findViewById(R.id.nextButton);
      nextButton.setOnClickListener(this);

      prevButton = (Button)findViewById(R.id.prevButton);
      prevButton.setOnClickListener(this);

      resultList = (ListView)findViewById(R.id.resultList);
      outputArrayAdapter = new ArrayAdapter(this,R.layout.result_list_item,outputArrayList);
      resultList.setAdapter(outputArrayAdapter);

      setupAutoCompleteView();

      inputText.setOnKeyListener(new OnKeyListener() {
         public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(   (event.getAction() == KeyEvent.ACTION_DOWN)
               && (keyCode == KeyEvent.KEYCODE_ENTER)) {
               StringBuilder sb = new StringBuilder(inputText.getText());
               String input = sb.toString();
               evaluate(input);
               clear();
               return true;
            }
            return false;
         }
      });
   }

   private void evaluate(String cmd) {
      String executeCmd;
      String [] result;
      String output;

      /* check for empty command */
      if (cmd.replaceAll ("^[:whitespace:]*$", "").length () != 0) {
         inputArrayList.add(cmd);
         cursor++;

         if (cmd.startsWith ("??", 0)) {
            Intent show_url = new Intent(Intent.ACTION_VIEW);
            show_url.setData(
               Uri.parse(getString(R.string.doc_url)
                         + cmd.replaceAll("^\\?*", "")
                              .replaceAll("^ *", "")));
            startActivity(show_url);
            return;
         }
         else
             executeCmd = cmd;

         result = PariNative.paridroidEval(executeCmd);

         /* remove extraneous line breaks to save screen space */
         output = result [0].replaceAll("\n\n", "\n").replaceAll("\n$", "");

         if (output.length () != 0)
            outputArrayList.add(0, "? " + cmd + "\n" + output);
         else
            outputArrayList.add(0, "? " + cmd);
         outputArrayAdapter.notifyDataSetChanged();
      }
   }

   private void clear() {
      inputText.setText("");
   }

   public void onClick(View v) {
      String in = inputText.getText().toString();

      switch (v.getId()) {
      case R.id.enterButton:
         evaluate(in);
         clear();
         break;
      case R.id.clearButton:
         clear();
         break;
      case R.id.prevButton:
         clear();
         cursor--;
         if (cursor < 0)
            cursor = inputArrayList.size() - 1; /* cycle through */
         inputText.setText(getFromHistory(cursor));
         break;
      case R.id.nextButton:
         clear();
         cursor++;
         if (cursor >= inputArrayList.size())
            cursor = 0; /* cycle through */
         inputText.setText(getFromHistory(cursor));
         break;

      default:
         break;
      }
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
      inflater.inflate(R.menu.paridroid_menu, menu);
      return true;
   }

   static final int OPEN_FILE_REQUEST = 10;

   protected void onActivityResult(int requestCode, int resultCode,
                                   Intent data) {
      if (requestCode == OPEN_FILE_REQUEST) {
         if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.d(MSG_TAG, "open_file " + uri.toString());
            Log.d(MSG_TAG, "file auth is " + uri.getAuthority());

            try {
               InputStream is = getContentResolver().openInputStream(uri);
               InputStreamReader ir = new InputStreamReader(is);
               BufferedReader bu = new BufferedReader(ir);
               String s;
               String ss = "";
               while ((s = bu.readLine()) != null) {
                  ss = ss + "\n" + s;
               }
               bu.close();
               Log.d("content", ss);
               evaluate(ss);
            } catch (FileNotFoundException e) {
               e.printStackTrace();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
   }

   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.settings:
         Intent settings = new Intent (this, PariSettings.class);
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
       case R.id.open:
         Intent openFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
         openFile.setType("*/*");
         openFile.addCategory(Intent.CATEGORY_OPENABLE);
         startActivityForResult(openFile,OPEN_FILE_REQUEST);
         return true;
      default:
         return super.onOptionsItemSelected(item);
      }
   }

   public void setupAutoCompleteView() {
      
       Resources res = getResources();
       
       String[] s = PariNative.getCommands();
       ArrayList<String> functions = new ArrayList<String>(Arrays.asList(s));
       Collections.sort(functions);
       ArrayAdapter<String> adapter
         = new ArrayAdapter<String>(this, R.layout.suggestion_list_item,
                                    functions);
       inputText.setAdapter(adapter);

       inputText.setTokenizer(new MultiAutoCompleteTextView.Tokenizer(){

          public int findTokenStart(CharSequence text, int cursor) {
             // Returns the start of the token that ends at offset cursor
             // within text.
             int i = cursor;
             while (i > 0 && Character.isLetter(text.charAt(i - 1)))
                i--;
             return i;
          }

          public int findTokenEnd(CharSequence text, int cursor) {
             // Returns the end of the token (minus trailing punctuation)
             // that begins at offset cursor within text.
             int i = cursor;
             int len = text.length();
             while (i < len)
                if (Character.isLetter(text.charAt(i)))
                   i++;
             return i;
          }

          public CharSequence terminateToken(CharSequence text) {
             // Returns text, modified, if necessary, to ensure that it
             // ends with a token terminator (for example a space or comma).
            return text + "(" ;
          }

      });
   }
    
   public void onDestroy() {
      super.onDestroy();
   }
}
