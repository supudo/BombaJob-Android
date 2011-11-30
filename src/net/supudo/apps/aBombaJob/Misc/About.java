package net.supudo.apps.aBombaJob.Misc;

import net.supudo.apps.aBombaJob.MainActivity;
import net.supudo.apps.aBombaJob.Database.DataHelper;
import net.supudo.apps.aBombaJob.Database.Models.TextContentModel;

import com.supudo.net.apps.aBombaJob.R;

import android.os.Bundle;
import android.webkit.WebView;

public class About extends MainActivity {
	
	private WebView aboutWebView;
	private DataHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		setTitle(R.string.about);
		
		if (dbHelper == null)
			dbHelper = new DataHelper(this);
		
		TextContentModel t = dbHelper.GetTextContent(35);
	    aboutWebView = (WebView)findViewById(R.id.webview);
	    aboutWebView.setBackgroundColor(0);
		aboutWebView.loadDataWithBaseURL("bj://localurl", t.Content, "text/html", "utf-8", "");
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

}
