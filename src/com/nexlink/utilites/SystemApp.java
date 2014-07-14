package com.nexlink.utilites;

import java.io.File;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;

import com.nexlink.utilites.Shell.ShellException;

public class SystemApp {
	/*
	 * 0 = No, 1 = System App, 2 = Updated System App
	 */
	public static int isSystemApp(Context context, String packageName){
		int type = 0;
		ApplicationInfo ai = null;
		try {
			ai = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if(ai != null && (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0){
			type = 1;
			if((ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){
				type = 2;
			}
		}
		return type;
	}
	
	public static String grantTempPermissions(String packageName, String[] permissions){
		String out = "";
		String cmd = "pm grant " + packageName;
		for(String permission : permissions){
		cmd += " " + permission;
		}
		try {
			out = Shell.sudo(cmd);
		} catch (ShellException e) { out = e.getMessage();}
		return out;
	}
	
	public static boolean installAsSystemApp(String apkPath, String apkName, boolean restart){
		boolean success = false;
		String sysapkpath = (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ? "/system/app/" : "/system/priv-app/") + apkName/*apkpath.substring(apkpath.lastIndexOf("/")+1, apkpath.length())*/;
		try {
			Shell.sudo("mount -o rw,remount /system;");
			//Remove the old version
			if(new File("/system/app/NexlinkStatusBar.apk").exists()){
				Shell.sudo("rm /system/app/" + apkName);
			}
			if(new File("/system/priv-app/" + apkName).exists()){
				Shell.sudo("rm /system/priv-app/" + apkName);
			}
			
			Shell.sudo(
					"cp " + apkPath + " " + sysapkpath 
					+ ";rm " + apkPath 
					+ ";chmod 644 " + sysapkpath 
					+ ";chown 0.0 " + sysapkpath
					+ ";mount -o ro,remount /system"
					+ ";sync"
					+ (restart ? ";stop;start" : "")
					);
			success = new File(sysapkpath).exists() && !new File(apkPath).exists();
		} catch (ShellException e) {System.out.println(e.getMessage());}
		return success;
	}
	
	public static void sendProtectedBroadcast(Intent i){
		HashMap<String, String> myMap = new HashMap<String, String>();
		//There are flags than this, but this should do for now...
		myMap.put("String", "--es");
        myMap.put("Boolean", "--ez");
        myMap.put("Integer", "--ei");
        myMap.put("Long", "--el");
        myMap.put("Float", "--ef");
        myMap.put("URI", "--eu");
        
		String action = i.getAction();
		Bundle bundle = i.getExtras();
		String args = "";
		for(String key : bundle.keySet()){
			Object value = bundle.get(key);
			args += " " + myMap.get(value.getClass().getName()) + " " + key + " " + value.toString();
		}
		try {
			Shell.sudo("am broadcast -a "+ action + args);
		} catch (ShellException e) {
			e.printStackTrace();
		}
	}
}
