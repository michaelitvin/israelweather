package com.litvin.israelweather;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.widget.ArrayAdapter;

public class MainActivity extends FragmentActivity implements
		ActionBar.OnNavigationListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private static final String[] FRAGMENT_TAGS = {"tempMapFragment", "rainRadarFragmant", "tideTableFragment"};
	
	private static final int RAIN_RADAR_FRAMES = 12;
	private Fragment tempMapFragment, rainRadarFragmant, tideTableFragment;
	private Fragment fragmentArray[];
	
	private Bitmap errorBitmap;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(getActionBarThemedContextCompat(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, new String[] {
								getString(R.string.title_section_temperatures_map),
								getString(R.string.title_section_rain_radar),
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
		
		tempMapFragment = (ImageFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAGS[0]);
		if (tempMapFragment == null)
			tempMapFragment = ImageFragment.newInstance(new String[] {getString(R.string.ims_temperatures_map_url)}, savedInstanceState, errorBitmap);
		
		rainRadarFragmant = (ImageFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAGS[1]);
		if (rainRadarFragmant == null)
			rainRadarFragmant = ImageFragment.newInstance(rainRadarUrls, savedInstanceState, errorBitmap);
		
		tideTableFragment = (ImageFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAGS[2]);
		if (tideTableFragment == null)
			tideTableFragment = ImageFragment.newInstance(new String[] {getString(R.string.isramar_tide_table_url)}, savedInstanceState, errorBitmap);
		
		fragmentArray = new Fragment[] {tempMapFragment, rainRadarFragmant, tideTableFragment};
	}

	/**
	 * Backward-compatible version of {@link ActionBar#getThemedContext()} that
	 * simply returns the {@link android.app.Activity} if
	 * <code>getThemedContext</code> is unavailable.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private Context getActionBarThemedContextCompat() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return getActionBar().getThemedContext();
		} else {
			return this;
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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


}
