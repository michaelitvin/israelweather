package com.litvin.israelweather;

import java.util.Locale;

import com.google.analytics.tracking.android.EasyTracker;

import android.annotation.TargetApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;


public class MainActivity extends ActionBarActivity implements
		ActionBar.OnNavigationListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	public static final String GENERAL_PREFS = "General preferences";
	public static final String PREF_LANG = "Language index";
	private static final Locale[] LOCALES = {new Locale("en"), new Locale("he"), new Locale("ru")};

	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private static final String[] FRAGMENT_TAGS = {"imsForecastCountryFragment", "imsForecastCitiesFragment", "rainForecastFragmant", "rainRadarFragmant", "tempMapFragment", "tideTableFragment"};
	
	private static final int RAIN_RADAR_FRAMES = 12;
	private Fragment imsForecastCountryFragment, imsForecastCitiesFragment, rainForecastFragmant, rainRadarFragmant, tempMapFragment, tideTableFragment;
	private Fragment fragmentArray[];
	
	private Bitmap errorBitmap;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		SharedPreferences settings = getSharedPreferences(GENERAL_PREFS, 0);
		int lang = settings.getInt(PREF_LANG, getDefaultLanguage());
		setLocale(lang);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(getActionBarThemedContextCompat(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, new String[] {
								getString(R.string.title_section_ims_forecast_country),
								getString(R.string.title_section_ims_forecast_cities),
								getString(R.string.title_section_rain_forecast),
								getString(R.string.title_section_rain_radar),
								getString(R.string.title_section_temperatures_map),
								getString(R.string.title_section_tide_table), }), this);
		
		Drawable drw = getResources().getDrawable(R.drawable.bullet_error);
	    errorBitmap = ((BitmapDrawable)drw).getBitmap();
	    
		String[] rainRadarUrls = new String[RAIN_RADAR_FRAMES];
		for (int i = 0; i < rainRadarUrls.length; i++)
			rainRadarUrls[i] = String.format(getString(R.string.ims_rain_radar_url), i, RAIN_RADAR_FRAMES);
		
		
		/*
		tempMapFragment = new ImageFragment();
		rainRadarFragmant = new ImageFragment();
		tideTableFragment = new ImageFragment();
		Bundle tempMapFragmentArgs = new Bundle();
		tempMapFragmentArgs.putStringArray(ImageFragment.ARG_URLS, new String[] {getString(R.string.ims_temperatures_map_url)});
		Bundle rainRadarFragmantArgs = new Bundle();
		rainRadarFragmantArgs.putStringArray(ImageFragment.ARG_URLS, rainRadarUrls);
		Bundle tideTableFragmentArgs = new Bundle();
		tideTableFragmentArgs.putStringArray(ImageFragment.ARG_URLS, new String[] {getString(R.string.isramar_tide_table_url)});
		tempMapFragment.setArguments(tempMapFragmentArgs);
		rainRadarFragmant.setArguments(rainRadarFragmantArgs);
		tideTableFragment.setArguments(tideTableFragmentArgs);
		*/

		String fragmentTag;
		
		fragmentTag = FRAGMENT_TAGS[0];
		imsForecastCountryFragment = (ImsForecastCountryFragment)getSupportFragmentManager().findFragmentByTag(fragmentTag);
		if (imsForecastCountryFragment == null)
			imsForecastCountryFragment = ImsForecastCountryFragment.newInstance(fragmentTag, getString(R.string.ims_forecast_home_url), savedInstanceState);
		
		fragmentTag = FRAGMENT_TAGS[1];
		imsForecastCitiesFragment = (ImsForecastCitiesFragment)getSupportFragmentManager().findFragmentByTag(fragmentTag);
		if (imsForecastCitiesFragment == null)
			imsForecastCitiesFragment = ImsForecastCitiesFragment.newInstance(fragmentTag, getString(R.string.ims_forecast_today_url), getString(R.string.ims_forecast_next_days_url), savedInstanceState);

		fragmentTag = FRAGMENT_TAGS[2];
		rainForecastFragmant = (ImsRainForecastFragment)getSupportFragmentManager().findFragmentByTag(fragmentTag);
		if (rainForecastFragmant == null)
			rainForecastFragmant = ImsRainForecastFragment.newInstance(fragmentTag, new String[] {getString(R.string.ims_rain_forecast_url)}, savedInstanceState, errorBitmap);

		fragmentTag = FRAGMENT_TAGS[3];
		rainRadarFragmant = (ImageFragment)getSupportFragmentManager().findFragmentByTag(fragmentTag);
		if (rainRadarFragmant == null)
			rainRadarFragmant = ImageFragment.newInstance(fragmentTag, rainRadarUrls, savedInstanceState, errorBitmap);

		fragmentTag = FRAGMENT_TAGS[4];
		tempMapFragment = (ImageFragment)getSupportFragmentManager().findFragmentByTag(fragmentTag);
		if (tempMapFragment == null)
			tempMapFragment = ImageFragment.newInstance(fragmentTag, new String[] {getString(R.string.ims_temperatures_map_url)}, savedInstanceState, errorBitmap);
		
		fragmentTag = FRAGMENT_TAGS[5];
		tideTableFragment = (ImageFragment)getSupportFragmentManager().findFragmentByTag(fragmentTag);
		if (tideTableFragment == null)
			tideTableFragment = ImageFragment.newInstance(fragmentTag, new String[] {getString(R.string.isramar_tide_table_url)}, savedInstanceState, errorBitmap);
		
		fragmentArray = new Fragment[] {imsForecastCountryFragment, imsForecastCitiesFragment, rainForecastFragmant, rainRadarFragmant, tempMapFragment, tideTableFragment};
	}

	
	/**
	 * Backward-compatible version of {@link ActionBar#getThemedContext()} that
	 * simply returns the {@link android.app.Activity} if
	 * <code>getThemedContext</code> is unavailable.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private Context getActionBarThemedContextCompat() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return getSupportActionBar().getThemedContext();
		} else {
			return this;
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getSupportActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getSupportActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, fragmentArray[position], FRAGMENT_TAGS[position])
				.commit();
		return true;
	}
	

	
	public void setLocale(int lang) { 
		Resources res = getResources(); 
		DisplayMetrics dm = res.getDisplayMetrics(); 
		Configuration conf = res.getConfiguration(); 
		conf.locale = LOCALES[lang]; 
		res.updateConfiguration(conf, dm); 
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_language:
	    		// We need an Editor object to make preference changes.
	    		// All objects are from android.context.Context
	    		SharedPreferences settings = getSharedPreferences(GENERAL_PREFS, 0);
	    		int oldLang = settings.getInt(PREF_LANG, getDefaultLanguage());
	    		int newLang = (oldLang+1)%LOCALES.length;
	    		SharedPreferences.Editor editor = settings.edit();
	    		editor.putInt(PREF_LANG, newLang);
	    		// Commit the edits!
	    		editor.commit();

	    		setLocale(newLang);
	    		Intent refresh = new Intent(this, MainActivity.class); 
	    		finish();
	    		startActivity(refresh);
	    		return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private int getDefaultLanguage() {
		Locale defLocale = Locale.getDefault();
		String defLangStr = defLocale.getLanguage();
		int defLang = 0;
		for (int i = 0; i < LOCALES.length; i++) {
			if (LOCALES[i].getLanguage().equals(defLangStr)) {
				defLang = i;
				break;
			}
		}
		return defLang;
	}

	@Override
	protected void onStart() {
		EasyTracker.getInstance(this).activityStart(this); 
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		EasyTracker.getInstance(this).activityStop(this);
		super.onStop();
	}
	
	
}
