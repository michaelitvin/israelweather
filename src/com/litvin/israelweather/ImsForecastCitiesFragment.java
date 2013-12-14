package com.litvin.israelweather;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;

public class ImsForecastCitiesFragment extends ImsForecastFragment implements OnItemSelectedListener  {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String GENERAL_PREFS = "General preferences";
	public static final String PREF_CITY_IDX = "City index";
			
	private String[] cityNames;
	private int[] cityCodes;
	private int cityIdx;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		
		cityCodes = getActivity().getResources().getIntArray(R.array.city_codes);
		cityNames = getActivity().getResources().getStringArray(R.array.city_names);

		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(),
		        R.array.city_names, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinCities.setAdapter(adapter);
		spinCities.setOnItemSelectedListener(this);
		SharedPreferences settings = getActivity().getSharedPreferences(GENERAL_PREFS, 0);
	    cityIdx = settings.getInt(PREF_CITY_IDX, getActivity().getResources().getInteger(R.integer.default_city_index));
		spinCities.setSelection(cityIdx);
		downloadContent();
		spinCities.setVisibility(View.VISIBLE);

		downloadContent();

		return rootView;
	}
	
	void downloadContent() {
		//TODO load selection from state or user store
	    String imsForecastTodayFmt = String.format(urlToday, cityCodes[cityIdx], cityNames[cityIdx] );
		try {
			imsForecastTodayFmt = String.format(urlToday, cityCodes[cityIdx], URLEncoder.encode(cityNames[cityIdx],"UTF-8") );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    String imsForecastNextDaysFmt = String.format(urlNextDays, cityCodes[cityIdx]);

		dlToday = new DownloadHTMLTask(this, ARG_URL_FORECAST_TODAY);
		dlToday.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imsForecastTodayFmt);
		dlNextDays = new DownloadHTMLTask(this, ARG_URL_FORECAST_FEW_DAYS);
		dlNextDays.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imsForecastNextDaysFmt);

	}
	
	
	public static ImsForecastCitiesFragment newInstance(String urlToday, String urlNextDays, Bundle state) {
		ImsForecastCitiesFragment ret = new ImsForecastCitiesFragment();
		ret.setRetainInstance(true);
		Bundle args = new Bundle();
		args.putString(ImsForecastCitiesFragment.ARG_URL_FORECAST_TODAY, urlToday);
		args.putString(ImsForecastCitiesFragment.ARG_URL_FORECAST_FEW_DAYS, urlNextDays);
		ret.setArguments(args);
		//ret.downloadContent(urlToday, urlNextDays);
		
		return ret;
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		html = "";
		display();
		progressCircle.setVisibility(View.VISIBLE);
		cityIdx = pos;
		downloadContent();
		
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = getActivity().getSharedPreferences(GENERAL_PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(PREF_CITY_IDX, pos);
		// Commit the edits!
		editor.commit();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}
	
}