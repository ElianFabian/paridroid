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
    public String BIN_DIR = "/data/data/com.math.paridroid/bin/";
    public String LIB_DIR = "/data/data/com.math.paridroid/lib/";
    public String DATA_DIR = "/data/data/com.math.paridroid/pari/";
    public String SHARE_DIR = "/data/data/com.math.paridroid/share/";
    public String EXTRAS_DIR = "/data/data/com.math.paridroid/share/pari/tarballs/";

    public String GALPOL_DIR = "/data/data/com.math.paridroid/share/pari/galpol/";
    public String ELLDATA_DIR = "/data/data/com.math.paridroid/share/pari/elldata/";
    public String GALDATA_DIR = "/data/data/com.math.paridroid/share/pari/galdata/";
    public String SEADATA_DIR = "/data/data/com.math.paridroid/share/pari/seadata/";
    public String NFTABLES_DIR = "/data/data/com.math.paridroid/share/pari/nftables/";


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
	    install();
	} else {
	    Log.d(TAG, "Not checking for existing Paridroid install.");
	}
    }

    public void runCommand(ArrayList<String> cmd)
    {
	Log.d(TAG, "Attempting to run command: " + cmd);
	try {
	    Process process = new ProcessBuilder()
		.command(cmd)
		.redirectErrorStream(true)
		.start();
	    try {
		InputStream in = process.getInputStream();
		OutputStream out = process.getOutputStream();
		
		BufferedReader bfrd = new BufferedReader(new InputStreamReader(in));
		String prnt = bfrd.readLine();
		while(prnt != null) {
		    Log.d(TAG, "line of output: " + prnt);
		    prnt = bfrd.readLine();
		}
		
	    } catch (Exception ex) {
		Log.e(TAG, "Exception, failed to run command " + cmd);
	    } finally {
		process.destroy();
	    }
	} catch (IOException io) {
	    Log.e(TAG, "Caught IOException.");
	}
    }

    public void install()
    {
	ArrayList<String> mkBinDir = new ArrayList<String>();
	mkBinDir.add("mkdir");
	mkBinDir.add(BIN_DIR);
	runCommand(mkBinDir);
	
       	ArrayList<String> mkShareDir = new ArrayList<String>();
	mkShareDir.add("mkdir");
	mkShareDir.add(SHARE_DIR);
	runCommand(mkShareDir);

       	ArrayList<String> mkExtrasDir = new ArrayList<String>();
	mkExtrasDir.add("mkdir");
	mkExtrasDir.add(EXTRAS_DIR);
	runCommand(mkExtrasDir);

	copyAssets();

	ArrayList<String> mvGpBin = new ArrayList<String>();
	mvGpBin.add("mv");
	mvGpBin.add(BIN_DIR+"gp.jpg");
	mvGpBin.add(BIN_DIR+"gp");
	runCommand(mvGpBin);

	ArrayList<String> mkBinExec = new ArrayList<String>();
	mkBinExec.add("chmod");
	mkBinExec.add("774");
	mkBinExec.add(BIN_DIR+"gp");
	runCommand(mkBinExec);

	ArrayList<String> mvGalData = new ArrayList<String>();
	mvGalData.add("mv");
	mvGalData.add(SHARE_DIR+"galdata.tar.jpg");
	mvGalData.add(SHARE_DIR+"galdata.tar");
	runCommand(mvGalData);

	NativeTask.install();

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
		if(fileName.equals("gp.jpg")) {
		    in = assetManager.open(fileName);
		    out = new FileOutputStream(BIN_DIR + fileName);
		    copyFile(in, out);
		    in.close();
		    in = null;
		    out.flush();
		    out.close();
		    out = null;
		} else if(fileName.contains("galdata")) {
		    in = assetManager.open(fileName);
		    out = new FileOutputStream(SHARE_DIR + fileName);
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
