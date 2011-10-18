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

public class PariDroidActivity extends Activity implements OnClickListener {
    
    public static final String MSG_TAG = "PariDroid";

    private EditText inputText;

    private EditText outputText;
   
    private Button enterButton;

    private Button clearButton;

    private ListView historyList;

    private OutputStringArrayAdapter outputArrayAdapter;

    private ArrayList<String> outputArrayList = new ArrayList<String>();
    
    private ArrayList<String> inputArrayList = new ArrayList<String>();

    private int historyIndex = -1;

    private String toggleHistoryEntry;
        
    static {
	try {
	    System.loadLibrary("pari-gmp");
	} catch (Exception ex) {
	    Log.e(MSG_TAG, "Error: " + ex.getMessage());
	}
    }

    static {
	try {
	    System.loadLibrary("paridroid");
	} catch (Exception ex) {
	    Log.e(MSG_TAG, "Error: " + ex.getMessage());
	}
    }
        
    public void onCreate(Bundle savedInstanceState) {
	Log.d(MSG_TAG, "Call to onCreate()");
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	paridroidInit();
	initialize();
    }

    private void initialize() {
	setTitle(getString(R.string.app_name));

	inputText = (EditText)findViewById(R.id.inputText);

	outputText = (EditText)findViewById(R.id.outputText);

	enterButton = (Button)findViewById(R.id.enterButton);
	enterButton.setOnClickListener(this);

	clearButton = (Button)findViewById(R.id.clearButton);
	clearButton.setOnClickListener(this);

	historyList = (ListView)findViewById(R.id.historyList);
	outputArrayAdapter = new OutputStringArrayAdapter(this,outputArrayList);
	historyList.setAdapter(outputArrayAdapter);

	String example = "factor(38975)";
	inputText.setText(example);

	inputText.setOnKeyListener(new OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
		    if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
			String output = evaluate(inputText.getText().toString());
			setOutputView(output);
			return true;
		    }
		    return false;
		}
	    });
    }

    private String evaluate(String cmd) {

	inputArrayList.add(cmd);
	if (cmd == null) {
	    return "NULL";
	} else {
	    String result = paridroidEval(cmd);
	    outputArrayList.add(result);
	    return result;
	}
    }
    
    private void setOutputView(String result) {
	String last = outputText.getText().toString();
	outputArrayList.add(0,last);
	outputArrayAdapter.notifyDataSetChanged();
	inputText.setText("");
	outputText.setText(result);
    }

    private void clearOutputView() {
	String last = outputText.getText().toString();
	outputArrayList.add(0,last);
	outputArrayAdapter.notifyDataSetChanged();
	inputText.setText("");
	outputText.setText("");
    }

    public void onClick(View v) {
	String in = inputText.getText().toString();
	String out;

	switch (v.getId()) {
	case R.id.enterButton:
	    out = evaluate(in);
	    setOutputView(out);
	    break;
	case R.id.clearButton:
	    clearOutputView();
	    break;
	default:
	    break;
	}
    }
    
    public void onDestroy() {
	paridroidClose();
    }

    /* JNI CALLBACK DECLARATIONS */

    /**
     * Declaration of JNI callback to paridroid_init().
     * @see libparidroid
     */
    public native void paridroidInit();


    /**
     * Declaration of JNI callback to paridroid_eval().
     * @param cmd: The GP command to run.
     * @return Output from GP interpreter.
     * @see libparidroid
     */
    public native String paridroidEval(String cmd);


    /**
     * Declaration of JNI callback to paridroid_close().
     * @see libparidroid.
     */
    public native void paridroidClose();

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
	    txt.setText(this.getItem(position));
	    return txt;
	}
    }
}