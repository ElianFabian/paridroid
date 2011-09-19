package com.math.paridroid;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.util.*;

public class PariDroidActivity extends Activity
{

    public String PREFIX = "/data/data/com.math.paridroid/";

    public String BIN;

    private final static String TAG = "PariDroidActivity";

    private boolean checkInstall = true;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
	if(checkInstall == true) {
	    Log.d(TAG, "Preparing to install PariDroid...");
	    if(mkBinDir() == false) {
		Log.e(TAG,"Error: Failed to create /bin directory under " + PREFIX);
		displayToastMessage("Error: See logcat for details.");
	    } else {
		copyAssets();
		displayToastMessage("Installing gp binary to " + BIN);
		File gp = new File(BIN,"gp");
		if (gp.exists()) {
		    try {
			if (gp.setExecutable(true)) {
			    Log.d(TAG,"Set gp binary to executable");
			} else {
			    Log.e(TAG, "Error: Unable to set gp binary to executable.");
			    displayToastMessage("Error: See logcat for details.");
			}
		    } catch (Exception ex) {
			Log.e(TAG,ex.getMessage());
		    } 
		} else {
		    Log.e(TAG,"Unable to locate gp binary.");
		    displayToastMessage("Error: See logcat for details.");
		}
	    }
	} else {
	    Log.d(TAG, "Not checking for existing PariDroid install.");
	}
    }
	
    private void copyAssets() {
	AssetManager assetManager = getAssets();
	String[] files = null;
	try {
	    files = assetManager.list("");
	} catch (IOException e) {
	    Log.e(TAG, e.getMessage());
	}
	for(int i=0; i<files.length; i++) {
	    InputStream in = null;
	    OutputStream out = null;
	    String fileName = files[i];
	    if (fileName.contains("gp")) {
		try {
		    String mFileName = "gp";
		    in = assetManager.open(fileName);
		    out = new FileOutputStream(BIN + mFileName);
		    copyFile(in, out);
		    in.close();
		    in = null;
		    out.flush();
		    out.close();
		    out = null;
		} catch(Exception e) {
		    Log.e(TAG, e.getMessage());
		}       
	    }
	}
    }

    private boolean mkBinDir() {
	boolean ret = false;
	File binDir = new File(PREFIX, "bin");
	try {
	    if (binDir.exists()) {
		ret = true;
	    } else {
		ret = binDir.mkdir();
		BIN = binDir.getAbsolutePath();
	    }
	} catch (Exception ex) {
	    Log.e(TAG,ex.getMessage());
	}
	return ret;
    }
    
    private void copyFile(InputStream in, OutputStream out) throws IOException {
	byte[] buffer = new byte[1024];
	int read;
	while((read = in.read(buffer)) != -1){
	    out.write(buffer, 0, read);
	}
    }

    public void displayToastMessage(String message) {
	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
}