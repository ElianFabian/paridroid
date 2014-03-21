/**
 * Copyright (C) 2012, 2013 Charles Boyd
 * Copyright (C) 2014 Andreas Enge <andreas.enge@inria.fr>
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

package org.freemonad.paridroid;

import static org.freemonad.paridroid.ParidroidUtils.makeToast;
import static org.freemonad.paridroid.ParidroidUtils.showOkAlertDialog;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.io.*;

public class PariActivity extends Activity implements View.OnClickListener {
    
    public static final String MSG_TAG = "PariDroid";

    private AutoCompleteTextView inputText;
   
    private Button enterButton;

    private Button clearButton;

    private Button nextButton;

    private Button prevButton;

    private ListView historyList;

    private OutputStringArrayAdapter outputArrayAdapter;

    private ArrayList<String> outputArrayList = new ArrayList<String>(255);
    
    private ArrayList<String> inputArrayList = new ArrayList<String>(255);
    
    private int cursor = 0;
    
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	PariNative.paridroidInit();
	initialize();
    }

    private void initialize() {
	setTitle(getString(R.string.app_name));

	inputText = (AutoCompleteTextView)findViewById(R.id.inputText);
	inputText.setDropDownHeight (ViewGroup.LayoutParams.MATCH_PARENT);

	enterButton = (Button)findViewById(R.id.enterButton);
	enterButton.setOnClickListener(this);

	clearButton = (Button)findViewById(R.id.clearButton);
	clearButton.setOnClickListener(this);

	nextButton = (Button)findViewById(R.id.nextButton);
	nextButton.setOnClickListener(this);

	prevButton = (Button)findViewById(R.id.prevButton);
	prevButton.setOnClickListener(this);
	
	historyList = (ListView)findViewById(R.id.historyList);
	outputArrayAdapter = new OutputStringArrayAdapter(this,outputArrayList);
	historyList.setAdapter(outputArrayAdapter);

	setupAutoCompleteView();

	inputText.setOnKeyListener(new OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
		    if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
			StringBuilder sb = new StringBuilder(inputText.getText());
			String input = sb.toString();
			evaluate(input); clear();
			return true;
		    }
		    return false;
		}
	    });
    }

    private void evaluate(String cmd) {
        String executeCmd = cmd;
        String [] result;
        String output;
        boolean isHelp = false;
        
        if (cmd.length() >= 1 && cmd.charAt(0) == '?') {
           /* treat metacommand "?" or an arbitrary number of leading "?" */
           executeCmd = "help(" + cmd.replaceAll ("^\\?*", "") + ")";
           isHelp = true;
        }

        result = PariNative.paridroidEval(executeCmd);

        /* remove extraneous line breaks to save screen space */
        output = result [0].replaceAll("\n\n", "\n").replaceAll("\n$", "");

        inputArrayList.add(cmd);
        cursor = inputArrayList.size ();
        if (result [1].equals ("1"))
            outputArrayList.add(0, "? " + cmd + "\n" + output);
	else
            outputArrayList.add(0, "? " + cmd + "\n"
               + "%" + inputArrayList.size () + " = " + output);
	outputArrayAdapter.notifyDataSetChanged();
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
	    if (cursor == inputArrayList.size())
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

    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.app_settings:
	    return true;
	case R.id.setMaxprime:
	    clear();
	    inputText.setText("default(primelimit, ? )");
	    return true;
	case R.id.setStacksize:
	    clear();
	    inputText.setText("default(parisize, ? )");
	    return true;
	case R.id.setRealPrecision:
	    clear();
	    inputText.setText("default(realprecision, ? )");
	    return true;
	case R.id.setSeriesPrecision:
	    clear();
	    inputText.setText("default(seriesprecision, ? )");
	    return true;
	case R.id.docs:
	    makeToast(this,"For documentation, please visit:\n"+
				"http://www.paridroid.libremath.org");
	    return true;
	case R.id.app_license:
	    showOkAlertDialog(this,"This program is free software: you can redistribute it and/or modify\n"+
				"it under the terms of the GNU General Public License as published by\n"+
				"the Free Software Foundation, either version 3 of the License, or\n"+
				"(at your option) any later version.\n\n"+
				"This program is distributed in the hope that it will be useful,\n"+
				"but WITHOUT ANY WARRANTY; without even the implied warranty of\n"+
				"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n\n"+
				"See the GNU General Public License for more details.\n"+
				"http://www.gnu.org/licenses/gpl.txt");
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    public void setupAutoCompleteView() {

	Resources res = getResources();

        String [] s = PariNative.getCommands ();
        ArrayList<String> functions = new ArrayList<String>(Arrays.asList (s));
	Collections.sort (functions);
	ArrayAdapter<String> adapter =
	   new ArrayAdapter<String>(this,R.layout.list_item,
	                            functions);
	inputText.setAdapter(adapter);
    }

    public void onDestroy() {
	super.onDestroy();
    }

    /* Nested Classes */
    protected class OutputStringArrayAdapter extends ArrayAdapter<String> {
	
	OutputStringArrayAdapter(Context context, ArrayList<String> stringArrayList) {
	    super(context, android.R.layout.simple_list_item_1, stringArrayList);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
	    TextView txt = new TextView(this.getContext());
	    txt.setTypeface (Typeface.MONOSPACE);
	    txt.setText(this.getItem(position));
	    return txt;
	}
    }
}
