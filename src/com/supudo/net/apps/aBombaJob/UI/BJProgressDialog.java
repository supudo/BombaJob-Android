package com.supudo.net.apps.aBombaJob.UI;

import com.supudo.net.apps.aBombaJob.R;
import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

public class BJProgressDialog extends Dialog {

	public static BJProgressDialog show(Context context, CharSequence title, CharSequence message) {
	    return show(context, title, message, false);
	}

	public static BJProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate) {
	    return show(context, title, message, indeterminate, false, null);
	}

	public static BJProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable) {
	    return show(context, title, message, indeterminate, cancelable, null);
	}

	public static BJProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable, OnCancelListener cancelListener) {
		BJProgressDialog dialog = new BJProgressDialog(context);
	    dialog.setTitle(title);
	    dialog.setCancelable(cancelable);
	    dialog.setOnCancelListener(cancelListener);
	    dialog.addContentView(new ProgressBar(context), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	    dialog.show();
	    return dialog;
	}

	public BJProgressDialog(Context context) {
	    super(context, R.style.BJProgressDialogStyle);
	}
	
}
