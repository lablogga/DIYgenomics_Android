package com.melanieswan.pg;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.flurry.android.FlurryAgent;
import com.melanieswan.pg.Data.LoaderCallback;
import com.melanieswan.pg.utils.Flurry;

/**
 * the main activity show an initial graphic and a progress spinner while
 * loading the data load data in background
 */
public class Main extends Activity implements LoaderCallback {

	static Data sData;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (sData == null) {
			loadData(this, this);
		} else {
			done(sData);
		}
		View view = getLayoutInflater().inflate(R.layout.main, null);
		setContentView(view);
	}

	@Override
	public void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, Flurry.KEY);
	}

	@Override
	public void onStop() {
		FlurryAgent.onEndSession(this);
		super.onStop();
	}

	private static void initializeData(final Activity context, final LoaderCallback callback) {
		AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				sData.loadCategory(context.getAssets(), 
						"Health Conditions", "conditions.csv", 4);
				sData.loadCategory(context.getAssets(), 
						"Drug Response - Top 30", "drugs_top30.csv", 7);
				sData.loadCategory(context.getAssets(), 
						"Drug Response - More", "drugs.csv", 5);
				sData.loadCategory(context.getAssets(), 
						"Athletic Performance",	"athperf_cats.csv", 6);
				// add new categories here
				// loadCategory( asset manager, name of category, csv filename, mapping table index (0 start)
				sData.load(context.getAssets());
				sData.loadGenome(context);
				return null;
			}

			protected void onPostExecute(Void result) {
				callback.done(sData);
			}
		};
		loader.execute((Void) null);

	}

	public static Data loadData(Activity activity, LoaderCallback callback) {
		if (sData == null) {
			sData = Data.getInstance();
			initializeData(activity, callback);
			return null;
		} else {
			return sData;
		}
	}

	public static Data getData() {
		return sData;
	}
	
	public void done(Data data) {
		startActivity(new Intent(Main.this, CategoriesActivity.class));
	}

}