package com.jit.sharelocation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

public class UIHelper {

	private static ProgressDialog mDialog;
	private static Toast mToast;

	public static void hideTitle(Activity activity) {
		activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public static void showProgressDialog(Context context, String title, String msg) {
		if (mDialog != null && mDialog.isShowing() == true) {
			return;
		}
		mDialog = ProgressDialog.show(context, title, msg, true, false);
	}

	public static void showProgressDialog(Context context, int titleId, int msgId) {
		showProgressDialog(context, context.getString(titleId),
				context.getString(msgId));
	}

	public static void dismissProgressDialog() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}
	
	public static boolean isDialogShowing() {
		if (mDialog != null) {
			return mDialog.isShowing();
		}
		return false;
	}

	public static void showToastShort(Context context, String content) {
		showToast(context, content, 0);
	}

	public static void showToastShort(Context context, int resId) {
		showToastShort(context, context.getString(resId));
	}

	public static void showToastLong(Context context, String content) {
		showToast(context, content, 1);
	}

	public static void showToastLong(Context context, int resId) {
		showToastLong(context, context.getString(resId));
	}

	public static void showToast(Context context, String content, int duration) {
		if (mToast == null) {
			mToast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
		} else {
			mToast.setText(content);
		}
		mToast.show();
	}

	public static void toast(Context context, String content) {
		showToastShort(context, content);
	}

	public static void toast(Context context, int contentid) {
		toast(context, context.getString(contentid));
	}

	public static void showToast(Context context, int resId, int duration) {
		showToast(context, context.getString(resId), duration);
	}
	
	public static void jumpActivity(Context context, Class<?> cls){
		jumpActivityWithBundle(context, cls, null);
	}
	
	public static void jumpActivityWithBundle(Context context, Class<?> cls, Bundle bundle){
		Intent intent = new Intent(context, cls);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		context.startActivity(intent);
	}
	
	public static void jumpActivityForResult(Context context, Class<?> cls, int requestCode){
		jumpActivityWithBundleForResult(context, cls, null, requestCode);
	}
	
	public static void jumpActivityWithBundleForResult(Context context, Class<?> cls, Bundle bundle, int requestCode){
		Intent intent = new Intent(context, cls);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		((Activity)context).startActivityForResult(intent, requestCode);
	}
	
	

}
