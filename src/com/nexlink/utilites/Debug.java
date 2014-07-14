package com.nexlink.utilites;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Debug {
	private static boolean debugEnabled = false;
	public static void setEnabled(boolean enabled){
		debugEnabled = enabled;
	}
	public static void toast(Context context, String text){
		if(debugEnabled){
			Toast.makeText(context, text, Toast.LENGTH_LONG).show();
		}
	}
	public static void log(String text){
		if(debugEnabled){
			Log.i("Debug", text);
		}
	}
}
