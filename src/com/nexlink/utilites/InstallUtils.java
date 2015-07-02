package com.nexlink.utilites;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;

import com.nexlink.utilites.Shell.ShellException;

public final class InstallUtils {
	
	private Context mContext;
	private PackageManager mPackageManager;
	
	public InstallUtils(Context c){
		mContext = c;
		mPackageManager = c.getPackageManager();
	}
	
	public PackageInfo getPackageInfoFromFile(File apkFile){
		PackageInfo packageInfo = mPackageManager.getPackageArchiveInfo(Uri.fromFile(apkFile).getPath(), 0);
		if(packageInfo != null){
			packageInfo.applicationInfo.sourceDir = apkFile.getAbsolutePath();
			packageInfo.applicationInfo.publicSourceDir = apkFile.getAbsolutePath();
		}
		return packageInfo;
	}
	
	public boolean isInstalled(String packageName, int versionCode){
		PackageInfo packageInfo = null;
		try {
			packageInfo = mPackageManager.getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {}
		return packageInfo != null && (versionCode == -1 || packageInfo.versionCode == versionCode);
	}
	public boolean isInstalled(String packageName){
		return isInstalled(packageName, -1);
	}
	public boolean isInstalled(File apkFile){
		PackageInfo packageInfo = getPackageInfoFromFile(apkFile);
		return packageInfo != null ? isInstalled(packageInfo.packageName, packageInfo.versionCode) : false;
	}
	
	public boolean installIntent(File apkFile){
		Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        return true;
	}
	
	public boolean installAPI(File apkFile){
		try{
		    mContext.getPackageManager().installPackage(Uri.fromFile(apkFile), null, PackageManager.INSTALL_REPLACE_EXISTING | PackageManager.INSTALL_ALLOW_TEST | PackageManager.INSTALL_ALLOW_DOWNGRADE, mContext.getPackageName());
		}catch(SecurityException e){return false;}
		return isInstalled(apkFile);
	}
	
	public boolean installRoot(File apkFile, boolean system, boolean restart){
		boolean success = false;
		PackageInfo packageInfo = getPackageInfoFromFile(apkFile);
        if(!system){
            try {
				Shell.sudo("pm install -r -d -t " + apkFile.getAbsolutePath());
				success = true;
			} catch (Exception e) {}
        }
        else{
            String path = (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ? "/system/app/" : "/system/priv-app/") + packageInfo.packageName + ".apk";
            try {
				Shell.sudo(
				"mount -o rw,remount /system"
				+ ";cp " + apkFile.getAbsolutePath() + " " + path
				+ ";chmod 644 " + path
				+ ";chown 0.0 " + path
				+ ";mount -o ro,remount /system"
				+ ";sync" + (restart ? ";restart" : "")
				);
			} catch (ShellException e) {}
            File copied = new File(path);
            success = copied.exists() && copied.isFile();
        }
		return success;
	}
	
	public boolean uninstallIntent(String packageName){
		Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", packageName, null));
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
		return true;
	}
	
	public boolean uninstallAPI(String packageName){
		try{
		    mContext.getPackageManager().deletePackage(packageName, null, PackageManager.DELETE_ALL_USERS);
		}catch(SecurityException e){return false;}
		return !isInstalled(packageName);
	}
	
	public boolean uninstallRoot(String packageName, boolean restart){
		boolean success = false;
		try{
		    String path = mPackageManager.getPackageInfo(packageName, 0).applicationInfo.sourceDir;
		    if(path.indexOf("/system/") != 0){
		        Shell.sudo("pm uninstall " + packageName);
		        success = !isInstalled(packageName);
		    }
		    else{
		        Shell.sudo(
		               "mount -o rw,remount /system"
		                + ";chmod 644 " + path
		                + ";chown 0.0 " + path
		                + ";rm " + path
		                + ";mount -o ro,remount /system"
		                + ";sync" + (restart ? ";restart" : "")
		                );
		        File removed = new File(path);
		        success = !removed.exists();
		        }
		}catch(Exception e){}
		return success;
	}
}
