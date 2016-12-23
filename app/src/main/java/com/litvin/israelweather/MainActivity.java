package com.litvin.israelweather;

import java.lang.reflect.Field;
import java.util.Locale;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import android.annotation.TargetApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.net.Uri;
import android.os.Bundle;
import android.content.ActivityNotFoundException;
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
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;


public class MainActivity extends ActionBarActivity implements
		ActionBar.OnNavigationListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	public static final String GENERAL_PREFS = "General preferences";
	public static final String PREF_LANG = "Language index";
	public static final String PREF_ANNOUNCE = "Announce_";
	public static final String PREF_LAST_NAVIGATION_ITEM = "Navigation item";
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
		displayOverflowMenu();

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(getActionBarThemedContextCompat(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, new String[] {
								getString(R.string.title_section_ims_forecast_country),
								getString(R.string.title_section_ims_forecast_cities),
								/*announceNew(getString(R.string.title_section_rain_forecast)),*/
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
		
		int lastItem = settings.getInt(PREF_LAST_NAVIGATION_ITEM, -1);
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM))
			lastItem = savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM);
		if (lastItem >= 0)
			getSupportActionBar().setSelectedNavigationItem(lastItem);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(PREF_LAST_NAVIGATION_ITEM);
		editor.commit();
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
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			int lastItem = savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM);
			getSupportActionBar().setSelectedNavigationItem(lastItem);
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
		Intent refresh;
		SharedPreferences settings = getSharedPreferences(GENERAL_PREFS, 0);
		SharedPreferences.Editor editor;
		
	    // Handle item selection
	    switch (item.getItemId()) {
        case R.id.action_refresh:
        	editor = settings.edit();
    		editor.putInt(PREF_LAST_NAVIGATION_ITEM, getSupportActionBar().getSelectedNavigationIndex());
    		editor.commit();
    		
        	trackButtonPress("refresh_button");
    		refresh = new Intent(this, MainActivity.class); 
    		finish();
    		startActivity(refresh);
        	return true;
        case R.id.action_share:
	        	// Create the share intent using Intent.ACTION_SEND, this is the action apps
	            // like Facebook will be listening for
	            Intent share_intent = new Intent(Intent.ACTION_SEND);
	         
	            // In this example I'm sharing some text and a URL to my Google Play page for
	            // my app therefore the Type is text/plain.
	            // Change .setType if your sending an image, video, etc.
	            share_intent.setType("text/plain");
	         
	            // Add your message and URL that you'd like to share. In this case, I created 
	            // a string resource called pitch which includes a short promotional message 
	            // (with targeted #hashtags) which is included before the URL to my Google Play page.
	            share_intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.pitch_subject));
	            share_intent 
	                .putExtra(
	                    Intent.EXTRA_TEXT, getString(R.string.pitch_text) // Promotional string
	                    + " \n"
	                    + "https://play.google.com/store/apps/details?referrer=utm_source%3Dapp_share&id=" + getPackageName()); // URL
	         
	            // Starts the Activity launching the Intent's chooser method. The chooser method is the
	            // menu users get upon selecting Share, this menu shows all apps available to share with.
	            startActivity(Intent.createChooser(share_intent, getString(R.string.action_share))); // Creates the sharing menu titled "Share..."
	            trackButtonPress("share_button");
	        	return true;
	        case R.id.action_rate:
	        	Uri uri = Uri.parse("market://details?id=" + getPackageName());
	        	Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
	        	try {
	        	  startActivity(goToMarket);
	        	} catch (ActivityNotFoundException e) {
	        	  startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
	        	}
	        	
	        	trackButtonPress("rate_button");
	        	return true;
	        case R.id.action_language:
	    		// We need an Editor object to make preference changes.
	    		// All objects are from android.context.Context
	    		int oldLang = settings.getInt(PREF_LANG, getDefaultLanguage());
	    		int newLang = (oldLang+1)%LOCALES.length;
	    		editor = settings.edit();
	    		editor.putInt(PREF_LANG, newLang);
	    		editor.putInt(PREF_LAST_NAVIGATION_ITEM, getSupportActionBar().getSelectedNavigationIndex());
	    		// Commit the edits!
	    		editor.commit();

	    		setLocale(newLang);
	    		trackButtonPress("language_button");
	    		refresh = new Intent(this, MainActivity.class); 
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
	
	protected String announceNew(String str) {
		SharedPreferences settings = getSharedPreferences(GENERAL_PREFS, 0);
	    int count = settings.getInt(PREF_ANNOUNCE + str, 0);
	    if (count >= 0)
	    	return "** " + str + " **";
	    return str;
	}
	
	public static void stopAnnouncingNew(Context context, String str) {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = context.getSharedPreferences(GENERAL_PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(PREF_ANNOUNCE + str, -1);
		// Commit the edits!
		editor.commit();
	}
	
	private void displayOverflowMenu() {
	     try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private void trackButtonPress(String name) {
        EasyTracker.getInstance(this).send(MapBuilder
      	      .createEvent(	"ui_action",      // Event category (required)
                        	"button_press",   // Event action (required)
                        	name, 			  // Event label
                        	null)             // Event value
                        			 .build() );
	}
	
}
