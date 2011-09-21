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
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

public class PariDroidActivity extends Activity {
    
    /** Tag for our logcat messages. */
    public static final String MSG_TAG = "PariDroid";

    /** Box where we can input GP commands. */
    private EditText inputEdit;
    
    /** Button we hit to send a string to GP. */
    private Button enterButton;
    
    /** View showing the output of our command. */
    private TextView outputView;

    /** Handler for processing IPC messages. */
    Handler handler = new Handler();
        
    /**
     * Initializes the Activity.
     * @param savedInstanceState: saved state we can load.
     * @see android.app.Activity
     * @overide
     */
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	
	// Set up the input/output mechanisms.
	inputEdit = (EditText)findViewById(R.id.inputEdit);
	enterButton = (Button)findViewById(R.id.enterButton);
	enterButton.setOnClickListener(onEnterButtonClick);
	outputView = (TextView)findViewById(R.id.outputView);
    }
    
    /** Set the behavior of the enter button. */
    private OnClickListener onEnterButtonClick = new OnClickListener() {
	    public void onClick(View v) {
		//String output = paridroidEval(inputEdit.getText().toString());
		setOutputView(inputEdit.getText().toString());
	    }
	};
    
    /**
     * Writes data from a String to the output view.
     * @param str: The string to write.
     */
    private void setOutputView(final String str) {
	Runnable proc = new Runnable() {
		public void run() {
		    outputView.setText(str);
		}
	    };
	handler.post(proc);
    }
    
    /**
     * Displays a Toast message to inform the user of something.
     * @param message: the message for the user.
     */
	public void displayToastMessage(String message) {
	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Declaration of JNI callback to libparidroid.
     * @param cmd: The GP command to run.
     * @return Output from GP interpreter.
     */
    public native String paridroidEval(String cmd);

}