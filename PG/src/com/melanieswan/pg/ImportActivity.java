package com.melanieswan.pg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

import com.melanieswan.pg.Data.LoaderCallback;
import com.melanieswan.pg.utils.MLog;

/**
 * the main activity show an initial graphic and a progress spinner while
 * loading the data load data in background
 */
public class ImportActivity extends Activity implements OnClickListener {

	private static final String TAG = "import";
	
	private Object lock;
	private Data mData;
	private ProgressBar mProgress;
	private Button mContinue;
	private Button mCancel;
	private View mLabel;
	private View mWarning;
	private View mButtons;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MLog.enable(TAG);
		View view = getLayoutInflater().inflate(R.layout.import_screen, null);
		mButtons = view.findViewById(R.id.buttons);
		mButtons.setVisibility(View.VISIBLE);
		mContinue = (Button) view.findViewById(R.id.cont);
		mCancel = (Button) view.findViewById(R.id.cancel);
		mContinue.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		mWarning = view.findViewById(R.id.warning);
		mWarning.setVisibility(View.VISIBLE);
		mLabel = view.findViewById(R.id.label);
		mLabel.setVisibility(View.GONE);
		mProgress = (ProgressBar) view.findViewById(R.id.progress_bar);
		mProgress.setVisibility(View.GONE);
		mProgress.setProgress(0);
		lock = new Object();
		setContentView(view);
	}

	@Override
	public void onClick(View view) {
		if (view == mCancel) {
			finish();
		} else if (view == mContinue) {
			mButtons.setVisibility(View.GONE);
			mWarning.setVisibility(View.GONE);
			mLabel.setVisibility(View.VISIBLE);
			mProgress.setVisibility(View.VISIBLE);
			mProgress.setProgress(0);
			loadData(getIntent());
		}
	}

	private void loadData(final Intent intent) {
		AsyncTask<Void, Integer, Void> loader = new AsyncTask<Void, Integer, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				MLog.i(TAG, "import a zip file data: ",intent.getData());
				// make sure that the csv data is loaded
				// Main either has the data already loaded, in which case the data is returned
				// if the data is null, it is loaded asynchronously
				//   block this thread until the callback is called
				synchronized(lock) {
					try {
						mData = Main.loadData(ImportActivity.this, new LoaderCallback() {
							public void done(Data data) {
								mData = data;
								synchronized(lock) {
									MLog.i(TAG,"got loader result");
									lock.notifyAll();
								}
							}
						});
						if (mData == null) {
							MLog.i(TAG, "waiting for loader");
							lock.wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Uri uri = intent.getData();
				try {
					InputStream in = getContentResolver().openInputStream(uri);
					ZipInputStream zin = new ZipInputStream(in);
					ZipEntry entry = zin.getNextEntry();
					if (entry != null) {
						MLog.i(TAG, "found a zip entry: ",entry.getName()," size: ",entry.getSize());
						long total = entry.getSize();
						BufferedReader reader = new BufferedReader(new InputStreamReader(zin));
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(Data.FILE_GENOME, MODE_APPEND)));
						int count = 0;
						int bread = 0;
						int lineCounter = 0;
						while (true) {
							String line = reader.readLine();
							if (line != null) {
								bread += line.length();
								lineCounter++;
								if ((total != -1) && (lineCounter % 100 == 0)) {
									publishProgress((int)(bread * 100 / total));
								}
								if (!line.startsWith("#")) {
									StringTokenizer st = new StringTokenizer(line);
									String snip = st.nextToken();
									st.nextToken();
									st.nextToken();
									String gtype = st.nextToken();
									if (mData.getVariantMap().get(snip) != null) {
										writer.append(snip);
										writer.append(",");
										writer.append(gtype);
										writer.append("\n");
										count++;
									} else {
									}
								}
							} else {
								break;
							}
						}
						reader.close();
						writer.close();
						mData.loadGenome(ImportActivity.this);
						MLog.i(TAG, "found ",count," relevant snips");
					}
					zin.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					Log.e(TAG,"ioerror: "+e);
					e.printStackTrace();
				}
				return null;
			}
			
		    protected void onProgressUpdate(Integer... progress) {
		    	final ProgressBar p = mProgress;
		    	p.setProgress(progress[0]);
		     }
			
			protected void onPostExecute(Void result) {
				done();
			}
		};
		loader.execute((Void) null);
	}

	public void done() {
		startActivity(new Intent(ImportActivity.this, CategoriesActivity.class));
	}


}