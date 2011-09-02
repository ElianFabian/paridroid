package com.math.paridroid;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.*;
import java.util.*;
import java.lang.Process;
import java.lang.Runtime;

public class PariDroidActivity extends Activity
{

    public String PREFIX = "/data/data/com.math.paridroid/";

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
	    copyAssets();
	    if (NativeTask.install() != 0) {
		Log.e(TAG, "Install script failed to run. Try to install manually.");
	    } else {
		Log.d(TAG, "Install script ran successfully.");
	    }
	} else {
	    Log.d(TAG, "Not checking for existing Paridroid install.");
	}
    }

    private void copyAssets() {
	AssetManager assetManager = getAssets();
	String[] files = null;
	try {
	    files = assetManager.list("");
	} catch (IOException e) {
	    Log.e("tag", e.getMessage());
	}
	for(int i=0; i<files.length; i++) {
	    InputStream in = null;
	    OutputStream out = null;
	    String fileName = files[i];
	    try {
		if(fileName.contains("gp") || fileName.contains(".sh")) {
		    in = assetManager.open(fileName);
		    out = new FileOutputStream(PREFIX + fileName);
		    copyFile(in, out);
		    in.close();
		    in = null;
		    out.flush();
		    out.close();
		    out = null;
		} else if(fileName.contains("galdata")) {
		    in = assetManager.open(fileName);
		    out = new FileOutputStream(PREFIX + fileName);
		    copyFile(in, out);
		    in.close();
		    in = null;
		    out.flush();
		    out.close();
		    out = null;
		} else if (fileName.contains("lib")) {
		    in = assetManager.open(fileName);
		    out = new FileOutputStream(PREFIX + fileName);
		    copyFile(in, out);
		    in.close();
		    in = null;
		    out.flush();
		    out.close();
		    out = null;
		}
	    } catch(Exception e) {
		Log.e(TAG, e.getMessage());
	    }       
	}
    }
   
    private void copyFile(InputStream in, OutputStream out) throws IOException {
	byte[] buffer = new byte[1024];
	int read;
	while((read = in.read(buffer)) != -1){
	    out.write(buffer, 0, read);
	}
    }
}