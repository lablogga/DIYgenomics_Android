package org.diygenomics.pg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;

public class InfoHandler implements DialogInterface.OnClickListener {
	
	private static InfoHandler mInstance;

	private Dialog mDialog;
	
	private InfoHandler() {}
	
	public static InfoHandler getInstance() {
		if (mInstance == null) {
			mInstance = new InfoHandler();
		}
		return mInstance;
	}
	
	public void showInfo(Context c, int filenameid) {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
		AlertDialog.Builder b = new AlertDialog.Builder(c);
		WebView html = new WebView(c);
		html.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		html.loadData(loadAssetFile(c, c.getString(filenameid)), "text/html", "UTF-8");
		mDialog = b.setIcon(null)
				.setView(html)
				.setPositiveButton("OK", this)
				.show();
	}
	
	private String loadAssetFile(Context c, String name) {
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(c.getAssets().open(name)));
			StringBuilder sb = new StringBuilder();
			do {
				String line = r.readLine();
				if (line != null) {
					sb.append(line);
				} else {
					break;
				}
			} while (true);
			r.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	
}
