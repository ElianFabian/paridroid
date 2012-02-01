/**
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
 *
 * Written by Charles Boyd
 * UMass Amherst - Dept. of Mathematics
 * (Copyright 2011)
 */

package com.math.paridroid;

import com.math.paridroid.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.io.*;

public class PariDroidActivity extends Activity implements OnClickListener {
    
    public static final String MSG_TAG = "PariDroid";

    private EditText inputText;
   
    private Button enterButton;

    private Button clearButton;

    private Button nextButton;

    private Button prevButton;

    private ListView historyList;

    private OutputStringArrayAdapter outputArrayAdapter;

    private ArrayList<String> outputArrayList = new ArrayList<String>();
    
    private ArrayList<String> inputArrayList = new ArrayList<String>();
    
    private int histSize;

    private int cursor = 0;
    
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(MSG_TAG, "Call to onCreate()");
	setContentView(R.layout.main);
	PariNative.paridroidInit();
	initialize();
    }

    private void initialize() {
	setTitle(getString(R.string.app_name));

	inputText = (EditText)findViewById(R.id.inputText);

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

	String example = "factor(38975)";
	inputText.setText(example);

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

        String result;
        
        if (cmd.length () >= 1 && cmd.charAt (0) == '?')
           /* treat metacommand "?" or "??" */
           if (cmd.length () >= 2 && cmd.charAt (1) == '?')
              result = PariNative.paridroidEval ("help(" + cmd.substring (2) + ")");
           else
              result = PariNative.paridroidEval ("help(" + cmd.substring (1) + ")");
        else
           result = PariNative.paridroidEval(cmd);

        /* remove extraneous line breaks to save screen space */
        result = result.replaceAll ("\n\n", "\n");
        result = result.replaceAll ("\n$", "");

	histSize = PariNative.getHistSize();

	if (inputArrayList.size() < histSize) {
	    inputArrayList.add(cmd);
	    Log.d(MSG_TAG,"Added command to input history");
	    cursor=0;
            outputArrayList.add(0, "? " + cmd + "\n" + "%" + histSize + " = " + result);
	}
	else
            outputArrayList.add(0, "? " + cmd + "\n" + result);

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
	    cursor++;
	    Log.d(MSG_TAG,"Cursor = " + cursor);
	    inputText.setText(getFromHistory(histSize - cursor));
	    break;
	case R.id.nextButton:
	    clear();
	    cursor--;
	    Log.d(MSG_TAG,"Cursor = " + cursor);
	    inputText.setText(getFromHistory(histSize - cursor));
	    break;
	default:
	    break;
	}
    }
    
    public String getFromHistory(int i) {
	int n = inputArrayList.size();
	String histCommand = "";
		
	try {
	    histCommand = inputArrayList.get(i);
	    Log.d(MSG_TAG,"Retreived " + i + "th element from input history.");
	} catch (Exception ex) {
	    Log.e(MSG_TAG,"Exception trying to retreive " + i + "th element from input history");
	    cursor = 0;
	}
	return histCommand;
    }
		
    public void onDestroy() {
	super.onDestroy();
    }

    /* Nested Classes */
    /**
     * Custom String ArrayAdapter class that allows us to manipulate the row
     * colors etc.
     */
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