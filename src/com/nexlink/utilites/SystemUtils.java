package com.nexlink.utilites;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import com.nexlink.utilites.Shell.ShellException;

public class SystemUtils {
	/*
	 * 0 = No, 1 = SystemUtils App, 2 = Updated SystemUtils App
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
	
	public static String grantPermissions(String packageName, String[] permissions){
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
	
	public static void shutdown(boolean reboot){
		if(Shell.su()){
			try {
				Shell.sudo(reboot ? "reboot" : "reboot -p");
			} catch (ShellException e) {}
		}
	}
	
	public static void restartVM(){
		if(Shell.su()){
			try {
				Shell.sudo("restart");
			} catch (ShellException e) {}
		}
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
